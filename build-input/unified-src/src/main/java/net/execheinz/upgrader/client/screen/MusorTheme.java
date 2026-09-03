package net.execheinz.upgrader.client.screen;

import net.minecraft.client.gui.GuiGraphics;

/** Compact pixel-native visual language for Musor Drop. */
public final class MusorTheme {
    public static final int BG = 0xFF07050A;
    public static final int BG_SOFT = 0xFF0B0810;
    public static final int PANEL = 0xFF110B17;
    public static final int PANEL_HOVER = 0xFF1A1022;
    public static final int PANEL_DEEP = 0xFF09060D;
    public static final int PANEL_RAISED = 0xFF1B1124;

    public static final int BORDER = 0xFF563368;
    public static final int BORDER_SOFT = 0xFF34203E;
    public static final int BORDER_FAINT = 0xFF24162B;

    public static final int ACCENT = 0xFFBC6CF4;
    public static final int ACCENT_2 = 0xFF9650C8;
    public static final int ACCENT_DARK = 0xFF6A3983;
    public static final int ACCENT_GLOW = 0xFFDDA8FF;

    public static final int TEXT = 0xFFF7F1FB;
    public static final int DIM = 0xFFB7A8C1;
    public static final int MUTED = 0xFF7D7086;
    public static final int GOLD = 0xFFE7C16C;
    public static final int GOLD_SOFT = 0xFF8B6B39;
    public static final int SUCCESS = 0xFF82D5AA;
    public static final int FAIL = 0xFFEA7487;

    /** One-pixel frame with restrained depth. Avoids the old nested-box look. */
    public static void panel(GuiGraphics g, int x, int y, int w, int h, int fill, int border) {
        if (w <= 1 || h <= 1) return;
        g.fill(x + 1, y + h, x + w + 1, y + h + 1, 0x46000000);
        g.fill(x + w, y + 1, x + w + 1, y + h + 1, 0x34000000);
        g.fill(x, y, x + w, y + h, border);
        g.fill(x + 1, y + 1, x + w - 1, y + h - 1, fill);
        if (w >= 12 && h >= 8) {
            g.fill(x + 2, y + 2, x + w - 2, y + 3, mix(fill, 0xFFFFFFFF, 0.055F));
            g.fill(x + 2, y + h - 2, x + w - 2, y + h - 1, darken(fill, 10));
        }
    }

    public static void inset(GuiGraphics g, int x, int y, int w, int h) {
        g.fill(x, y, x + w, y + h, BORDER_FAINT);
        g.fill(x + 1, y + 1, x + w - 1, y + h - 1, 0xFF060409);
        if (w > 5) g.fill(x + 2, y + 2, x + w - 2, y + 3, 0xFF1A1020);
    }

    public static void softPanel(GuiGraphics g, int x, int y, int w, int h) {
        panel(g, x, y, w, h, PANEL, BORDER_SOFT);
    }

    public static void chip(GuiGraphics g, int x, int y, int w, int h, int accent, boolean active) {
        int border = active ? darken(accent, 24) : BORDER_SOFT;
        int fill = active ? mix(PANEL_RAISED, accent, 0.10F) : PANEL_DEEP;
        panel(g, x, y, w, h, fill, border);
        if (active && w > 10) g.fill(x + 5, y + h - 2, x + w - 5, y + h - 1, accent);
    }

    public static void slot(GuiGraphics g, int x, int y) {
        g.fill(x - 1, y - 1, x + 19, y + 19, 0xFF432752);
        g.fill(x, y, x + 18, y + 18, 0xFF08050C);
        g.fill(x + 1, y + 1, x + 17, y + 2, 0xFF5A366E);
        g.fill(x + 1, y + 2, x + 2, y + 17, 0xFF3D234A);
        g.fill(x + 1, y + 16, x + 17, y + 17, 0xFF180D1E);
        g.fill(x + 16, y + 2, x + 17, y + 17, 0xFF120A17);
    }

    public static void separator(GuiGraphics g, int x1, int y, int x2) {
        if (x2 <= x1) return;
        g.fill(x1, y, x2, y + 1, BORDER_FAINT);
        if (x2 - x1 > 22) g.fill(x1 + 10, y, x2 - 10, y + 1, 0xFF472857);
    }

    public static void glowLine(GuiGraphics g, int x1, int y, int x2, int accent) {
        if (x2 <= x1) return;
        g.fill(x1, y, x2, y + 1, darken(accent, 60));
        if (x2 - x1 > 12) g.fill(x1 + 6, y, x2 - 6, y + 1, accent);
    }

    /** Short corner ticks, not full decorative brackets. */
    public static void cornerAccents(GuiGraphics g, int x, int y, int w, int h, int accent) {
        if (w < 9 || h < 9) return;
        int soft = darken(accent, 36);
        g.fill(x + 2, y + 2, x + 7, y + 3, accent);
        g.fill(x + 2, y + 2, x + 3, y + 7, accent);
        g.fill(x + w - 7, y + 2, x + w - 2, y + 3, soft);
        g.fill(x + w - 3, y + 2, x + w - 2, y + 7, soft);
    }

    public static int mix(int a, int b, float t) {
        t = Math.max(0F, Math.min(1F, t));
        int aa = (a >>> 24) & 255;
        int ar = (a >>> 16) & 255;
        int ag = (a >>> 8) & 255;
        int ab = a & 255;
        int ba = (b >>> 24) & 255;
        int br = (b >>> 16) & 255;
        int bg = (b >>> 8) & 255;
        int bb = b & 255;
        return ((int) (aa + (ba - aa) * t) << 24)
            | ((int) (ar + (br - ar) * t) << 16)
            | ((int) (ag + (bg - ag) * t) << 8)
            | (int) (ab + (bb - ab) * t);
    }

    public static int brighten(int color, int delta) {
        int a = (color >>> 24) & 255;
        int r = Math.min(255, ((color >>> 16) & 255) + delta);
        int g = Math.min(255, ((color >>> 8) & 255) + delta);
        int b = Math.min(255, (color & 255) + delta);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    public static int darken(int color, int delta) {
        int a = (color >>> 24) & 255;
        int r = Math.max(0, ((color >>> 16) & 255) - delta);
        int g = Math.max(0, ((color >>> 8) & 255) - delta);
        int b = Math.max(0, (color & 255) - delta);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private MusorTheme() {}
}
