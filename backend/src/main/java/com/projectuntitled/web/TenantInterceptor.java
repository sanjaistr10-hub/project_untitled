package com.projectuntitled.web;

import com.projectuntitled.auth.Audience;
import com.projectuntitled.auth.AuthPrincipal;
import com.projectuntitled.common.error.ApiException;
import com.projectuntitled.community.Community;
import com.projectuntitled.community.CommunityService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

/**
 * Runs for every /api/v1/c/{slug}/** request. It resolves the community (404 if missing), and, for
 * signed-in members, rejects tokens issued for a different community. This central check means a
 * new member endpoint can't forget tenant isolation.
 */
@Component
public class TenantInterceptor implements HandlerInterceptor {

    private final CommunityService communityService;

    public TenantInterceptor(CommunityService communityService) {
        this.communityService = communityService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        @SuppressWarnings("unchecked")
        Map<String, String> pathVariables =
                (Map<String, String>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        String slug = pathVariables == null ? null : pathVariables.get("slug");
        if (slug == null) {
            return true;
        }
        Community community = communityService.requireActiveBySlug(slug);
        AuthPrincipal.current().ifPresent(principal -> {
            if (principal.audience() == Audience.MEMBER && !community.getId().equals(principal.communityId())) {
                throw ApiException.forbidden("WRONG_COMMUNITY", "You're signed in to a different community.");
            }
        });
        TenantContext.set(request, community);
        return true;
    }
}
