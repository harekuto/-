package com.voxline.voice.audio;

import io.github.jaredmdobson.concentus.OpusApplication;
import io.github.jaredmdobson.concentus.OpusDecoder;
import io.github.jaredmdobson.concentus.OpusEncoder;
import io.github.jaredmdobson.concentus.OpusException;
import io.github.jaredmdobson.concentus.OpusSignal;

import java.util.Arrays;

public final class OpusCodec {
    private OpusCodec(){}
    public static final class Encoder {
        private final OpusEncoder encoder; private int bitrate=-1; private boolean fec=true;
        public Encoder() throws OpusException { encoder=new OpusEncoder(AudioSpec.SAMPLE_RATE,1,OpusApplication.OPUS_APPLICATION_VOIP);encoder.setComplexity(8);encoder.setSignalType(OpusSignal.OPUS_SIGNAL_VOICE);encoder.setUseVBR(true);encoder.setUseDTX(false);encoder.setPacketLossPercent(8);encoder.setUseInbandFEC(true); }
        public byte[] encode(short[] pcm,int kbps,boolean useFec)throws OpusException{int br=Math.max(16,Math.min(64,kbps))*1000;if(br!=bitrate){encoder.setBitrate(br);bitrate=br;}if(useFec!=fec){encoder.setUseInbandFEC(useFec);fec=useFec;}byte[] out=new byte[AudioSpec.MAX_OPUS_BYTES];int len=encoder.encode(pcm,0,AudioSpec.SAMPLES_PER_FRAME,out,0,out.length);return len<=0?new byte[0]:Arrays.copyOf(out,len);}
    }
    public static final class Decoder {
        private final OpusDecoder decoder;
        public Decoder()throws OpusException{decoder=new OpusDecoder(AudioSpec.SAMPLE_RATE,1);}
        public short[] decode(byte[] data,boolean fec)throws OpusException{short[] out=new short[AudioSpec.SAMPLES_PER_FRAME];int n=decoder.decode(data,0,data==null?0:data.length,out,0,AudioSpec.SAMPLES_PER_FRAME,fec);return n==out.length?out:Arrays.copyOf(out,Math.max(0,n));}
    }
}
