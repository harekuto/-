package net.execheinz.upgrader.menu;

/**
 * Shared pixel-snapped coordinates for the Musor Drop premium station.
 *
 * The 336x240 surface intentionally balances readability and high GUI-scale compatibility:
 * it still fits inside a common 1440x1024 viewport at GUI scale 4, while giving localized
 * labels and the 9x4 vanilla inventory enough horizontal breathing room.
 */
public final class StationLayout {
    public static final int GUI_W = 336;
    public static final int GUI_H = 240;

    /** Server-owned transaction slot shared by Upgrade and Cases/Sell. */
    public static final int INPUT_X = 28;
    public static final int INPUT_Y = 184;

    /** Player inventory remains on the vanilla 18px grid. */
    public static final int INVENTORY_X = 166;
    public static final int INVENTORY_Y = 166;
    public static final int HOTBAR_Y = 221;

    private StationLayout() {}
}
