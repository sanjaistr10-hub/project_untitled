import { StudioShell } from "@/components/studio/StudioShell";

// The studio checks the session in the browser (StudioShell) and renders pages once it's loaded,
// so the server can't prerender page content for instant navigation. Opt out of that validation.
// To make it instant later, read the session on the server (see Next's "Authentication with Cache
// Components" guide in node_modules/next/dist/docs).
export const instant = false;

export default function DashboardLayout({ children }: LayoutProps<"/dashboard">) {
  return <StudioShell>{children}</StudioShell>;
}
