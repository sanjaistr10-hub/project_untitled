package com.projectuntitled.auth.otp;

import com.projectuntitled.auth.api.OtpStartedResponse;
import com.projectuntitled.common.error.ApiException;
import com.projectuntitled.config.AppProperties;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Passwordless sign-in: email a short numeric code, then verify it. Signup and login are the same
 * flow; the caller creates the account on first successful verification.
 */
@Service
public class OtpService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final OtpChallengeRepository challenges;
    private final OtpSender sender;
    private final AppProperties.Otp config;
    private final Clock clock;
    private final SecretKeySpec hmacKey;

    public OtpService(OtpChallengeRepository challenges, OtpSender sender, AppProperties properties, Clock clock) {
        this.challenges = challenges;
        this.sender = sender;
        this.config = properties.otp();
        this.clock = clock;
        this.hmacKey = new SecretKeySpec(config.hashSecret().getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }

    @Transactional
    public OtpStartedResponse start(OtpScope scope, String email, String brandName) {
        Instant now = clock.instant();

        challenges.findFirstByScopeAndEmailOrderByCreatedAtDesc(scope.key(), email).ifPresent(latest -> {
            Instant resendAt = latest.getCreatedAt().plus(config.resendCooldown());
            if (resendAt.isAfter(now)) {
                throw ApiException.tooManyRequests("OTP_COOLDOWN",
                        "Please wait a moment before requesting another code.", secondsUntil(now, resendAt));
            }
        });
        long sentLastHour = challenges.countByScopeAndEmailAndCreatedAtAfter(scope.key(), email, now.minus(Duration.ofHours(1)));
        if (sentLastHour >= config.maxSendsPerHour()) {
            throw ApiException.tooManyRequests("OTP_RATE_LIMITED",
                    "Too many codes requested. Try again later.", Duration.ofHours(1).toSeconds());
        }

        challenges.invalidateActive(scope.key(), email, now);
        String code = generateCode();
        challenges.save(new OtpChallenge(scope.key(), email, hash(scope, email, code), now.plus(config.ttl())));
        sender.send(new OtpMessage(email, code, brandName, config.ttl()));

        return new OtpStartedResponse(email, config.ttl().toSeconds(), config.resendCooldown().toSeconds());
    }

    /** Throws ApiException if the code is wrong or expired. Failed attempts are committed regardless. */
    @Transactional(noRollbackFor = ApiException.class)
    public void verify(OtpScope scope, String email, String code) {
        Instant now = clock.instant();
        OtpChallenge challenge = challenges
                .findFirstByScopeAndEmailAndConsumedAtIsNullOrderByCreatedAtDesc(scope.key(), email)
                .filter(c -> c.getExpiresAt().isAfter(now))
                .orElseThrow(() -> ApiException.badRequest("OTP_EXPIRED", "This code has expired. Request a new one."));

        byte[] expected = challenge.getCodeHash().getBytes(StandardCharsets.UTF_8);
        byte[] actual = hash(scope, email, code).getBytes(StandardCharsets.UTF_8);
        if (!MessageDigest.isEqual(expected, actual)) {
            challenge.recordFailedAttempt();
            int remaining = Math.max(0, config.maxAttempts() - challenge.getAttempts());
            if (remaining == 0) {
                challenge.consume(now);
                throw ApiException.badRequest("OTP_TOO_MANY_ATTEMPTS", "Too many wrong attempts. Request a new code.");
            }
            throw ApiException.badRequest("OTP_INVALID", "That code isn't right.").with("attemptsRemaining", remaining);
        }
        challenge.consume(now);
    }

    private String generateCode() {
        int bound = (int) Math.pow(10, config.length());
        return String.format("%0" + config.length() + "d", RANDOM.nextInt(bound));
    }

    private String hash(OtpScope scope, String email, String code) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(hmacKey);
            byte[] digest = mac.doFinal((scope.key() + '|' + email + '|' + code).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("HMAC-SHA256 unavailable", e);
        }
    }

    private static long secondsUntil(Instant now, Instant then) {
        return Math.max(1, (long) Math.ceil(Duration.between(now, then).toMillis() / 1000.0));
    }
}
