package com.projectuntitled.creator;

import com.projectuntitled.auth.AuthPaths;
import com.projectuntitled.common.web.UpdateProfileRequest;
import com.projectuntitled.common.web.UsernameAvailabilityResponse;
import com.projectuntitled.community.Community;
import com.projectuntitled.community.CommunityService;
import com.projectuntitled.web.CurrentCreator;
import com.projectuntitled.web.CurrentCreatorId;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(AuthPaths.CREATOR)
@Tag(name = "Creator: account")
public class CreatorController {

    private final CreatorService creatorService;
    private final CommunityService communityService;

    public CreatorController(CreatorService creatorService, CommunityService communityService) {
        this.creatorService = creatorService;
        this.communityService = communityService;
    }

    /** The studio calls this on load to decide between onboarding and the dashboard. */
    @GetMapping("/me")
    public CreatorMeResponse me(@CurrentCreator CreatorAccount creator) {
        Community community = communityService.findByOwner(creator.getId()).orElse(null);
        return new CreatorMeResponse(
                CreatorDto.from(creator),
                community == null ? null : communityService.toDto(community),
                CreatorOnboarding.nextStep(creator, community));
    }

    @PutMapping("/me/profile")
    public CreatorDto updateProfile(@CurrentCreatorId UUID creatorId, @Valid @RequestBody UpdateProfileRequest request) {
        return CreatorDto.from(creatorService.updateProfile(creatorId, request));
    }

    @GetMapping("/username-availability")
    public UsernameAvailabilityResponse usernameAvailability(@CurrentCreatorId UUID creatorId, @RequestParam String username) {
        return creatorService.usernameAvailability(creatorId, username);
    }
}
