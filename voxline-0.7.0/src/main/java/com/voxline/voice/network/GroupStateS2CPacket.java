package com.voxline.voice.network;

import io.netty.handler.codec.DecoderException;
import net.minecraft.network.FriendlyByteBuf;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record GroupStateS2CPacket(boolean inGroup, UUID groupId, String groupName, UUID leader,
                                  List<Member> members, boolean hasInvite, String inviteLabel,
                                  List<Candidate> candidates) {
    public record Member(UUID id, String name, boolean online, boolean leader) { }
    public record Candidate(UUID id, String name) { }

    public static void encode(GroupStateS2CPacket msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.inGroup);
        if (msg.inGroup) {
            buf.writeUUID(msg.groupId);
            buf.writeUtf(msg.groupName, 48);
            buf.writeUUID(msg.leader);
        }
        buf.writeVarInt(Math.min(24, msg.members.size()));
        for (int i = 0; i < Math.min(24, msg.members.size()); i++) {
            Member m = msg.members.get(i);
            buf.writeUUID(m.id); buf.writeUtf(m.name, 32); buf.writeBoolean(m.online); buf.writeBoolean(m.leader);
        }
        buf.writeBoolean(msg.hasInvite);
        if (msg.hasInvite) buf.writeUtf(msg.inviteLabel, 64);
        buf.writeVarInt(Math.min(64, msg.candidates.size()));
        for (int i = 0; i < Math.min(64, msg.candidates.size()); i++) {
            Candidate c = msg.candidates.get(i); buf.writeUUID(c.id); buf.writeUtf(c.name, 32);
        }
    }

    public static GroupStateS2CPacket decode(FriendlyByteBuf buf) {
        boolean in = buf.readBoolean();
        UUID group = null, leader = null; String name = "";
        if (in) { group = buf.readUUID(); name = buf.readUtf(48); leader = buf.readUUID(); }
        int mc = buf.readVarInt(); if (mc < 0 || mc > 24) throw new DecoderException("Invalid member count");
        List<Member> members = new ArrayList<>(mc);
        for (int i=0;i<mc;i++) members.add(new Member(buf.readUUID(), buf.readUtf(32), buf.readBoolean(), buf.readBoolean()));
        boolean invite = buf.readBoolean(); String inviteLabel = invite ? buf.readUtf(64) : "";
        int cc = buf.readVarInt(); if (cc < 0 || cc > 64) throw new DecoderException("Invalid candidate count");
        List<Candidate> candidates = new ArrayList<>(cc);
        for (int i=0;i<cc;i++) candidates.add(new Candidate(buf.readUUID(), buf.readUtf(32)));
        return new GroupStateS2CPacket(in, group, name, leader, List.copyOf(members), invite, inviteLabel, List.copyOf(candidates));
    }
}
