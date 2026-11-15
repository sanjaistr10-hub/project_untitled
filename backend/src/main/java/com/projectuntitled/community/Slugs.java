package com.projectuntitled.community;

import java.text.Normalizer;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

/** A slug is the community's subdomain ({slug}.platform.com), so it must be a valid DNS label. */
public final class Slugs {

    public static final int MIN_LENGTH = 3;
    public static final int MAX_LENGTH = 40;

    private static final Pattern VALID = Pattern.compile("^[a-z0-9](?:[a-z0-9-]*[a-z0-9])?$");

    /** Subdomains the platform keeps for itself. */
    private static final Set<String> RESERVED = Set.of(
            "app", "www", "api", "admin", "dashboard", "studio", "static", "assets", "cdn", "media", "img",
            "mail", "email", "smtp", "help", "support", "status", "blog", "docs", "auth", "login", "signup",
            "logout", "creator", "creators", "community", "communities", "site", "sites", "dev", "staging",
            "test", "demo", "billing", "pay", "payments", "checkout", "settings", "account", "accounts",
            "about", "terms", "privacy", "security", "legal", "root", "system", "official", "internal");

    private Slugs() {
    }

    public static String normalize(String slug) {
        return slug == null ? null : slug.trim().toLowerCase(Locale.ROOT);
    }

    /** "Arjun's IFVG Room!" becomes "arjuns-ifvg-room". Always returns something valid-looking. */
    public static String fromName(String name) {
        String slug = Normalizer.normalize(name == null ? "" : name, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replace("'", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-+)|(-+$)", "");
        if (slug.length() > MAX_LENGTH) {
            slug = trimTo(slug, MAX_LENGTH);
        }
        if (slug.isEmpty()) {
            return "community";
        }
        return slug.length() < MIN_LENGTH ? slug + "-community" : slug;
    }

    /** Why a slug can't be used, or empty if it's fine (availability is checked separately). */
    public static Optional<String> problem(String slug) {
        if (slug == null || slug.length() < MIN_LENGTH) {
            return Optional.of("Use at least " + MIN_LENGTH + " characters.");
        }
        if (slug.length() > MAX_LENGTH) {
            return Optional.of("Use at most " + MAX_LENGTH + " characters.");
        }
        if (!VALID.matcher(slug).matches()) {
            return Optional.of("Use lowercase letters, numbers and hyphens (not at the start or end).");
        }
        if (RESERVED.contains(slug)) {
            return Optional.of("That address is reserved.");
        }
        return Optional.empty();
    }

    static String trimTo(String slug, int maxLength) {
        String cut = slug.length() <= maxLength ? slug : slug.substring(0, maxLength);
        return cut.replaceAll("-+$", "");
    }
}
