package com.voxline.voice.server;

import com.voxline.voice.network.GroupActionC2SPacket;
import com.voxline.voice.network.GroupStateS2CPacket;
import com.voxline.voice.network.VoiceChannel;
import com.voxline.voice.network.VoiceNetwork;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.*;
import java.util.function.Supplier;

public final class GroupManager {
    private static final Map<UUID, Group> GROUPS = new HashMap<>();
    private static final Map<UUID, UUID> MEMBER_TO_GROUP = new HashMap<>();
    private static final Map<UUID, String> MEMBER_NAMES = new HashMap<>();
    private static final Map<UUID, Invite> INVITES = new HashMap<>();
    private static final long INVITE_TTL_MS = 60_000L;

    private record Invite(UUID groupId, UUID inviter, long expiresAt) { }

    private static final class Group {
        final UUID id = UUID.randomUUID();
        String name;
        UUID leader;
        final LinkedHashSet<UUID> members = new LinkedHashSet<>();
        Group(ServerPlayer creator) {
            leader = creator.getUUID();
            name = creator.getGameProfile().getName() + "'s Group";
            members.add(leader);
        }
    }

    private GroupManager() { }

    public static synchronized void handleAction(GroupActionC2SPacket packet, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ServerPlayer sender = ctx.getSender();
        if (sender == null || packet == null || !ServerConfig.INSTANCE.groupsEnabled) {
            ctx.setPacketHandled(true);
            return;
        }
        remember(sender);
        MinecraftServer server = sender.server;
        cleanupInvites();
        switch (packet.action()) {
            case CREATE -> create(sender);
            case LEAVE -> leave(sender);
            case DISBAND -> disband(sender);
            case INVITE -> invite(sender, packet.target());
            case ACCEPT -> accept(sender);
            case DECLINE -> { INVITES.remove(sender.getUUID()); sendState(sender); }
            case KICK -> kick(sender, packet.target());
            case PROMOTE -> promote(sender, packet.target());
        }
        refreshAll(server);
        ctx.setPacketHandled(true);
    }

    private static void create(ServerPlayer player) {
        if (MEMBER_TO_GROUP.containsKey(player.getUUID())) return;
        remember(player);
        Group g = new Group(player);
        GROUPS.put(g.id, g);
        MEMBER_TO_GROUP.put(player.getUUID(), g.id);
    }

    private static void invite(ServerPlayer sender, UUID targetId) {
        if (targetId == null) return;
        Group g = groupOf(sender.getUUID());
        if (g == null || !g.leader.equals(sender.getUUID()) || g.members.size() >= ServerConfig.INSTANCE.maxGroupSize) return;
        ServerPlayer target = sender.server.getPlayerList().getPlayer(targetId);
        if (target == null || MEMBER_TO_GROUP.containsKey(targetId)) return;
        remember(target);
        INVITES.put(targetId, new Invite(g.id, sender.getUUID(), System.currentTimeMillis() + INVITE_TTL_MS));
        sendState(target);
    }

    private static void accept(ServerPlayer player) {
        Invite inv = INVITES.remove(player.getUUID());
        if (inv == null || inv.expiresAt < System.currentTimeMillis() || MEMBER_TO_GROUP.containsKey(player.getUUID())) return;
        Group g = GROUPS.get(inv.groupId);
        if (g == null || g.members.size() >= ServerConfig.INSTANCE.maxGroupSize) return;
        remember(player);
        g.members.add(player.getUUID());
        MEMBER_TO_GROUP.put(player.getUUID(), g.id);
    }

    private static void leave(ServerPlayer player) {
        Group g = groupOf(player.getUUID());
        if (g == null) return;
        UUID id = player.getUUID();
        g.members.remove(id);
        MEMBER_TO_GROUP.remove(id);
        INVITES.remove(id);
        if (g.members.isEmpty()) {
            GROUPS.remove(g.id);
            return;
        }
        if (g.leader.equals(id)) g.leader = g.members.iterator().next();
    }

    private static void disband(ServerPlayer player) {
        Group g = groupOf(player.getUUID());
        if (g == null || !g.leader.equals(player.getUUID())) return;
        for (UUID id : g.members) MEMBER_TO_GROUP.remove(id);
        GROUPS.remove(g.id);
        INVITES.entrySet().removeIf(e -> e.getValue().groupId.equals(g.id));
    }

    private static void kick(ServerPlayer sender, UUID target) {
        Group g = groupOf(sender.getUUID());
        if (g == null || target == null || !g.leader.equals(sender.getUUID()) || target.equals(sender.getUUID())) return;
        if (g.members.remove(target)) MEMBER_TO_GROUP.remove(target);
        INVITES.remove(target);
    }

    private static void promote(ServerPlayer sender, UUID target) {
        Group g = groupOf(sender.getUUID());
        if (g == null || target == null || !g.leader.equals(sender.getUUID()) || !g.members.contains(target)) return;
        g.leader = target;
    }

    public static synchronized Set<UUID> membersOf(UUID player) {
        Group g = groupOf(player);
        return g == null ? Set.of() : Set.copyOf(g.members);
    }

    /** Hot-path relay helper: fills the dedup map without allocating a temporary member Set. */
    public static synchronized void addGroupRecipients(UUID player, Map<UUID, VoiceChannel> recipients) {
        if (recipients == null) return;
        Group g = groupOf(player);
        if (g == null) return;
        for (UUID id : g.members) if (!id.equals(player)) recipients.put(id, VoiceChannel.GROUP);
    }

    public static synchronized boolean sameGroup(UUID a, UUID b) {
        UUID ga = MEMBER_TO_GROUP.get(a), gb = MEMBER_TO_GROUP.get(b);
        return ga != null && ga.equals(gb);
    }

    private static Group groupOf(UUID player) {
        UUID gid = MEMBER_TO_GROUP.get(player);
        return gid == null ? null : GROUPS.get(gid);
    }

    public static synchronized void sendState(ServerPlayer player) {
        remember(player);
        cleanupInvites();
        Group g = groupOf(player.getUUID());
        List<GroupStateS2CPacket.Member> members = new ArrayList<>();
        if (g != null) {
            for (UUID id : g.members) {
                ServerPlayer online = player.server.getPlayerList().getPlayer(id);
                if (online != null) remember(online);
                String name = online != null ? online.getGameProfile().getName() : MEMBER_NAMES.getOrDefault(id, id.toString().substring(0, 8));
                members.add(new GroupStateS2CPacket.Member(id, name, online != null, id.equals(g.leader)));
            }
        }

        Invite inv = INVITES.get(player.getUUID());
        String inviteLabel = "";
        if (inv != null) {
            Group ig = GROUPS.get(inv.groupId);
            ServerPlayer inviter = player.server.getPlayerList().getPlayer(inv.inviter);
            String inviterName = inviter != null ? inviter.getGameProfile().getName() : MEMBER_NAMES.getOrDefault(inv.inviter, "Player");
            inviteLabel = inviterName + " invited you to " + (ig == null ? "a group" : ig.name);
        }

        List<GroupStateS2CPacket.Candidate> candidates = new ArrayList<>();
        for (ServerPlayer p : player.server.getPlayerList().getPlayers()) {
            UUID id = p.getUUID();
            remember(p);
            if (!id.equals(player.getUUID()) && !MEMBER_TO_GROUP.containsKey(id)) {
                candidates.add(new GroupStateS2CPacket.Candidate(id, p.getGameProfile().getName()));
            }
        }

        VoiceNetwork.sendTo(player, new GroupStateS2CPacket(
            g != null,
            g == null ? null : g.id,
            g == null ? "" : g.name,
            g == null ? player.getUUID() : g.leader,
            List.copyOf(members),
            inv != null,
            inviteLabel,
            List.copyOf(candidates)
        ));
    }

    private static void remember(ServerPlayer player) {
        if (player != null) MEMBER_NAMES.put(player.getUUID(), player.getGameProfile().getName());
    }

    private static void refreshAll(MinecraftServer server) {
        for (ServerPlayer p : server.getPlayerList().getPlayers()) sendState(p);
    }

    private static void cleanupInvites() {
        long now = System.currentTimeMillis();
        INVITES.entrySet().removeIf(e -> e.getValue().expiresAt < now || !GROUPS.containsKey(e.getValue().groupId));
    }

    public static synchronized void onLogout(ServerPlayer player) {
        if (player == null) return;
        remember(player);
        INVITES.remove(player.getUUID());
        refreshAll(player.server);
    }

    public static synchronized void clear() {
        GROUPS.clear();
        MEMBER_TO_GROUP.clear();
        MEMBER_NAMES.clear();
        INVITES.clear();
    }
}
