import { NextResponse, type NextRequest } from "next/server";
import { resolveTenant } from "@/lib/tenant";

/**
 * White-label routing. The same Next.js app serves:
 *   localhost:3100/...            -> creator studio  (app/(studio)/...)
 *   {slug}.localhost:3100/...     -> member site     (app/site/[slug]/...)
 *   learn.creator.com/...         -> member site, after looking the domain up in the API
 * Visitors never see /site/{slug} in the URL; the studio host refuses /site/* paths, and unknown
 * communities get a real 404 before any page renders.
 */
export async function proxy(request: NextRequest) {
  const { pathname } = request.nextUrl;
  const tenant = resolveTenant(request.headers.get("host"));
  const isSitePath = pathname === "/site" || pathname.startsWith("/site/");

  if (tenant.kind === "studio") {
    return isSitePath ? notFound(request) : NextResponse.next();
  }

  const slug = tenant.kind === "site" ? await existingSlug(tenant.slug) : await slugForCustomDomain(tenant.host);
  if (!slug || isSitePath) {
    return notFound(request);
  }
  const url = request.nextUrl.clone();
  url.pathname = `/site/${slug}${pathname === "/" ? "" : pathname}`;
  return NextResponse.rewrite(url);
}

function notFound(request: NextRequest) {
  return NextResponse.rewrite(new URL("/404", request.url), { status: 404 });
}

// ---- Lookups against the public API, cached briefly per server process (best effort). ----

const BACKEND_URL = process.env.BACKEND_URL ?? "http://localhost:8081";
const FOUND_TTL_MS = 60_000;
const MISSING_TTL_MS = 5_000; // short, so a just-created community works almost immediately
const lookups = new Map<string, { slug: string | null; expires: number }>();

async function cachedLookup(key: string, path: string, pick: (body: unknown) => string): Promise<string | null> {
  const hit = lookups.get(key);
  if (hit && hit.expires > Date.now()) return hit.slug;
  let slug: string | null = null;
  try {
    const response = await fetch(`${BACKEND_URL}${path}`, { cache: "no-store", headers: { Accept: "application/json" } });
    if (response.ok) slug = pick(await response.json());
  } catch {
    slug = null;
  }
  lookups.set(key, { slug, expires: Date.now() + (slug ? FOUND_TTL_MS : MISSING_TTL_MS) });
  return slug;
}

function existingSlug(slug: string) {
  return cachedLookup(`slug:${slug}`, `/api/v1/public/communities/${encodeURIComponent(slug)}`, (body) => (body as { slug: string }).slug);
}

function slugForCustomDomain(host: string) {
  return cachedLookup(`host:${host}`, `/api/v1/public/domains/resolve?host=${encodeURIComponent(host)}`, (body) => (body as { slug: string }).slug);
}

export const config = {
  // Everything except the API proxy, Next internals and static files.
  matcher: ["/((?!api/|_next/|favicon.ico|brand/|images/|.*\\.(?:svg|png|jpg|jpeg|gif|webp|ico|txt|xml)$).*)"],
};
