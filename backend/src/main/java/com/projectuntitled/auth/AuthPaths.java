package com.projectuntitled.auth;

import java.util.regex.Pattern;

/**
 * URL layout of the API. Every endpoint must live under one of these roots; anything else is denied
 * by SecurityConfig.
 *
 * <pre>
 * /api/v1/public/**            no auth (community branding, host resolution)
 * /api/v1/creator/auth/**      creator sign-in (no auth)
 * /api/v1/creator/**           ROLE_CREATOR
 * /api/v1/c/{slug}/auth/**     member sign-in for one community (no auth)
 * /api/v1/c/{slug}/**          ROLE_MEMBER of that community
 * </pre>
 */
public final class AuthPaths {

    public static final String API = "/api/v1";
    public static final String PUBLIC = API + "/public";
    public static final String CREATOR = API + "/creator";
    public static final String CREATOR_AUTH = CREATOR + "/auth";
    public static final String MEMBER = API + "/c";

    private static final Pattern MEMBER_AUTH = Pattern.compile("^" + MEMBER + "/[^/]+/auth(/.*)?$");

    private AuthPaths() {
    }

    public static String memberBase(String slug) {
        return MEMBER + "/" + slug;
    }

    public static String memberAuth(String slug) {
        return memberBase(slug) + "/auth";
    }

    /** True for endpoints that act on a signed-in account, i.e. the ones that read an access token. */
    public static boolean isProtectedApi(String path) {
        if (path.startsWith(CREATOR + "/")) {
            return !path.startsWith(CREATOR_AUTH + "/");
        }
        if (path.startsWith(MEMBER + "/")) {
            return !MEMBER_AUTH.matcher(path).matches();
        }
        return false;
    }

    public static boolean isCreatorPath(String path) {
        return path.startsWith(CREATOR + "/");
    }
}
