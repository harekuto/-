package net.execheinz.upgrader.client.screen;

import static org.junit.jupiter.api.Assertions.*;
import net.execheinz.upgrader.menu.StationLayout;
import org.junit.jupiter.api.Test;

final class StationLayoutTest {
    @Test
    void inventoryAndHotbarFitExactlyInsideWindow() {
        int invRight = StationLayout.INVENTORY_X + 9 * 18;
        int thirdRowBottom = StationLayout.INVENTORY_Y + 3 * 18;
        int hotbarBottom = StationLayout.HOTBAR_Y + 18;
        int customBevelBottom = StationLayout.HOTBAR_Y + 19;
        assertTrue(invRight <= StationLayout.GUI_W);
        assertTrue(thirdRowBottom <= StationLayout.HOTBAR_Y);
        assertTrue(hotbarBottom < StationLayout.GUI_H);
        assertTrue(customBevelBottom <= StationLayout.GUI_H);
    }

    @Test
    void inputDockDoesNotOverlapPlayerInventory() {
        assertTrue(StationLayout.INPUT_X + 18 < StationLayout.INVENTORY_X);
        assertTrue(StationLayout.INPUT_Y >= StationLayout.INVENTORY_Y);
        assertTrue(StationLayout.INPUT_Y + 19 <= StationLayout.GUI_H);
    }

    @Test
    void scaleFourFitsCommon1440By1024Viewport() {
        assertTrue(StationLayout.GUI_W * 4 <= 1440);
        assertTrue(StationLayout.GUI_H * 4 <= 1024);
    }

    @Test
    void inventoryRowsHaveAStableGapBeforeHotbar() {
        int lastInventorySlotY = StationLayout.INVENTORY_Y + 2 * 18;
        int lastInventoryVisualBottom = lastInventorySlotY + 19;
        int hotbarVisualTop = StationLayout.HOTBAR_Y - 1;
        assertTrue(lastInventoryVisualBottom <= hotbarVisualTop + 1,
            "Slot bevels must meet cleanly without a multi-pixel overlap");
    }
}
