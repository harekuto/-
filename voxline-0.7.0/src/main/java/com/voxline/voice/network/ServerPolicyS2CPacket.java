package com.voxline.voice.network;

import io.netty.handler.codec.DecoderException;
import net.minecraft.network.FriendlyByteBuf;

public record ServerPolicyS2CPacket(boolean voiceEnabled, boolean groupsEnabled, float maxRange, int maxGroupSize) {
    public static void encode(ServerPolicyS2CPacket msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.voiceEnabled); buf.writeBoolean(msg.groupsEnabled); buf.writeFloat(msg.maxRange); buf.writeVarInt(msg.maxGroupSize);
    }
    public static ServerPolicyS2CPacket decode(FriendlyByteBuf buf) {
        boolean voice = buf.readBoolean(), groups = buf.readBoolean(); float range = buf.readFloat(); int size = buf.readVarInt();
        if (!Float.isFinite(range) || range < 4f || range > 64f || size < 2 || size > 24) throw new DecoderException("Invalid Voxline policy");
        return new ServerPolicyS2CPacket(voice, groups, range, size);
    }
}
