package net.execheinz.upgrader.menu;

/**
 * Shared logical coordinates for the single Musor Drop station screen.
 *
 * The 352x256 canvas is deliberately capped so GUI scale 4 still fits a
 * 1440x~1030 desktop while giving the redesigned views enough breathing room.
 */
public final class StationLayout {
    public static final int GUI_W = 352;
    public static final int GUI_H = 256;

    /** Transaction slot shared by Upgrade and Cases/Sell. */
    public static final int INPUT_X = 31;
    public static final int INPUT_Y = 194;

    /** Player inventory starts after the left utility dock. */
    public static final int INVENTORY_X = 178;
    public static final int INVENTORY_Y = 182;
    public static final int HOTBAR_Y = 237;

    private StationLayout() {}
}
