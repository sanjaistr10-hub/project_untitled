package com.projectuntitled.community;

import com.projectuntitled.common.error.ApiException;
import com.projectuntitled.config.AppProperties;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class CommunityService {

    static final String DEFAULT_ACCENT = "#111111";

    private final CommunityRepository communities;
    private final CommunityDomainRepository domains;
    private final AppProperties properties;

    public CommunityService(CommunityRepository communities, CommunityDomainRepository domains, AppProperties properties) {
        this.communities = communities;
        this.domains = domains;
        this.properties = properties;
    }

    /** v1: one community per creator (enforced by a unique index; drop it to allow several). */
    @Transactional
    public Community create(UUID ownerId, CreateCommunityRequest request) {
        if (communities.findByOwnerId(ownerId).isPresent()) {
            throw ApiException.conflict("COMMUNITY_EXISTS", "You already have a community.");
        }
        String slug;
        if (StringUtils.hasText(request.slug())) {
            slug = Slugs.normalize(request.slug());
            Optional<String> problem = Slugs.problem(slug);
            if (problem.isPresent()) {
                throw ApiException.badRequest("SLUG_INVALID", problem.get());
            }
            if (communities.existsBySlug(slug)) {
                throw ApiException.conflict("SLUG_TAKEN", "That address is already taken.")
                        .with("suggestion", uniqueSlugFrom(slug));
            }
        } else {
            slug = uniqueSlugFrom(request.name());
        }
        return communities.save(new Community(ownerId, request.name().trim(), slug, DEFAULT_ACCENT));
    }

    @Transactional(readOnly = true)
    public SlugAvailabilityResponse checkSlug(String rawSlug) {
        String slug = Slugs.normalize(rawSlug);
        Optional<String> problem = Slugs.problem(slug);
        if (problem.isPresent()) {
            return new SlugAvailabilityResponse(slug, false, problem.get(), uniqueSlugFrom(slug), null);
        }
        if (communities.existsBySlug(slug)) {
            return new SlugAvailabilityResponse(slug, false, "That address is already taken.", uniqueSlugFrom(slug), null);
        }
        return new SlugAvailabilityResponse(slug, true, null, null, properties.frontend().memberSiteUrl(slug));
    }

    /** First free slug for a name: "fitness-club", then "fitness-club-2", "fitness-club-3"... */
    @Transactional(readOnly = true)
    public String uniqueSlugFrom(String name) {
        String root = Slugs.fromName(name);
        if (Slugs.problem(root).isPresent()) {
            root = Slugs.trimTo(root + "-community", Slugs.MAX_LENGTH);
        }
        String candidate = root;
        for (int n = 2; Slugs.problem(candidate).isPresent() || communities.existsBySlug(candidate); n++) {
            String suffix = "-" + n;
            candidate = Slugs.trimTo(root, Slugs.MAX_LENGTH - suffix.length()) + suffix;
        }
        return candidate;
    }

    @Transactional(readOnly = true)
    public Optional<Community> findByOwner(UUID ownerId) {
        return communities.findByOwnerId(ownerId);
    }

    @Transactional(readOnly = true)
    public Community requireByOwner(UUID ownerId) {
        return communities.findByOwnerId(ownerId)
                .orElseThrow(() -> ApiException.notFound("COMMUNITY_NOT_FOUND", "Create your community first."));
    }

    @Transactional(readOnly = true)
    public Community requireActiveBySlug(String slug) {
        return communities.findBySlug(Slugs.normalize(slug))
                .filter(Community::isActive)
                .orElseThrow(() -> ApiException.notFound("COMMUNITY_NOT_FOUND", "This community doesn't exist."));
    }

    @Transactional
    public Community updateBranding(UUID ownerId, UpdateBrandingRequest request) {
        Community community = requireByOwner(ownerId);
        if (request.name() != null) {
            community.setName(request.name().trim());
        }
        if (request.tagline() != null) {
            community.setTagline(blankToNull(request.tagline()));
        }
        if (request.description() != null) {
            community.setDescription(blankToNull(request.description()));
        }
        if (request.accentColor() != null) {
            community.setAccentColor(request.accentColor().toUpperCase(Locale.ROOT));
        }
        if (request.logoUrl() != null) {
            community.setLogoUrl(blankToNull(request.logoUrl()));
        }
        return community;
    }

    /** Custom domain to slug, used by the frontend proxy. Only verified domains resolve. */
    @Transactional(readOnly = true)
    public ResolvedHostResponse resolveCustomDomain(String host) {
        String normalized = host == null ? "" : host.trim().toLowerCase(Locale.ROOT);
        return domains.findByHost(normalized)
                .filter(domain -> domain.getVerifiedAt() != null)
                .flatMap(domain -> communities.findById(domain.getCommunityId()))
                .filter(Community::isActive)
                .map(community -> new ResolvedHostResponse(normalized, community.getSlug()))
                .orElseThrow(() -> ApiException.notFound("DOMAIN_NOT_FOUND", "No community uses this domain."));
    }

    public CommunityDto toDto(Community c) {
        return new CommunityDto(c.getId(), c.getName(), c.getSlug(), c.getTagline(), c.getDescription(),
                c.getLogoUrl(), c.getAccentColor(), properties.frontend().memberSiteUrl(c.getSlug()), c.getCreatedAt());
    }

    public PublicCommunityDto toPublicDto(Community c) {
        return new PublicCommunityDto(c.getName(), c.getSlug(), c.getTagline(), c.getDescription(),
                c.getLogoUrl(), c.getAccentColor());
    }

    private static String blankToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
