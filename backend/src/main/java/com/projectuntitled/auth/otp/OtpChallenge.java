package com.projectuntitled.auth.otp;

import com.projectuntitled.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;

/** One emailed sign-in code. Only an HMAC of the code is stored. */
@Entity
@Table(name = "otp_challenge")
public class OtpChallenge extends BaseEntity {

    /** "creator" or "member:{communityId}"; see {@link OtpScope#key()}. */
    @Column(nullable = false, length = 64)
    private String scope;

    @Column(nullable = false, length = 320)
    private String email;

    @Column(name = "code_hash", nullable = false, length = 64)
    private String codeHash;

    @Column(nullable = false)
    private int attempts;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    /** Set when the code is used, replaced by a newer code, or locked after too many attempts. */
    @Column(name = "consumed_at")
    private Instant consumedAt;

    protected OtpChallenge() {
    }

    public OtpChallenge(String scope, String email, String codeHash, Instant expiresAt) {
        this.scope = scope;
        this.email = email;
        this.codeHash = codeHash;
        this.expiresAt = expiresAt;
    }

    public void recordFailedAttempt() {
        attempts++;
    }

    public void consume(Instant at) {
        consumedAt = at;
    }

    public String getScope() {
        return scope;
    }

    public String getEmail() {
        return email;
    }

    public String getCodeHash() {
        return codeHash;
    }

    public int getAttempts() {
        return attempts;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public Instant getConsumedAt() {
        return consumedAt;
    }
}
