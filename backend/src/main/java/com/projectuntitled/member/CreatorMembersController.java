package com.projectuntitled.member;

import com.projectuntitled.auth.AuthPaths;
import com.projectuntitled.common.web.PageResponse;
import com.projectuntitled.community.Community;
import com.projectuntitled.community.CommunityService;
import com.projectuntitled.web.CurrentCreatorId;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** The creator's view of their members (the seed of the CRM). */
@RestController
@RequestMapping(AuthPaths.CREATOR + "/members")
@Tag(name = "Creator: members")
public class CreatorMembersController {

    private final CommunityService communityService;
    private final MemberService memberService;

    public CreatorMembersController(CommunityService communityService, MemberService memberService) {
        this.communityService = communityService;
        this.memberService = memberService;
    }

    @GetMapping
    public PageResponse<MemberDto> list(@CurrentCreatorId UUID creatorId,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Community community = communityService.requireByOwner(creatorId);
        return memberService.list(community.getId(), q, page, size);
    }
}
