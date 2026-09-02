package com.voxline.voice.network;

import io.netty.handler.codec.DecoderException;
import net.minecraft.network.FriendlyByteBuf;

import java.util.UUID;

public record GroupActionC2SPacket(Action action, UUID target) {
    public enum Action { CREATE, LEAVE, DISBAND, INVITE, ACCEPT, DECLINE, KICK, PROMOTE }

    public static void encode(GroupActionC2SPacket msg, FriendlyByteBuf buf) {
        buf.writeByte(msg.action.ordinal());
        buf.writeBoolean(msg.target != null);
        if (msg.target != null) buf.writeUUID(msg.target);
    }

    public static GroupActionC2SPacket decode(FriendlyByteBuf buf) {
        int id = buf.readUnsignedByte();
        if (id >= Action.values().length) throw new DecoderException("Invalid group action");
        UUID target = buf.readBoolean() ? buf.readUUID() : null;
        return new GroupActionC2SPacket(Action.values()[id], target);
    }
}
