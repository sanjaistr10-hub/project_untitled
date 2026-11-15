package com.projectuntitled.community;

/** What anyone may see: enough to brand the member sign-in page. No ids, no owner details. */
public record PublicCommunityDto(
        String name,
        String slug,
        String tagline,
        String description,
        String logoUrl,
        String accentColor) {
}
