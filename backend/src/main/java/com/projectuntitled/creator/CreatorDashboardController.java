package com.projectuntitled.creator;

import com.projectuntitled.auth.AuthPaths;
import com.projectuntitled.community.Community;
import com.projectuntitled.community.CommunityService;
import com.projectuntitled.member.MemberService;
import com.projectuntitled.web.CurrentCreatorId;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Numbers for the studio home. Add products, revenue, etc. here as those modules land. */
@RestController
@RequestMapping(AuthPaths.CREATOR + "/dashboard")
@Tag(name = "Creator: dashboard")
public class CreatorDashboardController {

    private final CommunityService communityService;
    private final MemberService memberService;

    public CreatorDashboardController(CommunityService communityService, MemberService memberService) {
        this.communityService = communityService;
        this.memberService = memberService;
    }

    @GetMapping("/summary")
    public DashboardSummaryResponse summary(@CurrentCreatorId UUID creatorId) {
        Community community = communityService.requireByOwner(creatorId);
        MemberService.MemberStats stats = memberService.stats(community.getId());
        return new DashboardSummaryResponse(stats.total(), stats.joinedLast7Days(), stats.recent());
    }
}
