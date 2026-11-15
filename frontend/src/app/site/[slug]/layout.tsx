import { Suspense } from "react";
import type { Metadata } from "next";
import { notFound } from "next/navigation";
import { getPublicCommunity } from "@/lib/api/server";
import { CommunityProvider } from "@/components/site/CommunityProvider";
import { FullPageSpinner } from "@/components/ui/Spinner";

/**
 * A community's white-label site. Visitors reach it on {slug}.ROOT_DOMAIN (or a custom domain);
 * src/proxy.ts rewrites those hosts to /site/{slug}/... so they never see this path.
 * Everything here uses the community's name, logo and colour, never the platform's.
 * Every page under here must render <RequestTimeMarker /> (see that component for why).
 */
export async function generateMetadata({ params }: LayoutProps<"/site/[slug]">): Promise<Metadata> {
  const { slug } = await params;
  const community = await getPublicCommunity(slug);
  if (!community) return { title: "Not found" };
  return {
    title: { default: community.name, template: `%s · ${community.name}` },
    description: community.tagline ?? undefined,
    icons: community.logoUrl ? { icon: community.logoUrl } : undefined,
  };
}

export default function SiteLayout({ params, children }: LayoutProps<"/site/[slug]">) {
  // The slug is only known per request, so the branded tree streams in behind this boundary.
  return (
    <Suspense fallback={<FullPageSpinner />}>
      <CommunityBoundary params={params}>{children}</CommunityBoundary>
    </Suspense>
  );
}

async function CommunityBoundary({ params, children }: { params: Promise<{ slug: string }>; children: React.ReactNode }) {
  const { slug } = await params;
  const community = await getPublicCommunity(slug);
  if (!community) notFound();
  return <CommunityProvider community={community}>{children}</CommunityProvider>;
}
