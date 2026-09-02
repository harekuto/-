package net.execheinz.upgrader.client.screen;

import net.execheinz.upgrader.menu.StationLayout;

public final class CaseLayout {
    public static final int GUI_W = StationLayout.GUI_W;
    public static final int GUI_H = StationLayout.GUI_H;

    public static final int GRID_X = 12;
    public static final int GRID_Y = 58;
    public static final int CARD_W = 78;
    public static final int CARD_H = 38;
    public static final int GAP_X = 6;
    public static final int GAP_Y = 5;

    public static Rect card(int index) {
        int col = index & 3;
        int row = index >> 2;
        return new Rect(GRID_X + col * (CARD_W + GAP_X), GRID_Y + row * (CARD_H + GAP_Y), CARD_W, CARD_H);
    }

    public static Rect prevButton() { return new Rect(12, 143, 28, 18); }
    public static Rect categoryButton() { return new Rect(44, 143, 70, 18); }
    public static Rect nextButton() { return new Rect(118, 143, 28, 18); }
    public static Rect selectedPanel() { return new Rect(150, 143, 104, 18); }
    public static Rect openButton() { return new Rect(258, 143, 78, 18); }

    public record Rect(int x, int y, int w, int h) {
        public boolean contains(double px, double py) {
            return px >= x && px < x + w && py >= y && py < y + h;
        }
        public boolean overlaps(Rect o) {
            return x < o.x + o.w && x + w > o.x && y < o.y + o.h && y + h > o.y;
        }
    }

    private CaseLayout() {}
}
