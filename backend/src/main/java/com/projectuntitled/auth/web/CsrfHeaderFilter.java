package com.projectuntitled.auth.web;

import com.projectuntitled.common.error.ApiException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

/**
 * CSRF protection for cookie-authenticated writes using the "custom request header" pattern: every
 * POST/PUT/PATCH/DELETE to /api must carry {@code X-Requested-With}. HTML forms can't set headers,
 * and cross-origin fetch() with a custom header needs a CORS preflight the API never approves.
 * Requests with an Authorization header are exempt because browsers never attach those on their own.
 */
public class CsrfHeaderFilter extends OncePerRequestFilter {

    public static final String HEADER = "X-Requested-With";
    private static final Set<String> UNSAFE_METHODS = Set.of("POST", "PUT", "PATCH", "DELETE");

    private final HandlerExceptionResolver exceptionResolver;

    public CsrfHeaderFilter(HandlerExceptionResolver exceptionResolver) {
        this.exceptionResolver = exceptionResolver;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        boolean needsHeader = UNSAFE_METHODS.contains(request.getMethod())
                && request.getRequestURI().startsWith(request.getContextPath() + "/api/")
                && request.getHeader(HttpHeaders.AUTHORIZATION) == null;
        if (needsHeader && !StringUtils.hasText(request.getHeader(HEADER))) {
            exceptionResolver.resolveException(request, response, null,
                    ApiException.forbidden("CSRF_HEADER_MISSING", "Missing " + HEADER + " header."));
            return;
        }
        chain.doFilter(request, response);
    }
}
