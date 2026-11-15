package com.projectuntitled.auth.api;

import com.projectuntitled.common.OnboardingStep;

/**
 * Body of a successful sign-in. Tokens are not in the body: they are set as httpOnly cookies.
 * {@code nextStep} tells the UI whether to show onboarding or go straight to the dashboard.
 */
public record AuthResponse<T>(T account, boolean newAccount, OnboardingStep nextStep) {
}
