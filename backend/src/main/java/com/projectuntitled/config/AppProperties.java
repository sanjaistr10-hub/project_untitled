package com.projectuntitled.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Every {@code app.*} setting in one typed place. Defaults live in application.yml; secrets are
 * only set in application-dev.yml (local) or via environment variables (prod).
 */
@Validated
@ConfigurationProperties(prefix = "app")
public record AppProperties(
        @NotBlank String brandName,
        @Valid @NotNull Frontend frontend,
        @Valid @NotNull Security security,
        @Valid @NotNull Otp otp,
        @Valid @NotNull Mail mail) {

    public record Frontend(
            /** Where the creator studio is served, e.g. http://localhost:3000 */
            @NotBlank String appUrl,
            /** Member site URL with a {slug} placeholder, e.g. http://{slug}.localhost:3000 */
            @NotBlank String memberSiteUrlTemplate) {

        public String memberSiteUrl(String slug) {
            return memberSiteUrlTemplate.replace("{slug}", slug);
        }
    }

    public record Security(
            @NotBlank @Size(min = 32, message = "must be at least 32 characters") String jwtSecret,
            @NotBlank String jwtIssuer,
            @NotNull Duration accessTokenTtl,
            @NotNull Duration refreshTokenTtl,
            boolean cookieSecure) {
    }

    public record Otp(
            @NotBlank @Size(min = 32, message = "must be at least 32 characters") String hashSecret,
            @Min(4) @Max(8) int length,
            @NotNull Duration ttl,
            @NotNull Duration resendCooldown,
            @Min(1) int maxAttempts,
            @Min(1) int maxSendsPerHour) {
    }

    public record Mail(@NotNull Delivery delivery, @NotBlank String fromAddress) {

        public enum Delivery {
            /** Print codes to the backend log (local development). */
            LOG,
            /** Send real email through spring.mail.* SMTP settings. */
            SMTP
        }
    }
}
