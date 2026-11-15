import type { Metadata } from "next";
import { BRAND_NAME } from "@/lib/config";

// The creator studio. Served on the root domain only (see src/proxy.ts).
export const metadata: Metadata = {
  title: { default: BRAND_NAME, template: `%s · ${BRAND_NAME}` },
  description: "Your community, on your own brand.",
  icons: { icon: "/brand/mark.svg" },
};

export default function StudioLayout({ children }: LayoutProps<"/">) {
  return children;
}
