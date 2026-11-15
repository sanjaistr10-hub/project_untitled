import type { Metadata } from "next";
import { MemberHome } from "@/components/site/MemberHome";
import { RequestTimeMarker } from "@/components/site/RequestTimeMarker";

export const metadata: Metadata = { title: "Home" };

export default function MemberHomePage() {
  return (
    <>
      <MemberHome />
      <RequestTimeMarker />
    </>
  );
}
