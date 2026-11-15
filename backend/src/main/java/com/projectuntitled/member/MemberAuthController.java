package com.projectuntitled.member;

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
import com.projectuntitled.community.Community;
import com.projectuntitled.web.CurrentCommunity;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Member sign-up / sign-in on a creator's white-label site. Same flow as creators, but every code,
 * account and session is scoped to the community in the URL, and emails carry the community's name.
 */
@RestController
@RequestMapping(AuthPaths.MEMBER + "/{slug}/auth")
@Tag(name = "Member: auth", description = "Passwordless email-code sign-in for one community's members")
public class MemberAuthController {

    private final OtpService otpService;
    private final TokenService tokenService;
    private final MemberService memberService;
    private final AuthCookies cookies;

    public MemberAuthController(OtpService otpService, TokenService tokenService, MemberService memberService,
            AuthCookies cookies) {
        this.otpService = otpService;
        this.tokenService = tokenService;
        this.memberService = memberService;
        this.cookies = cookies;
    }

    @PostMapping("/otp/start")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public OtpStartedResponse start(@PathVariable String slug, @CurrentCommunity Community community,
            @Valid @RequestBody StartOtpRequest request) {
        return otpService.start(OtpScope.member(community.getId()), Emails.normalize(request.email()), community.getName());
    }

    @PostMapping("/otp/verify")
    public AuthResponse<MemberDto> verify(@PathVariable String slug, @CurrentCommunity Community community,
            @Valid @RequestBody VerifyOtpRequest request, HttpServletRequest http, HttpServletResponse response) {
        String email = Emails.normalize(request.email());
        otpService.verify(OtpScope.member(community.getId()), email, request.code());

        LoginResult<MemberAccount> login = memberService.recordLogin(community.getId(), email);
        MemberAccount account = login.account();
        TokenPair tokens = tokenService.issue(Audience.MEMBER, account.getId(), community.getId(),
                http.getHeader(HttpHeaders.USER_AGENT));
        cookies.setMemberTokens(response, community.getSlug(), tokens);

        return new AuthResponse<>(MemberDto.from(account), login.newAccount(), MemberService.nextStep(account));
    }

    @PostMapping("/refresh")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void refresh(@PathVariable String slug, @CurrentCommunity Community community,
            @CookieValue(name = AuthCookies.MEMBER_REFRESH, required = false) String refreshToken,
            HttpServletRequest http, HttpServletResponse response) {
        TokenService.Rotation rotation = tokenService.rotate(refreshToken, Audience.MEMBER, community.getId(),
                http.getHeader(HttpHeaders.USER_AGENT));
        cookies.setMemberTokens(response, community.getSlug(), rotation.tokens());
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@PathVariable String slug, @CurrentCommunity Community community,
            @CookieValue(name = AuthCookies.MEMBER_REFRESH, required = false) String refreshToken,
            HttpServletResponse response) {
        tokenService.revoke(refreshToken);
        cookies.clearMemberTokens(response, community.getSlug());
    }
}
