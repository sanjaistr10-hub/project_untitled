# Contributing

## Workflow

- Branch from `main`: `feature/<short-name>`, `fix/<short-name>`.
- Keep PRs focused. CI must be green (backend tests, frontend lint + typecheck + build).
- Commit messages: imperative and specific, e.g. `Add course chapter reordering endpoint`.
- Read [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) before your first PR.

## Backend: adding a feature

1. **Pick or create a feature package** (`creator`, `member`, `community`, or a new one such as
   `product`). Inside it: entity, repository, service, controller, DTO records.
2. **Put the endpoint under the right root.** Security denies everything else:
   - `/api/v1/creator/**`: signed-in creator. Get them with `@CurrentCreatorId UUID creatorId`
     (no DB read) or `@CurrentCreator CreatorAccount creator`.
   - `/api/v1/c/{slug}/**`: signed-in member of that community. Use `@CurrentCommunity Community`
     and `@CurrentMember MemberAccount`. `TenantInterceptor` already rejects other communities'
     tokens, but still **scope every query by `communityId`**.
   - `/api/v1/public/**`: no auth. Never return private data here.
3. **Schema changes** go in a new Flyway file: `backend/src/main/resources/db/migration/V2__add_products.sql`.
   Never edit a migration that's been merged. Use `uuid` ids, `timestamptz`, and `created_at`/`updated_at`
   (entities extend `BaseEntity`).
4. **Errors**: throw `ApiException.notFound("PRODUCT_NOT_FOUND", "…")` and so on. Add new codes to `docs/API.md`.
5. **Settings**: add them to `config/AppProperties` and `application.yml`; secrets only via env vars.
6. **Tests**: extend or copy `AuthFlowIntegrationTest` (MockMvc against the real test database;
   tables are truncated before each test). Run `./mvnw test`.

## Frontend: adding a page

- **Studio page**: `src/app/(studio)/dashboard/<name>/page.tsx` renders a client component from
  `src/components/studio/`. Use `useCreator()` for the signed-in creator. Export
  `export const instant = false` (see `dashboard/layout.tsx` for why) and add it to `NAV` in `StudioShell.tsx`.
- **Member-site page**: `src/app/site/[slug]/<name>/page.tsx`. Links are host-relative (`/home`,
  not `/site/slug/home`). Use `useCommunity()` for branding and a community-scoped API client.
  Render `<RequestTimeMarker />` in the page. Use `bg-accent` / `text-accent-foreground` for
  brand colour; never show the platform's name or logo on member sites.
- **API calls**: add typed functions in `src/lib/api/creator.ts` or `member.ts`, and keep
  `src/lib/api/types.ts` in sync with the backend DTOs.
- **Styling**: Tailwind with the tokens in `src/app/globals.css` (`ink`, `muted`, `line`, `surface`, `accent`).
- Next.js 16 differs from most tutorials. Check `frontend/node_modules/next/dist/docs` (see `frontend/AGENTS.md`).
- Run `npm run lint && npm run typecheck && npm run build` before pushing.

## Local tips

Sign-in codes print in the backend console (`[DEV] … sign-in code for …`). How to get codes,
test as several people, reset your data and fix common problems is all in
[docs/DEVELOPER_GUIDE.md](docs/DEVELOPER_GUIDE.md).
