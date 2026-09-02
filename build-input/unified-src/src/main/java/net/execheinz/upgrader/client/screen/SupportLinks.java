package net.execheinz.upgrader.client.screen;

import java.net.URI;
import java.util.Set;

/**
 * Curated external community/support links. These links never affect gameplay,
 * case odds, Musor Shards, item values, or any server-authoritative transaction.
 */
public final class SupportLinks {
    public static final String BOOSTY = "https://boosty.to/harekuto/donate";
    public static final String DONATION_ALERTS = "https://www.donationalerts.com/r/harekuto";
    public static final String DISCORD = "https://discord.gg/micro";

    private static final Set<String> TRUSTED_HOSTS = Set.of(
        "boosty.to",
        "www.donationalerts.com",
        "discord.gg"
    );

    public static boolean isTrusted(String value) {
        if (value == null || value.isBlank() || value.length() > 256) return false;
        try {
            URI uri = URI.create(value);
            String host = uri.getHost();
            return "https".equalsIgnoreCase(uri.getScheme())
                && host != null
                && TRUSTED_HOSTS.contains(host.toLowerCase())
                && uri.getUserInfo() == null;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    private SupportLinks() {}
}
