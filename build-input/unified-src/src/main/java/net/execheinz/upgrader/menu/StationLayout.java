package net.execheinz.upgrader.menu;

/** Shared logical coordinates for the single Musor Drop container screen. */
public final class StationLayout {
    /**
     * Intentionally compact enough to remain fully visible at Minecraft GUI scale 4
     * on a 1536x1024 display. The old 384x284 canvas exceeded the logical viewport
     * and caused the lower inventory/hotbar to be clipped.
     */
    public static final int GUI_W = 348;
    public static final int GUI_H = 248;

    public static final int INPUT_X = 28;
    public static final int INPUT_Y = 185;

    public static final int INVENTORY_X = 174;
    public static final int INVENTORY_Y = 174;
    public static final int HOTBAR_Y = 229;

    private StationLayout() {}
}
