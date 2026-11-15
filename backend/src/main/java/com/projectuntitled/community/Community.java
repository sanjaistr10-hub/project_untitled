package com.projectuntitled.community;

import com.projectuntitled.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.util.UUID;

/**
 * A creator's white-label space: its own name, address ({slug}.platform or a custom domain),
 * branding and member base. This is the tenant boundary for all member data.
 */
@Entity
@Table(name = "community")
public class Community extends BaseEntity {

    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    @Column(nullable = false, length = 80)
    private String name;

    @Column(nullable = false, length = 40)
    private String slug;

    @Column(length = 160)
    private String tagline;

    @Column(columnDefinition = "text")
    private String description;

    @Column(name = "logo_url", length = 1024)
    private String logoUrl;

    @Column(name = "accent_color", nullable = false, length = 7)
    private String accentColor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CommunityStatus status = CommunityStatus.ACTIVE;

    protected Community() {
    }

    public Community(UUID ownerId, String name, String slug, String accentColor) {
        this.ownerId = ownerId;
        this.name = name;
        this.slug = slug;
        this.accentColor = accentColor;
    }

    public boolean isActive() {
        return status == CommunityStatus.ACTIVE;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSlug() {
        return slug;
    }

    public String getTagline() {
        return tagline;
    }

    public void setTagline(String tagline) {
        this.tagline = tagline;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public String getAccentColor() {
        return accentColor;
    }

    public void setAccentColor(String accentColor) {
        this.accentColor = accentColor;
    }

    public CommunityStatus getStatus() {
        return status;
    }
}
