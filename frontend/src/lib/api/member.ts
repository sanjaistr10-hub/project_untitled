import { api, type Scope } from "./client";
import type { AuthResult, Member, MemberMe, OtpStarted, ProfileInput, UsernameAvailability } from "./types";

/** Member endpoints for one community: /api/v1/c/{slug}/** */
export function memberApi(slug: string) {
  const scope: Scope = { kind: "member", slug };
  const base = `/c/${encodeURIComponent(slug)}`;
  return {
    startOtp: (email: string) => api<OtpStarted>(null, `${base}/auth/otp/start`, { method: "POST", body: { email } }),
    verifyOtp: (email: string, code: string) =>
      api<AuthResult<Member>>(null, `${base}/auth/otp/verify`, { method: "POST", body: { email, code } }),
    logout: () => api<void>(null, `${base}/auth/logout`, { method: "POST" }),

    me: () => api<MemberMe>(scope, `${base}/me`),
    updateProfile: (input: ProfileInput) => api<Member>(scope, `${base}/me/profile`, { method: "PUT", body: input }),
    usernameAvailability: (username: string) =>
      api<UsernameAvailability>(scope, `${base}/username-availability`, { query: { username } }),
  };
}

export type MemberApi = ReturnType<typeof memberApi>;
