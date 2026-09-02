package com.voxline.voice.network;

import io.netty.handler.codec.DecoderException;
import net.minecraft.network.FriendlyByteBuf;

import java.util.UUID;

public record VoiceS2CPacket(UUID sender, String senderName, int sequence, VoiceChannel channel, byte[] opus, float levelDb) {
    public static void encode(VoiceS2CPacket msg, FriendlyByteBuf buf) {
        buf.writeUUID(msg.sender);
        buf.writeUtf(msg.senderName, 32);
        buf.writeVarInt(msg.sequence);
        buf.writeByte(msg.channel.ordinal());
        buf.writeVarInt(msg.opus.length);
        buf.writeBytes(msg.opus);
        buf.writeFloat(msg.levelDb);
    }

    public static VoiceS2CPacket decode(FriendlyByteBuf buf) {
        UUID sender = buf.readUUID();
        String name = buf.readUtf(32);
        int seq = buf.readVarInt();
        if (seq < 0) throw new DecoderException("Negative voice sequence");
        VoiceChannel channel = VoiceChannel.byId(buf.readUnsignedByte());
        int len = buf.readVarInt();
        if (len <= 0 || len > VoiceC2SPacket.MAX_OPUS_BYTES || buf.readableBytes() < len + Float.BYTES) {
            throw new DecoderException("Invalid Voxline Opus payload");
        }
        byte[] data = new byte[len];
        buf.readBytes(data);
        return new VoiceS2CPacket(sender, name, seq, channel, data, buf.readFloat());
    }
}
