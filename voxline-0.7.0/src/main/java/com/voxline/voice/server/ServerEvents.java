package com.voxline.voice.server;

import com.voxline.voice.network.ServerPolicyS2CPacket;
import com.voxline.voice.network.VoiceNetwork;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class ServerEvents {
    @SubscribeEvent
    public void onServerStart(ServerAboutToStartEvent event) {
        ServerConfig.INSTANCE.load();
        GroupManager.clear();
        VoiceRelay.clear();
    }

    @SubscribeEvent
    public void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer p) {
            ServerConfig c = ServerConfig.INSTANCE;
            VoiceNetwork.sendTo(p, new ServerPolicyS2CPacket(c.enabled, c.groupsEnabled, (float) c.maxRange, c.maxGroupSize));
            GroupManager.sendState(p);
        }
    }

    @SubscribeEvent
    public void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer p) {
            VoiceRelay.clear(p.getUUID());
            GroupManager.onLogout(p);
        }
    }

    @SubscribeEvent
    public void onStop(ServerStoppingEvent event) {
        GroupManager.clear();
        VoiceRelay.clear();
    }
}
