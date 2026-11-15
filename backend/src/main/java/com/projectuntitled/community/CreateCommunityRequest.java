package com.projectuntitled.community;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Onboarding "What's your community called?". The slug is derived from the name if omitted. */
public record CreateCommunityRequest(
        @NotBlank @Size(max = 80) String name,
        @Size(max = Slugs.MAX_LENGTH) String slug) {
}
