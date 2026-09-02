package com.voxline.voice.server;

import com.voxline.voice.network.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public final class VoiceRelay {
    private static final long WINDOW_NS = 1_000_000_000L;
    private static final Map<UUID, RateState> RATE = new ConcurrentHashMap<>();
    private static final class RateState { long start; int count; int lastSeq = -1; long lastSeen; }
    private VoiceRelay() { }

    public static void handle(VoiceC2SPacket packet, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ServerPlayer sender = ctx.getSender();
        if (sender == null || packet == null || !ServerConfig.INSTANCE.enabled || !allow(sender.getUUID(), packet.sequence())) {
            ctx.setPacketHandled(true);
            return;
        }

        float level = Float.isFinite(packet.levelDb()) ? Math.max(-90f, Math.min(0f, packet.levelDb())) : -90f;
        LinkedHashMap<UUID, VoiceChannel> recipients = new LinkedHashMap<>();

        if (packet.channel() == VoiceChannel.PROXIMITY || packet.channel() == VoiceChannel.BOTH) {
            double range = Math.max(4.0, Math.min(64.0, ServerConfig.INSTANCE.maxRange));
            double maxSq = range * range;
            for (ServerPlayer target : sender.serverLevel().players()) {
                if (target != sender && target.distanceToSqr(sender) <= maxSq) recipients.put(target.getUUID(), VoiceChannel.PROXIMITY);
            }
        }

        if (ServerConfig.INSTANCE.groupsEnabled && (packet.channel() == VoiceChannel.GROUP || packet.channel() == VoiceChannel.BOTH)) {
            GroupManager.addGroupRecipients(sender.getUUID(), recipients);
        }

        VoiceS2CPacket proximityPacket = null;
        VoiceS2CPacket groupPacket = null;
        for (Map.Entry<UUID, VoiceChannel> e : recipients.entrySet()) {
            ServerPlayer target = sender.server.getPlayerList().getPlayer(e.getKey());
            if (target == null) continue;
            if (e.getValue() == VoiceChannel.GROUP) {
                if (groupPacket == null) groupPacket = new VoiceS2CPacket(sender.getUUID(), sender.getGameProfile().getName(), packet.sequence(), VoiceChannel.GROUP, packet.opus(), level);
                VoiceNetwork.sendTo(target, groupPacket);
            } else {
                if (proximityPacket == null) proximityPacket = new VoiceS2CPacket(sender.getUUID(), sender.getGameProfile().getName(), packet.sequence(), VoiceChannel.PROXIMITY, packet.opus(), level);
                VoiceNetwork.sendTo(target, proximityPacket);
            }
        }
        ctx.setPacketHandled(true);
    }

    private static boolean allow(UUID id, int seq) {
        long now = System.nanoTime();
        RateState s = RATE.computeIfAbsent(id, ignored -> {
            RateState r = new RateState();
            r.start = now;
            return r;
        });
        synchronized (s) {
            s.lastSeen = now;
            if (now - s.start >= WINDOW_NS) {
                s.start = now;
                s.count = 0;
            }
            if (s.lastSeq >= 0) {
                boolean rollover = s.lastSeq > Integer.MAX_VALUE - 1024 && seq < 1024;
                if (!rollover && seq <= s.lastSeq) return false;
            }
            s.lastSeq = seq;
            return ++s.count <= ServerConfig.INSTANCE.maxPacketsPerSecond;
        }
    }

    public static void clear(UUID playerId) {
        if (playerId != null) RATE.remove(playerId);
    }

    public static void clear() { RATE.clear(); }
}
