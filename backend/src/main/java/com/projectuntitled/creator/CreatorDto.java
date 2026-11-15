package com.projectuntitled.creator;

import java.time.Instant;
import java.util.UUID;

public record CreatorDto(UUID id, String email, String fullName, String username, String avatarUrl, Instant createdAt) {

    public static CreatorDto from(CreatorAccount a) {
        return new CreatorDto(a.getId(), a.getEmail(), a.getFullName(), a.getUsername(), a.getAvatarUrl(), a.getCreatedAt());
    }
}
