package com.projectuntitled.auth.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record StartOtpRequest(@NotBlank @Email @Size(max = 320) String email) {
}
