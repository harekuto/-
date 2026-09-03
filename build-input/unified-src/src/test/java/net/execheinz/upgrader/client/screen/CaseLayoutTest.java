package net.execheinz.upgrader.client.screen;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

final class CaseLayoutTest {
    @Test
    void pageCardsFitAndDoNotOverlap() {
        assertEquals(6, CaseLayout.PAGE_SIZE);
        for (int i = 0; i < CaseLayout.PAGE_SIZE; i++) {
            CaseLayout.Rect a = CaseLayout.card(i);
            assertTrue(a.x() >= 12 && a.y() >= 57);
            assertTrue(a.x() + a.w() <= CaseLayout.GUI_W - 12, "Cards need a 12px right breathing margin");
            assertTrue(a.y() + a.h() < CaseLayout.prevButton().y(), "Cards must stay above navigation row");
            for (int j = i + 1; j < CaseLayout.PAGE_SIZE; j++) {
                assertFalse(a.overlaps(CaseLayout.card(j)), "Cards overlap: " + i + " and " + j);
            }
        }
    }

    @Test
    void navigationAndPrimaryActionStayInsideGui() {
        CaseLayout.Rect[] rects = {
            CaseLayout.prevButton(), CaseLayout.categoryButton(), CaseLayout.nextButton(),
            CaseLayout.selectedPanel(), CaseLayout.openButton()
        };
        for (CaseLayout.Rect r : rects) {
            assertTrue(r.x() >= 0 && r.y() >= 0);
            assertTrue(r.x() + r.w() <= CaseLayout.GUI_W - 12);
            assertTrue(r.y() + r.h() <= 162, "Case controls must remain above inventory separator");
        }
        assertFalse(CaseLayout.prevButton().overlaps(CaseLayout.categoryButton()));
        assertFalse(CaseLayout.categoryButton().overlaps(CaseLayout.nextButton()));
        assertFalse(CaseLayout.nextButton().overlaps(CaseLayout.selectedPanel()));
        assertFalse(CaseLayout.selectedPanel().overlaps(CaseLayout.openButton()));
        assertTrue(CaseLayout.openButton().w() >= 130, "Primary open action must remain visually dominant");
    }

    @Test
    void exactPremiumGridGeometryIsStable() {
        assertEquals(336, CaseLayout.GUI_W);
        assertEquals(240, CaseLayout.GUI_H);
        assertEquals(12, CaseLayout.card(0).x());
        assertEquals(57, CaseLayout.card(0).y());
        assertEquals(224, CaseLayout.card(2).x());
        assertEquals(101, CaseLayout.card(3).y());
        assertEquals(324, CaseLayout.card(5).x() + CaseLayout.card(5).w());
        assertEquals(140, CaseLayout.card(5).y() + CaseLayout.card(5).h());
    }

    @Test
    void gridUsesReadablePixelSpacing() {
        CaseLayout.Rect a = CaseLayout.card(0);
        CaseLayout.Rect b = CaseLayout.card(1);
        CaseLayout.Rect d = CaseLayout.card(3);
        assertEquals(6, b.x() - (a.x() + a.w()));
        assertEquals(5, d.y() - (a.y() + a.h()));
        assertTrue(a.w() >= 100, "Localized names need a 100px card");
        assertTrue(a.h() >= 39, "Cards need name/rarity/cost/category hierarchy");
    }

    @Test
    void navigationHasConsistentFourPixelGaps() {
        assertEquals(4, CaseLayout.categoryButton().x() - (CaseLayout.prevButton().x() + CaseLayout.prevButton().w()));
        assertEquals(4, CaseLayout.nextButton().x() - (CaseLayout.categoryButton().x() + CaseLayout.categoryButton().w()));
        assertEquals(4, CaseLayout.selectedPanel().x() - (CaseLayout.nextButton().x() + CaseLayout.nextButton().w()));
        assertEquals(4, CaseLayout.openButton().x() - (CaseLayout.selectedPanel().x() + CaseLayout.selectedPanel().w()));
    }
}
