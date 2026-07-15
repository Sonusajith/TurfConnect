# AGENTS.md
This file is read automatically by Codex CLI and Google Antigravity (cross-tool standard). Keep it accurate — it's the single source of truth both agents work from every session.

## Project
Sports turf booking platform (Playo/Hudle-style) — academic project, built solo, module by module, with AI coding assistance reviewed and understood at every step.

## Tech Stack
- Backend: Spring Boot (Java), microservices — one per business capability
- Database: MongoDB, database-per-service, no cross-service DB access
- Cache/locking: Redis (distributed locks, cache-aside, sorted sets, rate limiting)
- Messaging: RabbitMQ (Kafka only if explicitly requested later)
- Real-time: WebSockets (STOMP/SockJS)
- Auth: JWT + refresh tokens, OAuth2 (Google/Apple), BCrypt, Spring Security `@PreAuthorize`
- Payments: Razorpay/Stripe, **test mode only**
- Containers/orchestration: deferred — do not add Docker/K8s files until explicitly told the project has reached that stage
- Frontend: (fill in once chosen)

## Hard Rules — Never Violate
1. **Database-per-service.** A service may never query another service's MongoDB collections directly. Cross-service data goes through REST calls or events, or is denormalized at write time.
2. **Stay inside the current module's scope** (see "Current Module" below). Do not implement features from a later module even if it seems convenient or "while I'm in there."
3. **Every new piece of business logic needs a unit test.** Don't skip this because it's a student project — the tests are also how the developer proves the code works.
4. **Comment anything non-obvious** — especially Redis locking, MongoDB aggregation pipelines, and state machine transitions — in plain language, since these need to be explained in a viva.
5. **No Docker/Kubernetes files** until the project reaches the containerization module — this is intentionally deferred.
6. Ask before making an architectural decision not already specified in `/docs` (e.g., choosing between two valid approaches) rather than silently picking one.

## Current Module
> **Update this section every time you move to a new module. This is the single most important line in this file — agents only build what's listed here.**

**Module:** Module 1 — Shared foundation + gateway skeleton
**Scope (IN):** Shared model classes, base utility packages, gateway security filters skeleton, routing configuration templates, basic validation dependencies.
**Explicitly OUT:** Database/Redis connections integration, real-time WebSocket messaging, full JWT security filters implementation.
**Definition of done:** Gateway and shared foundation compile and package successfully; gateway routes map successfully on local testing.

## Reference Docs
- `/docs/srs.md` — full feature specs (functional requirements, acceptance criteria, API design, DB schema, etc.)
- `/docs/backend-architecture.md` — service map, communication rules, security, deployment design
- `/docs/module-roadmap.md` — the full module-by-module build order, current stage
- `/docs/PROGRESS.md` — what's done, what's pending
- `/docs/DEV_LOG.md` — running log of decisions and issues per module

## Testing Expectations
- Unit tests for all service-layer logic
- At least one test that proves any concurrency-sensitive logic (e.g., the slot-lock race condition) actually works under simultaneous requests
- Manual test steps included in the PR description for anything with an API endpoint

## Git Convention
- One branch per module: `feature/module-<number>-<short-name>`
- Commit message format: `feat(<service>): <what> — <why>`
- Do not merge a module's branch until every diff has been read and understood by the developer, not just the agent

---

## Antigravity-Specific Notes
Use Antigravity primarily for **UI/visual modules** (frontend screens, admin dashboard, real-time WebSocket UI). After building any UI piece, verify it in the integrated browser and capture a screenshot/walkthrough artifact — these double as evidence for the project report. Do not use multi-agent parallel orchestration on backend modules; work one module at a time so every change stays traceable and explainable.
