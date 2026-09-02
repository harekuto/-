package com.voxline.voice.client;
import com.voxline.voice.network.*;
public final class ClientPacketHandlers{private ClientPacketHandlers(){}public static void handleVoice(VoiceS2CPacket p){ClientVoiceController.get().receive(p);}public static void handleGroup(GroupStateS2CPacket p){ClientVoiceController.get().applyGroupState(p);}public static void handlePolicy(ServerPolicyS2CPacket p){ClientVoiceController.get().applyPolicy(p);}}
