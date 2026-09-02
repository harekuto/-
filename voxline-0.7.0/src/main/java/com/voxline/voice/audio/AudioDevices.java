package com.voxline.voice.audio;

import javax.sound.sampled.*;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class AudioDevices {
    public static final String SYSTEM_DEFAULT="System Default";
    private AudioDevices(){}
    public static List<String> inputs(){return list(TargetDataLine.class,AudioSpec.input());}
    public static List<String> outputs(){return list(SourceDataLine.class,AudioSpec.output());}
    private static List<String> list(Class<? extends Line> cls,AudioFormat format){Set<String> out=new LinkedHashSet<>();out.add(SYSTEM_DEFAULT);DataLine.Info info=new DataLine.Info(cls,format);for(Mixer.Info mi:AudioSystem.getMixerInfo()){Mixer m=AudioSystem.getMixer(mi);if(m.isLineSupported(info))out.add(mi.getName());}return new ArrayList<>(out);}
    public static TargetDataLine openInput(String preferred)throws LineUnavailableException{AudioFormat f=AudioSpec.input();Mixer m=find(preferred,TargetDataLine.class,f);return m==null?AudioSystem.getTargetDataLine(f):(TargetDataLine)m.getLine(new DataLine.Info(TargetDataLine.class,f));}
    public static SourceDataLine openOutput(String preferred)throws LineUnavailableException{AudioFormat f=AudioSpec.output();Mixer m=find(preferred,SourceDataLine.class,f);return m==null?AudioSystem.getSourceDataLine(f):(SourceDataLine)m.getLine(new DataLine.Info(SourceDataLine.class,f));}
    private static Mixer find(String preferred,Class<? extends Line> cls,AudioFormat f){if(preferred==null||preferred.isBlank()||SYSTEM_DEFAULT.equals(preferred))return null;DataLine.Info info=new DataLine.Info(cls,f);for(Mixer.Info mi:AudioSystem.getMixerInfo()){if(mi.getName().equals(preferred)){Mixer m=AudioSystem.getMixer(mi);if(m.isLineSupported(info))return m;}}return null;}
}
