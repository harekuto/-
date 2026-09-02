package net.execheinz.upgrader.client.screen;

import net.minecraft.client.gui.GuiGraphics;

/** Central visual language for Musor Drop. Keep every view on the same palette and depth rules. */
public final class MusorTheme {
    public static final int BG = 0xFF07040A;
    public static final int BG_SOFT = 0xFF0C0711;
    public static final int PANEL = 0xFF12091A;
    public static final int PANEL_HOVER = 0xFF1B0E27;
    public static final int PANEL_DEEP = 0xFF09050D;
    public static final int PANEL_RAISED = 0xFF1D1028;

    public static final int BORDER = 0xFF5A3470;
    public static final int BORDER_SOFT = 0xFF352041;
    public static final int BORDER_FAINT = 0xFF23152C;

    public static final int ACCENT = 0xFFC77CFF;
    public static final int ACCENT_2 = 0xFFA85CDA;
    public static final int ACCENT_DARK = 0xFF6B3A88;
    public static final int ACCENT_GLOW = 0xFFE0B1FF;

    public static final int TEXT = 0xFFF8F2FD;
    public static final int DIM = 0xFFB9A9C5;
    public static final int MUTED = 0xFF7C6D87;
    public static final int GOLD = 0xFFE7BE72;
    public static final int GOLD_SOFT = 0xFF8E7040;
    public static final int SUCCESS = 0xFF87DDB1;
    public static final int FAIL = 0xFFEF7789;

    public static void panel(GuiGraphics g, int x, int y, int w, int h, int fill, int border) {
        shadow(g, x, y, w, h);
        g.fill(x, y, x + w, y + h, 0xFF040206);
        g.fill(x + 1, y + 1, x + w - 1, y + h - 1, border);
        g.fill(x + 2, y + 2, x + w - 2, y + h - 2, fill);
        if (w >= 10 && h >= 10) {
            g.fill(x + 3, y + 3, x + w - 3, y + 4, mix(border, 0xFFFFFFFF, 0.18F));
            g.fill(x + 3, y + 4, x + 4, y + h - 4, mix(fill, 0xFFFFFFFF, 0.055F));
            g.fill(x + 3, y + h - 4, x + w - 3, y + h - 3, darken(border, 44));
            g.fill(x + w - 4, y + 4, x + w - 3, y + h - 4, 0xFF09050D);
        }
    }

    public static void inset(GuiGraphics g, int x, int y, int w, int h) {
        g.fill(x, y, x + w, y + h, BORDER_FAINT);
        g.fill(x + 1, y + 1, x + w - 1, y + h - 1, 0xFF050309);
        g.fill(x + 2, y + 2, x + w - 2, y + 3, 0xFF1E1127);
    }

    public static void softPanel(GuiGraphics g, int x, int y, int w, int h) {
        panel(g, x, y, w, h, PANEL, BORDER_SOFT);
    }

    public static void chip(GuiGraphics g, int x, int y, int w, int h, int accent, boolean active) {
        int border = active ? brighten(accent, 12) : BORDER_SOFT;
        int fill = active ? mix(PANEL_RAISED, accent, 0.14F) : PANEL_DEEP;
        panel(g, x, y, w, h, fill, border);
        if (active && w > 12) {
            g.fill(x + 6, y + h - 3, x + w - 6, y + h - 2, accent);
        }
    }

    public static void slot(GuiGraphics g, int x, int y) {
        g.fill(x - 1, y - 1, x + 19, y + 19, 0xFF47265A);
        g.fill(x, y, x + 18, y + 18, 0xFF09050E);
        g.fill(x + 1, y + 1, x + 17, y + 2, 0xFF684084);
        g.fill(x + 1, y + 2, x + 2, y + 17, 0xFF4B2A5E);
        g.fill(x + 1, y + 16, x + 17, y + 17, 0xFF1E1027);
        g.fill(x + 16, y + 2, x + 17, y + 17, 0xFF160B1D);
    }

    public static void separator(GuiGraphics g, int x1, int y, int x2) {
        g.fill(x1, y, x2, y + 1, BORDER_FAINT);
        if (x2 - x1 > 18) {
            g.fill(x1 + 8, y, x2 - 8, y + 1, 0xFF4C2A60);
        }
    }

    public static void glowLine(GuiGraphics g, int x1, int y, int x2, int accent) {
        g.fill(x1, y, x2, y + 1, darken(accent, 54));
        if (x2 - x1 > 10) g.fill(x1 + 5, y, x2 - 5, y + 1, accent);
    }

    public static void cornerAccents(GuiGraphics g, int x, int y, int w, int h, int accent) {
        int soft = darken(accent, 34);
        g.fill(x + 3, y + 3, x + 11, y + 4, accent);
        g.fill(x + 3, y + 3, x + 4, y + 11, accent);
        g.fill(x + w - 11, y + 3, x + w - 3, y + 4, soft);
        g.fill(x + w - 4, y + 3, x + w - 3, y + 11, soft);
        g.fill(x + 3, y + h - 4, x + 9, y + h - 3, soft);
        g.fill(x + w - 9, y + h - 4, x + w - 3, y + h - 3, soft);
    }

    private static void shadow(GuiGraphics g, int x, int y, int w, int h) {
        g.fill(x + 2, y + h, x + w + 2, y + h + 2, 0x4E000000);
        g.fill(x + w, y + 2, x + w + 2, y + h + 2, 0x3E000000);
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
