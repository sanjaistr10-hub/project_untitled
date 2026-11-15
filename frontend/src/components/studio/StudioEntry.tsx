"use client";

import { useEffect } from "react";
import { useRouter } from "next/navigation";
import { creatorApi } from "@/lib/api/creator";
import { creatorHome } from "@/lib/routes";
import { FullPageSpinner } from "@/components/ui/Spinner";

/** "/" in the studio: send the creator to sign-in, onboarding or the dashboard. */
export function StudioEntry() {
  const router = useRouter();
  useEffect(() => {
    creatorApi
      .me()
      .then((me) => router.replace(creatorHome(me.nextStep)))
      .catch(() => router.replace("/login"));
  }, [router]);
  return <FullPageSpinner />;
}
