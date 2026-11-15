import { api, type Scope } from "./client";
import type {
  AuthResult,
  BrandingInput,
  Community,
  Creator,
  CreatorMe,
  DashboardSummary,
  Member,
  OtpStarted,
  PageResponse,
  ProfileInput,
  SlugAvailability,
  UsernameAvailability,
} from "./types";

const scope: Scope = { kind: "creator" };

/** Creator studio endpoints: /api/v1/creator/** */
export const creatorApi = {
  startOtp: (email: string) => api<OtpStarted>(null, "/creator/auth/otp/start", { method: "POST", body: { email } }),
  verifyOtp: (email: string, code: string) =>
    api<AuthResult<Creator>>(null, "/creator/auth/otp/verify", { method: "POST", body: { email, code } }),
  logout: () => api<void>(null, "/creator/auth/logout", { method: "POST" }),

  me: () => api<CreatorMe>(scope, "/creator/me"),
  updateProfile: (input: ProfileInput) => api<Creator>(scope, "/creator/me/profile", { method: "PUT", body: input }),
  usernameAvailability: (username: string) =>
    api<UsernameAvailability>(scope, "/creator/username-availability", { query: { username } }),

  createCommunity: (name: string, slug?: string) =>
    api<Community>(scope, "/creator/community", { method: "POST", body: { name, slug } }),
  slugAvailability: (query: { slug?: string; name?: string }) =>
    api<SlugAvailability>(scope, "/creator/community/slug-availability", { query }),
  updateBranding: (input: BrandingInput) =>
    api<Community>(scope, "/creator/community", { method: "PATCH", body: input }),

  dashboardSummary: () => api<DashboardSummary>(scope, "/creator/dashboard/summary"),
  members: (query: { q?: string; page?: number; size?: number }) =>
    api<PageResponse<Member>>(scope, "/creator/members", { query }),
};
