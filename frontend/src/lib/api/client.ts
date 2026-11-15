/**
 * Browser-side API client. Calls go to same-origin /api/v1/... (proxied to Spring by next.config.ts),
 * carry the httpOnly auth cookies automatically, and add the X-Requested-With header the backend
 * requires on writes (CSRF guard). On a 401 it refreshes the session once and retries.
 */

export const API_BASE = "/api/v1";

/** Which session a request uses: the creator's (studio) or a member's (one community). */
export type Scope = { kind: "creator" } | { kind: "member"; slug: string };

export class ApiError extends Error {
  constructor(
    readonly status: number,
    readonly code: string,
    message: string,
    readonly fieldErrors: Record<string, string> = {},
    readonly details: Record<string, unknown> = {},
  ) {
    super(message);
    this.name = "ApiError";
  }

  get isUnauthorized() {
    return this.status === 401;
  }
}

interface RequestOptions {
  method?: "GET" | "POST" | "PUT" | "PATCH" | "DELETE";
  body?: unknown;
  query?: Record<string, string | number | undefined | null>;
}

const refreshes = new Map<string, Promise<boolean>>();

function refreshPath(scope: Scope) {
  return scope.kind === "creator" ? `${API_BASE}/creator/auth/refresh` : `${API_BASE}/c/${scope.slug}/auth/refresh`;
}

/** One refresh at a time per scope; concurrent 401s share it. */
function refresh(scope: Scope): Promise<boolean> {
  const key = refreshPath(scope);
  let pending = refreshes.get(key);
  if (!pending) {
    pending = send(key, { method: "POST" })
      .then((response) => response.ok)
      .catch(() => false)
      .finally(() => refreshes.delete(key));
    refreshes.set(key, pending);
  }
  return pending;
}

function send(path: string, { method = "GET", body, query }: RequestOptions): Promise<Response> {
  let url = path;
  if (query) {
    const params = new URLSearchParams();
    for (const [key, value] of Object.entries(query)) {
      if (value !== undefined && value !== null && value !== "") params.set(key, String(value));
    }
    const qs = params.toString();
    if (qs) url += `?${qs}`;
  }
  const headers: Record<string, string> = { Accept: "application/json", "X-Requested-With": "fetch" };
  if (body !== undefined) headers["Content-Type"] = "application/json";
  return fetch(url, {
    method,
    headers,
    body: body === undefined ? undefined : JSON.stringify(body),
    credentials: "same-origin",
    cache: "no-store",
  });
}

async function parse<T>(response: Response): Promise<T> {
  if (response.status === 204) return undefined as T;
  const text = await response.text();
  const data = text ? safeJson(text) : null;
  if (response.ok) return data as T;

  const problem = (data ?? {}) as Record<string, unknown>;
  const { code, detail, errors, ...rest } = problem;
  throw new ApiError(
    response.status,
    typeof code === "string" ? code : `HTTP_${response.status}`,
    typeof detail === "string" ? detail : "Something went wrong. Please try again.",
    (errors as Record<string, string>) ?? {},
    rest,
  );
}

function safeJson(text: string): unknown {
  try {
    return JSON.parse(text);
  } catch {
    return null;
  }
}

export async function api<T>(scope: Scope | null, path: string, options: RequestOptions = {}): Promise<T> {
  const fullPath = `${API_BASE}${path}`;
  let response = await send(fullPath, options);
  if (response.status === 401 && scope && !path.includes("/auth/") && (await refresh(scope))) {
    // If refresh fails (e.g. another tab rotated the token first), callers redirect to sign-in,
    // whose own session check then finds the fresh cookies and sends the user straight back.
    response = await send(fullPath, options);
  }
  return parse<T>(response);
}
