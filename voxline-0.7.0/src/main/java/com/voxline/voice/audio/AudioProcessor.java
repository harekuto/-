package com.voxline.voice.audio;

import com.voxline.voice.client.ClientConfig;

public final class AudioProcessor {
    public record Stats(float inputDb,float noiseFloorDb,float gateGain,float agcGainDb,float outputPeakDb){}
    private float dcX,dcY,noiseFloor=.0025f,gateGain=1f,agcGain=1f,prev=0f;
    private volatile Stats stats=new Stats(-90,-90,1,0,-90);
    public Stats stats(){return stats;}

    public float process(short[] pcm,ClientConfig c){
        double inSq=0,outPeak=0; float manual=(float)Math.pow(10,c.microphoneGainDb/20.0);
        for(int i=0;i<pcm.length;i++){
            float x=pcm[i]/32768f; inSq+=x*x;
            float hp=x-dcX+.995f*dcY; dcX=x;dcY=hp;
            float presence=hp+(hp-prev)*(.10f*c.voiceClarity); prev=hp;
            pcm[i]=(short)clamp(Math.round(presence*manual*32767f));
        }
        float inputDb=db((float)Math.sqrt(inSq/Math.max(1,pcm.length)));
        float amp=(float)Math.pow(10,inputDb/20f);
        if(inputDb<c.activationThresholdDb-6f)noiseFloor=noiseFloor*.985f+amp*.015f;
        float floorDb=db(Math.max(noiseFloor,1e-5f));
        float gateOpen=Math.max(c.noiseGateDb,floorDb+5f);
        float desired=inputDb<gateOpen?(1f-c.suppressionStrength):1f;
        gateGain+= (desired-gateGain)*(desired>gateGain?.30f:.08f);
        float targetGain=1f;
        if(c.agcEnabled && inputDb>gateOpen+3f){float need=c.agcTargetDb-inputDb;need=Math.max(0f,Math.min(c.agcMaxGainDb,need));targetGain=(float)Math.pow(10,need/20f);}
        agcGain+= (targetGain-agcGain)*(targetGain>agcGain?.025f:.10f);
        float threshold=.42f; float compMix=c.compressorEnabled?c.compressorStrength:0f;
        for(int i=0;i<pcm.length;i++){
            float x=pcm[i]/32768f*gateGain*agcGain; float a=Math.abs(x);
            if(a>threshold){float compressed=threshold+(a-threshold)/(1f+5f*compMix);x=Math.copySign(x*(1f-compMix)+compressed*compMix,x);}
            x=(float)Math.tanh(x*1.15f)/1.15f; outPeak=Math.max(outPeak,Math.abs(x)); pcm[i]=(short)clamp(Math.round(x*32767f));
        }
        float agcDb=(float)(20*Math.log10(Math.max(.0001f,agcGain))); stats=new Stats(inputDb,floorDb,gateGain,agcDb,db((float)outPeak)); return inputDb;
    }
    private static int clamp(int v){return Math.max(Short.MIN_VALUE,Math.min(Short.MAX_VALUE,v));}
    private static float db(float x){return x<=1e-6f?-90f:Math.max(-90f,(float)(20*Math.log10(x)));}
}
