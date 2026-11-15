package com.projectuntitled.creator;

import com.projectuntitled.common.OnboardingStep;
import com.projectuntitled.community.Community;

/** Creator onboarding = profile, then community. Derived from data, so there is no step column to drift. */
final class CreatorOnboarding {

    private CreatorOnboarding() {
    }

    static OnboardingStep nextStep(CreatorAccount account, Community community) {
        if (!account.isProfileComplete()) {
            return OnboardingStep.PROFILE;
        }
        return community == null ? OnboardingStep.COMMUNITY : OnboardingStep.DONE;
    }
}
