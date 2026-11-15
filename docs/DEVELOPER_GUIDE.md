# Developer guide: using the app while you build it

This page covers two things: how to click through the app on your own machine, and what
changes on the way to a release. For how the code is organised, read
[ARCHITECTURE.md](ARCHITECTURE.md).

## The short version

- Sign-in uses a 6-digit email code. **On your machine no email is sent.** The code is
  printed in the backend terminal instead.
- Any email address works, even a made-up one like `test1@example.com`.
- The creator studio is at http://localhost:3100. Each community's own site is at
  `http://<slug>.localhost:3100` (for example http://morning-yoga-den.localhost:3100).
- Use Chrome, Edge or Firefox. Safari can't open `*.localhost` addresses.

## 1. Start everything

Three things must be running, in this order:

| What | Command | Address |
| ---- | ------- | ------- |
| Database | `docker compose up -d postgres`, or Postgres from Homebrew | localhost:5432 |
| Backend | `cd backend && ./mvnw spring-boot:run` | http://localhost:8081 |
| Frontend | `cd frontend && npm run dev` | http://localhost:3100 |

The first time, also run `./scripts/db-setup.sh` (if you're not using Docker) and
`cp .env.example .env.local && npm install` in `frontend/`. The full steps are in the
[README](../README.md#run-it-locally).

- **Frontend changes** appear as soon as you save.
- **Backend changes** need a restart: press Ctrl+C, then run it again.
- **Database changes** go in a new migration file. It runs automatically when the backend starts.
- There is no mock mode. Even if you only work on screens, you need the backend and database running.

## 2. Get your sign-in code

### Option A: read it in the backend terminal (default)

Type an email and press **Continue**. The backend terminal prints a line like this:

```
    [DEV] Project Untitled sign-in code for test1@example.com: 482913   (valid 10 min)
```

On a member site the line shows the community's name instead, for example
`[DEV] Morning Yoga Den sign-in code for ...`. Search the terminal for `[DEV]` to find the
latest one. If you run the backend from IntelliJ, the line appears in the Run window.

### Option B: see real emails in a fake inbox (Mailpit)

Use this when you work on the email itself or want the real experience. Mailpit catches
every email, so nothing reaches a real inbox.

1. Start Mailpit: `docker compose up -d mailpit`. Without Docker, run `brew install mailpit`
   once, then `mailpit`.
2. Start the backend in email mode:

   ```bash
   cd backend && APP_MAIL_DELIVERY=smtp ./mvnw spring-boot:run
   ```

3. Open http://localhost:8025. Each sign-in arrives as an email, for example
   "482913 is your Morning Yoga Den sign-in code".

In email mode, if Mailpit isn't running, sign-in shows "Something went wrong on our side."
Start Mailpit, or restart the backend without `APP_MAIL_DELIVERY`.

### Option C: call the API without the UI (Swagger)

Open http://localhost:8081/swagger-ui.html, run `otp/start`, take the code from the backend
terminal, then run `otp/verify`. The browser keeps the sign-in cookie, so the other endpoints
on that page now work as that user. Leave the pre-filled `X-Requested-With` field as it is.

## 3. Everyday testing

| I want to... | Do this |
| ------------ | ------- |
| Sign up as a new creator | Use a new email at http://localhost:3100. You go through profile, then community, then the dashboard. |
| Sign in as an existing creator | Same screen. An account that has finished onboarding goes straight to the dashboard. |
| Join a community as a member | On the dashboard click **Open site**, or go to `http://<slug>.localhost:3100`, and sign in with any email. |
| Be a creator and a member at the same time | Just do it in the same browser. The studio and the member site are different addresses, so their logins stay separate. One email can be both. |
| Be two members of the same community at once | Use a private window, or a second browser profile, for the second member. |
| Check a branding change | Save it under **Branding**, wait about 5 seconds, then reload the member site. |
| Sign out | Studio: **Sign out** in the sidebar (top bar on a phone). Member site: **Sign out** at the top right. |

- Every developer has their **own empty database**, and there's no sample data yet. The first
  creator and community you create become your test data.
- You stay signed in for 30 days. Restarting the backend doesn't sign you out; wiping the
  database does.

## 4. Limits you might hit

Sign-in codes have the same limits on your machine as in production:

| What you see | Why | What to do |
| ------------ | --- | ---------- |
| The **Resend** button counts down | You can request one code every 30 seconds per email | Wait, or use another email |
| "Too many codes requested. Try again later." | Max 5 codes per email per hour | Use another email, or clear the codes (below) |
| "That code isn't right." | Wrong code. You get 5 tries | Check the newest `[DEV]` line, since every new code cancels the old one |
| "This code has expired." or "Too many wrong attempts." | A code lasts 10 minutes and allows 5 tries | Request a new code |

**Clear all sign-in codes:**

```bash
psql -h localhost -U project_untitled project_untitled -c "DELETE FROM otp_challenge"
```

**Wipe everything** (all accounts, communities and logins):

```bash
psql -h localhost -U project_untitled project_untitled -c "TRUNCATE refresh_token, otp_challenge, member_account, community_domain, community, creator_account CASCADE"
```

If `psql` asks for a password, it's `project_untitled`. With Docker, start the command with
`docker compose exec postgres psql -U project_untitled project_untitled -c ...` instead.

## 5. Common problems

| Problem | Fix |
| ------- | --- |
| No code in the terminal | Look at the backend terminal, not the frontend one. If you started the backend with `APP_MAIL_DELIVERY=smtp`, the code is in Mailpit. |
| "Something went wrong on our side." when asking for a code | Email mode is on but Mailpit isn't running. See Option B. |
| A member site address won't open | You're probably in Safari. Use another browser, or add `127.0.0.1 <slug>.localhost` to `/etc/hosts`. |
| A member site shows "404" | That community doesn't exist in **your** database. Create it from the studio first. |
| Pages load but nothing works, or "We couldn't reach the server." | The backend isn't running on 8081, or `BACKEND_URL` in `frontend/.env.local` is wrong. |
| The backend says port 8081 is in use | Run `PORT=8082 ./mvnw spring-boot:run` and set `BACKEND_URL=http://localhost:8082` in `frontend/.env.local`. |
| The backend fails with a Flyway or "schema validation" error | Someone changed a migration that already ran on your database. Wipe your local database and start the backend again. Never edit a merged migration; add a new `V<n>__...sql` file. |

## 6. From your machine to a release

The same code runs everywhere. Only the settings change.

| | Your machine | Test server (staging) | Production |
| --- | --- | --- | --- |
| Spring profile | `dev` (the default) | `prod` | `prod` |
| Sign-in codes | Backend terminal, or Mailpit | Real emails, ideally to a test-inbox service | Real emails |
| Addresses | `localhost:3100`, `<slug>.localhost:3100` | `staging-domain.com`, `<slug>.staging-domain.com` | `yourdomain.com`, `<slug>.yourdomain.com` |
| HTTPS | No | Required | Required |
| Secrets | Built-in dev values | Environment variables | Environment variables, different from staging |
| Data | Your own local database | Shared test data | Real users |

**No staging or production server exists yet.** Where to host is an open question in
[ROADMAP.md](ROADMAP.md). Whichever host you choose, a server needs the following.

### Backend

Build it with `cd backend && ./mvnw -B package`, then run
`java -jar target/project-untitled-backend-0.1.0-SNAPSHOT.jar` with these environment variables:

| Variable | Example | Notes |
| -------- | ------- | ----- |
| `SPRING_PROFILES_ACTIVE` | `prod` | Turns off all dev defaults |
| `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` | `jdbc:postgresql://db-host:5432/project_untitled` | |
| `APP_JWT_SECRET` | a long random string | At least 32 characters. Generate one with `openssl rand -base64 48` |
| `APP_OTP_SECRET` | another long random string | Must be different from the JWT secret |
| `APP_FRONTEND_URL` | `https://yourdomain.com` | Where the studio lives |
| `APP_MEMBER_SITE_URL_TEMPLATE` | `https://{slug}.yourdomain.com` | Where community sites live |
| `APP_MAIL_FROM` | `no-reply@yourdomain.com` | |
| `SMTP_HOST`, `SMTP_PORT`, `SMTP_USERNAME`, `SMTP_PASSWORD` | from your email provider | The port defaults to 587 |

If a required secret is missing, the backend refuses to start. That's on purpose.

### Frontend

Run `cd frontend && npm ci && npm run build`, then `npm start` (port 3100). Set these
environment variables **for both the build and the start**:

- `BACKEND_URL`: where the frontend server can reach the backend, for example `http://localhost:8081`.
- `ROOT_DOMAIN`: the studio's domain without a port, for example `yourdomain.com`.

### Domain, HTTPS and email

- Point both `yourdomain.com` and the wildcard `*.yourdomain.com` at the frontend. Your HTTPS
  certificate must cover the wildcard too.
- Only the frontend needs to be public, because it forwards `/api` calls to the backend.
  Keep the backend private.
- HTTPS is required. Production login cookies are marked `Secure`, so over plain `http` sign-in
  seems to work but you land back on the login page.
- Send email from a domain you own and set up SPF and DKIM with your email provider. Otherwise
  codes end up in spam. On staging, a test-inbox service keeps test emails away from real people.

### Before each release

- [ ] CI is green on `main` (backend tests, frontend lint, typecheck and build).
- [ ] New migrations only add things and have been tried on staging data.
- [ ] You signed in on staging as a creator and as a member, with real emails.
- [ ] Secrets live in the host's settings, never in files in the repository.
- [ ] Database backups are switched on.
