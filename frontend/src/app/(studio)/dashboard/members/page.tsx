import type { Metadata } from "next";
import { MembersView } from "@/components/studio/MembersView";

// Rendered after the browser loads the session; see dashboard/layout.tsx.
export const instant = false;

export const metadata: Metadata = { title: "Members" };

export default function MembersPage() {
  return <MembersView />;
}
