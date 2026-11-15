"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { ApiError } from "@/lib/api/client";
import type { OnboardingStep } from "@/lib/api/types";

export type SessionState<T> =
  | { status: "loading" }
  | { status: "ready"; data: T }
  | { status: "error"; message: string };

interface GateOptions<T> {
  /** Loads the signed-in account, e.g. creatorApi.me. Must be stable (module-level or memoised). */
  load: () => Promise<T>;
  /** Where to go when signed out. */
  signInPath: string;
  /** The onboarding step this page is for. Users on a different step are sent to their own page. */
  step: OnboardingStep;
  nextStepOf: (data: T) => OnboardingStep;
  pathFor: (step: OnboardingStep) => string;
}

/**
 * Client-side route guard: loads the session, sends signed-out users to sign in and users on another
 * onboarding step to the right page. The API is still the real security boundary.
 */
export function useGatedSession<T>({ load, signInPath, step, nextStepOf, pathFor }: GateOptions<T>) {
  const router = useRouter();
  const [state, setState] = useState<SessionState<T>>({ status: "loading" });

  useEffect(() => {
    let cancelled = false;
    load().then(
      (data) => {
        if (cancelled) return;
        const actual = nextStepOf(data);
        if (actual !== step) router.replace(pathFor(actual));
        else setState({ status: "ready", data });
      },
      (error: unknown) => {
        if (cancelled) return;
        if (error instanceof ApiError && error.isUnauthorized) router.replace(signInPath);
        else setState({ status: "error", message: error instanceof Error ? error.message : "Something went wrong." });
      },
    );
    return () => {
      cancelled = true;
    };
  }, [load, nextStepOf, pathFor, router, signInPath, step]);

  return { state, setData: (data: T) => setState({ status: "ready", data }) };
}
