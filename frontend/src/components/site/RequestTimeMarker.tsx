import { Suspense } from "react";
import { connection } from "next/server";

/**
 * Renders nothing. Every member-site page includes it to tell Next.js the page is intentionally
 * rendered per request (the community comes from the visitor's domain), so the per-community
 * <title> from generateMetadata in app/site/[slug]/layout.tsx isn't flagged as "blocking".
 * Add <RequestTimeMarker /> to any new page under app/site/[slug]/.
 */
export function RequestTimeMarker() {
  return (
    <Suspense>
      <Connection />
    </Suspense>
  );
}

async function Connection() {
  await connection();
  return null;
}
