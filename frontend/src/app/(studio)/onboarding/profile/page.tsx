import type { Metadata } from "next";
import { CreatorProfileStep } from "@/components/studio/CreatorOnboarding";

export const metadata: Metadata = { title: "About you" };

export default function CreatorProfilePage() {
  return <CreatorProfileStep />;
}
