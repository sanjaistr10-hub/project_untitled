package com.projectuntitled.web;

import com.projectuntitled.community.Community;
import jakarta.servlet.http.HttpServletRequest;

/** Request-scoped holder for the community resolved from /api/v1/c/{slug}/**. */
public final class TenantContext {

    static final String ATTRIBUTE = TenantContext.class.getName() + ".community";

    private TenantContext() {
    }

    static void set(HttpServletRequest request, Community community) {
        request.setAttribute(ATTRIBUTE, community);
    }

    public static Community require(HttpServletRequest request) {
        Object community = request.getAttribute(ATTRIBUTE);
        if (community == null) {
            throw new IllegalStateException("No community for this request. @CurrentCommunity and @CurrentMember "
                    + "only work on endpoints under /api/v1/c/{slug}/.");
        }
        return (Community) community;
    }
}
