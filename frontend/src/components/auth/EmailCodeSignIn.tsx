"use client";

import { useEffect, useState, type FormEvent, type ReactNode } from "react";
import { ApiError } from "@/lib/api/client";
import type { AuthResult, OtpStarted } from "@/lib/api/types";
import { Button } from "@/components/ui/Button";
import { TextField } from "@/components/ui/TextField";
import { AppleMark, GoogleMark } from "@/components/ui/icons";
import { AuthHeading } from "./AuthLayout";
import { OtpInput } from "./OtpInput";

interface EmailCodeSignInProps<TAccount> {
  title: ReactNode;
  subtitle?: ReactNode;
  startOtp: (email: string) => Promise<OtpStarted>;
  verifyOtp: (email: string, code: string) => Promise<AuthResult<TAccount>>;
  onSignedIn: (result: AuthResult<TAccount>) => void;
  /** Show Google / Apple buttons (studio only for now; OAuth is on the roadmap). */
  showSocial?: boolean;
  buttonVariant?: "primary" | "accent";
}

/**
 * Passwordless sign-in used by both creators and members (design slides 1-2): enter an email,
 * then the 6-digit code we email. The same flow signs up new accounts.
 */
export function EmailCodeSignIn<TAccount>({
  title,
  subtitle,
  startOtp,
  verifyOtp,
  onSignedIn,
  showSocial = false,
  buttonVariant = "primary",
}: EmailCodeSignInProps<TAccount>) {
  const [step, setStep] = useState<"email" | "code">("email");
  const [email, setEmail] = useState("");
  const [code, setCode] = useState("");
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [resendIn, setResendIn] = useState(0);

  useEffect(() => {
    if (resendIn <= 0) return;
    const timer = setTimeout(() => setResendIn((seconds) => seconds - 1), 1000);
    return () => clearTimeout(timer);
  }, [resendIn]);

  async function requestCode() {
    setBusy(true);
    setError(null);
    try {
      const started = await startOtp(email.trim());
      setEmail(started.email);
      setCode("");
      setStep("code");
      setResendIn(started.resendAfterSeconds);
    } catch (e) {
      if (e instanceof ApiError && e.code === "OTP_COOLDOWN") {
        setStep("code");
        setResendIn(Number(e.details.retryAfterSeconds) || 30);
      } else {
        setError(e instanceof ApiError ? (e.fieldErrors.email ?? e.message) : "We couldn't reach the server.");
      }
    } finally {
      setBusy(false);
    }
  }

  async function submitCode(value = code) {
    if (value.length < 6 || busy) return;
    setBusy(true);
    setError(null);
    try {
      onSignedIn(await verifyOtp(email, value));
    } catch (e) {
      setError(e instanceof ApiError ? e.message : "We couldn't reach the server.");
      if (e instanceof ApiError && (e.code === "OTP_EXPIRED" || e.code === "OTP_TOO_MANY_ATTEMPTS")) setCode("");
      setBusy(false);
    }
  }

  function onSubmit(event: FormEvent) {
    event.preventDefault();
    if (step === "email") void requestCode();
    else void submitCode();
  }

  return (
    <div>
      <AuthHeading title={title} subtitle={subtitle} />
      <form onSubmit={onSubmit} className="flex flex-col gap-4" noValidate>
        {step === "email" ? (
          <TextField
            label="Email address"
            hideLabel
            type="email"
            name="email"
            autoComplete="email"
            placeholder="Email address"
            value={email}
            onChange={(event) => setEmail(event.target.value)}
            error={error}
            autoFocus
            required
          />
        ) : (
          <>
            <div className="flex items-center justify-between rounded-lg border border-line bg-surface px-3 py-2.5 text-sm">
              <span className="truncate text-ink-soft">{email}</span>
              <button
                type="button"
                className="ml-3 shrink-0 text-[13px] font-medium text-link hover:underline"
                onClick={() => {
                  setStep("email");
                  setError(null);
                }}
              >
                Change
              </button>
            </div>
            <p className="text-center text-[13px] text-muted">Enter the 6-digit code we sent to your inbox.</p>
            <OtpInput value={code} onChange={setCode} onComplete={(value) => void submitCode(value)} disabled={busy} invalid={!!error} />
            {error && (
              <p className="text-center text-xs text-danger" role="alert">
                {error}
              </p>
            )}
            <p className="text-center text-[13px]">
              {resendIn > 0 ? (
                <span className="text-muted">Resend code in 0:{String(resendIn).padStart(2, "0")}</span>
              ) : (
                <button type="button" className="font-medium text-link hover:underline" onClick={() => void requestCode()} disabled={busy}>
                  Resend code
                </button>
              )}
            </p>
          </>
        )}
        <Button type="submit" variant={buttonVariant} block loading={busy} disabled={step === "email" ? !email.trim() : code.length < 6}>
          Continue
        </Button>
      </form>

      {showSocial && step === "email" && (
        <>
          <div className="my-6 flex items-center gap-3 text-xs text-muted">
            <span className="h-px flex-1 bg-line" />
            or
            <span className="h-px flex-1 bg-line" />
          </div>
          <div className="flex flex-col gap-2.5">
            <Button variant="secondary" block disabled title="Coming soon">
              <GoogleMark /> Continue with Google
            </Button>
            <Button variant="secondary" block disabled title="Coming soon">
              <AppleMark /> Continue with Apple
            </Button>
            <p className="text-center text-[11px] text-muted">Google and Apple sign-in are coming soon.</p>
          </div>
        </>
      )}
    </div>
  );
}
