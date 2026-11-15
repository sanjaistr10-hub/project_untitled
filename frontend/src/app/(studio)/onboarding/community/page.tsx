import type { Metadata } from "next";
import { CreatorCommunityStep } from "@/components/studio/CreatorOnboarding";

export const metadata: Metadata = { title: "Name your community" };

export default function CreatorCommunityPage() {
  return <CreatorCommunityStep />;
}
