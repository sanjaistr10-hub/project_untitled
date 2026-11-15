import { initials } from "@/lib/format";

const TONES = ["bg-amber-100 text-amber-900", "bg-sky-100 text-sky-900", "bg-emerald-100 text-emerald-900",
  "bg-rose-100 text-rose-900", "bg-violet-100 text-violet-900", "bg-zinc-200 text-zinc-800"];

function toneFor(seed: string) {
  let hash = 0;
  for (const char of seed) hash = (hash * 31 + char.charCodeAt(0)) | 0;
  return TONES[Math.abs(hash) % TONES.length];
}

export function Avatar({ name, seed, size = "md" }: { name: string | null; seed: string; size?: "sm" | "md" | "lg" }) {
  const dimension = size === "sm" ? "size-7 text-[11px]" : size === "lg" ? "size-16 text-lg" : "size-9 text-xs";
  return (
    <span
      className={`inline-flex shrink-0 items-center justify-center rounded-full font-semibold ${dimension} ${toneFor(seed)}`}
      aria-hidden="true"
    >
      {initials(name, seed)}
    </span>
  );
}
