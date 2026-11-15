"use client";

import { createContext, use, type ReactNode } from "react";
import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import { creatorApi } from "@/lib/api/creator";
import type { CreatorMe } from "@/lib/api/types";
import { creatorHome } from "@/lib/routes";
import { useGatedSession } from "@/hooks/useGatedSession";
import { Avatar } from "@/components/ui/Avatar";
import { Badge } from "@/components/ui/Badge";
import { Button } from "@/components/ui/Button";
import { Icon, type IconName } from "@/components/ui/icons";
import { FullPageSpinner } from "@/components/ui/Spinner";
import { creatorNextStep } from "./session";
import { StudioMark } from "./StudioBrand";

interface CreatorContextValue {
  me: CreatorMe;
  setMe: (me: CreatorMe) => void;
}

const CreatorContext = createContext<CreatorContextValue | null>(null);

/** The signed-in, fully onboarded creator. Only usable inside /dashboard. */
export function useCreator() {
  const value = use(CreatorContext);
  if (!value) throw new Error("useCreator must be used inside <StudioShell>");
  return value;
}

const NAV: Array<{ href: string; label: string; icon: IconName; soon?: boolean }> = [
  { href: "/dashboard", label: "Home", icon: "home" },
  { href: "/dashboard/members", label: "Members", icon: "users" },
  { href: "/dashboard/branding", label: "Branding", icon: "palette" },
  { href: "#products", label: "Products", icon: "package", soon: true },
  { href: "#payments", label: "Payments", icon: "wallet", soon: true },
  { href: "#settings", label: "Settings", icon: "settings", soon: true },
];

export function StudioShell({ children }: { children: ReactNode }) {
  const { state, setData } = useGatedSession({
    load: creatorApi.me,
    signInPath: "/login",
    step: "DONE",
    nextStepOf: creatorNextStep,
    pathFor: creatorHome,
  });

  if (state.status === "error") {
    return (
      <div className="flex min-h-screen flex-col items-center justify-center gap-2 text-sm text-muted">
        <p>We couldn&apos;t load your studio.</p>
        <p className="text-xs">{state.message}</p>
      </div>
    );
  }
  if (state.status !== "ready") return <FullPageSpinner />;

  return (
    <CreatorContext value={{ me: state.data, setMe: setData }}>
      <div className="min-h-screen bg-surface/60 md:flex">
        <Sidebar me={state.data} />
        <main className="min-w-0 flex-1 px-5 py-8 sm:px-8 lg:px-12">
          <div className="mx-auto max-w-5xl">{children}</div>
        </main>
      </div>
    </CreatorContext>
  );
}

function Sidebar({ me }: { me: CreatorMe }) {
  const pathname = usePathname();
  const router = useRouter();

  async function signOut() {
    await creatorApi.logout().catch(() => undefined);
    router.replace("/login");
  }

  return (
    <aside className="border-b border-line bg-white md:sticky md:top-0 md:flex md:h-screen md:w-60 md:shrink-0 md:flex-col md:border-r md:border-b-0">
      <div className="flex items-center justify-between px-5 py-4 md:block">
        <StudioMark />
        <p className="hidden truncate pt-3 text-xs text-muted md:block">{me.community?.name}</p>
        <Button variant="ghost" size="sm" onClick={signOut} className="md:hidden">
          <Icon name="logout" /> Sign out
        </Button>
      </div>
      <nav className="flex gap-1 overflow-x-auto px-3 pb-3 md:flex-1 md:flex-col md:pb-0" aria-label="Studio">
        {NAV.map((item) => {
          const active = item.href === "/dashboard" ? pathname === "/dashboard" : pathname.startsWith(item.href);
          if (item.soon) {
            return (
              <span key={item.label} className="flex shrink-0 cursor-default items-center gap-2.5 rounded-lg px-3 py-2 text-sm text-zinc-400" title="Coming soon">
                <Icon name={item.icon} />
                {item.label}
                <span className="ml-auto hidden md:inline">
                  <Badge>Soon</Badge>
                </span>
              </span>
            );
          }
          return (
            <Link
              key={item.label}
              href={item.href}
              aria-current={active ? "page" : undefined}
              className={[
                "flex shrink-0 items-center gap-2.5 rounded-lg px-3 py-2 text-sm transition",
                active ? "bg-surface font-medium text-ink" : "text-ink-soft hover:bg-surface",
              ].join(" ")}
            >
              <Icon name={item.icon} />
              {item.label}
            </Link>
          );
        })}
      </nav>
      <div className="hidden items-center gap-3 border-t border-line px-4 py-4 md:flex">
        <Avatar name={me.account.fullName} seed={me.account.email} />
        <div className="min-w-0 flex-1">
          <p className="truncate text-sm font-medium">{me.account.fullName}</p>
          <p className="truncate text-xs text-muted">{me.account.email}</p>
        </div>
        <Button variant="ghost" size="sm" onClick={signOut} aria-label="Sign out" title="Sign out" className="px-2">
          <Icon name="logout" />
        </Button>
      </div>
    </aside>
  );
}
