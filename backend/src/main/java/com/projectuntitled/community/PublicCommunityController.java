package com.projectuntitled.community;

import com.projectuntitled.auth.AuthPaths;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(AuthPaths.PUBLIC)
@Tag(name = "Public", description = "No sign-in required")
public class PublicCommunityController {

    private final CommunityService communityService;

    public PublicCommunityController(CommunityService communityService) {
        this.communityService = communityService;
    }

    /** Branding for a community's sign-in page and site chrome. */
    @GetMapping("/communities/{slug}")
    public PublicCommunityDto community(@PathVariable String slug) {
        return communityService.toPublicDto(communityService.requireActiveBySlug(slug));
    }

    /** Used by the frontend proxy to map a custom domain to its community. */
    @GetMapping("/domains/resolve")
    public ResolvedHostResponse resolve(@RequestParam String host) {
        return communityService.resolveCustomDomain(host);
    }
}
