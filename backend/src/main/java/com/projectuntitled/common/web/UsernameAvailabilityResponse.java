package com.projectuntitled.common.web;

public record UsernameAvailabilityResponse(String username, boolean available, String reason) {
}
