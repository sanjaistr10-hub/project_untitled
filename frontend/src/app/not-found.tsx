import Link from "next/link";

export default function NotFound() {
  return (
    <main className="flex min-h-screen flex-col items-center justify-center gap-3 px-6 text-center">
      <p className="text-sm font-medium text-muted">404</p>
      <h1 className="text-2xl font-semibold tracking-tight">This page doesn&apos;t exist</h1>
      <p className="max-w-sm text-sm text-muted">Check the address, or head back to the start.</p>
      <Link href="/" className="mt-2 text-sm font-medium text-link hover:underline">
        Go home
      </Link>
    </main>
  );
}
