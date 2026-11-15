import { AUTH_HERO_IMAGE, BRAND_NAME } from "@/lib/config";

export function StudioMark() {
  return (
    <span className="inline-flex items-center gap-2 text-sm font-semibold tracking-tight">
      {/* eslint-disable-next-line @next/next/no-img-element -- tiny static SVG */}
      <img src="/brand/mark.svg" alt="" className="size-7" />
      {BRAND_NAME}
    </span>
  );
}

export function StudioHero() {
  return (
    <div
      className="absolute inset-0 bg-[#1D1A18] bg-cover bg-center"
      style={{ backgroundImage: `url(${AUTH_HERO_IMAGE})` }}
      role="img"
      aria-label="A team working together around a table"
    />
  );
}
