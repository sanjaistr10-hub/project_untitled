import type { ReactNode } from "react";

/**
 * The split screen from the design: content on the left, a rounded visual on the right
 * (hidden on small screens). `mark` is the small logo in the top-left corner.
 */
export function AuthLayout({ mark, aside, children }: { mark: ReactNode; aside: ReactNode; children: ReactNode }) {
  return (
    <div className="grid min-h-screen bg-white lg:grid-cols-2">
      <div className="flex flex-col px-6 py-6 sm:px-10">
        <div className="h-8">{mark}</div>
        <main className="flex flex-1 items-center justify-center py-10">
          <div className="w-full max-w-[360px] animate-fade-up">{children}</div>
        </main>
      </div>
      <div className="hidden p-6 lg:block">
        <div className="relative h-full min-h-[560px] overflow-hidden rounded-[28px]">{aside}</div>
      </div>
    </div>
  );
}

export function AuthHeading({ title, subtitle }: { title: ReactNode; subtitle?: ReactNode }) {
  return (
    <div className="mb-7 text-center">
      <h1 className="text-[22px] font-semibold tracking-tight text-ink">{title}</h1>
      {subtitle && <p className="mt-1.5 text-[13px] text-muted">{subtitle}</p>}
    </div>
  );
}
