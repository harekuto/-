package com.voxline.voice.client;

import com.voxline.voice.VoxlineVoice;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Properties;

public final class ClientConfig {
    public enum ActivationMode { PUSH_TO_TALK, VOICE_ACTIVATION }
    public static final ClientConfig INSTANCE = new ClientConfig();

    public float masterVolume = 1.0f;
    public float groupVolume = 1.0f;
    public float microphoneGainDb = 0f;
    public float noiseGateDb = -52f;
    public float suppressionStrength = .78f;
    public float activationThresholdDb = -38f;
    public int vadHangoverMs = 240;
    public boolean agcEnabled = true;
    public float agcTargetDb = -18f;
    public float agcMaxGainDb = 14f;
    public boolean compressorEnabled = true;
    public float compressorStrength = .68f;
    public float voiceClarity = .28f;
    public boolean spatialVoice = true;
    public float proximityRange = 32f;
    public int opusBitrateKbps = 32;
    public int jitterFrames = 3;
    public boolean fecEnabled = true;
    public ActivationMode activationMode = ActivationMode.PUSH_TO_TALK;
    public boolean vadTargetsGroup = false;
    public String inputDevice = "System Default";
    public String outputDevice = "System Default";

    public String theme = "Deep Slate";
    public float uiOpacity = .94f;
    public float uiScale = 1f;
    public boolean rounded = true;
    public boolean streamerMode = false;
    public int hudAnchorX = 88;
    public int hudAnchorY = 12;
    public int hudWidth = 188;
    public int hudRowHeight = 28;
    public int maxHudSpeakers = 5;
    public boolean snapToGrid = true;
    public boolean showPlayerHead = true;
    public boolean showHatLayer = true;
    public boolean showNameplate = true;
    public boolean showMicIcon = true;
    public boolean showWaveform = true;
    public boolean showDistance = true;
    public boolean showStatusDot = true;
    public boolean showBackground = true;
    public boolean showSpeakingGlow = true;
    public boolean showChannelBadge = true;
    public float headScale = 1f;
    public float headOpacity = 1f;

    private final Path path = FMLPaths.CONFIGDIR.get().resolve("voxline-voice-client.properties");
    private ClientConfig() { }

    public synchronized void load() {
        if (!Files.exists(path)) { save(); return; }
        Properties p = new Properties();
        try (InputStream in = Files.newInputStream(path)) {
            p.load(in);
            masterVolume = f(p,"masterVolume",masterVolume,0f,2f); groupVolume=f(p,"groupVolume",groupVolume,0f,2f);
            microphoneGainDb=f(p,"microphoneGainDb",microphoneGainDb,-12f,24f); noiseGateDb=f(p,"noiseGateDb",noiseGateDb,-80f,-20f);
            suppressionStrength=f(p,"suppressionStrength",suppressionStrength,0f,1f); activationThresholdDb=f(p,"activationThresholdDb",activationThresholdDb,-65f,-15f);
            vadHangoverMs=i(p,"vadHangoverMs",vadHangoverMs,0,600); agcEnabled=b(p,"agcEnabled",agcEnabled); agcTargetDb=f(p,"agcTargetDb",agcTargetDb,-30f,-8f);
            agcMaxGainDb=f(p,"agcMaxGainDb",agcMaxGainDb,0f,24f); compressorEnabled=b(p,"compressorEnabled",compressorEnabled);
            compressorStrength=f(p,"compressorStrength",compressorStrength,0f,1f); voiceClarity=f(p,"voiceClarity",voiceClarity,0f,1f);
            spatialVoice=b(p,"spatialVoice",spatialVoice); proximityRange=f(p,"proximityRange",proximityRange,4f,64f);
            opusBitrateKbps=i(p,"opusBitrateKbps",opusBitrateKbps,16,64); jitterFrames=i(p,"jitterFrames",jitterFrames,1,6); fecEnabled=b(p,"fecEnabled",fecEnabled);
            try { activationMode=ActivationMode.valueOf(p.getProperty("activationMode",activationMode.name())); } catch (IllegalArgumentException ignored) { activationMode=ActivationMode.PUSH_TO_TALK; }
            vadTargetsGroup=b(p,"vadTargetsGroup",vadTargetsGroup); inputDevice=s(p,"inputDevice",inputDevice); outputDevice=s(p,"outputDevice",outputDevice);
            theme=s(p,"theme",theme); uiOpacity=f(p,"uiOpacity",uiOpacity,.5f,1f); uiScale=f(p,"uiScale",uiScale,.75f,1.3f); rounded=b(p,"rounded",rounded); streamerMode=b(p,"streamerMode",streamerMode);
            hudAnchorX=i(p,"hudAnchorX",hudAnchorX,0,100); hudAnchorY=i(p,"hudAnchorY",hudAnchorY,0,100); hudWidth=i(p,"hudWidth",hudWidth,140,280); hudRowHeight=i(p,"hudRowHeight",hudRowHeight,22,40); maxHudSpeakers=i(p,"maxHudSpeakers",maxHudSpeakers,1,8);
            snapToGrid=b(p,"snapToGrid",snapToGrid); showPlayerHead=b(p,"showPlayerHead",showPlayerHead); showHatLayer=b(p,"showHatLayer",showHatLayer); showNameplate=b(p,"showNameplate",showNameplate); showMicIcon=b(p,"showMicIcon",showMicIcon); showWaveform=b(p,"showWaveform",showWaveform); showDistance=b(p,"showDistance",showDistance); showStatusDot=b(p,"showStatusDot",showStatusDot); showBackground=b(p,"showBackground",showBackground); showSpeakingGlow=b(p,"showSpeakingGlow",showSpeakingGlow); showChannelBadge=b(p,"showChannelBadge",showChannelBadge); headScale=f(p,"headScale",headScale,.65f,1.5f); headOpacity=f(p,"headOpacity",headOpacity,.2f,1f);
        } catch (IOException e) { VoxlineVoice.LOGGER.warn("Could not read Voxline client config", e); }
    }

    public synchronized void save() {
        try {
            Files.createDirectories(path.getParent()); Properties p=new Properties();
            put(p,"masterVolume",masterVolume); put(p,"groupVolume",groupVolume); put(p,"microphoneGainDb",microphoneGainDb); put(p,"noiseGateDb",noiseGateDb); put(p,"suppressionStrength",suppressionStrength); put(p,"activationThresholdDb",activationThresholdDb); put(p,"vadHangoverMs",vadHangoverMs); put(p,"agcEnabled",agcEnabled); put(p,"agcTargetDb",agcTargetDb); put(p,"agcMaxGainDb",agcMaxGainDb); put(p,"compressorEnabled",compressorEnabled); put(p,"compressorStrength",compressorStrength); put(p,"voiceClarity",voiceClarity); put(p,"spatialVoice",spatialVoice); put(p,"proximityRange",proximityRange); put(p,"opusBitrateKbps",opusBitrateKbps); put(p,"jitterFrames",jitterFrames); put(p,"fecEnabled",fecEnabled); p.setProperty("activationMode",activationMode.name()); put(p,"vadTargetsGroup",vadTargetsGroup); p.setProperty("inputDevice",inputDevice); p.setProperty("outputDevice",outputDevice); p.setProperty("theme",theme); put(p,"uiOpacity",uiOpacity); put(p,"uiScale",uiScale); put(p,"rounded",rounded); put(p,"streamerMode",streamerMode); put(p,"hudAnchorX",hudAnchorX); put(p,"hudAnchorY",hudAnchorY); put(p,"hudWidth",hudWidth); put(p,"hudRowHeight",hudRowHeight); put(p,"maxHudSpeakers",maxHudSpeakers); put(p,"snapToGrid",snapToGrid); put(p,"showPlayerHead",showPlayerHead); put(p,"showHatLayer",showHatLayer); put(p,"showNameplate",showNameplate); put(p,"showMicIcon",showMicIcon); put(p,"showWaveform",showWaveform); put(p,"showDistance",showDistance); put(p,"showStatusDot",showStatusDot); put(p,"showBackground",showBackground); put(p,"showSpeakingGlow",showSpeakingGlow); put(p,"showChannelBadge",showChannelBadge); put(p,"headScale",headScale); put(p,"headOpacity",headOpacity);
            Path tmp=path.resolveSibling(path.getFileName()+".tmp"); try(OutputStream out=Files.newOutputStream(tmp)){p.store(out,"Voxline Voice 0.7 client settings");}
            try{Files.move(tmp,path,StandardCopyOption.REPLACE_EXISTING,StandardCopyOption.ATOMIC_MOVE);}catch(IOException ex){Files.move(tmp,path,StandardCopyOption.REPLACE_EXISTING);}
        } catch(IOException e){VoxlineVoice.LOGGER.warn("Could not save Voxline client config",e);}
    }
    public synchronized void reset(){ ClientConfig d=new ClientConfig(); copyFrom(d); save(); }
    private void copyFrom(ClientConfig d){ masterVolume=d.masterVolume;groupVolume=d.groupVolume;microphoneGainDb=d.microphoneGainDb;noiseGateDb=d.noiseGateDb;suppressionStrength=d.suppressionStrength;activationThresholdDb=d.activationThresholdDb;vadHangoverMs=d.vadHangoverMs;agcEnabled=d.agcEnabled;agcTargetDb=d.agcTargetDb;agcMaxGainDb=d.agcMaxGainDb;compressorEnabled=d.compressorEnabled;compressorStrength=d.compressorStrength;voiceClarity=d.voiceClarity;spatialVoice=d.spatialVoice;proximityRange=d.proximityRange;opusBitrateKbps=d.opusBitrateKbps;jitterFrames=d.jitterFrames;fecEnabled=d.fecEnabled;activationMode=d.activationMode;vadTargetsGroup=d.vadTargetsGroup;inputDevice=d.inputDevice;outputDevice=d.outputDevice;theme=d.theme;uiOpacity=d.uiOpacity;uiScale=d.uiScale;rounded=d.rounded;streamerMode=d.streamerMode;hudAnchorX=d.hudAnchorX;hudAnchorY=d.hudAnchorY;hudWidth=d.hudWidth;hudRowHeight=d.hudRowHeight;maxHudSpeakers=d.maxHudSpeakers;snapToGrid=d.snapToGrid;showPlayerHead=d.showPlayerHead;showHatLayer=d.showHatLayer;showNameplate=d.showNameplate;showMicIcon=d.showMicIcon;showWaveform=d.showWaveform;showDistance=d.showDistance;showStatusDot=d.showStatusDot;showBackground=d.showBackground;showSpeakingGlow=d.showSpeakingGlow;showChannelBadge=d.showChannelBadge;headScale=d.headScale;headOpacity=d.headOpacity; }
    private static boolean b(Properties p,String k,boolean d){return Boolean.parseBoolean(p.getProperty(k,Boolean.toString(d)));}
    private static float f(Properties p,String k,float d,float lo,float hi){try{float v=Float.parseFloat(p.getProperty(k));return Float.isFinite(v)?Math.max(lo,Math.min(hi,v)):d;}catch(Exception e){return d;}}
    private static int i(Properties p,String k,int d,int lo,int hi){try{return Math.max(lo,Math.min(hi,Integer.parseInt(p.getProperty(k))));}catch(Exception e){return d;}}
    private static String s(Properties p,String k,String d){String v=p.getProperty(k);return v==null||v.isBlank()?d:v;}
    private static void put(Properties p,String k,Object v){p.setProperty(k,String.valueOf(v));}
}
