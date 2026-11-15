# Project Untitled

A white-label community platform for creators. Like Whop, the creator and their community live
on one platform, but here **each community runs on the creator's own brand**: their own
subdomain or custom domain, their name, logo and colours. Members never see the platform.

This repository is the **first cut (milestone M0)**:

- Creators sign up, set up a profile and name their community, then land in the **studio dashboard**.
- Members sign up **on that community's own site** and land in their **member home**.
- Passwordless email-code sign-in for both, with separate, tenant-isolated accounts and sessions.

| Layer    | Tech                                                                                 |
| -------- | ------------------------------------------------------------------------------------ |
| Backend  | Java 17 (OpenJDK), Spring Boot 4.1, Spring Security (JWT), Spring Data JPA, Flyway   |
| Database | PostgreSQL 17                                                                        |
| API      | REST + JSON under `/api/v1`, errors as RFC 9457 problem+json, OpenAPI / Swagger UI   |
| Frontend | Next.js 16 (App Router), React 19, TypeScript, Tailwind CSS 4, Node.js 20+           |

## Documentation

| Read | For |
| ---- | --- |
| [docs/DEVELOPER_GUIDE.md](docs/DEVELOPER_GUIDE.md) | Using the app while you develop: sign-in codes, test accounts, limits, common problems, and what a release needs |
| [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) | How it's built: files, code, database, and how the browser talks to the API |
| [docs/API.md](docs/API.md) | Every endpoint and error code |
| [docs/ROADMAP.md](docs/ROADMAP.md) | Milestones (course builder, payments, custom domains, AI modules) and open questions |
| [CONTRIBUTING.md](CONTRIBUTING.md) | Branches, pull requests, and how to add a feature without breaking community isolation |

## Repository layout

```
backend/     Spring Boot REST API (Maven wrapper included, so no Maven install needed)
frontend/    Next.js app: creator studio + white-label member sites
docs/        Developer guide, architecture, API reference, roadmap
scripts/     db-setup.sh (local Postgres without Docker), docker init SQL
docker-compose.yml   Postgres 17 + Mailpit for local development
.github/workflows/   CI: backend tests against Postgres, frontend lint + typecheck + build
```

## Run it locally

**Prerequisites:** JDK 17+, Node.js 20.9+, and PostgreSQL 17 (via Docker or installed locally).

### 1. Database

With Docker:

```bash
docker compose up -d
```

Without Docker (e.g. `brew install postgresql@17 && brew services start postgresql@17`):

```bash
./scripts/db-setup.sh
```

Both create the `project_untitled` and `project_untitled_test` databases (user and password:
`project_untitled`). Tables are created by Flyway when the backend starts.

### 2. Backend (port 8081)

```bash
cd backend && ./mvnw spring-boot:run
```

- Swagger UI: http://localhost:8081/swagger-ui.html
- Health: http://localhost:8081/actuator/health
- **No email is sent in development. Sign-in codes are printed in this terminal** (look for
  `[DEV] ... sign-in code`). Any email address works, even a made-up one.
- To see codes as real emails in a fake inbox, start Mailpit (`docker compose up -d mailpit`, or
  `brew install mailpit && mailpit`), start the backend with `APP_MAIL_DELIVERY=smtp`, and open
  http://localhost:8025. Details are in [docs/DEVELOPER_GUIDE.md](docs/DEVELOPER_GUIDE.md#2-get-your-sign-in-code).

### 3. Frontend (port 3100)

```bash
cd frontend && cp .env.example .env.local && npm install && npm run dev
```

### 4. Try both flows

1. **Creator:** open http://localhost:3100, sign in with any email, then enter the code from the
   backend terminal, add your profile and name your community (e.g. "Morning Yoga Den").
2. **Member:** from the dashboard click **Open site**, which goes to
   http://morning-yoga-den.localhost:3100. Sign up there with another email. You'll see the
   community's own branding, and the member appears in the studio's **Members** page.
3. Change the colour or tagline under **Branding** and reload the member site.

> `*.localhost` subdomains resolve automatically in Chrome, Edge and Firefox. Safari doesn't
> resolve them; add `127.0.0.1 morning-yoga-den.localhost` to `/etc/hosts` or use another browser.

Stuck? The common problems and their fixes are in
[docs/DEVELOPER_GUIDE.md](docs/DEVELOPER_GUIDE.md#5-common-problems).

## How it's built, in short

- **Files:** the backend is grouped by feature (`auth`, `creator`, `community`, `member`). The
  frontend has studio pages in `app/(studio)` and community-site pages in `app/site/[slug]`.
- **Code:** every API request passes security checks, then a thin controller, a service (the
  rules), and a repository (the database). On the frontend, `proxy.ts` picks the studio or a
  community site from the address in the browser.
- **Database:** PostgreSQL, with the schema changed only through Flyway migration files. A member
  account belongs to exactly one community.
- **Client and server:** the browser only talks to Next.js, which forwards `/api` calls to Spring
  Boot. Logins are httpOnly cookies, kept separately on each community's address.

The full picture is in [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md).

## Tests and checks

```bash
cd backend && ./mvnw test                           # integration tests need the test database
cd frontend && npm run lint && npm run typecheck && npm run build
```

## Releasing

There's no staging or production server yet; hosting is an open question in the roadmap. What any
server needs (environment variables, wildcard domain, HTTPS, email setup) and a pre-release
checklist are in [docs/DEVELOPER_GUIDE.md](docs/DEVELOPER_GUIDE.md#6-from-your-machine-to-a-release).
