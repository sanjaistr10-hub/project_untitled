import type { Metadata } from "next";
import { MemberLogin } from "@/components/site/MemberAuth";
import { RequestTimeMarker } from "@/components/site/RequestTimeMarker";

export const metadata: Metadata = { title: "Sign in" };

export default function MemberLoginPage() {
  return (
    <>
      <MemberLogin />
      <RequestTimeMarker />
    </>
  );
}
