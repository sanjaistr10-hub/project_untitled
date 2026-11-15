package com.projectuntitled.creator;

import com.projectuntitled.common.AccountStatus;
import com.projectuntitled.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.Instant;

/** A creator: signs in to the platform's studio and owns a community. Email is globally unique. */
@Entity
@Table(name = "creator_account")
public class CreatorAccount extends BaseEntity {

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

    protected CreatorAccount() {
    }

    public CreatorAccount(String email) {
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
