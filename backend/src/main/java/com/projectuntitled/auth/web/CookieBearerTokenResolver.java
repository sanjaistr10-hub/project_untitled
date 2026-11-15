package com.projectuntitled.auth.web;

import com.projectuntitled.auth.AuthPaths;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.stereotype.Component;
import org.springframework.web.util.WebUtils;

/**
 * Finds the access token for protected endpoints: an {@code Authorization: Bearer} header if present
 * (Swagger, curl, future mobile apps), otherwise the creator or member cookie depending on the path.
 * Returns null on public and sign-in endpoints so a stale cookie can never block signing in.
 */
@Component
public class CookieBearerTokenResolver implements BearerTokenResolver {

    private final DefaultBearerTokenResolver headerResolver = new DefaultBearerTokenResolver();

    @Override
    public String resolve(HttpServletRequest request) {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        if (!AuthPaths.isProtectedApi(path)) {
            return null;
        }
        String fromHeader = headerResolver.resolve(request);
        if (fromHeader != null) {
            return fromHeader;
        }
        String cookieName = AuthPaths.isCreatorPath(path) ? AuthCookies.CREATOR_ACCESS : AuthCookies.MEMBER_ACCESS;
        Cookie cookie = WebUtils.getCookie(request, cookieName);
        return cookie == null || cookie.getValue().isBlank() ? null : cookie.getValue();
    }
}
