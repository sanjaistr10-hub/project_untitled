import { cacheLife, cacheTag } from "next/cache";
import type { PublicCommunity } from "./types";

// Server-side calls go straight to the backend (no browser, no cookies involved).
const BACKEND_URL = process.env.BACKEND_URL ?? "http://localhost:8081";

/**
 * Branding for a member site, read on the server so the page title and colours are right on first
 * paint. Cached for at most 5 seconds (expire), so a creator who saves branding and opens their
 * site sees the change on the first load. Tagged so a future webhook from the API can call
 * revalidateTag(`community:${slug}`) and the expiry can be raised.
 */
export async function getPublicCommunity(slug: string): Promise<PublicCommunity | null> {
  "use cache";
  cacheLife({ stale: 30, revalidate: 1, expire: 5 });
  cacheTag(`community:${slug}`);

  const response = await fetch(`${BACKEND_URL}/api/v1/public/communities/${encodeURIComponent(slug)}`, {
    headers: { Accept: "application/json" },
  });
  if (response.status === 404) return null;
  if (!response.ok) throw new Error(`Couldn't load community "${slug}" (HTTP ${response.status})`);
  return (await response.json()) as PublicCommunity;
}
