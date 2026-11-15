package com.projectuntitled.creator;

import com.projectuntitled.member.MemberDto;
import java.util.List;

public record DashboardSummaryResponse(long memberCount, long newMembersLast7Days, List<MemberDto> recentMembers) {
}
