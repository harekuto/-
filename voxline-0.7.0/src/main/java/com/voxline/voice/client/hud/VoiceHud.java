package com.voxline.voice.client.hud;

import com.voxline.voice.client.*;
import com.voxline.voice.client.gui.AvatarRenderer;
import com.voxline.voice.client.gui.UiKit;
import com.voxline.voice.client.gui.UiTheme;
import com.voxline.voice.network.VoiceChannel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

import java.util.List;

public final class VoiceHud {
    private VoiceHud() { }

    public static void render(GuiGraphics g) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui || mc.screen != null) return;

        ClientConfig c = ClientConfig.INSTANCE;
        ClientVoiceController ctl = ClientVoiceController.get();
        UiTheme t = UiTheme.current();
        int sw = mc.getWindow().getGuiScaledWidth(), sh = mc.getWindow().getGuiScaledHeight();
        List<ClientVoiceController.SpeakingInfo> active = ctl.activeSpeakers();
        int rows = Math.min(c.maxHudSpeakers, active.size());
        int w = Math.min(c.hudWidth, Math.max(130, sw - 8));
        int h = Math.max(24, c.hudRowHeight);
        int block = rows * (h + 3);
        int x = 4 + Math.round((Math.max(4, sw - w - 8) - 4) * (c.hudAnchorX / 100f));
        int y = 4 + Math.round((Math.max(4, sh - Math.max(block, 26) - 8) - 4) * (c.hudAnchorY / 100f));

        int statusY = y;
        if (ctl.deafened() || ctl.muted() || !ctl.policy().voiceEnabled()) {
            int color = !ctl.policy().voiceEnabled() ? t.amber() : ctl.deafened() ? t.purple() : t.red();
            String label = !ctl.policy().voiceEnabled() ? "VOICE DISABLED" : ctl.deafened() ? "DEAFENED" : "MIC MUTED";
            int pw = Math.min(126, w);
            UiKit.panel(g, x + w - pw, statusY, pw, 22, UiTheme.alpha(t.panel(), .96f), color);
            UiKit.text(g, label, x + w - pw + 8, statusY + 7, color);
            statusY += 25;
            y = statusY;
        }

        int shown = 0;
        for (ClientVoiceController.SpeakingInfo s : active) {
            if (shown++ >= c.maxHudSpeakers) break;
            int border = s.channel() == VoiceChannel.GROUP ? t.purple() : t.green();
            float level = Math.max(0f, Math.min(1f, (s.levelDb() + 60f) / 50f));

            if (c.showBackground) {
                int inset = Math.max(-2, Math.min(4, Math.round((1f - c.backgroundScale) * 4f)));
                UiKit.panel(g, x + inset, y + inset, Math.max(20, w - inset * 2), Math.max(18, h - inset * 2),
                    UiTheme.alpha(t.panel(), c.uiOpacity * c.backgroundOpacity), UiTheme.alpha(border, c.backgroundOpacity));
            }
            if (c.showSpeakingGlow) {
                int glowW = Math.max(2, Math.min(6, Math.round(3f * c.speakingGlowScale)));
                g.fill(x, y, x + glowW, y + h, UiTheme.alpha(border, c.speakingGlowOpacity));
            }

            int rightReserve = 0;
            if (c.showVolumeCircle) {
                int size = Math.max(10, Math.min(18, Math.round(13f * c.volumeCircleScale)));
                drawLevelRing(g, x + w - size - 6, y + (h - size) / 2, size, level,
                    UiTheme.alpha(border, c.volumeCircleOpacity), UiTheme.alpha(0xFF3B4348, c.volumeCircleOpacity));
                rightReserve = size + 9;
            }

            int tx = x + 8;
            if (c.showPlayerHead) {
                int size = Math.max(14, Math.min(h - 6, Math.round(18f * c.headScale)));
                AvatarRenderer.draw(g, s.id(), x + 7, y + (h - size) / 2, size, c.showHatLayer, c.headOpacity);
                tx = x + 13 + size;
            }
            if (c.showMicIcon) {
                int size = Math.max(7, Math.min(13, Math.round(9f * c.micIconScale)));
                drawMic(g, tx, y + (h - size) / 2, size, UiTheme.alpha(border, c.micIconOpacity));
                tx += size + 5;
            }
            if (c.showStatusDot) {
                int dot = Math.max(3, Math.min(8, Math.round(5f * c.statusScale)));
                g.fill(tx, y + (h - dot) / 2, tx + dot, y + (h + dot) / 2, UiTheme.alpha(border, c.statusOpacity));
                tx += dot + 5;
            }

            int textRight = w - 48 - rightReserve;
            if (c.showNameplate) {
                String name = UiKit.fit(c.streamerMode ? "Player" : s.name(), Math.max(25, textRight - (tx - x)));
                scaledText(g, name, tx, y + 6, UiTheme.alpha(t.text(), c.nameplateOpacity), c.nameplateScale);
            }
            if (c.showChannelBadge) {
                String badge = s.channel() == VoiceChannel.GROUP ? "GROUP" : "NEAR";
                UiKit.text(g, badge, x + w - 42 - rightReserve, y + 6, s.channel() == VoiceChannel.GROUP ? t.purple() : t.accent());
            }
            if (c.showDistance && s.channel() != VoiceChannel.GROUP && !c.streamerMode) {
                String d = s.distance() < 0 ? "" : String.format("%.0fm", s.distance());
                scaledText(g, d, x + w - 31 - rightReserve, y + h - 11, UiTheme.alpha(t.muted(), c.distanceOpacity), c.distanceScale);
            }
            if (c.showWaveform) {
                int bars = 6;
                int bw = Math.max(2, Math.round(4f * Math.min(1.25f, c.waveformScale)));
                int gap = Math.max(1, Math.round(2f * c.waveformScale));
                int total = bars * bw + (bars - 1) * gap;
                int bx = x + w - 76 - rightReserve - Math.max(0, total - 34);
                int by = y + h - 8;
                int activeBars = Math.max(1, Math.round(level * bars));
                for (int i = 0; i < bars; i++) {
                    int bh = Math.max(2, Math.round((3 + (i % 3) * 2) * c.waveformScale));
                    int col = i < activeBars ? UiTheme.alpha(border, c.waveformOpacity) : UiTheme.alpha(0xFF3B4348, c.waveformOpacity);
                    int px = bx + i * (bw + gap);
                    g.fill(px, by - bh, px + bw, by, col);
                }
            }
            y += h + 3;
        }

        if (ctl.groupState().inGroup() && active.isEmpty()) {
            int gw = 88, gy = Math.max(4, sh - 34);
            UiKit.panel(g, sw - gw - 6, gy, gw, 24, UiTheme.alpha(t.panel(), .9f), t.purple());
            UiKit.text(g, "GROUP READY", sw - gw + 2, gy + 8, t.purple());
        }
    }

    private static void scaledText(GuiGraphics g, String text, int x, int y, int color, float scale) {
        float s = Math.max(.65f, Math.min(1.5f, scale));
        g.pose().pushPose();
        g.pose().translate(x, y, 0);
        g.pose().scale(s, s, 1f);
        UiKit.text(g, text, 0, 0, color);
        g.pose().popPose();
    }

    private static void drawMic(GuiGraphics g, int x, int y, int size, int color) {
        int w = Math.max(3, size / 2);
        int cx = x + size / 2;
        int top = y + 1;
        int bottom = y + Math.max(4, size - 4);
        g.fill(cx - w / 2, top, cx + (w + 1) / 2, bottom, color);
        g.fill(cx - w / 2 - 2, bottom - 2, cx - w / 2, bottom + 1, color);
        g.fill(cx + (w + 1) / 2, bottom - 2, cx + (w + 1) / 2 + 2, bottom + 1, color);
        g.fill(cx - 1, bottom + 1, cx + 1, y + size - 1, color);
        g.fill(cx - 3, y + size - 2, cx + 3, y + size, color);
    }

    private static void drawLevelRing(GuiGraphics g, int x, int y, int size, float level, int active, int inactive) {
        int seg = Math.max(2, size / 5);
        int max = Math.max(0, Math.min(8, Math.round(level * 8f)));
        int[][] p = {
            {2,0},{5,0},{7,2},{7,5},{5,7},{2,7},{0,5},{0,2}
        };
        int unit = Math.max(1, (size - seg) / 7);
        for (int i = 0; i < p.length; i++) {
            int px = x + p[i][0] * unit;
            int py = y + p[i][1] * unit;
            g.fill(px, py, px + seg, py + seg, i < max ? active : inactive);
        }
    }
}
