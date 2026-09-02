package net.execheinz.upgrader.client.screen;

import net.minecraft.client.gui.GuiGraphics;

public final class MusorTheme {
    public static final int BG = 0xFF07040B;
    public static final int BG_SOFT = 0xFF0D0713;
    public static final int PANEL = 0xFF130A1C;
    public static final int PANEL_HOVER = 0xFF1A0E26;
    public static final int PANEL_DEEP = 0xFF09050E;
    public static final int PANEL_RAISED = 0xFF1B1025;

    public static final int BORDER = 0xFF56306F;
    public static final int BORDER_SOFT = 0xFF32203F;
    public static final int BORDER_FAINT = 0xFF21142B;

    public static final int ACCENT = 0xFFC884FF;
    public static final int ACCENT_2 = 0xFFA85DDA;
    public static final int ACCENT_DARK = 0xFF6D3A8D;
    public static final int ACCENT_GLOW = 0xFFDBA7FF;

    public static final int TEXT = 0xFFF7F0FD;
    public static final int DIM = 0xFFB8A8C6;
    public static final int MUTED = 0xFF796B85;
    public static final int GOLD = 0xFFE8BF72;
    public static final int SUCCESS = 0xFF87DDB2;
    public static final int FAIL = 0xFFEE798A;

    public static void panel(GuiGraphics g, int x, int y, int w, int h, int fill, int border) {
        shadow(g, x, y, w, h);
        g.fill(x, y, x + w, y + h, 0xFF050207);
        g.fill(x + 1, y + 1, x + w - 1, y + h - 1, border);
        g.fill(x + 2, y + 2, x + w - 2, y + h - 2, fill);
        if (w >= 8 && h >= 8) {
            g.fill(x + 3, y + 3, x + w - 3, y + 4, mix(border, 0xFFFFFFFF, 0.20F));
            g.fill(x + 3, y + h - 4, x + w - 3, y + h - 3, darken(border, 42));
            g.fill(x + 3, y + 4, x + 4, y + h - 4, mix(fill, 0xFFFFFFFF, 0.06F));
        }
    }

    public static void softPanel(GuiGraphics g, int x, int y, int w, int h) {
        panel(g, x, y, w, h, PANEL, BORDER_SOFT);
    }

    public static void chip(GuiGraphics g, int x, int y, int w, int h, int accent, boolean active) {
        int border = active ? brighten(accent, 18) : BORDER_SOFT;
        int fill = active ? mix(PANEL_RAISED, accent, 0.17F) : PANEL_DEEP;
        panel(g, x, y, w, h, fill, border);
        if (active && w > 10) {
            g.fill(x + 5, y + h - 3, x + w - 5, y + h - 2, accent);
        }
    }

    public static void slot(GuiGraphics g, int x, int y) {
        g.fill(x - 1, y - 1, x + 19, y + 19, 0xFF452358);
        g.fill(x, y, x + 18, y + 18, 0xFF0A050F);
        g.fill(x + 1, y + 1, x + 17, y + 2, 0xFF684085);
        g.fill(x + 1, y + 2, x + 2, y + 17, 0xFF4A2A5E);
        g.fill(x + 1, y + 16, x + 17, y + 17, 0xFF201029);
        g.fill(x + 16, y + 2, x + 17, y + 17, 0xFF180C20);
    }

    public static void separator(GuiGraphics g, int x1, int y, int x2) {
        g.fill(x1, y, x2, y + 1, BORDER_FAINT);
        if (x2 - x1 > 16) g.fill(x1 + 8, y, x2 - 8, y + 1, 0xFF4E2B64);
    }

    public static void glowLine(GuiGraphics g, int x1, int y, int x2, int accent) {
        g.fill(x1, y, x2, y + 1, darken(accent, 48));
        if (x2 - x1 > 8) g.fill(x1 + 4, y, x2 - 4, y + 1, accent);
    }

    private static void shadow(GuiGraphics g, int x, int y, int w, int h) {
        g.fill(x + 2, y + h, x + w + 2, y + h + 2, 0x55000000);
        g.fill(x + w, y + 2, x + w + 2, y + h + 2, 0x44000000);
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
