# API reference (v1)

Base path `/api/v1`. JSON in and out. The live, always-current spec is at
`http://localhost:8081/swagger-ui.html` (raw: `/v3/api-docs`).

## Conventions

- **Auth**: tokens arrive as httpOnly cookies from `otp/verify`. API clients that aren't browsers can
  send `Authorization: Bearer <access token>` instead.
- **CSRF**: every `POST/PUT/PATCH/DELETE` needs the header `X-Requested-With` (any value), unless it
  carries an `Authorization` header. The frontend client adds it automatically.
- **Errors** are RFC 9457 `application/problem+json` and always include a machine-readable `code`:

  ```json
  { "status": 400, "code": "OTP_INVALID", "detail": "That code isn't right.", "attemptsRemaining": 3 }
  ```

  Validation errors use `code: "VALIDATION_FAILED"` and an `errors` map of `field → message`.
  `429` responses include `retryAfterSeconds` and a `Retry-After` header.
- **Pagination**: `?page=0&size=20` → `{ items, page, size, totalItems, totalPages }`.
- `nextStep` (`PROFILE` | `COMMUNITY` | `DONE`) tells the UI which onboarding screen to show.

## Creator (studio): `/api/v1/creator`

| Method | Path | Auth | Purpose |
| ------ | ---- | ---- | ------- |
| POST | `/auth/otp/start` | none | Email a sign-in code `{email}` → `202 {email, expiresInSeconds, resendAfterSeconds}` |
| POST | `/auth/otp/verify` | none | `{email, code}` → `{account, newAccount, nextStep}` + cookies. Creates the account on first use |
| POST | `/auth/refresh` | refresh cookie | Rotate tokens → `204` + new cookies |
| POST | `/auth/logout` | refresh cookie | Revoke + clear cookies → `204` |
| GET | `/me` | creator | `{account, community \| null, nextStep}` |
| PUT | `/me/profile` | creator | `{fullName, username}` (onboarding step 1) |
| GET | `/username-availability?username=` | creator | `{username, available, reason}` |
| POST | `/community` | creator | `{name, slug?}` → `201` community (onboarding step 2) |
| GET | `/community` | creator | The creator's community (incl. `siteUrl`) |
| PATCH | `/community` | creator | Branding: `{name?, tagline?, description?, accentColor?, logoUrl?}` |
| GET | `/community/slug-availability?slug=` or `?name=` | creator | `{slug, available, reason, suggestion, siteUrl}` |
| GET | `/members?q=&page=&size=` | creator | Paginated members of the creator's community |
| GET | `/dashboard/summary` | creator | `{memberCount, newMembersLast7Days, recentMembers}` |

## Member (one community): `/api/v1/c/{slug}`

| Method | Path | Auth | Purpose |
| ------ | ---- | ---- | ------- |
| POST | `/auth/otp/start` | none | Email a code branded with the community's name |
| POST | `/auth/otp/verify` | none | → `{account, newAccount, nextStep}` + cookies scoped to `/api/v1/c/{slug}` |
| POST | `/auth/refresh` | refresh cookie | Rotate tokens |
| POST | `/auth/logout` | refresh cookie | Revoke + clear cookies |
| GET | `/me` | member of `{slug}` | `{account, community, nextStep}` |
| PUT | `/me/profile` | member of `{slug}` | `{fullName, username}` |
| GET | `/username-availability?username=` | member of `{slug}` | Unique within the community |

A member token for another community gets `403 WRONG_COMMUNITY`; an unknown slug gets `404 COMMUNITY_NOT_FOUND`.

## Public: `/api/v1/public`

| Method | Path | Purpose |
| ------ | ---- | ------- |
| GET | `/communities/{slug}` | Branding for a member site: `{name, slug, tagline, description, logoUrl, accentColor}` |
| GET | `/domains/resolve?host=` | Custom domain → `{host, slug}` (verified domains only) |

## Error codes you'll see

| Code | Status | Meaning |
| ---- | ------ | ------- |
| `VALIDATION_FAILED` | 400 | See `errors` |
| `OTP_INVALID` / `OTP_EXPIRED` / `OTP_TOO_MANY_ATTEMPTS` | 400 | Code problems; `attemptsRemaining` on `OTP_INVALID` |
| `OTP_COOLDOWN` / `OTP_RATE_LIMITED` | 429 | Asked for codes too fast |
| `UNAUTHENTICATED` / `TOKEN_INVALID` / `REFRESH_INVALID` | 401 | Sign in (again) |
| `FORBIDDEN` / `WRONG_COMMUNITY` / `CSRF_HEADER_MISSING` | 403 | Not allowed |
| `COMMUNITY_NOT_FOUND` / `DOMAIN_NOT_FOUND` | 404 | |
| `USERNAME_TAKEN` / `SLUG_TAKEN` / `COMMUNITY_EXISTS` / `CONFLICT` | 409 | |
| `SLUG_INVALID` | 400 | Reserved or malformed address |
