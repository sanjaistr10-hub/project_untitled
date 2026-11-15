package com.projectuntitled.auth.api;

/** Returned after a code is emailed. The UI shows a resend timer of {@code resendAfterSeconds}. */
public record OtpStartedResponse(String email, long expiresInSeconds, long resendAfterSeconds) {
}
