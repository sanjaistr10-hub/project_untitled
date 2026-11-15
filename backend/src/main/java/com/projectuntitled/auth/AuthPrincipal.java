package com.projectuntitled.auth;

import java.util.Optional;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

/** The signed-in account as read from a verified access token. */
public record AuthPrincipal(UUID accountId, Audience audience, UUID communityId) {

    public static final String CLAIM_ROLE = "role";
    public static final String CLAIM_COMMUNITY = "cid";

    public static AuthPrincipal from(Jwt jwt) {
        String communityId = jwt.getClaimAsString(CLAIM_COMMUNITY);
        return new AuthPrincipal(
                UUID.fromString(jwt.getSubject()),
                Audience.valueOf(jwt.getClaimAsString(CLAIM_ROLE)),
                communityId == null ? null : UUID.fromString(communityId));
    }

    public static Optional<AuthPrincipal> current() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken token) {
            return Optional.of(from(token.getToken()));
        }
        return Optional.empty();
    }
}
