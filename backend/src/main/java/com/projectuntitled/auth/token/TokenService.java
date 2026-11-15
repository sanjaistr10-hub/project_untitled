package com.projectuntitled.auth.token;

import com.projectuntitled.auth.Audience;
import com.projectuntitled.auth.AuthPrincipal;
import com.projectuntitled.common.error.ApiException;
import com.projectuntitled.config.AppProperties;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Objects;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * Issues short-lived access tokens (JWT) and rotating refresh tokens (opaque, single use).
 * Presenting an already-rotated refresh token outside a short grace window is treated as theft:
 * every session of that account is revoked.
 */
@Service
public class TokenService {

    private static final Logger log = LoggerFactory.getLogger(TokenService.class);
    private static final SecureRandom RANDOM = new SecureRandom();
    /** Two tabs refreshing at the same moment both present the same token; don't punish that. */
    private static final Duration REUSE_GRACE = Duration.ofSeconds(30);

    private final JwtEncoder jwtEncoder;
    private final RefreshTokenRepository refreshTokens;
    private final AppProperties.Security config;
    private final Clock clock;

    public TokenService(JwtEncoder jwtEncoder, RefreshTokenRepository refreshTokens, AppProperties properties, Clock clock) {
        this.jwtEncoder = jwtEncoder;
        this.refreshTokens = refreshTokens;
        this.config = properties.security();
        this.clock = clock;
    }

    public record Rotation(UUID accountId, TokenPair tokens) {
    }

    @Transactional
    public TokenPair issue(Audience audience, UUID accountId, UUID communityId, String userAgent) {
        return issueInternal(audience, accountId, communityId, userAgent).tokens();
    }

    @Transactional(noRollbackFor = ApiException.class)
    public Rotation rotate(String rawRefreshToken, Audience audience, UUID communityId, String userAgent) {
        if (!StringUtils.hasText(rawRefreshToken)) {
            throw invalidRefresh();
        }
        Instant now = clock.instant();
        RefreshToken current = refreshTokens.findByTokenHash(sha256(rawRefreshToken)).orElseThrow(TokenService::invalidRefresh);

        if (current.getAudience() != audience || !Objects.equals(current.getCommunityId(), communityId)) {
            throw invalidRefresh();
        }
        if (current.getRevokedAt() != null) {
            boolean benignRace = current.getReplacedById() != null && current.getRevokedAt().plus(REUSE_GRACE).isAfter(now);
            if (!benignRace) {
                int revoked = refreshTokens.revokeAllActive(audience, current.getAccountId(), now);
                log.warn("Refresh token reuse for {} {}: revoked {} active session(s)", audience, current.getAccountId(), revoked);
            }
            throw invalidRefresh();
        }
        if (!current.getExpiresAt().isAfter(now)) {
            throw invalidRefresh();
        }

        Issued next = issueInternal(audience, current.getAccountId(), communityId, userAgent);
        current.revoke(now, next.refreshTokenId());
        return new Rotation(current.getAccountId(), next.tokens());
    }

    @Transactional
    public void revoke(String rawRefreshToken) {
        if (!StringUtils.hasText(rawRefreshToken)) {
            return;
        }
        refreshTokens.findByTokenHash(sha256(rawRefreshToken))
                .filter(token -> token.getRevokedAt() == null)
                .ifPresent(token -> token.revoke(clock.instant(), null));
    }

    private record Issued(TokenPair tokens, UUID refreshTokenId) {
    }

    private Issued issueInternal(Audience audience, UUID accountId, UUID communityId, String userAgent) {
        Instant now = clock.instant();
        String rawRefresh = newOpaqueToken();
        RefreshToken saved = refreshTokens.save(new RefreshToken(audience, accountId, communityId, sha256(rawRefresh),
                now.plus(config.refreshTokenTtl()), truncate(userAgent, 255)));
        TokenPair pair = new TokenPair(accessToken(audience, accountId, communityId, now), config.accessTokenTtl(),
                rawRefresh, config.refreshTokenTtl());
        return new Issued(pair, saved.getId());
    }

    private String accessToken(Audience audience, UUID accountId, UUID communityId, Instant now) {
        JwtClaimsSet.Builder claims = JwtClaimsSet.builder()
                .issuer(config.jwtIssuer())
                .subject(accountId.toString())
                .issuedAt(now)
                .expiresAt(now.plus(config.accessTokenTtl()))
                .claim(AuthPrincipal.CLAIM_ROLE, audience.name());
        if (communityId != null) {
            claims.claim(AuthPrincipal.CLAIM_COMMUNITY, communityId.toString());
        }
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims.build())).getTokenValue();
    }

    private static ApiException invalidRefresh() {
        return ApiException.unauthorized("REFRESH_INVALID", "Your session has ended. Please sign in again.");
    }

    private static String newOpaqueToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String sha256(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }

    private static String truncate(String value, int max) {
        return value == null || value.length() <= max ? value : value.substring(0, max);
    }
}
