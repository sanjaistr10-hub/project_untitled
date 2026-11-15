package com.projectuntitled.community;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** PATCH semantics: null means "leave unchanged", an empty string clears optional fields. */
public record UpdateBrandingRequest(
        @Size(min = 1, max = 80) String name,
        @Size(max = 160) String tagline,
        @Size(max = 2000) String description,
        @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Use a hex colour like #1F5FD1") String accentColor,
        @Size(max = 1024) @Pattern(regexp = "^(https?://.+)?$", message = "Use an http(s) URL") String logoUrl) {
}
