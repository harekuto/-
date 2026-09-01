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
}
