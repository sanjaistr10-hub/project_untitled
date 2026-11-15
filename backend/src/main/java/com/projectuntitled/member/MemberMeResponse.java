package com.projectuntitled.member;

import com.projectuntitled.common.OnboardingStep;
import com.projectuntitled.community.PublicCommunityDto;

public record MemberMeResponse(MemberDto account, PublicCommunityDto community, OnboardingStep nextStep) {
}
