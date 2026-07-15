# Sports Turf Booking Platform — 5-Phase / 5-Day Implementation Plan

## Reality Check First

Before the plan itself: a genuinely production-grade platform on this stack (Spring Boot microservices, MongoDB, Redis, Kafka/RabbitMQ, WebSockets, Kubernetes, multi-region) is normally an **18–36 week** effort for a small team, even for just Phases 1–3. Phases 4 (AI) and 5 (Enterprise/multi-country) are typically 6–12+ months of additional work on top of a stable base.

So "5 phases in 5 days" only makes sense under one of these framings — pick whichever matches your actual goal, because it changes what "Day 1" means:

| Framing | What "1 day per phase" really means |
|---|---|
| **A. Hackathon/prototype** | Build a thin, working vertical slice per phase — real code, minimal edge-case handling, single-node deployment, no real scale-testing |
| **B. Planning sprint (not coding)** | Spend each day fully *designing* that phase (SRS, schema, APIs, diagrams) — code comes later, in weeks, from these docs |
| **C. Team of specialists working in parallel** | A day = one working day for a full squad (5-8 engineers) working phase tasks in parallel tracks, not sequentially |

The plan below is written as **Framing C** (a real team, 5 intense working days, each day = one phase), since that's the closest thing to a legitimate "5 days" reading of your roadmap. I've flagged where corners are necessarily cut for the timeline, and what "hardening" work would still need to happen afterward for true production-readiness.

---

## Team Assumption

- 1 Tech Lead / Architect
- 2 Backend Engineers (Spring Boot / MongoDB / Redis)
- 1 DevOps Engineer (Docker/K8s/CI-CD)
- 1-2 Frontend/Mobile Engineers
- 1 QA Engineer
- 1 Product/BA (writes stories, validates acceptance criteria)

---

## DAY 1 — Phase 1: MVP Core Booking Platform

**Goal:** A user can register, browse turfs, check slot availability, and complete a booking with payment.

### Morning — Design & Scaffolding
- Finalize MongoDB schemas: `users`, `turfs`, `slots`, `bookings`, `payments`
- Define indexes: unique on `users.email`, compound `(turfId, date, slotTime)` on `slots` for availability lookups
- Scaffold microservices: `user-service`, `turf-service`, `booking-service`, `payment-service`, API Gateway
- Set up Docker Compose for local dev (MongoDB, Redis, all services)
- Set up base CI pipeline (build + test on push)

### Midday — Core Build
- **user-service:** registration, login, JWT issuance, refresh tokens, BCrypt password hashing
- **turf-service:** CRUD for turf listing, turf search by city/sport, slot generation logic
- **booking-service:** slot lock (Redis distributed lock, TTL-based) → booking creation → booking state machine (`PENDING → CONFIRMED → CANCELLED`)
- **payment-service:** Razorpay/Stripe integration, order creation, webhook handler for payment confirmation
- Redis: slot-locking keys (`lock:slot:{turfId}:{date}:{time}`), TTL 5 min, released on payment success/failure

### Afternoon — Integration & UI
- API Gateway routing + JWT validation filter
- Frontend/mobile: Registration → Login → Browse turfs → Slot selection → Payment → Confirmation screens (loading/empty/error states minimally covered)
- WebSocket: real-time slot-lock broadcast so two users don't see a stale "available" slot

### End of Day — Validation
- Unit tests for booking state transitions and slot-locking race condition
- Manual QA: happy path booking end-to-end
- Deploy to a single-node staging environment (K8s manifests written, not yet HA)

**Explicitly deferred to later hardening:** sharding, multi-region, chaos testing, full RBAC, refund flows, dead-letter queues.

---

## DAY 2 — Phase 2: Enhanced Features (Payments, Reviews, Notifications)

**Goal:** Booking flow becomes robust — refunds, reviews, notifications, and better payment failure handling.

### Morning
- Extend `payments` collection: refund sub-document, `paymentStatus` state machine (`INITIATED → SUCCESS/FAILED → REFUND_INITIATED → REFUNDED`)
- Add `reviews` collection (embedded turf rating summary + referenced review docs)
- Introduce message queue (RabbitMQ/Kafka) for async notification events

### Midday
- **notification-service:** consumes `booking.confirmed`, `payment.failed`, `refund.processed` events → sends email/SMS/push
- Retry policy + Dead Letter Queue for failed notification deliveries
- Refund workflow: cancellation triggers refund state machine, idempotent processing keyed by `bookingId`
- Redis: cache turf details and average ratings (`cache:turf:{id}`, TTL 10 min, invalidated on update/new review)

### Afternoon
- Review submission UI (post-booking only, validated against booking completion)
- Notification preferences (opt-in/out) in user settings
- Add pagination, filtering, sorting to turf search API (`?sortBy=rating&city=&sport=&page=&size=`)

### End of Day — Validation
- Integration tests: refund idempotency (duplicate webhook delivery), notification retry/backoff
- Load test the turf-search endpoint with cached vs. uncached Redis paths

**Deferred:** advanced fraud checks on refunds, multi-channel notification templating at scale, SLA-based retry tuning.

---

## DAY 3 — Phase 3: Community Features (Teams, Matches, Tournaments)

**Goal:** Players can form teams, request matches, and turf owners/admins can run tournaments.

### Morning
- New collections: `teams`, `matches`, `tournaments`, `invitations`
- Design relationships: `teams` reference `users` (many-to-many via `teamMembers` embedded array with role), `matches` reference two `teams` + a `slot`/`booking`
- Match/Tournament state machines: `matches` (`SCHEDULED → ONGOING → COMPLETED/CANCELLED`), `tournaments` (`DRAFT → OPEN_FOR_REGISTRATION → IN_PROGRESS → COMPLETED`)

### Midday
- **community-service:** team creation, invite flow (WebSocket + notification event), match creation tied to a confirmed booking
- **tournament-service:** tournament creation (admin/turf owner), team registration, bracket/leaderboard generation
- Redis: leaderboard via sorted sets (`ZADD leaderboard:tournament:{id}`)

### Afternoon
- UI: Team dashboard, "Invite Player" flow, Match scheduling screen, Tournament bracket view
- Player invitation state machine (`SENT → ACCEPTED/DECLINED/EXPIRED` with TTL-based auto-expiry)

### End of Day — Validation
- Test concurrent team invitations, match double-booking prevention (must reference an already-locked slot)
- Verify tournament leaderboard updates in real time via WebSocket

**Deferred:** complex tournament formats (multi-round elimination edge cases), team-vs-team dispute/reporting flows.

---

## DAY 4 — Phase 4: AI Features (Recommendations, Predictions, Analytics)

**Goal:** Add a recommendation layer and basic predictive/analytics capability without disrupting core services.

### Morning
- Design **recommendation-service** as a separate microservice (keeps AI experimentation isolated from core booking reliability)
- Data pipeline: booking/review/search events streamed via Kafka into an analytics store (or MongoDB aggregation pipelines for a first-pass version, given the timeline)
- Define feature set for v1: turf recommendation by past bookings + city + sport popularity; demand-based dynamic pricing signal (not full ML yet — start with a rules/aggregation-based heuristic)

### Midday
- Build turf recommendation endpoint using MongoDB aggregation (co-occurrence of sport+city+rating) as the pragmatic Day-4 version; note in backlog that a real ML model (collaborative filtering / learned ranking) is the Phase-4 "true" version, needing a proper offline training pipeline
- Demand-prediction heuristic: aggregate historical booking density by slot/day-of-week to flag high-demand windows → feeds a suggested dynamic-pricing multiplier (human-approved, not auto-applied yet)
- Basic fraud-signal service: velocity checks (too many bookings/cancellations from one account in a short window) using Redis counters

### Afternoon
- Chatbot stub: FAQ-driven support bot (rules-based/intent-matching) with a clear seam to later swap in an LLM-based service
- Analytics dashboard (admin): bookings/day, revenue/turf, cancellation rate, top sports/cities — built off aggregation pipelines

### End of Day — Validation
- Validate recommendation endpoint doesn't add unacceptable latency to turf-browsing flow (should be async/pre-computed, not blocking)
- Confirm fraud-signal thresholds don't produce false positives on test data

**Important honesty note:** a real production ML system (training pipeline, model versioning, A/B testing, drift monitoring) is not something you build in one day. Day 4 here delivers the *architecture seam and a heuristic v1* — the "AI" is legitimately rules/aggregation-based on Day 4, with the ML upgrade path documented for a later phase.

---

## DAY 5 — Phase 5: Enterprise (Multi-country, Franchise Management, BI)

**Goal:** Platform is structured to support multiple countries/currencies and franchise-level administration, plus BI reporting.

### Morning
- Add `country`, `currency`, `locale` fields across relevant collections (`turfs`, `bookings`, `payments`) with validation
- Design franchise hierarchy: `organizations → franchises → turfs`, with role-based access scoping (an org admin sees only their franchises' data)
- Multi-currency payment handling: currency conversion at display time, settlement in local currency per Razorpay/Stripe regional accounts

### Midday
- RBAC expansion: `SUPER_ADMIN`, `ORG_ADMIN`, `FRANCHISE_ADMIN`, `TURF_OWNER`, `PLAYER` — enforce via JWT claims + method-level `@PreAuthorize`
- Sharding strategy discussion/design: shard key candidates (e.g., `country` or `city` as a shard key) to support geographic data locality and future multi-region reads
- Multi-region deployment plan: read replicas per region, active-active vs. active-passive decision, documented (actual multi-region cutover is infra work beyond a single day)

### Afternoon
- BI/reporting service: cross-franchise revenue reports, occupancy rates, growth trends — built as read-only aggregation views, isolated from transactional load (read from secondary replicas)
- Audit logging: all admin actions logged with actor, action, timestamp, before/after state

### End of Day — Validation
- Verify data isolation between franchises (an ORG_ADMIN cannot query another org's data)
- Run through full regression: booking → payment → refund → review → team → tournament → recommendation → admin report, end to end
- Final retro: log what was deferred, and produce a hardening backlog (see below)

---

## Post-Day-5 Hardening Backlog (Required Before Real Production Launch)

Even after this 5-day build, these are **not optional** for a genuine production system at the scale your prompt describes, and should be scheduled immediately after:

- MongoDB replica set + sharding rollout (not just designed)
- Kubernetes HPA, multi-AZ/multi-region deployment, and actual failover testing
- Chaos testing (MongoDB failover, Redis failure, queue outage, payment gateway timeout)
- Full RBAC + security audit (CORS, CSRF, XSS, injection, rate limiting, pen test)
- Load/stress testing at target scale (thousands of concurrent bookings)
- Real ML pipeline for recommendations/pricing (replacing the Day-4 heuristic)
- Comprehensive edge-case and race-condition test suite (concurrent slot booking, double refunds, WebSocket reconnect storms)
- Monitoring/observability stack (distributed tracing, dashboards, alerting) fully wired, not just service-level logs

---

## Summary Table

| Day | Phase | Core Deliverable | Biggest Risk If Rushed |
|---|---|---|---|
| 1 | MVP | End-to-end booking + payment | Slot-locking race conditions |
| 2 | Enhanced | Refunds, reviews, notifications | Refund idempotency, notification storms |
| 3 | Community | Teams, matches, tournaments | Double-booking via match scheduling |
| 4 | AI | Recommendations, fraud signals, analytics | Overstating "AI" — it's heuristic-based on Day 4 |
| 5 | Enterprise | Multi-country, franchise RBAC, BI | Data isolation leaks between franchises |

If you'd like, I can now go deep on **any single day/phase** using your full documentation template (Feature Overview through Failure Scenarios) — that's where the real implementation-ready detail (schemas, API specs, state machines, sequence diagrams) belongs, since one file can't hold all 5 phases at that depth without becoming unusable.
