"use client";

import { useState, type CSSProperties, type FormEvent } from "react";
import { ApiError } from "@/lib/api/client";
import { creatorApi } from "@/lib/api/creator";
import { contrastText, displayUrl } from "@/lib/format";
import { Button } from "@/components/ui/Button";
import { TextField } from "@/components/ui/TextField";
import { CommunityLogo } from "@/components/site/CommunityLogo";
import { useCreator } from "./StudioShell";

const PALETTE = ["#111111", "#C8302F", "#1F5FD1", "#1B7A4E", "#B45309", "#0E7490", "#7C3AED"];

/** White-label settings: what members see on the community's own site. */
export function BrandingView() {
  const { me, setMe } = useCreator();
  const community = me.community!;
  const [form, setForm] = useState({
    name: community.name,
    tagline: community.tagline ?? "",
    description: community.description ?? "",
    accentColor: community.accentColor,
    logoUrl: community.logoUrl ?? "",
  });
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [status, setStatus] = useState<"idle" | "saving" | "saved">("idle");

  const set = (key: keyof typeof form) => (value: string) => {
    setForm((current) => ({ ...current, [key]: value }));
    setStatus("idle");
  };

  async function onSubmit(event: FormEvent) {
    event.preventDefault();
    setStatus("saving");
    setErrors({});
    try {
      const updated = await creatorApi.updateBranding(form);
      setMe({ ...me, community: updated });
      setStatus("saved");
    } catch (e) {
      setErrors(e instanceof ApiError ? { ...e.fieldErrors, form: Object.keys(e.fieldErrors).length ? "" : e.message } : { form: "We couldn't reach the server." });
      setStatus("idle");
    }
  }

  const validHex = /^#[0-9a-fA-F]{6}$/.test(form.accentColor);
  const accent = validHex ? form.accentColor : community.accentColor;
  const previewStyle = { "--accent": accent, "--accent-foreground": contrastText(accent) } as CSSProperties;

  return (
    <div className="flex flex-col gap-6 animate-fade-up">
      <header>
        <h1 className="text-2xl font-semibold tracking-tight">Branding</h1>
        <p className="mt-1 text-sm text-muted">
          Your site at <span className="font-mono">{displayUrl(community.siteUrl)}</span> shows only your brand. Members never see ours.
        </p>
      </header>

      <div className="grid gap-6 lg:grid-cols-[1fr_340px]">
        <form onSubmit={onSubmit} className="flex flex-col gap-5 rounded-2xl border border-line bg-white p-6" noValidate>
          <TextField label="Community name" value={form.name} maxLength={80} onChange={(e) => set("name")(e.target.value)} error={errors.name} />
          <TextField label="Tagline" placeholder="One line about your community" value={form.tagline} maxLength={160} onChange={(e) => set("tagline")(e.target.value)} error={errors.tagline} />
          <div className="flex flex-col gap-1.5">
            <label htmlFor="description" className="text-[13px] font-medium text-ink-soft">About</label>
            <textarea
              id="description"
              rows={4}
              maxLength={2000}
              value={form.description}
              onChange={(e) => set("description")(e.target.value)}
              placeholder="What members get, who it's for…"
              className="rounded-lg border border-line px-3 py-2 text-sm outline-none focus:border-ink focus:ring-2 focus:ring-ink/10"
            />
          </div>
          <fieldset className="flex flex-col gap-2">
            <legend className="mb-1.5 text-[13px] font-medium text-ink-soft">Accent colour</legend>
            <div className="flex flex-wrap items-center gap-2">
              {PALETTE.map((color) => (
                <button
                  key={color}
                  type="button"
                  onClick={() => set("accentColor")(color)}
                  aria-label={`Use ${color}`}
                  aria-pressed={form.accentColor.toUpperCase() === color}
                  className="size-8 rounded-full ring-offset-2 transition aria-pressed:ring-2 aria-pressed:ring-ink"
                  style={{ background: color }}
                />
              ))}
              <input
                type="color"
                value={validHex ? form.accentColor : "#111111"}
                onChange={(e) => set("accentColor")(e.target.value.toUpperCase())}
                aria-label="Pick a custom colour"
                className="size-8 cursor-pointer rounded-full border border-line bg-white p-0.5"
              />
              <TextField label="Hex" hideLabel value={form.accentColor} maxLength={7} onChange={(e) => set("accentColor")(e.target.value)} className="w-28" error={errors.accentColor} />
            </div>
          </fieldset>
          <TextField
            label="Logo URL"
            placeholder="https://…/logo.png"
            value={form.logoUrl}
            onChange={(e) => set("logoUrl")(e.target.value)}
            error={errors.logoUrl}
            hint="Square image works best. File uploads are coming soon."
          />
          {errors.form && <p className="text-xs text-danger" role="alert">{errors.form}</p>}
          <div className="flex items-center gap-3">
            <Button type="submit" loading={status === "saving"} disabled={!form.name.trim() || !validHex}>
              Save changes
            </Button>
            {status === "saved" && <span className="text-[13px] text-success">Saved. Your site is updated.</span>}
          </div>
        </form>

        <aside className="flex flex-col gap-3">
          <p className="text-xs font-medium uppercase tracking-wide text-muted">Member sign-in preview</p>
          <div style={previewStyle} className="rounded-2xl border border-line bg-white p-6 text-center">
            <div className="flex justify-center">
              <CommunityLogo name={form.name || community.name} logoUrl={form.logoUrl || null} size="lg" />
            </div>
            <p className="mt-4 text-lg font-semibold tracking-tight">Welcome to {form.name || community.name}</p>
            <p className="mt-1 text-xs text-muted">{form.tagline || "Sign in or create your account"}</p>
            <div className="mt-5 h-10 rounded-lg border border-line text-left text-sm leading-10 text-zinc-400"><span className="px-3">Email address</span></div>
            <div className="mt-3 h-10 rounded-full bg-accent text-sm font-medium leading-10 text-accent-foreground">Continue</div>
          </div>
          <a href={community.siteUrl} target="_blank" rel="noreferrer" className="text-center text-[13px] font-medium text-link hover:underline">
            Open your site
          </a>
        </aside>
      </div>
    </div>
  );
}
