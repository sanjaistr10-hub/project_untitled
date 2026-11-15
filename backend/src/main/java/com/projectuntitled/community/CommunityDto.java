package com.projectuntitled.community;

import java.time.Instant;
import java.util.UUID;

/** The creator's full view of their community. */
public record CommunityDto(
        UUID id,
        String name,
        String slug,
        String tagline,
        String description,
        String logoUrl,
        String accentColor,
        String siteUrl,
        Instant createdAt) {
}
