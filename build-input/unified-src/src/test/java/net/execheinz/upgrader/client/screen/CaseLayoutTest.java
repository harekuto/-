package net.execheinz.upgrader.client.screen;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

final class CaseLayoutTest {
    @Test
    void eightCardsFitAndDoNotOverlap() {
        for (int i = 0; i < 8; i++) {
            CaseLayout.Rect a = CaseLayout.card(i);
            assertTrue(a.x() >= 0 && a.y() >= 0);
            assertTrue(a.x() + a.w() <= CaseLayout.GUI_W);
            assertTrue(a.y() + a.h() <= 161, "Card must stay above navigation row");
            for (int j = i + 1; j < 8; j++) {
                assertFalse(a.overlaps(CaseLayout.card(j)), "Cards overlap: " + i + " and " + j);
            }
        }
    }

    @Test
    void navigationAndSelectedPanelStayInsideGui() {
        CaseLayout.Rect[] rects = {
            CaseLayout.prevButton(),
            CaseLayout.categoryButton(),
            CaseLayout.nextButton(),
            CaseLayout.selectedPanel(),
            CaseLayout.openButton()
        };
        for (CaseLayout.Rect r : rects) {
            assertTrue(r.x() >= 0 && r.y() >= 0);
            assertTrue(r.x() + r.w() <= CaseLayout.GUI_W);
            assertTrue(r.y() + r.h() <= CaseLayout.GUI_H);
        }
        assertFalse(CaseLayout.prevButton().overlaps(CaseLayout.categoryButton()));
        assertFalse(CaseLayout.categoryButton().overlaps(CaseLayout.nextButton()));
        assertFalse(CaseLayout.selectedPanel().overlaps(CaseLayout.openButton()));
    }

    @Test
    void exactGridGeometryIsStable() {
        assertEquals(384, CaseLayout.GUI_W);
        assertEquals(284, CaseLayout.GUI_H);
        assertEquals(14, CaseLayout.card(0).x());
        assertEquals(58, CaseLayout.card(0).y());
        assertEquals(284, CaseLayout.card(3).x());
        assertEquals(112, CaseLayout.card(4).y());
        assertEquals(368, CaseLayout.card(7).x() + CaseLayout.card(7).w());
        assertEquals(160, CaseLayout.card(7).y() + CaseLayout.card(7).h());
    }
}
