package com.voxline.voice.audio;

import com.voxline.voice.VoxlineVoice;
import com.voxline.voice.client.ClientConfig;
import com.voxline.voice.client.ClientDiagnostics;

import javax.sound.sampled.SourceDataLine;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public final class VoicePlaybackEngine implements AutoCloseable {
    private record Packet(int seq, byte[] data) { }
    private static final class Speaker {
        final Deque<Packet> q = new ArrayDeque<>();
        final OpusCodec.Decoder decoder;
        int expected = -1;
        int plcBudget;
        float left = 1f, right = 1f;
        long last;
        Speaker() throws Exception { decoder = new OpusCodec.Decoder(); }
    }

    private final Map<UUID, Speaker> speakers = new ConcurrentHashMap<>();
    private final AtomicBoolean running = new AtomicBoolean();
    private SourceDataLine line;
    private Thread thread;
    private volatile String error = "";

    public synchronized void start() {
        if (running.get()) return;
        running.set(true);
        thread = new Thread(this::loop, "Voxline-Playback");
        thread.setDaemon(true);
        thread.start();
    }

    public void enqueue(UUID id, int seq, byte[] data, float left, float right) {
        if (id == null || seq < 0 || data == null || data.length == 0 || data.length > AudioSpec.MAX_OPUS_BYTES) return;
        try {
            Speaker s = speakers.computeIfAbsent(id, k -> {
                try { return new Speaker(); }
                catch (Exception e) { throw new RuntimeException(e); }
            });
            s.left = sane(left);
            s.right = sane(right);
            s.last = System.currentTimeMillis();
            synchronized (s.q) {
                if (s.q.size() >= 12) {
                    s.q.removeFirst();
                    ClientDiagnostics.INSTANCE.playbackDrops.incrementAndGet();
                }
                s.q.addLast(new Packet(seq, data.clone()));
            }
        } catch (RuntimeException e) {
            error = "Decoder init: " + e.getMessage();
        }
    }

    private void loop() {
        int[] l = new int[AudioSpec.SAMPLES_PER_FRAME];
        int[] r = new int[AudioSpec.SAMPLES_PER_FRAME];
        byte[] out = new byte[AudioSpec.OUTPUT_BYTES];
        while (running.get()) {
            try {
                ensureLine();
                Arrays.fill(l, 0);
                Arrays.fill(r, 0);
                long now = System.currentTimeMillis();
                int active = 0;
                for (var en : speakers.entrySet()) {
                    Speaker s = en.getValue();
                    if (now - s.last > 3000) {
                        speakers.remove(en.getKey(), s);
                        continue;
                    }
                    short[] pcm = next(s);
                    if (pcm == null) continue;
                    active++;
                    int n = Math.min(pcm.length, l.length);
                    for (int i = 0; i < n; i++) {
                        l[i] += Math.round(pcm[i] * s.left);
                        r[i] += Math.round(pcm[i] * s.right);
                    }
                }
                ByteBuffer b = ByteBuffer.wrap(out).order(ByteOrder.LITTLE_ENDIAN);
                for (int i = 0; i < l.length; i++) {
                    b.putShort((short) clamp(l[i]));
                    b.putShort((short) clamp(r[i]));
                }
                if (active == 0) Arrays.fill(out, (byte) 0);
                line.write(out, 0, out.length);
                error = "";
            } catch (Exception e) {
                if (!running.get()) break;
                error = e.getClass().getSimpleName() + ": " + String.valueOf(e.getMessage());
                VoxlineVoice.LOGGER.debug("Playback recovering", e);
                closeLine();
                sleep(400);
            }
        }
        closeLine();
    }

    private short[] next(Speaker s) throws Exception {
        Packet p;
        synchronized (s.q) {
            if (s.expected < 0 && s.q.size() < ClientConfig.INSTANCE.jitterFrames) return null;
            p = s.q.peekFirst();
        }

        if (p == null) {
            if (s.expected >= 0 && s.plcBudget > 0) {
                s.plcBudget--;
                ClientDiagnostics.INSTANCE.plcFrames.incrementAndGet();
                s.expected = nextSeq(s.expected);
                return s.decoder.decode(null, false);
            }
            return null;
        }

        if (s.expected < 0) s.expected = p.seq;
        long d = forward(s.expected, p.seq);
        if (d == 0) {
            synchronized (s.q) { s.q.removeFirst(); }
            s.expected = nextSeq(s.expected);
            s.plcBudget = 2;
            return s.decoder.decode(p.data, false);
        }

        if (d > 0 && d <= 3) {
            // Opus in-band FEC can reconstruct only the frame immediately preceding p.
            // For a 2-3 frame gap, conceal older frames first. Once d becomes 1, use FEC.
            if (d == 1 && ClientConfig.INSTANCE.fecEnabled) {
                ClientDiagnostics.INSTANCE.fecFrames.incrementAndGet();
                s.expected = nextSeq(s.expected);
                return s.decoder.decode(p.data, true);
            }
            ClientDiagnostics.INSTANCE.plcFrames.incrementAndGet();
            s.expected = nextSeq(s.expected);
            return s.decoder.decode(null, false);
        }

        if (d > 3 && d < ((long) Integer.MAX_VALUE + 1L) / 2L) {
            s.expected = p.seq;
            return next(s);
        }

        synchronized (s.q) { s.q.removeFirst(); }
        ClientDiagnostics.INSTANCE.lateFrames.incrementAndGet();
        return null;
    }

    public synchronized void clear() {
        speakers.clear();
        if (line != null && line.isOpen()) {
            try { line.flush(); } catch (Exception ignored) { }
        }
    }

    private synchronized void ensureLine() throws Exception {
        if (line != null && line.isOpen()) return;
        line = AudioDevices.openOutput(ClientConfig.INSTANCE.outputDevice);
        line.open(AudioSpec.output(), AudioSpec.OUTPUT_BYTES * 8);
        line.start();
    }

    private synchronized void closeLine() {
        if (line != null) {
            try { line.flush(); } catch (Exception ignored) { }
            try { line.stop(); } catch (Exception ignored) { }
            try { line.close(); } catch (Exception ignored) { }
            line = null;
        }
    }

    public String error() { return error; }
    public synchronized void restart() { closeLine(); clear(); if (!running.get()) start(); }
    private static int nextSeq(int x) { return x == Integer.MAX_VALUE ? 0 : x + 1; }
    private static long forward(int from, int to) { long m = (long) Integer.MAX_VALUE + 1L, d = (long) to - from; return d >= 0 ? d : d + m; }
    private static float sane(float v) { return Float.isFinite(v) ? Math.max(0, Math.min(2, v)) : 0; }
    private static int clamp(int v) { return Math.max(Short.MIN_VALUE, Math.min(Short.MAX_VALUE, v)); }
    private static void sleep(long ms) { try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); } }

    @Override public synchronized void close() {
        running.set(false);
        closeLine();
        if (thread != null) thread.interrupt();
        thread = null;
        speakers.clear();
    }
}
