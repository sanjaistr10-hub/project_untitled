"use client";

import { createContext, use, useMemo, type CSSProperties, type ReactNode } from "react";
import { memberApi, type MemberApi } from "@/lib/api/member";
import type { PublicCommunity } from "@/lib/api/types";
import { contrastText } from "@/lib/format";

interface CommunityContextValue {
  community: PublicCommunity;
  api: MemberApi;
}

const CommunityContext = createContext<CommunityContextValue | null>(null);

/** The community whose white-label site is being viewed, plus an API client scoped to it. */
export function useCommunity() {
  const value = use(CommunityContext);
  if (!value) throw new Error("useCommunity must be used inside <CommunityProvider>");
  return value;
}

/** Applies the community's accent colour to everything below it (bg-accent, text-accent...). */
export function CommunityProvider({ community, children }: { community: PublicCommunity; children: ReactNode }) {
  const value = useMemo(() => ({ community, api: memberApi(community.slug) }), [community]);
  const style = {
    "--accent": community.accentColor,
    "--accent-foreground": contrastText(community.accentColor),
  } as CSSProperties;
  return (
    <CommunityContext value={value}>
      <div style={style} className="min-h-screen">
        {children}
      </div>
    </CommunityContext>
  );
}
