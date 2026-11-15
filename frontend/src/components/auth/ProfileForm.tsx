"use client";

import { useEffect, useState, type FormEvent } from "react";
import { ApiError } from "@/lib/api/client";
import type { ProfileInput, UsernameAvailability } from "@/lib/api/types";
import { Button } from "@/components/ui/Button";
import { TextField } from "@/components/ui/TextField";
import { Icon } from "@/components/ui/icons";
import { AuthHeading } from "./AuthLayout";

interface ProfileFormProps {
  title?: string;
  subtitle?: string;
  initial?: Partial<ProfileInput>;
  checkUsername: (username: string) => Promise<UsernameAvailability>;
  save: (input: ProfileInput) => Promise<unknown>;
  onSaved: () => void;
  buttonVariant?: "primary" | "accent";
}

const USERNAME = /^[A-Za-z0-9._]{3,30}$/;

/** "Hello, Nice to meet you!" (design slide 3), shared by creators and members. */
export function ProfileForm({
  title = "Hello, Nice to meet you! 👋",
  subtitle = "Tell us a little about yourself",
  initial,
  checkUsername,
  save,
  onSaved,
  buttonVariant = "primary",
}: ProfileFormProps) {
  const [fullName, setFullName] = useState(initial?.fullName ?? "");
  const [username, setUsername] = useState(initial?.username ?? "");
  const [availability, setAvailability] = useState<UsernameAvailability | null>(null);
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [formError, setFormError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);

  const usernameValid = USERNAME.test(username.trim());

  // Debounced availability check while typing.
  useEffect(() => {
    const value = username.trim();
    if (!USERNAME.test(value)) return;
    let cancelled = false;
    const timer = setTimeout(() => {
      checkUsername(value)
        .then((result) => !cancelled && setAvailability(result))
        .catch(() => !cancelled && setAvailability(null));
    }, 350);
    return () => {
      cancelled = true;
      clearTimeout(timer);
    };
  }, [username, checkUsername]);

  const current = availability && availability.username === username.trim().toLowerCase() ? availability : null;

  async function onSubmit(event: FormEvent) {
    event.preventDefault();
    setBusy(true);
    setErrors({});
    setFormError(null);
    try {
      await save({ fullName: fullName.trim(), username: username.trim() });
      onSaved();
    } catch (e) {
      if (e instanceof ApiError) {
        if (e.code === "USERNAME_TAKEN") setErrors({ username: e.message });
        else if (Object.keys(e.fieldErrors).length) setErrors(e.fieldErrors);
        else setFormError(e.message);
      } else {
        setFormError("We couldn't reach the server.");
      }
      setBusy(false);
    }
  }

  let usernameHint: React.ReactNode = "Letters, numbers, dots and underscores.";
  if (username && !usernameValid) usernameHint = "Use 3-30 letters, numbers, dots or underscores.";
  else if (current?.available) usernameHint = <span className="text-success">Username available</span>;

  return (
    <div>
      <AuthHeading title={title} subtitle={subtitle} />
      <form onSubmit={onSubmit} className="flex flex-col gap-4" noValidate>
        <div className="flex justify-center">
          <div className="relative">
            <div className="flex size-16 items-center justify-center rounded-full border border-dashed border-line bg-surface text-muted">
              <Icon name="camera" className="size-5" />
            </div>
            <span className="absolute -bottom-1 left-1/2 -translate-x-1/2 whitespace-nowrap rounded-full bg-white px-1.5 text-[10px] text-muted shadow-sm">
              Photo soon
            </span>
          </div>
        </div>
        <TextField
          label="Full name"
          name="fullName"
          autoComplete="name"
          placeholder="Your full name"
          value={fullName}
          onChange={(event) => setFullName(event.target.value)}
          error={errors.fullName}
          autoFocus
          required
        />
        <TextField
          label="Username"
          name="username"
          autoComplete="username"
          placeholder="yourname"
          prefix="@"
          value={username}
          onChange={(event) => setUsername(event.target.value)}
          error={errors.username ?? (current && !current.available ? current.reason : null)}
          hint={usernameHint}
          required
        />
        {formError && (
          <p className="text-xs text-danger" role="alert">
            {formError}
          </p>
        )}
        <Button
          type="submit"
          variant={buttonVariant}
          block
          loading={busy}
          disabled={!fullName.trim() || !usernameValid || current?.available === false}
        >
          Continue
        </Button>
      </form>
    </div>
  );
}
