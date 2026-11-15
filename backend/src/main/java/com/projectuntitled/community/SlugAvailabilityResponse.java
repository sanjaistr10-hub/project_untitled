package com.projectuntitled.community;

public record SlugAvailabilityResponse(String slug, boolean available, String reason, String suggestion, String siteUrl) {
}
