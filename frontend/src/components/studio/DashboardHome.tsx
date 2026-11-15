"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { creatorApi } from "@/lib/api/creator";
import type { DashboardSummary } from "@/lib/api/types";
import { displayUrl, firstName, timeAgo } from "@/lib/format";
import { Avatar } from "@/components/ui/Avatar";
import { Badge } from "@/components/ui/Badge";
import { Button } from "@/components/ui/Button";
import { Icon, type IconName } from "@/components/ui/icons";
import { useCreator } from "./StudioShell";

/** Apps a creator can add to a product. Only Course is next on the roadmap (design slides 5-15). */
const APPS: Array<{ name: string; blurb: string; icon: IconName; status: "next" | "soon" }> = [
  { name: "Course", blurb: "Chapters, lessons, video and quizzes", icon: "book", status: "next" },
  { name: "Live classes", blurb: "Scheduled sessions with replays", icon: "video", status: "soon" },
  { name: "Community chat", blurb: "Channels for members and you", icon: "message", status: "soon" },
  { name: "Events", blurb: "Workshops and meetups with RSVPs", icon: "calendar", status: "soon" },
  { name: "Downloads", blurb: "Templates, PDFs and files", icon: "file", status: "soon" },
  { name: "AI assistant", blurb: "Answers members from your content", icon: "sparkle", status: "soon" },
];

export function DashboardHome() {
  const { me } = useCreator();
  const community = me.community!;
  const [summary, setSummary] = useState<DashboardSummary | null>(null);
  const [copied, setCopied] = useState(false);

  useEffect(() => {
    creatorApi.dashboardSummary().then(setSummary).catch(() => setSummary(null));
  }, []);

  async function copyLink() {
    await navigator.clipboard.writeText(community.siteUrl);
    setCopied(true);
    setTimeout(() => setCopied(false), 1800);
  }

  return (
    <div className="flex flex-col gap-8 animate-fade-up">
      <header>
        <h1 className="text-2xl font-semibold tracking-tight">Good to see you, {firstName(me.account.fullName)}</h1>
        <p className="mt-1 text-sm text-muted">Here&apos;s what&apos;s happening in {community.name}.</p>
      </header>

      <section className="flex flex-col gap-4 rounded-2xl border border-line bg-white p-5 sm:flex-row sm:items-center">
        <div className="min-w-0 flex-1">
          <p className="flex items-center gap-2 text-sm font-medium">
            <span className="size-2 rounded-full bg-success" aria-hidden="true" />
            Your community is live
          </p>
          <p className="mt-1 truncate font-mono text-[13px] text-muted">{displayUrl(community.siteUrl)}</p>
        </div>
        <div className="flex gap-2">
          <Button variant="secondary" size="sm" onClick={copyLink}>
            <Icon name={copied ? "check" : "copy"} /> {copied ? "Copied" : "Copy link"}
          </Button>
          <a
            href={community.siteUrl}
            target="_blank"
            rel="noreferrer"
            className="inline-flex h-8 items-center gap-2 rounded-full bg-ink px-3.5 text-[13px] font-medium text-white hover:bg-ink/85"
          >
            <Icon name="external" /> Open site
          </a>
        </div>
      </section>

      <section className="grid gap-4 sm:grid-cols-3" aria-label="Stats">
        <Stat label="Members" value={summary?.memberCount} />
        <Stat label="Joined this week" value={summary?.newMembersLast7Days} />
        <Stat label="Products" value={0} hint="Build your first one below" />
      </section>

      <section className="rounded-2xl border border-line bg-white p-5">
        <h2 className="text-base font-semibold">Let&apos;s build your first product 📚</h2>
        <p className="mt-1 text-[13px] text-muted">Pick an app to get started. Apps are what members get when they join.</p>
        <div className="mt-5 grid gap-3 sm:grid-cols-2 lg:grid-cols-3">
          {APPS.map((app) => (
            <div
              key={app.name}
              className={[
                "flex items-start gap-3 rounded-xl border p-4",
                app.status === "next" ? "border-ink/15 bg-white" : "border-line bg-surface/50",
              ].join(" ")}
            >
              <span className="flex size-9 shrink-0 items-center justify-center rounded-lg bg-surface text-ink-soft">
                <Icon name={app.icon} />
              </span>
              <div className="min-w-0 flex-1">
                <div className="flex items-center justify-between gap-2">
                  <p className="text-sm font-medium">{app.name}</p>
                  {app.status === "next" ? <Badge tone="green">Up next</Badge> : <Badge tone="violet">Coming soon</Badge>}
                </div>
                <p className="mt-0.5 text-xs text-muted">{app.blurb}</p>
              </div>
            </div>
          ))}
        </div>
      </section>

      <section className="rounded-2xl border border-line bg-white p-5">
        <div className="flex items-center justify-between">
          <h2 className="text-base font-semibold">Newest members</h2>
          <Link href="/dashboard/members" className="text-[13px] font-medium text-link hover:underline">
            View all
          </Link>
        </div>
        {summary && summary.recentMembers.length === 0 && (
          <p className="mt-4 rounded-xl bg-surface px-4 py-6 text-center text-sm text-muted">
            No members yet. Share your link and the first sign-ups will show up here.
          </p>
        )}
        <ul className="mt-3 divide-y divide-line">
          {summary?.recentMembers.map((member) => (
            <li key={member.id} className="flex items-center gap-3 py-3">
              <Avatar name={member.fullName} seed={member.email} />
              <div className="min-w-0 flex-1">
                <p className="truncate text-sm font-medium">{member.fullName ?? "New member"}</p>
                <p className="truncate text-xs text-muted">{member.email}</p>
              </div>
              <span className="text-xs text-muted">{timeAgo(member.joinedAt)}</span>
            </li>
          ))}
        </ul>
      </section>
    </div>
  );
}

function Stat({ label, value, hint }: { label: string; value: number | undefined; hint?: string }) {
  return (
    <div className="rounded-2xl border border-line bg-white p-5">
      <p className="text-[13px] text-muted">{label}</p>
      <p className="mt-2 text-3xl font-semibold tabular-nums tracking-tight">{value ?? "–"}</p>
      {hint && <p className="mt-1 text-xs text-muted">{hint}</p>}
    </div>
  );
}
