package com.projectuntitled.common;

import java.util.Locale;

public final class Usernames {

    /** Validated on input (any case); stored lower-cased. */
    public static final String PATTERN = "^[A-Za-z0-9._]{3,30}$";
    public static final String MESSAGE = "Use 3-30 letters, numbers, dots or underscores";

    private Usernames() {
    }

    public static String normalize(String username) {
        return username == null ? null : username.trim().toLowerCase(Locale.ROOT);
    }
}
