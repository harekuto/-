package net.execheinz.upgrader.client.screen;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

final class CaseLayoutTest {
    @Test
    void pageCardsFitAndDoNotOverlap() {
        assertEquals(6, CaseLayout.PAGE_SIZE);
        for (int i = 0; i < CaseLayout.PAGE_SIZE; i++) {
            CaseLayout.Rect a = CaseLayout.card(i);
            assertTrue(a.x() >= 0 && a.y() >= 0);
            assertTrue(a.x() + a.w() <= CaseLayout.GUI_W - 10, "Cards need a right-side breathing margin");
            assertTrue(a.y() + a.h() < CaseLayout.prevButton().y(), "Cards must stay above navigation row");
            for (int j = i + 1; j < CaseLayout.PAGE_SIZE; j++) {
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
        assertFalse(CaseLayout.nextButton().overlaps(CaseLayout.selectedPanel()));
        assertFalse(CaseLayout.selectedPanel().overlaps(CaseLayout.openButton()));
    }

    @Test
    void exactCompactGridGeometryIsStable() {
        assertEquals(320, CaseLayout.GUI_W);
        assertEquals(240, CaseLayout.GUI_H);
        assertEquals(12, CaseLayout.card(0).x());
        assertEquals(58, CaseLayout.card(0).y());
        assertEquals(212, CaseLayout.card(2).x());
        assertEquals(103, CaseLayout.card(3).y());
        assertEquals(307, CaseLayout.card(5).x() + CaseLayout.card(5).w());
        assertEquals(143, CaseLayout.card(5).y() + CaseLayout.card(5).h());
    }

    @Test
    void gridUsesProfessionalReadableSpacing() {
        CaseLayout.Rect a = CaseLayout.card(0);
        CaseLayout.Rect b = CaseLayout.card(1);
        CaseLayout.Rect d = CaseLayout.card(3);
        assertEquals(5, b.x() - (a.x() + a.w()), "Horizontal case-card gap must remain pixel stable");
        assertEquals(5, d.y() - (a.y() + a.h()), "Vertical case-card gap must remain pixel stable");
        assertTrue(a.w() >= 90, "Case cards must remain wide enough for localized names");
        assertTrue(a.h() >= 38, "Case cards must remain tall enough for name/rarity/cost hierarchy");
    }

    @Test
    void navigationHasComfortablePixelGaps() {
        assertTrue(CaseLayout.categoryButton().x() - (CaseLayout.prevButton().x() + CaseLayout.prevButton().w()) >= 4);
        assertTrue(CaseLayout.nextButton().x() - (CaseLayout.categoryButton().x() + CaseLayout.categoryButton().w()) >= 4);
        assertTrue(CaseLayout.selectedPanel().x() - (CaseLayout.nextButton().x() + CaseLayout.nextButton().w()) >= 4);
        assertTrue(CaseLayout.openButton().x() - (CaseLayout.selectedPanel().x() + CaseLayout.selectedPanel().w()) >= 4);
    }
}
