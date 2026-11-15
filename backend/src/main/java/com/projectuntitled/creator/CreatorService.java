package com.projectuntitled.creator;

import com.projectuntitled.common.AccountStatus;
import com.projectuntitled.common.LoginResult;
import com.projectuntitled.common.Usernames;
import com.projectuntitled.common.error.ApiException;
import com.projectuntitled.common.web.UpdateProfileRequest;
import com.projectuntitled.common.web.UsernameAvailabilityResponse;
import java.time.Clock;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreatorService {

    private final CreatorAccountRepository creators;
    private final Clock clock;

    public CreatorService(CreatorAccountRepository creators, Clock clock) {
        this.creators = creators;
        this.clock = clock;
    }

    /** Called after a verified code: finds the creator or creates them (signup and login are one flow). */
    @Transactional
    public LoginResult<CreatorAccount> recordLogin(String email) {
        Optional<CreatorAccount> existing = creators.findByEmail(email);
        CreatorAccount account = existing.orElseGet(() -> creators.save(new CreatorAccount(email)));
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw ApiException.forbidden("ACCOUNT_SUSPENDED", "This account is suspended.");
        }
        account.recordLogin(clock.instant());
        return new LoginResult<>(account, existing.isEmpty());
    }

    @Transactional(readOnly = true)
    public CreatorAccount requireById(UUID id) {
        return creators.findById(id)
                .filter(account -> account.getStatus() == AccountStatus.ACTIVE)
                .orElseThrow(() -> ApiException.unauthorized("ACCOUNT_NOT_FOUND", "Please sign in again."));
    }

    @Transactional
    public CreatorAccount updateProfile(UUID id, UpdateProfileRequest request) {
        CreatorAccount account = requireById(id);
        String username = Usernames.normalize(request.username());
        if (creators.existsByUsernameAndIdNot(username, id)) {
            throw ApiException.conflict("USERNAME_TAKEN", "That username is taken.");
        }
        account.updateProfile(request.fullName().trim(), username);
        return account;
    }

    @Transactional(readOnly = true)
    public UsernameAvailabilityResponse usernameAvailability(UUID id, String rawUsername) {
        String username = Usernames.normalize(rawUsername);
        if (username == null || !username.matches(Usernames.PATTERN)) {
            return new UsernameAvailabilityResponse(username, false, Usernames.MESSAGE);
        }
        boolean taken = creators.existsByUsernameAndIdNot(username, id);
        return new UsernameAvailabilityResponse(username, !taken, taken ? "That username is taken." : null);
    }
}
