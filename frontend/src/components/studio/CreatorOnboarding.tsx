"use client";

import { useEffect, useState, type FormEvent } from "react";
import { useRouter } from "next/navigation";
import { ApiError } from "@/lib/api/client";
import { creatorApi } from "@/lib/api/creator";
import type { SlugAvailability } from "@/lib/api/types";
import { displayUrl } from "@/lib/format";
import { creatorHome } from "@/lib/routes";
import { AuthHeading, AuthLayout } from "@/components/auth/AuthLayout";
import { ProfileForm } from "@/components/auth/ProfileForm";
import { useGatedSession } from "@/hooks/useGatedSession";
import { Button } from "@/components/ui/Button";
import { FullPageSpinner } from "@/components/ui/Spinner";
import { TextField } from "@/components/ui/TextField";
import { Icon } from "@/components/ui/icons";
import { creatorNextStep } from "./session";
import { StudioHero, StudioMark } from "./StudioBrand";

/** Onboarding step 1 (design slide 3). */
export function CreatorProfileStep() {
  const router = useRouter();
  const { state } = useGatedSession({
    load: creatorApi.me,
    signInPath: "/login",
    step: "PROFILE",
    nextStepOf: creatorNextStep,
    pathFor: creatorHome,
  });
  if (state.status !== "ready") return <FullPageSpinner />;

  return (
    <AuthLayout mark={<StudioMark />} aside={<StudioHero />}>
      <ProfileForm
        initial={{ fullName: state.data.account.fullName ?? "", username: state.data.account.username ?? "" }}
        checkUsername={creatorApi.usernameAvailability}
        save={creatorApi.updateProfile}
        onSaved={() => router.replace("/onboarding/community")}
      />
    </AuthLayout>
  );
}

/** Onboarding step 2 (design slide 4): name the community, which also picks its address. */
export function CreatorCommunityStep() {
  const router = useRouter();
  const { state } = useGatedSession({
    load: creatorApi.me,
    signInPath: "/login",
    step: "COMMUNITY",
    nextStepOf: creatorNextStep,
    pathFor: creatorHome,
  });

  const [name, setName] = useState("");
  const [customSlug, setCustomSlug] = useState<string | null>(null);
  const [preview, setPreview] = useState<SlugAvailability | null>(null);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // Live preview of the community's address as the name (or custom slug) is typed.
  useEffect(() => {
    const query = customSlug !== null ? { slug: customSlug.trim() } : { name: name.trim() };
    if (!(query.slug ?? query.name)) return;
    let cancelled = false;
    const timer = setTimeout(() => {
      creatorApi
        .slugAvailability(query)
        .then((result) => !cancelled && setPreview(result))
        .catch(() => !cancelled && setPreview(null));
    }, 300);
    return () => {
      cancelled = true;
      clearTimeout(timer);
    };
  }, [name, customSlug]);

  if (state.status !== "ready") return <FullPageSpinner />;

  const hasInput = customSlug !== null ? customSlug.trim() !== "" : name.trim() !== "";
  const shownPreview = hasInput ? preview : null;

  async function onSubmit(event: FormEvent) {
    event.preventDefault();
    setBusy(true);
    setError(null);
    try {
      await creatorApi.createCommunity(name.trim(), customSlug?.trim() || undefined);
      router.replace("/dashboard");
    } catch (e) {
      setError(e instanceof ApiError ? (e.fieldErrors.name ?? e.message) : "We couldn't reach the server.");
      setBusy(false);
    }
  }

  return (
    <AuthLayout mark={<StudioMark />} aside={<StudioHero />}>
      <AuthHeading title="What's your community called? 🌍" subtitle="Give a name to your community" />
      <form onSubmit={onSubmit} className="flex flex-col gap-4" noValidate>
        <TextField
          label="Community name"
          hideLabel
          placeholder="Community name"
          value={name}
          maxLength={80}
          onChange={(event) => setName(event.target.value)}
          error={error}
          autoFocus
          required
        />
        {customSlug !== null && (
          <TextField
            label="Community address"
            placeholder="your-community"
            value={customSlug}
            maxLength={40}
            onChange={(event) => setCustomSlug(event.target.value.toLowerCase())}
            hint="Lowercase letters, numbers and hyphens."
          />
        )}
        {shownPreview && (
          <div className="flex items-start gap-2 rounded-lg bg-surface px-3 py-2.5 text-[13px]">
            <Icon name="globe" className="mt-0.5 size-4 shrink-0 text-muted" />
            <div className="min-w-0 flex-1">
              {shownPreview.available && shownPreview.siteUrl ? (
                <p className="truncate">
                  <span className="text-muted">Your site: </span>
                  <span className="font-medium">{displayUrl(shownPreview.siteUrl)}</span>
                </p>
              ) : (
                <p className="text-danger">
                  {shownPreview.reason}
                  {shownPreview.suggestion && (
                    <>
                      {" "}
                      <button type="button" className="font-medium text-link hover:underline" onClick={() => setCustomSlug(shownPreview.suggestion)}>
                        Use {shownPreview.suggestion}
                      </button>
                    </>
                  )}
                </p>
              )}
            </div>
            {customSlug === null && (
              <button
                type="button"
                className="shrink-0 font-medium text-link hover:underline"
                onClick={() => setCustomSlug(shownPreview.slug)}
              >
                Edit
              </button>
            )}
          </div>
        )}
        <Button type="submit" block loading={busy} disabled={!name.trim() || (shownPreview !== null && !shownPreview.available)}>
          Continue
        </Button>
      </form>
    </AuthLayout>
  );
}
