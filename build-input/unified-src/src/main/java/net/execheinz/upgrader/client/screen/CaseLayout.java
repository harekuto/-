package net.execheinz.upgrader.client.screen;

import net.execheinz.upgrader.menu.StationLayout;

/** Pixel-snapped geometry for the premium 3x2 case browser. */
public final class CaseLayout {
    public static final int GUI_W = StationLayout.GUI_W;
    public static final int GUI_H = StationLayout.GUI_H;

    public static final int GRID_X = 12;
    public static final int GRID_Y = 57;
    public static final int CARD_W = 100;
    public static final int CARD_H = 39;
    public static final int GAP_X = 6;
    public static final int GAP_Y = 5;
    public static final int PAGE_SIZE = 6;

    public static Rect card(int index) {
        int col = index % 3;
        int row = index / 3;
        return new Rect(GRID_X + col * (CARD_W + GAP_X), GRID_Y + row * (CARD_H + GAP_Y), CARD_W, CARD_H);
    }

    public static Rect prevButton() { return new Rect(12, 144, 18, 16); }
    public static Rect categoryButton() { return new Rect(34, 144, 74, 16); }
    public static Rect nextButton() { return new Rect(112, 144, 18, 16); }
    public static Rect selectedPanel() { return new Rect(134, 144, 50, 16); }
    public static Rect openButton() { return new Rect(188, 144, 136, 16); }

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
