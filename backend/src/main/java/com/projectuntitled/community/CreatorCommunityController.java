package com.projectuntitled.community;

import com.projectuntitled.auth.AuthPaths;
import com.projectuntitled.web.CurrentCreatorId;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(AuthPaths.CREATOR + "/community")
@Tag(name = "Creator: community", description = "Create and brand the creator's white-label community")
public class CreatorCommunityController {

    private final CommunityService communityService;

    public CreatorCommunityController(CommunityService communityService) {
        this.communityService = communityService;
    }

    @GetMapping
    public CommunityDto get(@CurrentCreatorId UUID creatorId) {
        return communityService.toDto(communityService.requireByOwner(creatorId));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CommunityDto create(@CurrentCreatorId UUID creatorId, @Valid @RequestBody CreateCommunityRequest request) {
        return communityService.toDto(communityService.create(creatorId, request));
    }

    @PatchMapping
    public CommunityDto updateBranding(@CurrentCreatorId UUID creatorId, @Valid @RequestBody UpdateBrandingRequest request) {
        return communityService.toDto(communityService.updateBranding(creatorId, request));
    }

    /** Pass either a slug to check, or a name to get the slug we'd generate for it. */
    @GetMapping("/slug-availability")
    public SlugAvailabilityResponse slugAvailability(
            @RequestParam(required = false) String slug, @RequestParam(required = false) String name) {
        String candidate = slug != null ? slug : communityService.uniqueSlugFrom(name);
        return communityService.checkSlug(candidate);
    }
}
