package com.voxline.voice.network;

import com.voxline.voice.VoxlineVoice;
import com.voxline.voice.server.GroupManager;
import com.voxline.voice.server.VoiceRelay;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public final class VoiceNetwork {
    private static final String PROTOCOL = "7";
    public static final SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder
        .named(new ResourceLocation(VoxlineVoice.MOD_ID, "voice"))
        .networkProtocolVersion(() -> PROTOCOL)
        .clientAcceptedVersions(PROTOCOL::equals)
        .serverAcceptedVersions(PROTOCOL::equals)
        .simpleChannel();
    private static int id;
    private VoiceNetwork() { }

    public static void register() {
        CHANNEL.messageBuilder(VoiceC2SPacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
            .encoder(VoiceC2SPacket::encode).decoder(VoiceC2SPacket::decode).consumerMainThread(VoiceRelay::handle).add();
        CHANNEL.messageBuilder(VoiceS2CPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
            .encoder(VoiceS2CPacket::encode).decoder(VoiceS2CPacket::decode).consumerMainThread((msg, ctx) -> {
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> com.voxline.voice.client.ClientPacketHandlers.handleVoice(msg));
                ctx.get().setPacketHandled(true);
            }).add();
        CHANNEL.messageBuilder(GroupActionC2SPacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
            .encoder(GroupActionC2SPacket::encode).decoder(GroupActionC2SPacket::decode).consumerMainThread(GroupManager::handleAction).add();
        CHANNEL.messageBuilder(GroupStateS2CPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
            .encoder(GroupStateS2CPacket::encode).decoder(GroupStateS2CPacket::decode).consumerMainThread((msg, ctx) -> {
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> com.voxline.voice.client.ClientPacketHandlers.handleGroup(msg));
                ctx.get().setPacketHandled(true);
            }).add();
        CHANNEL.messageBuilder(ServerPolicyS2CPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
            .encoder(ServerPolicyS2CPacket::encode).decoder(ServerPolicyS2CPacket::decode).consumerMainThread((msg, ctx) -> {
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> com.voxline.voice.client.ClientPacketHandlers.handlePolicy(msg));
                ctx.get().setPacketHandled(true);
            }).add();
    }

    public static void sendVoice(VoiceC2SPacket packet) { CHANNEL.sendToServer(packet); }
    public static void sendGroupAction(GroupActionC2SPacket packet) { CHANNEL.sendToServer(packet); }
    public static void sendTo(ServerPlayer player, Object packet) { CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet); }
}
