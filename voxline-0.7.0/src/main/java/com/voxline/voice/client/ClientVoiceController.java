package com.voxline.voice.client;

import com.voxline.voice.audio.MicrophoneEngine;
import com.voxline.voice.audio.VoicePlaybackEngine;
import com.voxline.voice.network.*;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public final class ClientVoiceController implements AutoCloseable {
    public record SpeakingInfo(UUID id,String name,float distance,float levelDb,VoiceChannel channel,long lastPacket){}
    public record PlayerMix(float volume,boolean muted){}
    public record KnownPlayer(UUID id,String name,boolean online,boolean groupMember,float distance,float levelDb,VoiceChannel channel){}
    private record Outgoing(int sequence,VoiceChannel channel,byte[] data,float levelDb){}
    private static final ClientVoiceController INSTANCE=new ClientVoiceController(); public static ClientVoiceController get(){return INSTANCE;}
    private static final int MAX_QUEUE=12,MAX_SEND_PER_TICK=3;
    private final ConcurrentLinkedQueue<Outgoing> outgoing=new ConcurrentLinkedQueue<>();private final AtomicInteger queueSize=new AtomicInteger(),sequence=new AtomicInteger();
    private final Map<UUID,SpeakingInfo> speaking=new ConcurrentHashMap<>();private final Map<UUID,PlayerMix> mixes=new ConcurrentHashMap<>();
    private final VoicePlaybackEngine playback=new VoicePlaybackEngine();private final MicrophoneEngine mic=new MicrophoneEngine(this::queueFrame,this::pttActive,this::channelForMic);
    private volatile VoiceChannel heldChannel;private volatile boolean started,muted,deafened,preDeafenMuted;private volatile ServerPolicyS2CPacket policy=new ServerPolicyS2CPacket(true,true,64f,12);private volatile GroupStateS2CPacket groupState=new GroupStateS2CPacket(false,null,"",new UUID(0,0),List.of(),false,"",List.of());
    private ClientVoiceController(){}
    public synchronized void ensureStarted(){if(started)return;ClientConfig.INSTANCE.load();ClientDiagnostics.INSTANCE.reset();started=true;mic.start();playback.start();}
    public void tick(){if(!started)return;boolean p=KeyBindings.PTT.isDown(),g=KeyBindings.GROUP_PTT.isDown()&&policy.groupsEnabled()&&groupState.inGroup();heldChannel=p&&g?VoiceChannel.BOTH:g?VoiceChannel.GROUP:p?VoiceChannel.PROXIMITY:null;drain();long cutoff=System.currentTimeMillis()-1400;speaking.entrySet().removeIf(e->e.getValue().lastPacket()<cutoff);}
    private boolean pttActive(){return !muted&&heldChannel!=null;}
    private VoiceChannel channelForMic(){if(heldChannel!=null)return heldChannel;ClientConfig c=ClientConfig.INSTANCE;return c.vadTargetsGroup&&groupState.inGroup()&&policy.groupsEnabled()?VoiceChannel.GROUP:VoiceChannel.PROXIMITY;}
    private void queueFrame(byte[] data,float level,VoiceChannel channel){if(!started||muted||data==null||data.length==0)return;ClientDiagnostics.INSTANCE.encodedFrames.incrementAndGet();while(queueSize.get()>=MAX_QUEUE){if(outgoing.poll()==null)break;queueSize.decrementAndGet();ClientDiagnostics.INSTANCE.queueDrops.incrementAndGet();}int seq=sequence.getAndUpdate(v->v==Integer.MAX_VALUE?0:v+1);outgoing.offer(new Outgoing(seq,channel,data.clone(),level));queueSize.incrementAndGet();}
    private void drain(){Minecraft mc=Minecraft.getInstance();if(mc.getConnection()==null)return;for(int i=0;i<MAX_SEND_PER_TICK;i++){Outgoing o=outgoing.poll();if(o==null)break;queueSize.decrementAndGet();VoiceNetwork.sendVoice(new VoiceC2SPacket(o.sequence,o.channel,o.data,o.levelDb));ClientDiagnostics.INSTANCE.sentFrames.incrementAndGet();}}
    public void receive(VoiceS2CPacket packet){if(!started||deafened||packet==null)return;Minecraft mc=Minecraft.getInstance();if(mc.player==null)return;PlayerMix mix=mix(packet.sender());if(mix.muted())return;float master=ClientConfig.INSTANCE.masterVolume*mix.volume();float left=master,right=master,distance=-1f;if(packet.channel()==VoiceChannel.GROUP){left=right=master*ClientConfig.INSTANCE.groupVolume;}else{if(mc.level==null)return;Player p=mc.level.getPlayerByUUID(packet.sender());if(p==null)return;distance=mc.player.distanceTo(p);float range=Math.max(4f,Math.min(policy.maxRange(),ClientConfig.INSTANCE.proximityRange));if(distance>range)return;if(ClientConfig.INSTANCE.spatialVoice){float att=Math.max(0f,1f-distance/range);att*=att;double dx=p.getX()-mc.player.getX(),dz=p.getZ()-mc.player.getZ(),len=Math.max(.001,Math.sqrt(dx*dx+dz*dz));dx/=len;dz/=len;double yaw=Math.toRadians(mc.player.getYRot()),rx=-Math.cos(yaw),rz=-Math.sin(yaw);float pan=(float)Math.max(-1,Math.min(1,dx*rx+dz*rz));double a=(pan+1)*Math.PI*.25;left=(float)(Math.cos(a)*att*master);right=(float)(Math.sin(a)*att*master);}}
        playback.enqueue(packet.sender(),packet.sequence(),packet.opus(),left,right);ClientDiagnostics.INSTANCE.receivedFrames.incrementAndGet();float level=Float.isFinite(packet.levelDb())?Math.max(-90f,Math.min(0f,packet.levelDb())):-90f;speaking.put(packet.sender(),new SpeakingInfo(packet.sender(),packet.senderName(),distance,level,packet.channel(),System.currentTimeMillis()));}
    public List<SpeakingInfo> activeSpeakers(){long cutoff=System.currentTimeMillis()-1000;List<SpeakingInfo> out=new ArrayList<>();for(SpeakingInfo s:speaking.values())if(s.lastPacket()>=cutoff)out.add(s);out.sort(Comparator.comparing((SpeakingInfo s)->s.channel()!=VoiceChannel.GROUP).thenComparingDouble(s->s.distance()<0?9999:s.distance()));return out;}
    public List<KnownPlayer> knownPlayers(){Minecraft mc=Minecraft.getInstance();Map<UUID,KnownPlayer> out=new LinkedHashMap<>();if(mc.level!=null&&mc.player!=null)for(Player p:mc.level.players())if(p!=mc.player)out.put(p.getUUID(),new KnownPlayer(p.getUUID(),p.getName().getString(),true,isGroupMember(p.getUUID()),mc.player.distanceTo(p),-90,VoiceChannel.PROXIMITY));for(GroupStateS2CPacket.Member m:groupState.members())if(mc.player==null||!m.id().equals(mc.player.getUUID()))out.put(m.id(),new KnownPlayer(m.id(),m.name(),m.online(),true,-1,-90,VoiceChannel.GROUP));for(SpeakingInfo s:speaking.values())out.put(s.id(),new KnownPlayer(s.id(),s.name(),true,isGroupMember(s.id()),s.distance(),s.levelDb(),s.channel()));return new ArrayList<>(out.values());}
    public PlayerMix mix(UUID id){return mixes.getOrDefault(id,new PlayerMix(1f,false));}public void setPlayerVolume(UUID id,float v){PlayerMix m=mix(id);mixes.put(id,new PlayerMix(Math.max(0,Math.min(2,v)),m.muted()));}public void togglePlayerMute(UUID id){PlayerMix m=mix(id);mixes.put(id,new PlayerMix(m.volume(),!m.muted()));}public void resetMix(){mixes.clear();}
    public void toggleMute(){muted=!muted;}public void toggleDeafen(){if(!deafened){preDeafenMuted=muted;muted=true;deafened=true;}else{deafened=false;muted=preDeafenMuted;}}public boolean muted(){return muted;}public boolean deafened(){return deafened;}
    public float micLevelDb(){return mic.meterDb();}public String micError(){return mic.error();}public String outputError(){return playback.error();}public int sendQueue(){return queueSize.get();}public com.voxline.voice.audio.AudioProcessor.Stats audioStats(){return mic.stats();}
    public void applyPolicy(ServerPolicyS2CPacket p){if(p!=null)policy=p;}public ServerPolicyS2CPacket policy(){return policy;}public void applyGroupState(GroupStateS2CPacket s){if(s!=null)groupState=s;}public GroupStateS2CPacket groupState(){return groupState;}public boolean isGroupMember(UUID id){for(GroupStateS2CPacket.Member m:groupState.members())if(m.id().equals(id))return true;return false;}
    public void restartInput(){if(started)mic.restart();}public void restartOutput(){if(started)playback.restart();}
    public synchronized void disconnect(){close();speaking.clear();mixes.clear();outgoing.clear();queueSize.set(0);sequence.set(0);heldChannel=null;policy=new ServerPolicyS2CPacket(true,true,64f,12);groupState=new GroupStateS2CPacket(false,null,"",new UUID(0,0),List.of(),false,"",List.of());muted=deafened=false;}
    @Override public synchronized void close(){mic.close();playback.close();started=false;outgoing.clear();queueSize.set(0);}
}
