package com.projectuntitled.member;

import com.projectuntitled.auth.AuthPaths;
import com.projectuntitled.common.web.UpdateProfileRequest;
import com.projectuntitled.common.web.UsernameAvailabilityResponse;
import com.projectuntitled.community.Community;
import com.projectuntitled.community.CommunityService;
import com.projectuntitled.web.CurrentCommunity;
import com.projectuntitled.web.CurrentMember;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(AuthPaths.MEMBER + "/{slug}")
@Tag(name = "Member: account")
public class MemberController {

    private final MemberService memberService;
    private final CommunityService communityService;

    public MemberController(MemberService memberService, CommunityService communityService) {
        this.memberService = memberService;
        this.communityService = communityService;
    }

    @GetMapping("/me")
    public MemberMeResponse me(@PathVariable String slug, @CurrentCommunity Community community,
            @CurrentMember MemberAccount member) {
        return new MemberMeResponse(MemberDto.from(member), communityService.toPublicDto(community),
                MemberService.nextStep(member));
    }

    @PutMapping("/me/profile")
    public MemberDto updateProfile(@PathVariable String slug, @CurrentCommunity Community community,
            @CurrentMember MemberAccount member, @Valid @RequestBody UpdateProfileRequest request) {
        return MemberDto.from(memberService.updateProfile(member.getId(), community.getId(), request));
    }

    @GetMapping("/username-availability")
    public UsernameAvailabilityResponse usernameAvailability(@PathVariable String slug,
            @CurrentCommunity Community community, @CurrentMember MemberAccount member, @RequestParam String username) {
        return memberService.usernameAvailability(member.getId(), community.getId(), username);
    }
}
