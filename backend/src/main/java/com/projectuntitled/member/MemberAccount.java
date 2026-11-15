package com.projectuntitled.member;

import com.projectuntitled.common.AccountStatus;
import com.projectuntitled.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/**
 * A member of exactly one community. Email and username are unique per community, not globally:
 * a person who joins two creators' communities has two independent accounts (white label).
 */
@Entity
@Table(name = "member_account")
public class MemberAccount extends BaseEntity {

    @Column(name = "community_id", nullable = false)
    private UUID communityId;

    @Column(nullable = false, length = 320)
    private String email;

    @Column(name = "full_name", length = 120)
    private String fullName;

    @Column(length = 40)
    private String username;

    @Column(name = "avatar_url", length = 1024)
    private String avatarUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AccountStatus status = AccountStatus.ACTIVE;

    @Column(name = "last_login_at")
    private Instant lastLoginAt;

    protected MemberAccount() {
    }

    public MemberAccount(UUID communityId, String email) {
        this.communityId = communityId;
        this.email = email;
    }

    public boolean isProfileComplete() {
        return fullName != null && username != null;
    }

    public void updateProfile(String fullName, String username) {
        this.fullName = fullName;
        this.username = username;
    }

    public void recordLogin(Instant at) {
        this.lastLoginAt = at;
    }

    public UUID getCommunityId() {
        return communityId;
    }

    public String getEmail() {
        return email;
    }

    public String getFullName() {
        return fullName;
    }

    public String getUsername() {
        return username;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public Instant getLastLoginAt() {
        return lastLoginAt;
    }
}
