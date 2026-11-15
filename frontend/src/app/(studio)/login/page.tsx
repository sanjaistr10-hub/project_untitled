import type { Metadata } from "next";
import { CreatorLogin } from "@/components/studio/CreatorLogin";

export const metadata: Metadata = { title: "Sign in" };

export default function CreatorLoginPage() {
  return <CreatorLogin />;
}
