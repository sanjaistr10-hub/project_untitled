package com.projectuntitled.common;

import java.util.Locale;

public final class Emails {

    private Emails() {
    }

    /** Emails are stored trimmed and lower-cased so lookups and unique constraints are case-insensitive. */
    public static String normalize(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }
}
