package com.projectuntitled.auth.token;

import java.time.Duration;

public record TokenPair(String accessToken, Duration accessTtl, String refreshToken, Duration refreshTtl) {
}
