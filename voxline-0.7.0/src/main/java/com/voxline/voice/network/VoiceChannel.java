package com.voxline.voice.network;

public enum VoiceChannel {
    PROXIMITY,
    GROUP,
    BOTH;

    public static VoiceChannel byId(int id) {
        return switch (id) {
            case 1 -> GROUP;
            case 2 -> BOTH;
            default -> PROXIMITY;
        };
    }
}
