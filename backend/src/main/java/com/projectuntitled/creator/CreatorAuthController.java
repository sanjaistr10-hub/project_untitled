package com.projectuntitled.creator;

import com.projectuntitled.auth.Audience;
import com.projectuntitled.auth.AuthPaths;
import com.projectuntitled.auth.api.AuthResponse;
import com.projectuntitled.auth.api.OtpStartedResponse;
import com.projectuntitled.auth.api.StartOtpRequest;
import com.projectuntitled.auth.api.VerifyOtpRequest;
import com.projectuntitled.auth.otp.OtpScope;
import com.projectuntitled.auth.otp.OtpService;
import com.projectuntitled.auth.token.TokenPair;
import com.projectuntitled.auth.token.TokenService;
import com.projectuntitled.auth.web.AuthCookies;
import com.projectuntitled.common.Emails;
import com.projectuntitled.common.LoginResult;
import com.projectuntitled.community.CommunityService;
import com.projectuntitled.config.AppProperties;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Creator sign-up / sign-in on the platform studio (design slides 1-2). */
@RestController
@RequestMapping(AuthPaths.CREATOR_AUTH)
@Tag(name = "Creator: auth", description = "Passwordless email-code sign-in for creators")
public class CreatorAuthController {

    private final OtpService otpService;
    private final TokenService tokenService;
    private final CreatorService creatorService;
    private final CommunityService communityService;
    private final AuthCookies cookies;
    private final AppProperties properties;

    public CreatorAuthController(OtpService otpService, TokenService tokenService, CreatorService creatorService,
            CommunityService communityService, AuthCookies cookies, AppProperties properties) {
        this.otpService = otpService;
        this.tokenService = tokenService;
        this.creatorService = creatorService;
        this.communityService = communityService;
        this.cookies = cookies;
        this.properties = properties;
    }

    @PostMapping("/otp/start")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public OtpStartedResponse start(@Valid @RequestBody StartOtpRequest request) {
        return otpService.start(OtpScope.creator(), Emails.normalize(request.email()), properties.brandName());
    }

    @PostMapping("/otp/verify")
    public AuthResponse<CreatorDto> verify(@Valid @RequestBody VerifyOtpRequest request,
            HttpServletRequest http, HttpServletResponse response) {
        String email = Emails.normalize(request.email());
        otpService.verify(OtpScope.creator(), email, request.code());

        LoginResult<CreatorAccount> login = creatorService.recordLogin(email);
        CreatorAccount account = login.account();
        TokenPair tokens = tokenService.issue(Audience.CREATOR, account.getId(), null, http.getHeader(HttpHeaders.USER_AGENT));
        cookies.setCreatorTokens(response, tokens);

        var community = communityService.findByOwner(account.getId()).orElse(null);
        return new AuthResponse<>(CreatorDto.from(account), login.newAccount(), CreatorOnboarding.nextStep(account, community));
    }

    @PostMapping("/refresh")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void refresh(@CookieValue(name = AuthCookies.CREATOR_REFRESH, required = false) String refreshToken,
            HttpServletRequest http, HttpServletResponse response) {
        TokenService.Rotation rotation = tokenService.rotate(refreshToken, Audience.CREATOR, null, http.getHeader(HttpHeaders.USER_AGENT));
        cookies.setCreatorTokens(response, rotation.tokens());
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@CookieValue(name = AuthCookies.CREATOR_REFRESH, required = false) String refreshToken,
            HttpServletResponse response) {
        tokenService.revoke(refreshToken);
        cookies.clearCreatorTokens(response);
    }
}
