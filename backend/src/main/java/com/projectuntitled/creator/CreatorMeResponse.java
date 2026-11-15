package com.projectuntitled.creator;

import com.projectuntitled.common.OnboardingStep;
import com.projectuntitled.community.CommunityDto;

/** {@code community} is null until onboarding step COMMUNITY is done. */
public record CreatorMeResponse(CreatorDto account, CommunityDto community, OnboardingStep nextStep) {
}
