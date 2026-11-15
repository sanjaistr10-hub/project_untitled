"use client";

import { useEffect } from "react";
import { useRouter } from "next/navigation";
import { creatorApi } from "@/lib/api/creator";
import { creatorHome } from "@/lib/routes";
import { AuthLayout } from "@/components/auth/AuthLayout";
import { EmailCodeSignIn } from "@/components/auth/EmailCodeSignIn";
import { StudioHero, StudioMark } from "./StudioBrand";

export function CreatorLogin() {
  const router = useRouter();

  // Already signed in? Skip straight to where they left off.
  useEffect(() => {
    creatorApi
      .me()
      .then((me) => router.replace(creatorHome(me.nextStep)))
      .catch(() => undefined);
  }, [router]);

  return (
    <AuthLayout mark={<StudioMark />} aside={<StudioHero />}>
      <EmailCodeSignIn
        title="Your Community. You're in control."
        subtitle="Build engaging communities and monetise them, on your own brand"
        startOtp={creatorApi.startOtp}
        verifyOtp={creatorApi.verifyOtp}
        onSignedIn={(result) => router.replace(creatorHome(result.nextStep))}
        showSocial
      />
    </AuthLayout>
  );
}
