package com.projectuntitled.member;

import com.projectuntitled.common.AccountStatus;
import com.projectuntitled.common.LoginResult;
import com.projectuntitled.common.OnboardingStep;
import com.projectuntitled.common.Usernames;
import com.projectuntitled.common.error.ApiException;
import com.projectuntitled.common.web.PageResponse;
import com.projectuntitled.common.web.UpdateProfileRequest;
import com.projectuntitled.common.web.UsernameAvailabilityResponse;
import java.time.Clock;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class MemberService {

    private final MemberAccountRepository members;
    private final Clock clock;

    public MemberService(MemberAccountRepository members, Clock clock) {
        this.members = members;
        this.clock = clock;
    }

    public record MemberStats(long total, long joinedLast7Days, List<MemberDto> recent) {
    }

    @Transactional
    public LoginResult<MemberAccount> recordLogin(UUID communityId, String email) {
        Optional<MemberAccount> existing = members.findByCommunityIdAndEmail(communityId, email);
        MemberAccount account = existing.orElseGet(() -> members.save(new MemberAccount(communityId, email)));
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw ApiException.forbidden("ACCOUNT_SUSPENDED", "This account is suspended.");
        }
        account.recordLogin(clock.instant());
        return new LoginResult<>(account, existing.isEmpty());
    }

    @Transactional(readOnly = true)
    public MemberAccount requireInCommunity(UUID memberId, UUID communityId) {
        return members.findByIdAndCommunityId(memberId, communityId)
                .filter(account -> account.getStatus() == AccountStatus.ACTIVE)
                .orElseThrow(() -> ApiException.unauthorized("ACCOUNT_NOT_FOUND", "Please sign in again."));
    }

    @Transactional
    public MemberAccount updateProfile(UUID memberId, UUID communityId, UpdateProfileRequest request) {
        MemberAccount account = requireInCommunity(memberId, communityId);
        String username = Usernames.normalize(request.username());
        if (members.existsByCommunityIdAndUsernameAndIdNot(communityId, username, memberId)) {
            throw ApiException.conflict("USERNAME_TAKEN", "That username is taken.");
        }
        account.updateProfile(request.fullName().trim(), username);
        return account;
    }

    @Transactional(readOnly = true)
    public UsernameAvailabilityResponse usernameAvailability(UUID memberId, UUID communityId, String rawUsername) {
        String username = Usernames.normalize(rawUsername);
        if (username == null || !username.matches(Usernames.PATTERN)) {
            return new UsernameAvailabilityResponse(username, false, Usernames.MESSAGE);
        }
        boolean taken = members.existsByCommunityIdAndUsernameAndIdNot(communityId, username, memberId);
        return new UsernameAvailabilityResponse(username, !taken, taken ? "That username is taken." : null);
    }

    @Transactional(readOnly = true)
    public PageResponse<MemberDto> list(UUID communityId, String query, int page, int size) {
        PageRequest pageRequest = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100),
                Sort.by(Sort.Direction.DESC, "createdAt"));
        if (!StringUtils.hasText(query)) {
            return PageResponse.from(members.findByCommunityId(communityId, pageRequest), MemberDto::from);
        }
        String escaped = query.trim().toLowerCase(Locale.ROOT)
                .replace("!", "!!").replace("%", "!%").replace("_", "!_");
        return PageResponse.from(members.search(communityId, "%" + escaped + "%", pageRequest), MemberDto::from);
    }

    @Transactional(readOnly = true)
    public MemberStats stats(UUID communityId) {
        return new MemberStats(
                members.countByCommunityId(communityId),
                members.countByCommunityIdAndCreatedAtAfter(communityId, clock.instant().minus(Duration.ofDays(7))),
                members.findTop5ByCommunityIdOrderByCreatedAtDesc(communityId).stream().map(MemberDto::from).toList());
    }

    public static OnboardingStep nextStep(MemberAccount account) {
        return account.isProfileComplete() ? OnboardingStep.DONE : OnboardingStep.PROFILE;
    }
}
