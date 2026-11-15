package com.projectuntitled.auth.web;

import com.projectuntitled.auth.AuthPaths;
import com.projectuntitled.auth.token.TokenPair;
import com.projectuntitled.config.AppProperties;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

/**
 * Tokens travel only in httpOnly cookies, never in JS-readable storage. Cookies are host-only, so
 * each white-label domain (and the creator studio) keeps its own separate session automatically.
 * Paths are narrowed so a refresh token is only ever sent to its own /auth endpoints.
 */
@Component
public class AuthCookies {

    public static final String CREATOR_ACCESS = "creator_access_token";
    public static final String CREATOR_REFRESH = "creator_refresh_token";
    public static final String MEMBER_ACCESS = "member_access_token";
    public static final String MEMBER_REFRESH = "member_refresh_token";

    private final boolean secure;

    public AuthCookies(AppProperties properties) {
        this.secure = properties.security().cookieSecure();
    }

    public void setCreatorTokens(HttpServletResponse response, TokenPair tokens) {
        add(response, CREATOR_ACCESS, tokens.accessToken(), AuthPaths.CREATOR, tokens.accessTtl());
        add(response, CREATOR_REFRESH, tokens.refreshToken(), AuthPaths.CREATOR_AUTH, tokens.refreshTtl());
    }

    public void clearCreatorTokens(HttpServletResponse response) {
        add(response, CREATOR_ACCESS, "", AuthPaths.CREATOR, Duration.ZERO);
        add(response, CREATOR_REFRESH, "", AuthPaths.CREATOR_AUTH, Duration.ZERO);
    }

    public void setMemberTokens(HttpServletResponse response, String slug, TokenPair tokens) {
        add(response, MEMBER_ACCESS, tokens.accessToken(), AuthPaths.memberBase(slug), tokens.accessTtl());
        add(response, MEMBER_REFRESH, tokens.refreshToken(), AuthPaths.memberAuth(slug), tokens.refreshTtl());
    }

    public void clearMemberTokens(HttpServletResponse response, String slug) {
        add(response, MEMBER_ACCESS, "", AuthPaths.memberBase(slug), Duration.ZERO);
        add(response, MEMBER_REFRESH, "", AuthPaths.memberAuth(slug), Duration.ZERO);
    }

    private void add(HttpServletResponse response, String name, String value, String path, Duration maxAge) {
        ResponseCookie cookie = ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(secure)
                .sameSite("Lax")
                .path(path)
                .maxAge(maxAge)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
