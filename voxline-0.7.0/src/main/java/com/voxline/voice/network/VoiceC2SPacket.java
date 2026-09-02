package com.voxline.voice.network;

import io.netty.handler.codec.DecoderException;
import net.minecraft.network.FriendlyByteBuf;

public record VoiceC2SPacket(int sequence, VoiceChannel channel, byte[] opus, float levelDb) {
    public static final int MAX_OPUS_BYTES = 512;

    public static void encode(VoiceC2SPacket msg, FriendlyByteBuf buf) {
        if (msg.sequence < 0 || msg.opus == null || msg.opus.length == 0 || msg.opus.length > MAX_OPUS_BYTES) {
            throw new IllegalArgumentException("Invalid Voxline voice frame");
        }
        buf.writeVarInt(msg.sequence);
        buf.writeByte(msg.channel.ordinal());
        buf.writeVarInt(msg.opus.length);
        buf.writeBytes(msg.opus);
        buf.writeFloat(msg.levelDb);
    }

    public static VoiceC2SPacket decode(FriendlyByteBuf buf) {
        int seq = buf.readVarInt();
        if (seq < 0) throw new DecoderException("Negative voice sequence");
        VoiceChannel channel = VoiceChannel.byId(buf.readUnsignedByte());
        int len = buf.readVarInt();
        if (len <= 0 || len > MAX_OPUS_BYTES || buf.readableBytes() < len + Float.BYTES) {
            throw new DecoderException("Invalid Voxline Opus payload");
        }
        byte[] data = new byte[len];
        buf.readBytes(data);
        return new VoiceC2SPacket(seq, channel, data, buf.readFloat());
    }
}
