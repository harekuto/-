package com.voxline.voice.audio;

import javax.sound.sampled.AudioFormat;

public final class AudioSpec {
    public static final int SAMPLE_RATE=48_000;
    public static final int FRAME_MS=20;
    public static final int SAMPLES_PER_FRAME=SAMPLE_RATE*FRAME_MS/1000;
    public static final int PCM_BYTES=SAMPLES_PER_FRAME*2;
    public static final int MAX_OPUS_BYTES=512;
    public static final int OUTPUT_BYTES=SAMPLES_PER_FRAME*4;
    private AudioSpec(){}
    public static AudioFormat input(){return new AudioFormat(SAMPLE_RATE,16,1,true,false);}
    public static AudioFormat output(){return new AudioFormat(SAMPLE_RATE,16,2,true,false);}
}
