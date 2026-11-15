// Mirrors the backend DTOs (backend/src/main/java/com/projectuntitled/**/…Dto.java).
// Keep these in sync when you change an API response.

export type OnboardingStep = "PROFILE" | "COMMUNITY" | "DONE";

export interface OtpStarted {
  email: string;
  expiresInSeconds: number;
  resendAfterSeconds: number;
}

export interface AuthResult<TAccount> {
  account: TAccount;
  newAccount: boolean;
  nextStep: OnboardingStep;
}

export interface Creator {
  id: string;
  email: string;
  fullName: string | null;
  username: string | null;
  avatarUrl: string | null;
  createdAt: string;
}

export interface Community {
  id: string;
  name: string;
  slug: string;
  tagline: string | null;
  description: string | null;
  logoUrl: string | null;
  accentColor: string;
  siteUrl: string;
  createdAt: string;
}

/** What a community's visitors can see (branding for its white-label site). */
export interface PublicCommunity {
  name: string;
  slug: string;
  tagline: string | null;
  description: string | null;
  logoUrl: string | null;
  accentColor: string;
}

export interface CreatorMe {
  account: Creator;
  community: Community | null;
  nextStep: OnboardingStep;
}

export interface Member {
  id: string;
  email: string;
  fullName: string | null;
  username: string | null;
  avatarUrl: string | null;
  joinedAt: string;
  lastLoginAt: string | null;
}

export interface MemberMe {
  account: Member;
  community: PublicCommunity;
  nextStep: OnboardingStep;
}

export interface ProfileInput {
  fullName: string;
  username: string;
}

export interface UsernameAvailability {
  username: string;
  available: boolean;
  reason: string | null;
}

export interface SlugAvailability {
  slug: string;
  available: boolean;
  reason: string | null;
  suggestion: string | null;
  siteUrl: string | null;
}

export interface BrandingInput {
  name?: string;
  tagline?: string;
  description?: string;
  accentColor?: string;
  logoUrl?: string;
}

export interface DashboardSummary {
  memberCount: number;
  newMembersLast7Days: number;
  recentMembers: Member[];
}

export interface PageResponse<T> {
  items: T[];
  page: number;
  size: number;
  totalItems: number;
  totalPages: number;
}
