import type { OnboardingStep } from "./api/types";

/** Where a signed-in creator belongs, given the API's nextStep. */
export function creatorHome(step: OnboardingStep): string {
  if (step === "PROFILE") return "/onboarding/profile";
  if (step === "COMMUNITY") return "/onboarding/community";
  return "/dashboard";
}

/**
 * Where a signed-in member belongs. Member-site links are host-relative ("/home"): proxy.ts maps
 * them to /site/{slug}/home based on the domain, so the slug never appears in the URL.
 */
export function memberHome(step: OnboardingStep): string {
  return step === "PROFILE" ? "/onboarding" : "/home";
}
