package net.execheinz.upgrader.client.screen;

import static org.junit.jupiter.api.Assertions.*;
import net.execheinz.upgrader.menu.StationLayout;
import org.junit.jupiter.api.Test;

final class StationLayoutTest {
    @Test
    void inventoryAndHotbarFitInsideWindowWithSafeMargins() {
        int invRight = StationLayout.INVENTORY_X + 9 * 18;
        int thirdRowBottom = StationLayout.INVENTORY_Y + 3 * 18;
        int hotbarBottom = StationLayout.HOTBAR_Y + 18;
        int customBevelBottom = StationLayout.HOTBAR_Y + 19;
        assertTrue(invRight <= StationLayout.GUI_W - 8, "Inventory needs an 8px right breathing margin");
        assertTrue(thirdRowBottom <= StationLayout.HOTBAR_Y);
        assertTrue(hotbarBottom < StationLayout.GUI_H);
        assertTrue(customBevelBottom <= StationLayout.GUI_H);
    }

    @Test
    void inputDockDoesNotOverlapPlayerInventory() {
        assertTrue(StationLayout.INPUT_X + 19 < StationLayout.INVENTORY_X - 8);
        assertTrue(StationLayout.INPUT_Y >= StationLayout.INVENTORY_Y);
        assertTrue(StationLayout.INPUT_Y + 19 <= StationLayout.GUI_H);
    }

    @Test
    void premiumWidthStillFitsScaleFour1440Viewport() {
        assertEquals(336, StationLayout.GUI_W);
        assertEquals(240, StationLayout.GUI_H);
        assertTrue(StationLayout.GUI_W * 4 <= 1440);
        assertTrue(StationLayout.GUI_H * 4 <= 1024);
    }

    @Test
    void inventoryRowsMeetHotbarWithoutOverlap() {
        int lastInventorySlotY = StationLayout.INVENTORY_Y + 2 * 18;
        int lastInventoryVisualBottom = lastInventorySlotY + 19;
        int hotbarVisualTop = StationLayout.HOTBAR_Y - 1;
        assertTrue(lastInventoryVisualBottom <= hotbarVisualTop + 1,
            "Slot bevels must meet cleanly without a multi-pixel overlap");
    }
}
