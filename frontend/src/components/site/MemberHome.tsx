"use client";

import { useCallback } from "react";
import { useRouter } from "next/navigation";
import type { MemberMe } from "@/lib/api/types";
import { firstName, formatDate } from "@/lib/format";
import { memberHome } from "@/lib/routes";
import { useGatedSession } from "@/hooks/useGatedSession";
import { Avatar } from "@/components/ui/Avatar";
import { Button } from "@/components/ui/Button";
import { Icon } from "@/components/ui/icons";
import { FullPageSpinner } from "@/components/ui/Spinner";
import { CommunityLogo } from "./CommunityLogo";
import { useCommunity } from "./CommunityProvider";

const memberNextStep = (me: MemberMe) => me.nextStep;

/** The member's dashboard on the creator's white-label site. */
export function MemberHome() {
  const router = useRouter();
  const { community, api } = useCommunity();
  const load = useCallback(() => api.me(), [api]);
  const { state } = useGatedSession({ load, signInPath: "/login", step: "DONE", nextStepOf: memberNextStep, pathFor: memberHome });
  if (state.status !== "ready") return <FullPageSpinner />;
  const member = state.data.account;

  async function signOut() {
    await api.logout().catch(() => undefined);
    router.replace("/login");
  }

  return (
    <div className="min-h-screen bg-surface/60">
      <header className="border-b border-line bg-white">
        <div className="mx-auto flex h-16 max-w-5xl items-center justify-between px-5">
          <span className="inline-flex items-center gap-2.5 text-sm font-semibold tracking-tight">
            <CommunityLogo name={community.name} logoUrl={community.logoUrl} />
            {community.name}
          </span>
          <div className="flex items-center gap-2">
            <Avatar name={member.fullName} seed={member.email} size="sm" />
            <span className="hidden text-sm sm:inline">{member.fullName}</span>
            <Button variant="ghost" size="sm" onClick={signOut}>
              <Icon name="logout" /> <span className="hidden sm:inline">Sign out</span>
            </Button>
          </div>
        </div>
      </header>

      <main className="mx-auto flex max-w-5xl flex-col gap-6 px-5 py-10 animate-fade-up">
        <section className="relative overflow-hidden rounded-2xl bg-accent p-8 text-accent-foreground">
          <p className="text-sm opacity-80">{community.name}</p>
          <h1 className="mt-1 text-3xl font-semibold tracking-tight">Welcome, {firstName(member.fullName)} 👋</h1>
          {(community.tagline || community.description) && (
            <p className="mt-3 max-w-2xl text-sm leading-relaxed opacity-85">{community.description ?? community.tagline}</p>
          )}
        </section>

        <div className="grid gap-6 lg:grid-cols-[1fr_300px]">
          <section className="rounded-2xl border border-line bg-white p-6">
            <h2 className="text-base font-semibold">Your products</h2>
            <div className="mt-4 flex flex-col items-center rounded-xl bg-surface px-6 py-12 text-center">
              <span className="flex size-10 items-center justify-center rounded-full bg-white text-muted shadow-sm">
                <Icon name="package" />
              </span>
              <p className="mt-3 text-sm font-medium">Nothing here yet</p>
              <p className="mt-1 max-w-xs text-[13px] text-muted">
                {community.name} is getting things ready. Courses, live sessions and more will show up here.
              </p>
            </div>
          </section>

          <section className="rounded-2xl border border-line bg-white p-6">
            <h2 className="text-base font-semibold">Your profile</h2>
            <div className="mt-4 flex items-center gap-3">
              <Avatar name={member.fullName} seed={member.email} size="lg" />
              <div className="min-w-0">
                <p className="truncate font-medium">{member.fullName}</p>
                <p className="truncate text-sm text-muted">@{member.username}</p>
              </div>
            </div>
            <dl className="mt-5 flex flex-col gap-3 text-sm">
              <div className="flex justify-between gap-3">
                <dt className="text-muted">Email</dt>
                <dd className="truncate">{member.email}</dd>
              </div>
              <div className="flex justify-between gap-3">
                <dt className="text-muted">Member since</dt>
                <dd>{formatDate(member.joinedAt)}</dd>
              </div>
            </dl>
          </section>
        </div>
      </main>
    </div>
  );
}
