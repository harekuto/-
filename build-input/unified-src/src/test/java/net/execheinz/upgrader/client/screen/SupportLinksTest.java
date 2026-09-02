package net.execheinz.upgrader.client.screen;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

final class SupportLinksTest {
    @Test
    void bundledLinksAreTrustedHttpsDestinations() {
        assertTrue(SupportLinks.isTrusted(SupportLinks.BOOSTY));
        assertTrue(SupportLinks.isTrusted(SupportLinks.DONATION_ALERTS));
        assertTrue(SupportLinks.isTrusted(SupportLinks.DISCORD));
    }

    @Test
    void rejectsInsecureSchemesAndUnexpectedHosts() {
        assertFalse(SupportLinks.isTrusted("http://boosty.to/harekuto/donate"));
        assertFalse(SupportLinks.isTrusted("https://example.com/boosty.to/harekuto"));
        assertFalse(SupportLinks.isTrusted("file:///tmp/test"));
        assertFalse(SupportLinks.isTrusted("javascript:alert(1)"));
    }

    @Test
    void rejectsUserInfoAndMalformedInput() {
        assertFalse(SupportLinks.isTrusted("https://user:pass@boosty.to/harekuto/donate"));
        assertFalse(SupportLinks.isTrusted("not a url"));
        assertFalse(SupportLinks.isTrusted(""));
        assertFalse(SupportLinks.isTrusted(null));
    }

    @Test
    void exactCommunityDestinationsStayStable() {
        assertEquals("https://boosty.to/harekuto/donate", SupportLinks.BOOSTY);
        assertEquals("https://www.donationalerts.com/r/harekuto", SupportLinks.DONATION_ALERTS);
        assertEquals("https://discord.gg/micro", SupportLinks.DISCORD);
    }
}
