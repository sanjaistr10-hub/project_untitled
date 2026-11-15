package com.projectuntitled.common.web;

import com.projectuntitled.common.Usernames;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Body of PUT .../me/profile for both creators and members (onboarding "Nice to meet you"). */
public record UpdateProfileRequest(
        @NotBlank @Size(max = 120) String fullName,
        @NotBlank @Pattern(regexp = Usernames.PATTERN, message = Usernames.MESSAGE) String username) {
}
