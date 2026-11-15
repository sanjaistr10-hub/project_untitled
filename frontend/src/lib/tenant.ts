/**
 * Decides which "app" a request belongs to from its Host header:
 *   ROOT_DOMAIN            -> creator studio            (localhost:3100, app.example.com)
 *   {slug}.ROOT_DOMAIN     -> that community's site     (yoga-den.localhost:3100)
 *   anything else          -> a custom domain, looked up through the API
 */
export type Tenant =
  | { kind: "studio" }
  | { kind: "site"; slug: string }
  | { kind: "custom"; host: string };

const STUDIO_SUBDOMAINS = new Set(["app", "www"]);
const SLUG = /^[a-z0-9](?:[a-z0-9-]*[a-z0-9])?$/;
const IP_HOST = /^(\d{1,3}\.){3}\d{1,3}(:\d+)?$|^\[[0-9a-f:]+\](:\d+)?$/;

export function rootDomain(): string {
  return (process.env.ROOT_DOMAIN ?? "localhost:3100").toLowerCase();
}

export function resolveTenant(rawHost: string | null, root = rootDomain()): Tenant {
  const host = (rawHost ?? "").toLowerCase();
  if (!host || host === root || IP_HOST.test(host)) {
    return { kind: "studio" };
  }
  if (host.endsWith(`.${root}`)) {
    const sub = host.slice(0, -(root.length + 1));
    if (STUDIO_SUBDOMAINS.has(sub)) return { kind: "studio" };
    if (SLUG.test(sub)) return { kind: "site", slug: sub };
  }
  return { kind: "custom", host: host.replace(/:\d+$/, "") };
}
