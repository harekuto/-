package net.execheinz.upgrader.client.screen;

import net.minecraft.client.gui.GuiGraphics;

public final class MusorTheme {
    public static final int BG = 0xFF08050D;
    public static final int BG_SOFT = 0xFF100818;
    public static final int PANEL = 0xFF150A20;
    public static final int PANEL_HOVER = 0xFF21102F;
    public static final int PANEL_DEEP = 0xFF0D0713;
    public static final int BORDER = 0xFF5D3478;
    public static final int BORDER_SOFT = 0xFF3A214A;
    public static final int ACCENT = 0xFFC47DFF;
    public static final int ACCENT_2 = 0xFFA862D8;
    public static final int ACCENT_DARK = 0xFF6D3A8D;
    public static final int TEXT = 0xFFF6EEFF;
    public static final int DIM = 0xFFB8A6C7;
    public static final int MUTED = 0xFF80718D;
    public static final int GOLD = 0xFFE7BD70;
    public static final int SUCCESS = 0xFF82D6AE;
    public static final int FAIL = 0xFFE86F82;

    public static void panel(GuiGraphics g, int x, int y, int w, int h, int fill, int border) {
        g.fill(x, y, x + w, y + h, 0xFF060309);
        g.fill(x + 1, y + 1, x + w - 1, y + h - 1, border);
        g.fill(x + 2, y + 2, x + w - 2, y + h - 2, fill);
        if (w > 6 && h > 6) {
            g.fill(x + 3, y + 3, x + w - 3, y + 4, brighten(border, 20));
            g.fill(x + 3, y + h - 4, x + w - 3, y + h - 3, darken(border, 35));
        }
    }

    public static void slot(GuiGraphics g, int x, int y) {
        g.fill(x - 1, y - 1, x + 19, y + 19, 0xFF4A285F);
        g.fill(x, y, x + 18, y + 18, 0xFF0D0713);
        g.fill(x + 1, y + 1, x + 17, y + 2, 0xFF6C3C89);
        g.fill(x + 1, y + 16, x + 17, y + 17, 0xFF24132F);
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
