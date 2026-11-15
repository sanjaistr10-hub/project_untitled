package com.projectuntitled.member;

import java.time.Instant;
import java.util.UUID;

public record MemberDto(UUID id, String email, String fullName, String username, String avatarUrl,
        Instant joinedAt, Instant lastLoginAt) {

    public static MemberDto from(MemberAccount m) {
        return new MemberDto(m.getId(), m.getEmail(), m.getFullName(), m.getUsername(), m.getAvatarUrl(),
                m.getCreatedAt(), m.getLastLoginAt());
    }
}
