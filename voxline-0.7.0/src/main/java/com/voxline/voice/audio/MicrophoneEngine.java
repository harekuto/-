package com.voxline.voice.audio;

import com.voxline.voice.VoxlineVoice;
import com.voxline.voice.client.ClientConfig;
import com.voxline.voice.network.VoiceChannel;

import javax.sound.sampled.TargetDataLine;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

public final class MicrophoneEngine implements AutoCloseable {
    public interface Sink { void frame(byte[] opus, float levelDb, VoiceChannel channel); }

    private final Sink sink;
    private final BooleanSupplier ptt;
    private final Supplier<VoiceChannel> channel;
    private final AudioProcessor processor = new AudioProcessor();
    private final AtomicBoolean running = new AtomicBoolean();
    private final Deque<short[]> preRoll = new ArrayDeque<>();
    private TargetDataLine line;
    private Thread thread;
    private OpusCodec.Encoder encoder;
    private volatile float meter = -90f;
    private volatile String error = "";
    private int hangover;
    private boolean vadOpen;

    public MicrophoneEngine(Sink sink, BooleanSupplier ptt, Supplier<VoiceChannel> channel) {
        this.sink = sink;
        this.ptt = ptt;
        this.channel = channel;
    }

    public synchronized void start() {
        if (running.get()) return;
        running.set(true);
        thread = new Thread(this::loop, "Voxline-Microphone");
        thread.setDaemon(true);
        thread.start();
    }

    public float meterDb() { return meter; }
    public String error() { return error; }
    public AudioProcessor.Stats stats() { return processor.stats(); }

    public synchronized void restart() {
        closeLine();
        encoder = null;
        preRoll.clear();
        hangover = 0;
        vadOpen = false;
        if (!running.get()) start();
    }

    private void loop() {
        byte[] bytes = new byte[AudioSpec.PCM_BYTES];
        short[] pcm = new short[AudioSpec.SAMPLES_PER_FRAME];
        while (running.get()) {
            try {
                ensureLine();
                if (encoder == null) encoder = new OpusCodec.Encoder();
                if (!readFully(bytes)) continue;
                ByteBuffer b = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
                for (int i = 0; i < pcm.length; i++) pcm[i] = b.getShort();

                ClientConfig c = ClientConfig.INSTANCE;
                meter = processor.process(pcm, c);
                boolean transmit;
                boolean openedNow = false;

                if (c.activationMode == ClientConfig.ActivationMode.PUSH_TO_TALK) {
                    transmit = ptt.getAsBoolean();
                    vadOpen = false;
                    hangover = 0;
                } else {
                    float open = c.activationThresholdDb;
                    float close = open - 5f;
                    if (meter >= open) {
                        openedNow = !vadOpen;
                        vadOpen = true;
                        hangover = Math.max(0, c.vadHangoverMs / AudioSpec.FRAME_MS);
                    } else if (vadOpen && meter >= close) {
                        hangover = Math.max(hangover, 2);
                    } else if (hangover > 0) {
                        hangover--;
                    } else {
                        vadOpen = false;
                    }
                    transmit = vadOpen || hangover > 0;
                }

                VoiceChannel ch = transmit ? channel.get() : null;
                if (transmit && ch != null) {
                    if (openedNow) flushPreRoll(c, ch);
                    byte[] enc = encoder.encode(pcm, c.opusBitrateKbps, c.fecEnabled);
                    if (enc.length > 0) sink.frame(enc, meter, ch);
                } else if (transmit) {
                    // Muted/deafened Voice Activation may still be logically open; do not encode
                    // and do not keep private muted audio in pre-roll for later transmission.
                    preRoll.clear();
                } else {
                    preRoll.addLast(pcm.clone());
                    while (preRoll.size() > 2) preRoll.removeFirst();
                }
                error = "";
            } catch (Exception e) {
                if (!running.get()) break;
                error = e.getClass().getSimpleName() + ": " + String.valueOf(e.getMessage());
                VoxlineVoice.LOGGER.debug("Microphone pipeline recovering", e);
                closeLine();
                encoder = null;
                sleep(500);
            }
        }
        closeLine();
    }

    private void flushPreRoll(ClientConfig c, VoiceChannel ch) throws Exception {
        while (!preRoll.isEmpty()) {
            byte[] enc = encoder.encode(preRoll.removeFirst(), c.opusBitrateKbps, c.fecEnabled);
            if (enc.length > 0) sink.frame(enc, meter, ch);
        }
    }

    private boolean readFully(byte[] dst) {
        int off = 0;
        while (off < dst.length && running.get()) {
            int n = line.read(dst, off, dst.length - off);
            if (n <= 0) return false;
            off += n;
        }
        return off == dst.length;
    }

    private synchronized void ensureLine() throws Exception {
        if (line != null && line.isOpen()) return;
        line = AudioDevices.openInput(ClientConfig.INSTANCE.inputDevice);
        line.open(AudioSpec.input(), AudioSpec.PCM_BYTES * 8);
        line.start();
    }

    private synchronized void closeLine() {
        if (line != null) {
            try { line.stop(); } catch (Exception ignored) { }
            try { line.close(); } catch (Exception ignored) { }
            line = null;
        }
    }

    private static void sleep(long ms) {
        try { Thread.sleep(ms); }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }

    @Override public synchronized void close() {
        running.set(false);
        closeLine();
        if (thread != null) thread.interrupt();
        thread = null;
        encoder = null;
        preRoll.clear();
    }
}
