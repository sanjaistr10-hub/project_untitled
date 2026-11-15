package com.projectuntitled.auth.otp;

import com.projectuntitled.auth.Audience;
import java.util.Objects;
import java.util.UUID;

/** Keeps creator codes and each community's member codes apart, even for the same email. */
public record OtpScope(Audience audience, UUID communityId) {

    public static OtpScope creator() {
        return new OtpScope(Audience.CREATOR, null);
    }

    public static OtpScope member(UUID communityId) {
        return new OtpScope(Audience.MEMBER, Objects.requireNonNull(communityId));
    }

    public String key() {
        return communityId == null ? "creator" : "member:" + communityId;
    }
}
