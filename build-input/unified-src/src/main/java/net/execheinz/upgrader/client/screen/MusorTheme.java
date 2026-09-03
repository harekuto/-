package net.execheinz.upgrader.client.screen;

import net.minecraft.client.gui.GuiGraphics;

/**
 * Premium pixel-native visual language for Musor Drop.
 *
 * The theme deliberately avoids the old "border inside border" look. Surfaces use a single
 * crisp edge, a restrained top highlight, and a one-pixel drop shadow so hierarchy comes from
 * spacing and contrast rather than noisy decoration.
 */
public final class MusorTheme {
    public static final int BG = 0xFF07050A;
    public static final int BG_SOFT = 0xFF0D0912;
    public static final int PANEL = 0xFF120C18;
    public static final int PANEL_HOVER = 0xFF1A1122;
    public static final int PANEL_DEEP = 0xFF09060E;
    public static final int PANEL_RAISED = 0xFF1B1124;

    public static final int BORDER = 0xFF4E315E;
    public static final int BORDER_SOFT = 0xFF302139;
    public static final int BORDER_FAINT = 0xFF221828;

    public static final int ACCENT = 0xFFB866EE;
    public static final int ACCENT_2 = 0xFF8F4FC1;
    public static final int ACCENT_DARK = 0xFF663A7E;
    public static final int ACCENT_GLOW = 0xFFD79BFF;

    public static final int TEXT = 0xFFF6F0FA;
    public static final int DIM = 0xFFB9A9C3;
    public static final int MUTED = 0xFF817386;
    public static final int GOLD = 0xFFE4BE69;
    public static final int GOLD_SOFT = 0xFF846738;
    public static final int SUCCESS = 0xFF78D6A4;
    public static final int FAIL = 0xFFED7488;
    public static final int INFO = 0xFF8CA9FF;

    public static void panel(GuiGraphics g, int x, int y, int w, int h, int fill, int border) {
        if (w <= 1 || h <= 1) return;
        g.fill(x + 1, y + h, x + w + 1, y + h + 1, 0x4A000000);
        g.fill(x + w, y + 1, x + w + 1, y + h + 1, 0x32000000);
        g.fill(x, y, x + w, y + h, border);
        g.fill(x + 1, y + 1, x + w - 1, y + h - 1, fill);
        if (w >= 10 && h >= 7) {
            g.fill(x + 2, y + 2, x + w - 2, y + 3, mix(fill, 0xFFFFFFFF, 0.045F));
        }
    }

    public static void softPanel(GuiGraphics g, int x, int y, int w, int h) {
        panel(g, x, y, w, h, PANEL, BORDER_SOFT);
    }

    public static void inset(GuiGraphics g, int x, int y, int w, int h) {
        if (w <= 1 || h <= 1) return;
        g.fill(x, y, x + w, y + h, BORDER_FAINT);
        g.fill(x + 1, y + 1, x + w - 1, y + h - 1, 0xFF060409);
        if (w > 5) g.fill(x + 2, y + 2, x + w - 2, y + 3, 0xFF17101D);
    }

    public static void field(GuiGraphics g, int x, int y, int w, int h, boolean focused) {
        int border = focused ? ACCENT_DARK : BORDER_FAINT;
        panel(g, x, y, w, h, PANEL_DEEP, border);
        if (focused && w > 12) g.fill(x + 4, y + h - 2, x + w - 4, y + h - 1, ACCENT);
    }

    public static void chip(GuiGraphics g, int x, int y, int w, int h, int accent, boolean active) {
        int border = active ? darken(accent, 32) : BORDER_SOFT;
        int fill = active ? mix(PANEL_RAISED, accent, 0.075F) : PANEL_DEEP;
        panel(g, x, y, w, h, fill, border);
    }

    public static void badge(GuiGraphics g, int x, int y, int w, int h, int accent) {
        g.fill(x, y, x + w, y + h, darken(accent, 54));
        g.fill(x + 1, y + 1, x + w - 1, y + h - 1, mix(PANEL_DEEP, accent, 0.09F));
        if (w > 5) g.fill(x + 2, y + h - 1, x + w - 2, y + h, accent);
    }

    public static void slot(GuiGraphics g, int x, int y) {
        g.fill(x - 1, y - 1, x + 19, y + 19, 0xFF3D2949);
        g.fill(x, y, x + 18, y + 18, 0xFF07050B);
        g.fill(x + 1, y + 1, x + 17, y + 2, 0xFF523561);
        g.fill(x + 1, y + 2, x + 2, y + 17, 0xFF42284F);
        g.fill(x + 1, y + 16, x + 17, y + 17, 0xFF170E1B);
        g.fill(x + 16, y + 2, x + 17, y + 17, 0xFF120B16);
    }

    public static void separator(GuiGraphics g, int x1, int y, int x2) {
        if (x2 <= x1) return;
        g.fill(x1, y, x2, y + 1, BORDER_FAINT);
        if (x2 - x1 > 28) g.fill(x1 + 12, y, x2 - 12, y + 1, 0xFF3E2849);
    }

    public static void glowLine(GuiGraphics g, int x1, int y, int x2, int accent) {
        if (x2 <= x1) return;
        g.fill(x1, y, x2, y + 1, darken(accent, 62));
        if (x2 - x1 > 12) g.fill(x1 + 7, y, x2 - 7, y + 1, accent);
    }

    public static void cornerAccents(GuiGraphics g, int x, int y, int w, int h, int accent) {
        if (w < 9 || h < 9) return;
        int soft = darken(accent, 40);
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
