import { initials } from "@/lib/format";

/** The community's logo, or its initials on the accent colour. Never the platform's mark. */
export function CommunityLogo({ name, logoUrl, size = "md" }: { name: string; logoUrl: string | null; size?: "md" | "lg" }) {
  const dimension = size === "lg" ? "size-14 rounded-2xl text-lg" : "size-8 rounded-lg text-xs";
  if (logoUrl) {
    // eslint-disable-next-line @next/next/no-img-element -- creator-supplied URL on any host
    return <img src={logoUrl} alt={`${name} logo`} className={`${dimension} object-cover`} />;
  }
  return (
    <span className={`inline-flex items-center justify-center bg-accent font-semibold text-accent-foreground ${dimension}`} aria-hidden="true">
      {initials(name)}
    </span>
  );
}
