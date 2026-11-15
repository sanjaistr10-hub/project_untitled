import type { Metadata } from "next";
import { DashboardHome } from "@/components/studio/DashboardHome";

// Rendered after the browser loads the session; see dashboard/layout.tsx.
export const instant = false;

export const metadata: Metadata = { title: "Home" };

export default function DashboardPage() {
  return <DashboardHome />;
}
