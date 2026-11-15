"use client";

import { useCallback, useEffect } from "react";
import { useRouter } from "next/navigation";
import type { MemberMe } from "@/lib/api/types";
import { memberHome } from "@/lib/routes";
import { AuthLayout } from "@/components/auth/AuthLayout";
import { EmailCodeSignIn } from "@/components/auth/EmailCodeSignIn";
import { ProfileForm } from "@/components/auth/ProfileForm";
import { useGatedSession } from "@/hooks/useGatedSession";
import { FullPageSpinner } from "@/components/ui/Spinner";
import { CommunityLogo } from "./CommunityLogo";
import { useCommunity } from "./CommunityProvider";

const memberNextStep = (me: MemberMe) => me.nextStep;

function CommunityMark() {
  const { community } = useCommunity();
  return (
    <span className="inline-flex items-center gap-2 text-sm font-semibold tracking-tight">
      <CommunityLogo name={community.name} logoUrl={community.logoUrl} />
      {community.name}
    </span>
  );
}

/** Right-hand panel of the member sign-in: the community's own colours and words. */
function CommunityAside() {
  const { community } = useCommunity();
  return (
    <div className="absolute inset-0 flex flex-col justify-end bg-accent p-10 text-accent-foreground">
      <div
        className="pointer-events-none absolute inset-0 opacity-25"
        style={{
          backgroundImage:
            "radial-gradient(circle at 20% 20%, rgba(255,255,255,.55) 0, transparent 35%), radial-gradient(circle at 85% 30%, rgba(255,255,255,.35) 0, transparent 30%), radial-gradient(circle at 60% 90%, rgba(0,0,0,.35) 0, transparent 40%)",
        }}
        aria-hidden="true"
      />
      <div className="relative max-w-md">
        <p className="text-3xl font-semibold leading-tight tracking-tight">{community.tagline ?? community.name}</p>
        {community.description && <p className="mt-4 text-sm leading-relaxed opacity-80">{community.description}</p>}
      </div>
    </div>
  );
}

export function MemberLogin() {
  const router = useRouter();
  const { community, api } = useCommunity();

  useEffect(() => {
    api
      .me()
      .then((me) => router.replace(memberHome(me.nextStep)))
      .catch(() => undefined);
  }, [api, router]);

  return (
    <AuthLayout mark={<CommunityMark />} aside={<CommunityAside />}>
      <EmailCodeSignIn
        title={`Welcome to ${community.name}`}
        subtitle="Sign in, or create your account in seconds"
        startOtp={api.startOtp}
        verifyOtp={api.verifyOtp}
        onSignedIn={(result) => router.replace(memberHome(result.nextStep))}
        buttonVariant="accent"
      />
    </AuthLayout>
  );
}

export function MemberOnboarding() {
  const router = useRouter();
  const { api } = useCommunity();
  const load = useCallback(() => api.me(), [api]);
  const { state } = useGatedSession({ load, signInPath: "/login", step: "PROFILE", nextStepOf: memberNextStep, pathFor: memberHome });
  if (state.status !== "ready") return <FullPageSpinner />;

  return (
    <AuthLayout mark={<CommunityMark />} aside={<CommunityAside />}>
      <ProfileForm
        subtitle="How should other members know you?"
        checkUsername={api.usernameAvailability}
        save={api.updateProfile}
        onSaved={() => router.replace("/home")}
        buttonVariant="accent"
      />
    </AuthLayout>
  );
}

/** "/" on a member site. */
export function SiteEntry() {
  const router = useRouter();
  const { api } = useCommunity();
  useEffect(() => {
    api
      .me()
      .then((me) => router.replace(memberHome(me.nextStep)))
      .catch(() => router.replace("/login"));
  }, [api, router]);
  return <FullPageSpinner />;
}
