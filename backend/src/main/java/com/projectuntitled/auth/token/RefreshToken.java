package com.projectuntitled.auth.token;

import com.projectuntitled.auth.Audience;
import com.projectuntitled.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/** Long-lived, single-use session token. Only its SHA-256 hash is stored. */
@Entity
@Table(name = "refresh_token")
public class RefreshToken extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Audience audience;

    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    @Column(name = "community_id")
    private UUID communityId;

    @Column(name = "token_hash", nullable = false, length = 64)
    private String tokenHash;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    /** The token that replaced this one on rotation; lets us tell a benign race from token theft. */
    @Column(name = "replaced_by_id")
    private UUID replacedById;

    @Column(name = "user_agent", length = 255)
    private String userAgent;

    protected RefreshToken() {
    }

    public RefreshToken(Audience audience, UUID accountId, UUID communityId, String tokenHash,
            Instant expiresAt, String userAgent) {
        this.audience = audience;
        this.accountId = accountId;
        this.communityId = communityId;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
        this.userAgent = userAgent;
    }

    public void revoke(Instant at, UUID replacedBy) {
        this.revokedAt = at;
        this.replacedById = replacedBy;
    }

    public Audience getAudience() {
        return audience;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public UUID getCommunityId() {
        return communityId;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public Instant getRevokedAt() {
        return revokedAt;
    }

    public UUID getReplacedById() {
        return replacedById;
    }
}
