import type { Metadata } from "next";
import { MemberOnboarding } from "@/components/site/MemberAuth";
import { RequestTimeMarker } from "@/components/site/RequestTimeMarker";

export const metadata: Metadata = { title: "Welcome" };

export default function MemberOnboardingPage() {
  return (
    <>
      <MemberOnboarding />
      <RequestTimeMarker />
    </>
  );
}
