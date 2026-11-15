# Roadmap

Status key: ✅ done · 🔜 next · 🗓 planned · 💡 idea

## M0: Foundations ✅ (this repository)

- ✅ Monorepo: Spring Boot API, Next.js frontend, Postgres + Flyway, docker-compose, CI
- ✅ Creator sign-up / sign-in (email code), onboarding (profile → community name), studio dashboard
- ✅ Member sign-up on the community's own subdomain, onboarding, member home
- ✅ White label: per-community subdomain routing, branding (name, tagline, about, accent, logo URL),
  community-branded emails, tenant-isolated accounts and sessions
- ✅ Members list for creators (search, pagination), dashboard stats
- ✅ Integration tests for auth, onboarding, tenant isolation, refresh-token rotation, CSRF

## M1: First product: the Course app 🔜 (design slides 5–15)

Backend
- `product` (belongs to a community: name, description, thumbnail, status draft/published)
- `product_app` (which apps a product includes; Course first)
- `course_chapter`, `course_lesson` (ordering, title, description, content type: video upload,
  video embed, document, quiz), `lesson_attachment`
- File storage abstraction (`StorageService`): local disk in dev, S3-compatible (AWS S3,
  Cloudflare R2, MinIO) in prod, with pre-signed uploads so files don't pass through the API
- Video: start with embeds (YouTube/Vimeo/Loom) + direct upload to storage; evaluate Mux or
  Cloudflare Stream for adaptive playback later
- Publish flow + shareable product link ("Your course is ready!")

Frontend
- "Let's build your first product" picker → "Create your course" modal (thumbnail, name, description)
- Course builder: chapters sidebar, add lesson, lesson editor (upload/embed video, document, quiz,
  attachments, description), reorder by drag-and-drop
- Member side: product page, lesson player, progress tracking

Also in M1
- Avatar and logo uploads (reuse `StorageService`)
- Google and Apple sign-in (buttons are already in the UI)

## M2: Monetisation 🗓

- Pricing per product: one-time, subscription (monthly/yearly), free trial
- Payment gateway: **open question** (Razorpay for UPI/INR-first, Stripe for global; possibly both
  behind a `PaymentProvider` interface)
- Checkout on the white-label site, entitlements (`member_product_access`), webhooks
- Creator payouts / connected accounts, invoices, refunds
- Studio: Payments page, revenue on the dashboard

## M3: Custom domains & deeper white label 🗓

- "Use your own domain" flow: add domain → show DNS records (CNAME) → verify → mark `verified_at`
  (the table and the proxy lookup already exist)
- Automatic TLS for custom domains: Caddy on-demand TLS, Cloudflare for SaaS, or Vercel's domains
  API, depending on hosting (**open question**)
- Per-community email sender domain (SPF/DKIM), custom favicon, hide "powered by" (plan-gated)
- Plans/tiers for creators (e.g. custom domain on Pro)

## M4: Community modules 🗓

Live classes with replays, community chat (channels by product), events with RSVPs, forums/Q&A,
announcements, downloads, Discord/Telegram/WhatsApp access on purchase. The prototype in
`../enclave_sample` sketches many of these.

## M5: AI modules 💡 (the differentiator)

Ideas to validate with creators. Postgres + `pgvector` keeps the stack simple for retrieval:

- **Course co-pilot**: turn a topic or a few voice notes into a course outline, lesson scripts,
  quizzes and descriptions
- **Member assistant**: a chatbot on the white-label site that answers from the creator's own
  lessons and posts (RAG), in the creator's voice, with citations to lessons
- **Churn and engagement insights**: flag members likely to cancel (no logins, stalled progress,
  failed payments) and draft a personal nudge
- **Content repurposing**: lesson → newsletter, social posts, short clips with captions
- **Auto-moderation and community digest**: summarise chat/forums, surface unanswered questions
- **Onboarding copywriter**: write the site's tagline, about and product pages from a short brief

Architecture note: keep AI behind an `ai/` backend module with a provider interface, store
prompts/versions, log usage per community (for metering and pricing), and never mix tenants'
content in one retrieval index.

## Platform hardening (ongoing) 🗓

- Per-IP rate limiting (e.g. Bucket4j + Redis) on sign-in endpoints
- Scheduled cleanup of expired codes and refresh tokens
- Audit log for creator actions; admin console for platform staff
- Observability: structured logs, metrics (Micrometer → Prometheus/Grafana), error tracking
- Dockerfiles for both apps and a deploy pipeline (**open question**: hosting target)
- Move the studio session read to the server (Next.js Cache Components pattern) for instant navigation
- Dark mode, accessibility audit, i18n

## Open questions for the team

1. **Payments**: Razorpay, Stripe, or both? Which countries first?
2. **Hosting**: where do the API, frontend and Postgres run (AWS, GCP, Render/Railway, Vercel + a JVM host)?
   This decides the custom-domain TLS approach.
3. **Product name and domain**: replaces "Project Untitled" (`BRAND_NAME` in the frontend,
   `app.brand-name` in the backend, Java package `com.projectuntitled`).
4. **Multiple communities per creator**: needed soon, or later? (One unique constraint to drop.)
5. **Which AI module ships first?** The member assistant and the course co-pilot are the strongest candidates.
6. **Mobile apps**: native apps per creator (true white label) or a PWA first?
