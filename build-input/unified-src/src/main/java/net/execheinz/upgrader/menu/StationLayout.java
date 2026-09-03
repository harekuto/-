package net.execheinz.upgrader.menu;

/**
 * Shared pixel-snapped coordinates for the compact Musor Drop station.
 *
 * 320x240 deliberately leaves breathing room on 16:9 and 4:3 screens at high GUI scales
 * while still fitting the complete 9x4 player inventory and the server-owned transaction slot.
 */
public final class StationLayout {
    public static final int GUI_W = 320;
    public static final int GUI_H = 240;

    /** Transaction slot shared by Upgrade and Cases/Sell. */
    public static final int INPUT_X = 29;
    public static final int INPUT_Y = 181;

    /** Player inventory is aligned to the vanilla 18px grid on the right. */
    public static final int INVENTORY_X = 146;
    public static final int INVENTORY_Y = 164;
    public static final int HOTBAR_Y = 219;

    private StationLayout() {}
}
