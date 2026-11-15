package com.projectuntitled;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.projectuntitled.auth.web.AuthCookies;
import com.projectuntitled.support.CapturingOtpSender;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

/**
 * End-to-end API tests for sign-up, onboarding and tenant isolation, against a real Postgres.
 * Every test starts from empty tables.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(CapturingOtpSender.Config.class)
class AuthFlowIntegrationTest {

    private static final String API = "/api/v1";

    @Autowired
    MockMvc mvc;

    @Autowired
    JdbcTemplate jdbc;

    @Autowired
    CapturingOtpSender otp;

    @BeforeEach
    void cleanDatabase() {
        jdbc.execute("TRUNCATE refresh_token, otp_challenge, member_account, community_domain, community, creator_account CASCADE");
        otp.clear();
    }

    // ---------------------------------------------------------------- creator

    @Test
    @DisplayName("Creator: sign up with an email code, onboard, and land on the dashboard")
    void creatorSignupAndOnboarding() throws Exception {
        Session creator = signIn(API + "/creator/auth", "Creator@Example.com", AuthCookies.CREATOR_ACCESS, AuthCookies.CREATOR_REFRESH);
        assertThat(creator.body().getContentAsString()).contains("\"newAccount\":true", "\"nextStep\":\"PROFILE\"");

        mvc.perform(get(API + "/creator/me").cookie(creator.access()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.account.email").value("creator@example.com"))
                .andExpect(jsonPath("$.nextStep").value("PROFILE"));

        mvc.perform(json(put(API + "/creator/me/profile"), "{\"fullName\":\"Arjun S\",\"username\":\"Arjun.S\"}").cookie(creator.access()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("arjun.s"));

        mvc.perform(get(API + "/creator/me").cookie(creator.access()))
                .andExpect(jsonPath("$.nextStep").value("COMMUNITY"));

        mvc.perform(json(post(API + "/creator/community"), "{\"name\":\"Arjun's IFVG Room\"}").cookie(creator.access()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.slug").value("arjuns-ifvg-room"))
                .andExpect(jsonPath("$.siteUrl").value("http://arjuns-ifvg-room.localhost:3100"));

        mvc.perform(get(API + "/creator/me").cookie(creator.access()))
                .andExpect(jsonPath("$.nextStep").value("DONE"))
                .andExpect(jsonPath("$.community.name").value("Arjun's IFVG Room"));

        // Signing in again is a login, not a second signup.
        Session again = signIn(API + "/creator/auth", "creator@example.com", AuthCookies.CREATOR_ACCESS, AuthCookies.CREATOR_REFRESH);
        assertThat(again.body().getContentAsString()).contains("\"newAccount\":false", "\"nextStep\":\"DONE\"");
    }

    @Test
    @DisplayName("Creator: second community and reserved or taken slugs are rejected")
    void communityRules() throws Exception {
        Session creator = onboardedCreator("one@example.com", "one", "Fitness Club");
        mvc.perform(json(post(API + "/creator/community"), "{\"name\":\"Another\"}").cookie(creator.access()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("COMMUNITY_EXISTS"));

        Session other = signIn(API + "/creator/auth", "two@example.com", AuthCookies.CREATOR_ACCESS, AuthCookies.CREATOR_REFRESH);
        mvc.perform(get(API + "/creator/community/slug-availability").param("slug", "fitness-club").cookie(other.access()))
                .andExpect(jsonPath("$.available").value(false))
                .andExpect(jsonPath("$.suggestion").value("fitness-club-2"));
        mvc.perform(json(post(API + "/creator/community"), "{\"name\":\"Admin\",\"slug\":\"admin\"}").cookie(other.access()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("SLUG_INVALID"));
        mvc.perform(json(post(API + "/creator/community"), "{\"name\":\"Fitness Club\"}").cookie(other.access()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.slug").value("fitness-club-2"));
    }

    // ---------------------------------------------------------------- members

    @Test
    @DisplayName("Member: sign up on a community's site, onboard, and show up in the creator's member list")
    void memberSignupIsVisibleToCreator() throws Exception {
        Session creator = onboardedCreator("host@example.com", "host", "Yoga Den");

        mvc.perform(get(API + "/public/communities/yoga-den"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Yoga Den"));

        Session member = signIn(API + "/c/yoga-den/auth", "member@example.com", AuthCookies.MEMBER_ACCESS, AuthCookies.MEMBER_REFRESH);
        assertThat(otp.lastFor("member@example.com").brandName()).isEqualTo("Yoga Den"); // white-label email
        assertThat(member.access().getPath()).isEqualTo("/api/v1/c/yoga-den");
        assertThat(member.refresh().getPath()).isEqualTo("/api/v1/c/yoga-den/auth");
        assertThat(member.access().isHttpOnly()).isTrue();

        mvc.perform(get(API + "/c/yoga-den/me").cookie(member.access()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nextStep").value("PROFILE"))
                .andExpect(jsonPath("$.community.slug").value("yoga-den"));

        mvc.perform(json(put(API + "/c/yoga-den/me/profile"), "{\"fullName\":\"Meera P\",\"username\":\"meera\"}").cookie(member.access()))
                .andExpect(status().isOk());
        mvc.perform(get(API + "/c/yoga-den/me").cookie(member.access()))
                .andExpect(jsonPath("$.nextStep").value("DONE"));

        mvc.perform(get(API + "/creator/members").cookie(creator.access()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalItems").value(1))
                .andExpect(jsonPath("$.items[0].fullName").value("Meera P"));
        mvc.perform(get(API + "/creator/members").param("q", "meer").cookie(creator.access()))
                .andExpect(jsonPath("$.totalItems").value(1));
        mvc.perform(get(API + "/creator/members").param("q", "100%").cookie(creator.access()))
                .andExpect(jsonPath("$.totalItems").value(0));
        mvc.perform(get(API + "/creator/dashboard/summary").cookie(creator.access()))
                .andExpect(jsonPath("$.memberCount").value(1))
                .andExpect(jsonPath("$.newMembersLast7Days").value(1));
    }

    @Test
    @DisplayName("Isolation: same email in two communities is two accounts, and tokens don't cross tenants or roles")
    void tenantIsolation() throws Exception {
        Session creatorA = onboardedCreator("a@example.com", "creator_a", "Alpha");
        onboardedCreator("b@example.com", "creator_b", "Beta");

        Session inAlpha = signIn(API + "/c/alpha/auth", "same@example.com", AuthCookies.MEMBER_ACCESS, AuthCookies.MEMBER_REFRESH);
        Session inBeta = signIn(API + "/c/beta/auth", "same@example.com", AuthCookies.MEMBER_ACCESS, AuthCookies.MEMBER_REFRESH);
        assertThat(inBeta.body().getContentAsString()).contains("\"newAccount\":true");
        assertThat(jdbc.queryForObject("select count(*) from member_account where email = 'same@example.com'", Long.class)).isEqualTo(2);

        // An Alpha member token used on Beta's API.
        mvc.perform(get(API + "/c/beta/me").header("Authorization", "Bearer " + inAlpha.access().getValue()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("WRONG_COMMUNITY"));

        // A member token on the creator API: cookie is ignored there, bearer header has the wrong role.
        mvc.perform(get(API + "/creator/me").cookie(new Cookie(AuthCookies.CREATOR_ACCESS, inAlpha.access().getValue())))
                .andExpect(status().isForbidden());
        mvc.perform(get(API + "/creator/me").cookie(inAlpha.access()))
                .andExpect(status().isUnauthorized());

        // A creator token on the member API.
        mvc.perform(get(API + "/c/alpha/me").header("Authorization", "Bearer " + creatorA.access().getValue()))
                .andExpect(status().isForbidden());

        // Alpha's creator only sees Alpha's members.
        mvc.perform(get(API + "/creator/members").cookie(creatorA.access()))
                .andExpect(jsonPath("$.totalItems").value(1));

        mvc.perform(get(API + "/public/communities/nope"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("COMMUNITY_NOT_FOUND"));
        mvc.perform(json(post(API + "/c/nope/auth/otp/start"), "{\"email\":\"x@example.com\"}"))
                .andExpect(status().isNotFound());
    }

    // ---------------------------------------------------------------- codes and sessions

    @Test
    @DisplayName("Codes: wrong codes count down, then lock; resending has a cooldown")
    void otpLimits() throws Exception {
        String start = API + "/creator/auth/otp/start";
        String verify = API + "/creator/auth/otp/verify";
        mvc.perform(json(post(start), "{\"email\":\"limit@example.com\"}")).andExpect(status().isAccepted());

        mvc.perform(json(post(start), "{\"email\":\"limit@example.com\"}"))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.code").value("OTP_COOLDOWN"))
                .andExpect(header().exists("Retry-After"));

        String wrong = "{\"email\":\"limit@example.com\",\"code\":\"" + wrongCode("limit@example.com") + "\"}";
        for (int remaining = 4; remaining >= 1; remaining--) {
            mvc.perform(json(post(verify), wrong))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("OTP_INVALID"))
                    .andExpect(jsonPath("$.attemptsRemaining").value(remaining));
        }
        mvc.perform(json(post(verify), wrong)).andExpect(jsonPath("$.code").value("OTP_TOO_MANY_ATTEMPTS"));

        String right = "{\"email\":\"limit@example.com\",\"code\":\"" + otp.lastFor("limit@example.com").code() + "\"}";
        mvc.perform(json(post(verify), right))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("OTP_EXPIRED"));

        mvc.perform(json(post(verify), "{\"email\":\"not-an-email\",\"code\":\"12\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.errors.email").exists())
                .andExpect(jsonPath("$.errors.code").exists());
    }

    @Test
    @DisplayName("Sessions: refresh rotates tokens; replaying an old refresh token revokes every session")
    void refreshRotation() throws Exception {
        Session first = signIn(API + "/creator/auth", "rotate@example.com", AuthCookies.CREATOR_ACCESS, AuthCookies.CREATOR_REFRESH);

        MockHttpServletResponse rotated = mvc.perform(csrf(post(API + "/creator/auth/refresh")).cookie(first.refresh()))
                .andExpect(status().isNoContent()).andReturn().getResponse();
        Cookie newRefresh = rotated.getCookie(AuthCookies.CREATOR_REFRESH);
        Cookie newAccess = rotated.getCookie(AuthCookies.CREATOR_ACCESS);
        assertThat(newRefresh.getValue()).isNotEqualTo(first.refresh().getValue());
        mvc.perform(get(API + "/creator/me").cookie(newAccess)).andExpect(status().isOk());

        // Simulate theft: replay the old token after the grace window.
        jdbc.update("update refresh_token set revoked_at = now() - interval '5 minutes' where revoked_at is not null");
        mvc.perform(csrf(post(API + "/creator/auth/refresh")).cookie(first.refresh()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("REFRESH_INVALID"));
        mvc.perform(csrf(post(API + "/creator/auth/refresh")).cookie(newRefresh))
                .andExpect(status().isUnauthorized());

        // Logout revokes and clears cookies.
        Session second = signIn(API + "/creator/auth", "rotate@example.com", AuthCookies.CREATOR_ACCESS, AuthCookies.CREATOR_REFRESH);
        MockHttpServletResponse loggedOut = mvc.perform(csrf(post(API + "/creator/auth/logout")).cookie(second.refresh()))
                .andExpect(status().isNoContent()).andReturn().getResponse();
        assertThat(loggedOut.getCookie(AuthCookies.CREATOR_ACCESS).getMaxAge()).isZero();
        mvc.perform(csrf(post(API + "/creator/auth/refresh")).cookie(second.refresh()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Security: writes need the CSRF header, unknown paths are denied, stale cookies don't block sign-in")
    void securityGuards() throws Exception {
        mvc.perform(post(API + "/creator/auth/otp/start").contentType(MediaType.APPLICATION_JSON).content("{\"email\":\"x@example.com\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("CSRF_HEADER_MISSING"));

        mvc.perform(get(API + "/creator/me")).andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHENTICATED"));
        mvc.perform(get(API + "/creator/me").cookie(new Cookie(AuthCookies.CREATOR_ACCESS, "garbage")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("TOKEN_INVALID"));
        mvc.perform(get(API + "/somewhere-else")).andExpect(status().is4xxClientError());

        // A broken access cookie must not stop someone from signing in again.
        mvc.perform(json(post(API + "/creator/auth/otp/start"), "{\"email\":\"x@example.com\"}")
                        .cookie(new Cookie(AuthCookies.CREATOR_ACCESS, "garbage")))
                .andExpect(status().isAccepted());

        mvc.perform(get("/actuator/health")).andExpect(status().isOk());
        mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/v1/creator/auth/otp/start'].post.parameters[*].name", hasItem("X-Requested-With")));
    }

    // ---------------------------------------------------------------- helpers

    private record Session(Cookie access, Cookie refresh, MockHttpServletResponse body) {
    }

    /** Runs otp/start + otp/verify under {@code authBase} and returns the session cookies. */
    private Session signIn(String authBase, String email, String accessCookie, String refreshCookie) throws Exception {
        mvc.perform(json(post(authBase + "/otp/start"), "{\"email\":\"" + email + "\"}")).andExpect(status().isAccepted());
        String normalized = email.toLowerCase();
        String code = otp.lastFor(normalized).code();
        // Let the next signIn for the same email skip the resend cooldown.
        jdbc.update("update otp_challenge set created_at = created_at - interval '1 minute' where email = ?", normalized);
        MvcResult result = mvc.perform(json(post(authBase + "/otp/verify"), "{\"email\":\"" + email + "\",\"code\":\"" + code + "\"}"))
                .andExpect(status().isOk())
                .andReturn();
        MockHttpServletResponse response = result.getResponse();
        return new Session(response.getCookie(accessCookie), response.getCookie(refreshCookie), response);
    }

    private Session onboardedCreator(String email, String username, String communityName) throws Exception {
        Session creator = signIn(API + "/creator/auth", email, AuthCookies.CREATOR_ACCESS, AuthCookies.CREATOR_REFRESH);
        mvc.perform(json(put(API + "/creator/me/profile"), "{\"fullName\":\"Test Creator\",\"username\":\"" + username + "\"}")
                .cookie(creator.access())).andExpect(status().isOk());
        mvc.perform(json(post(API + "/creator/community"), "{\"name\":\"" + communityName + "\"}")
                .cookie(creator.access())).andExpect(status().isCreated());
        return creator;
    }

    private String wrongCode(String email) {
        String real = otp.lastFor(email).code();
        return real.equals("000000") ? "111111" : "000000";
    }

    private static MockHttpServletRequestBuilder csrf(MockHttpServletRequestBuilder builder) {
        return builder.header("X-Requested-With", "fetch");
    }

    private static MockHttpServletRequestBuilder json(MockHttpServletRequestBuilder builder, String body) {
        return csrf(builder).contentType(MediaType.APPLICATION_JSON).content(body);
    }
}
