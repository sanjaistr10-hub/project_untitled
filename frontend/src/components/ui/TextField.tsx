import { useId, type InputHTMLAttributes, type ReactNode } from "react";

interface TextFieldProps extends Omit<InputHTMLAttributes<HTMLInputElement>, "size" | "prefix"> {
  label: string;
  hint?: ReactNode;
  error?: string | null;
  /** Shown inside the field on the left, e.g. a URL prefix. */
  prefix?: ReactNode;
  hideLabel?: boolean;
}

export function TextField({ label, hint, error, prefix, hideLabel, id, className = "", ...rest }: TextFieldProps) {
  const generatedId = useId();
  const inputId = id ?? generatedId;
  const describedBy = error ? `${inputId}-error` : hint ? `${inputId}-hint` : undefined;
  return (
    <div className={`flex flex-col gap-1.5 ${className}`}>
      <label htmlFor={inputId} className={hideLabel ? "sr-only" : "text-[13px] font-medium text-ink-soft"}>
        {label}
      </label>
      <div
        className={[
          "flex h-10 items-center rounded-lg border bg-white px-3 transition",
          "focus-within:border-ink focus-within:ring-2 focus-within:ring-ink/10",
          error ? "border-danger" : "border-line",
        ].join(" ")}
      >
        {prefix && <span className="mr-1 shrink-0 text-sm text-muted">{prefix}</span>}
        <input
          id={inputId}
          aria-invalid={error ? true : undefined}
          aria-describedby={describedBy}
          className="h-full w-full min-w-0 bg-transparent text-sm outline-none placeholder:text-zinc-400"
          {...rest}
        />
      </div>
      {error ? (
        <p id={`${inputId}-error`} className="text-xs text-danger" role="alert">
          {error}
        </p>
      ) : hint ? (
        <div id={`${inputId}-hint`} className="text-xs text-muted">
          {hint}
        </div>
      ) : null}
    </div>
  );
}
