package com.projectuntitled.auth.otp;

import java.time.Duration;

/**
 * What to email. {@code brandName} is the platform name for creators and the community's own name
 * for members, so member emails never mention the platform (white label).
 */
public record OtpMessage(String email, String code, String brandName, Duration validFor) {
}
