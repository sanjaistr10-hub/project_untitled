package com.projectuntitled.community;

import com.projectuntitled.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/**
 * A custom domain (e.g. learn.creator.com) mapped to a community. The table and the public resolve
 * endpoint exist now so the frontend routing is ready; the "add your domain" + DNS verification UI
 * is on the roadmap.
 */
@Entity
@Table(name = "community_domain")
public class CommunityDomain extends BaseEntity {

    @Column(name = "community_id", nullable = false)
    private UUID communityId;

    @Column(nullable = false, length = 253)
    private String host;

    @Column(name = "verified_at")
    private Instant verifiedAt;

    protected CommunityDomain() {
    }

    public CommunityDomain(UUID communityId, String host) {
        this.communityId = communityId;
        this.host = host;
    }

    public void markVerified(Instant at) {
        this.verifiedAt = at;
    }

    public UUID getCommunityId() {
        return communityId;
    }

    public String getHost() {
        return host;
    }

    public Instant getVerifiedAt() {
        return verifiedAt;
    }
}
