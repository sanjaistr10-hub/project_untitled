package com.projectuntitled.web;

import com.projectuntitled.auth.Audience;
import com.projectuntitled.auth.AuthPrincipal;
import com.projectuntitled.common.error.ApiException;
import com.projectuntitled.community.Community;
import com.projectuntitled.creator.CreatorService;
import com.projectuntitled.member.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/** Supplies @CurrentCreator, @CurrentCreatorId, @CurrentMember and @CurrentCommunity parameters. */
@Component
public class CurrentAccountArgumentResolver implements HandlerMethodArgumentResolver {

    private final CreatorService creatorService;
    private final MemberService memberService;

    public CurrentAccountArgumentResolver(CreatorService creatorService, MemberService memberService) {
        this.creatorService = creatorService;
        this.memberService = memberService;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentCreator.class)
                || parameter.hasParameterAnnotation(CurrentCreatorId.class)
                || parameter.hasParameterAnnotation(CurrentMember.class)
                || parameter.hasParameterAnnotation(CurrentCommunity.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        if (parameter.hasParameterAnnotation(CurrentCommunity.class)) {
            return TenantContext.require(request);
        }
        if (parameter.hasParameterAnnotation(CurrentMember.class)) {
            Community community = TenantContext.require(request);
            return memberService.requireInCommunity(principal(Audience.MEMBER).accountId(), community.getId());
        }
        AuthPrincipal creator = principal(Audience.CREATOR);
        return parameter.hasParameterAnnotation(CurrentCreatorId.class)
                ? creator.accountId()
                : creatorService.requireById(creator.accountId());
    }

    private static AuthPrincipal principal(Audience expected) {
        return AuthPrincipal.current()
                .filter(principal -> principal.audience() == expected)
                .orElseThrow(() -> ApiException.unauthorized("UNAUTHENTICATED", "Sign in to continue."));
    }
}
