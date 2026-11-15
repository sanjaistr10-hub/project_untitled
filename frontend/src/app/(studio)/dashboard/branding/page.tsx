import type { Metadata } from "next";
import { BrandingView } from "@/components/studio/BrandingView";

// Rendered after the browser loads the session; see dashboard/layout.tsx.
export const instant = false;

export const metadata: Metadata = { title: "Branding" };

export default function BrandingPage() {
  return <BrandingView />;
}
