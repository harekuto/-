package net.execheinz.upgrader.client.screen;

import net.execheinz.upgrader.menu.StationLayout;

/** Pixel-snapped geometry for the compact 3x2 case browser. */
public final class CaseLayout {
    public static final int GUI_W = StationLayout.GUI_W;
    public static final int GUI_H = StationLayout.GUI_H;

    public static final int GRID_X = 12;
    public static final int GRID_Y = 58;
    public static final int CARD_W = 95;
    public static final int CARD_H = 40;
    public static final int GAP_X = 5;
    public static final int GAP_Y = 5;
    public static final int PAGE_SIZE = 6;

    public static Rect card(int index) {
        int col = index % 3;
        int row = index / 3;
        return new Rect(GRID_X + col * (CARD_W + GAP_X), GRID_Y + row * (CARD_H + GAP_Y), CARD_W, CARD_H);
    }

    public static Rect prevButton() { return new Rect(12, 145, 24, 15); }
    public static Rect categoryButton() { return new Rect(40, 145, 88, 15); }
    public static Rect nextButton() { return new Rect(132, 145, 24, 15); }
    public static Rect selectedPanel() { return new Rect(160, 145, 60, 15); }
    public static Rect openButton() { return new Rect(224, 145, 84, 15); }

    public record Rect(int x, int y, int w, int h) {
        public boolean contains(double px, double py) {
            return px >= x && px < x + w && py >= y && py < y + h;
        }

        public boolean overlaps(Rect other) {
            return x < other.x + other.w && x + w > other.x && y < other.y + other.h && y + h > other.y;
        }
    }

    private CaseLayout() {}
}
