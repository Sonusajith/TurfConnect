# Sports Turf Booking Platform — Backend Architecture Plan

This maps directly onto the 5-Day / 5-Phase Implementation Plan. Each phase adds services and infrastructure on top of what the previous phase built — nothing is thrown away or re-architected later, only extended.

**Stack:** Spring Boot (microservices) · MongoDB · Redis · RabbitMQ/Kafka · WebSockets (STOMP over SockJS) · JWT + OAuth2 · Razorpay/Stripe · Docker · Kubernetes · AWS/GCP/Azure

---

## 1. Architecture Style & Principles

- **Pattern:** Microservices, one bounded context per business capability, each owning its own MongoDB database (database-per-service — no service reaches into another's collections directly).
- **Communication:**
  - **Synchronous (REST, via API Gateway):** anything the client needs an immediate answer for (login, search, slot check).
  - **Asynchronous (Kafka/RabbitMQ events):** anything that can happen "after the fact" without the caller waiting (notifications, analytics updates, leaderboard refresh, audit logs).
- **Design principles applied:** SOLID, Clean Architecture (controller → service → repository layering inside each service), Domain-Driven Design for bounded contexts, Repository Pattern via Spring Data MongoDB, CQRS only where read/write patterns genuinely diverge (analytics/BI service), Twelve-Factor App config (externalized config via Spring Cloud Config or K8s ConfigMaps/Secrets).
- **Statelessness:** every service is stateless and horizontally scalable; all session/lock state lives in Redis, not in-process memory.

---

## 2. Service Inventory (mapped to the 5-day phases)

| Service | Introduced | Owns (MongoDB DB) | Core Responsibility |
|---|---|---|---|
| **api-gateway** | Day 1 | — | Routing, JWT validation, rate limiting, request logging |
| **auth-service** | Day 1 | `authdb` | Registration, login, JWT issuance/refresh, OAuth2, password hashing |
| **turf-service** | Day 1 | `turfdb` | Turf CRUD, search, slot generation |
| **booking-service** | Day 1 | `bookingdb` | Slot locking, booking state machine |
| **payment-service** | Day 1 | `paymentdb` | Payment orders, webhooks, refunds (extended Day 2) |
| **notification-service** | Day 2 | `notificationdb` | Email/SMS/push delivery, retry, DLQ |
| **review-service** | Day 2 | `reviewdb` | Reviews, rating aggregation |
| **community-service** | Day 3 | `communitydb` | Teams, invitations, matches |
| **tournament-service** | Day 3 | `tournamentdb` | Tournament lifecycle, brackets, leaderboards |
| **recommendation-service** | Day 4 | `recommenddb` (or reads via aggregation) | Turf recommendations, demand heuristics |
| **fraud-signal-service** | Day 4 | uses Redis only | Velocity/anomaly checks |
| **analytics-bi-service** | Day 4/5 | `analyticsdb` (read replica-backed) | Dashboards, BI reports |
| **org-admin-service** | Day 5 | `orgdb` | Franchise hierarchy, RBAC scoping, audit logs |

Each service is independently deployable, independently scalable, and has its own CI/CD pipeline and container image.

---

## 3. High-Level Component Diagram (text-based)

```
                                   ┌─────────────────────┐
                                   │   Client Apps        │
                                   │ (Web / iOS / Android) │
                                   └──────────┬───────────┘
                                              │ HTTPS
                                   ┌──────────▼───────────┐
                                   │     API Gateway       │
                                   │ (routing, JWT check,  │
                                   │  rate limiting)        │
                                   └──────────┬───────────┘
              ┌──────────────┬────────────────┼────────────────┬──────────────┐
              │              │                │                │              │
        ┌─────▼─────┐  ┌─────▼─────┐   ┌──────▼──────┐  ┌──────▼──────┐ ┌─────▼──────┐
        │auth-service│  │turf-service│  │booking-service│ │payment-service│ │review-service│
        └─────┬─────┘  └─────┬─────┘   └──────┬──────┘  └──────┬──────┘ └─────┬──────┘
              │              │                │                │              │
              │        ┌─────▼──────────────────────────────────▼──────┐       │
              │        │            Redis Cluster                       │◄──────┘
              │        │ (slot locks, cache, sessions, leaderboards,     │
              │        │  fraud counters)                                │
              │        └─────────────────────────────────────────────────┘
              │
        ┌─────▼─────────────────────────────────────────────────────────────┐
        │                     Kafka / RabbitMQ (event bus)                    │
        │  topics: booking.confirmed, payment.failed, refund.processed,       │
        │  review.created, invitation.sent, match.scheduled, ...              │
        └─────┬──────────────┬───────────────┬───────────────┬───────────────┘
              │              │               │               │
       ┌──────▼──────┐ ┌─────▼──────┐  ┌─────▼──────┐  ┌─────▼───────────┐
       │notification- │ │community-  │  │tournament- │  │recommendation /  │
       │service       │ │service     │  │service     │  │analytics-bi svc  │
       └──────────────┘ └────────────┘  └────────────┘  └──────────────────┘

  Each service → its own MongoDB DB (replica set). WebSocket gateway sits alongside
  API Gateway for real-time slot-lock, invitation, and leaderboard push events.
```

---

## 4. Service-to-Service Communication Rules

| Interaction | Mechanism | Why |
|---|---|---|
| Client → any service | REST via API Gateway | Gateway is the single entry point; centralizes auth + rate limiting |
| booking-service → payment-service | Synchronous REST (create payment order) | Client is waiting for a payment link/order ID |
| payment-service → booking-service | Kafka event (`payment.success` / `payment.failed`) | Booking confirmation shouldn't block on webhook round-trip |
| booking-service → notification-service | Kafka event (`booking.confirmed`) | Fire-and-forget, async, retryable |
| community-service → notification-service | Kafka event (`invitation.sent`) | Same reasoning |
| any service → analytics-bi-service | Kafka event stream | BI reads should never add load/latency to transactional path |
| Cross-service data lookups (e.g., booking needs turf name) | Either (a) REST call with circuit breaker, or (b) denormalized copy stored at write time | Prefer (b) for frequently-read, rarely-changed fields (turf name, city) to avoid chatty synchronous calls — classic MongoDB denormalization strategy applied at the service-integration level |

**Resilience:** every synchronous inter-service call goes through a circuit breaker (Resilience4j) with timeout + fallback, so one struggling service doesn't cascade-fail its callers.

---

## 5. API Gateway Design

- **Responsibilities:** routing to services, JWT signature + expiry validation, role extraction from JWT claims, per-user/IP rate limiting (Redis-backed token bucket), request/response logging, CORS policy enforcement.
- **Routing table example:**
  - `/api/v1/auth/**` → auth-service
  - `/api/v1/turfs/**` → turf-service
  - `/api/v1/bookings/**` → booking-service
  - `/api/v1/payments/**` → payment-service
  - `/api/v1/teams/**`, `/api/v1/matches/**` → community-service
  - `/api/v1/tournaments/**` → tournament-service
  - `/api/v1/recommendations/**` → recommendation-service
  - `/api/v1/admin/**` → org-admin-service (requires ORG_ADMIN/SUPER_ADMIN role claim)
- **Not a business-logic layer** — the gateway never contains booking/payment logic itself; it only routes and enforces cross-cutting concerns.

---

## 6. Data Layer Architecture

- **Database-per-service:** each service owns its schema; no service queries another's MongoDB directly. Cross-service reads happen via REST/events, never shared DB access.
- **Replica sets:** every MongoDB deployment is a 3-node replica set (1 primary, 2 secondaries) from Day 1 — non-negotiable even in the fast-tracked build, since slot-booking correctness depends on durable writes.
- **Read preference:** transactional services (booking, payment) read from `primary` for consistency; reporting/analytics reads from `secondaryPreferred` to avoid competing with transactional load.
- **Write concern:** `majority` for booking/payment writes (must survive a primary failover); `1` acceptable for non-critical writes like notification logs.
- **Sharding (introduced conceptually Day 5, executed post-launch):** shard key candidate `country` or `city` for turf/booking collections, since queries are naturally scoped by geography — this keeps chunks aligned with access patterns and enables regional data locality for the multi-country phase.
- **Transactions:** MongoDB multi-document transactions used narrowly — e.g., booking confirmation + slot-status update within booking-service — kept short-lived to avoid holding locks.

---

## 7. Redis Usage Across Phases

| Use case | Introduced | Pattern |
|---|---|---|
| Slot distributed lock | Day 1 | `SET lock:slot:{turfId}:{date}:{time} NX PX 300000` |
| Turf/search cache | Day 2 | Cache-aside, externalized TTL, scan eviction on update |
| Tournament leaderboard | Day 3 | Sorted sets (`ZADD`/`ZREVRANGE`) |
| Fraud velocity counters | Day 4 | `INCR` with expiring window keys |
| Session/refresh-token blacklist | Day 1 | Key-value with TTL matching token expiry |
| Rate limiting at gateway | Day 1 | Token bucket counters per user/IP |

### Redis Caching Architecture (Module 12)

#### 1. Cache-Aside Workflow
On read requests:
1. Generate versioned cache key.
2. Check Redis via `RedisTemplate`. If hit, deserialize using Jackson and return immediately.
3. On miss, fetch from MongoDB, serialize to Jackson JSON format, set in Redis with TTL, and return.
4. On write requests (create, update, delete, review submission), evict relevant cache keys immediately to maintain consistency.

```
       [Read Request]
             │
             ▼
      Check Redis Cache
        /         \
   (Hit)           (Miss / Failure)
    /                 \
Return Data       Fetch from MongoDB
                       │
                  Write to Redis
                       │
                  Return Data
```

#### 2. Key Versioning and Conventions
All keys prefix with a version identifier (`v1`) to allow instant invalidation of the entire cache when document schemas evolve:
- **Single Turf:** `v1:cache:turf:{turfId}` (TTL: 10 minutes)
- **Search Pages:** `v1:cache:turfs:{SHA-256 of criteria parameters}` (TTL: 5 minutes)
- **Reviews List:** `v1:cache:reviews:turf:{turfId}` (TTL: 5 minutes)

#### 3. Serialization Configuration
Both `turf-service` and `review-service` configure custom `RedisTemplate` beans utilizing:
- **StringRedisSerializer** for keys to keep them human-readable.
- **Jackson2JsonRedisSerializer** for values, configured with `JavaTimeModule` to support modern Java Time APIs, and a secure `BasicPolymorphicTypeValidator` to allow safe round-trip deserialization of target responses (`com.turfconnect.*`, `java.util.*`, `org.springframework.data.domain.*`). Native JVM serialization is strictly avoided.

#### 4. Safe Eviction Strategy
- **Scan-based Wildcard Invalidation:** Wildcard eviction of search cache pages (e.g. invalidating search pages on turf updates) uses Redis `SCAN` commands via `redisTemplate.scan(ScanOptions)` to progressively identify keys matching `v1:cache:turfs:*`. This avoids blocking the Redis event loop, which occurs with the O(N) `KEYS` command.
- **Eviction Hooks:** Cache eviction is triggered on turf creation/updates/deletions, review submissions, review edits, review deletions, and owner replies to ensure reviews list and turf data remains consistent.

#### 5. Graceful Degradation (Fail-Open)
All interactions with Redis are wrapped in protective try-catch handlers. If Redis goes offline:
- Redis failures are logged as warnings.
- The request execution degrades gracefully to the database (MongoDB) without failing user requests (fail-open strategy).
- Redis health indicators are disabled (`management.health.redis.enabled: false`) in development environments to prevent mock/offline configurations from causing overall application health checks to report as `DOWN`.

---

## 8. Messaging / Queue Architecture

- **Broker choice:** Kafka for high-throughput event streams that analytics/BI will later consume (booking, payment, review events); RabbitMQ acceptable alternative for simpler fire-and-forget notification queues if the team is more familiar with it — **recommendation: standardize on Kafka platform-wide** to avoid running two broker technologies in production, even though RabbitMQ is simpler for Day-2 notifications alone.
- **Delivery guarantees:** at-least-once delivery; every consumer is idempotent (dedup by event ID / booking ID) since duplicates are expected on retry.
- **Dead Letter Queue:** after N retry attempts (exponential backoff), message moves to a DLQ topic/queue for manual inspection — critical for `payment.failed` and `refund.processed` events where silent drops are unacceptable.
- **Ordering:** partition by `bookingId` (Kafka) so all events for one booking are processed in order on one consumer instance.
- **Consumer scaling:** consumer groups scale horizontally with partition count; K8s HPA scales notification-service/analytics-service pods based on consumer lag metrics.

---

## 9. WebSocket Architecture

- **Transport:** STOMP over SockJS, fronted by a dedicated `websocket-gateway` (can be co-located with API Gateway for the fast-tracked build, split out later for scale).
- **Channels:**
  - `/topic/slot-lock/{turfId}/{date}` — real-time slot availability updates (Day 1)
  - `/topic/invitation/{userId}` — team invitation push (Day 3)
  - `/topic/leaderboard/{tournamentId}` — live leaderboard updates (Day 3)
- **Backing:** Redis Pub/Sub used to fan out WebSocket messages across multiple gateway instances (since WebSocket connections are sticky to one instance, but the event may originate from any backend service).
- **Failure handling:** client-side reconnect with exponential backoff; server holds no critical state in the WebSocket session itself — reconnecting client re-fetches current state via REST, then resumes live updates.

---

## 10. Security Architecture

- **AuthN:** JWT access tokens (short-lived, ~15 min) + refresh tokens (long-lived, stored hashed, rotated on use) issued by auth-service; OAuth2 (Google/Apple) as an alternative login path feeding into the same JWT issuance flow.
- **AuthZ:** Role claims embedded in JWT (`PLAYER`, `TURF_OWNER`, `FRANCHISE_ADMIN`, `ORG_ADMIN`, `SUPER_ADMIN` — full set lands Day 5); enforced via Spring Security `@PreAuthorize` at the method level in each service, not just at the gateway.
- **Password storage:** BCrypt, cost factor tuned per infra benchmarking (typically 10-12).
- **Transport:** HTTPS/TLS everywhere, including internal service-to-service calls in production (mTLS optional hardening step for the service mesh, post-launch).
- **Application-layer protections:** CORS allow-list per environment, CSRF not needed for stateless JWT APIs (but required if any cookie-based session is introduced), input validation at controller layer to block injection, output encoding to prevent XSS in any server-rendered content.
- **Rate limiting:** enforced at gateway (Redis token bucket) — protects auth endpoints from brute force and booking endpoints from scalping bots.
- **Audit logs:** every admin/franchise-level action logged with actor, action, before/after state (org-admin-service, Day 5) — stored in an append-only `auditlogs` collection.

---

## 11. Deployment Architecture

- **Containerization:** one Dockerfile per service, multi-stage builds (build stage with Maven/Gradle, slim runtime image with just the JAR + JRE).
- **Orchestration:** Kubernetes — each service is a Deployment + Service (+ HPA); MongoDB and Redis run as StatefulSets (or as managed cloud services — **recommended** for production: MongoDB Atlas / managed Redis, rather than self-hosting stateful data infra in K8s, since managed services handle replica-set failover and backups far more reliably than a self-run operator for a team of this size).
- **Environments:** `dev` → `staging` → `production`, promoted via CI/CD pipeline (build → test → containerize → deploy), config externalized via K8s ConfigMaps/Secrets (Twelve-Factor).
- **Ingress:** single ingress controller routes to API Gateway; gateway is the only public-facing service.
- **Scaling:** HPA on CPU/memory initially; upgraded to custom metrics (Kafka consumer lag, Redis lock contention) once the platform has real traffic data to tune against.
- **Multi-region (Day 5 concept, real work post-launch):** active-passive to start (single write region, DR region on standby) is the pragmatic recommendation over active-active — active-active multi-region with MongoDB requires careful shard/zone design and conflict handling that's disproportionate to invest in before the platform has proven multi-country demand.

---

## 12. Observability

- **Logging:** structured JSON logs from every service, shipped to a central store (ELK/EFK stack or cloud-native equivalent).
- **Metrics:** Micrometer + Prometheus for service-level metrics (latency, error rate, throughput); Grafana dashboards per service.
- **Distributed tracing:** OpenTelemetry/Zipkin trace ID propagated through gateway → services → queue consumers, so a single booking request can be traced end-to-end across the async boundary.
- **Health checks:** Spring Boot Actuator `/health` and `/ready` endpoints wired to K8s liveness/readiness probes.
- **Alerting:** on error-rate spikes, consumer lag growth, Redis lock contention, MongoDB replica lag.

---

## 13. Architecture Evolution Across the 5 Days (Summary)

| Day | Architectural Addition |
|---|---|
| 1 | Core services, API Gateway, Redis locking, WebSocket for slot updates, single Kafka/RabbitMQ topic set for basics |
| 2 | Notification-service + DLQ, review-service, expanded Redis caching, refund state machine |
| 3 | Community/tournament services, Redis leaderboards, more WebSocket channels |
| 4 | Recommendation + fraud-signal services, analytics event stream, BI aggregation reads from secondary replicas |
| 5 | Org-admin-service, full RBAC, sharding strategy finalized, multi-region deployment design, audit logging |

Nothing from Day 1 is discarded — Day 5's architecture is Day 1's architecture with services, queues, and infra layered on top, which is why a database-per-service and event-driven boundary from the very start matters: it's what makes the later phases additive instead of requiring a rewrite.

---

## What This Plan Deliberately Doesn't Cover Yet

Per the hardening backlog from the implementation plan, this architecture is *designed for* but doesn't yet include fully executed: production sharding, chaos-tested failover, a real ML pipeline behind recommendation-service, and multi-region cutover. Those are infrastructure and data-science workstreams that follow this architecture, not deviate from it.

---

If useful, I can next produce:
1. A **sequence diagram** (text-based) for the booking → payment → confirmation flow specifically, showing every service/queue hop, or
2. A **MongoDB schema design doc** per service (collections, fields, indexes) to sit underneath this architecture, or
3. A **Kubernetes manifest set** (Deployment/Service/HPA YAML) for one or two of these services as a concrete starting point.

## Fraud-Signal Service Details

### Workflow
1. Event Processing Lifecycle: The service consumes BookingEvent messages from RabbitMQ using a durable queue (raud.booking.queue) with a Dead Letter Queue (DLQ) configured for failed processing.
2. Redis Counter Strategy: Uses atomic INCR followed conditionally by EXPIRE to maintain rolling window velocity metrics. Redis acts as the sole data store, enforcing a stateless and highly concurrent microservice.
3. Configurable Thresholds: All thresholds (e.g., max 5 bookings in 1 hour) are externalized to application configuration for dynamic tuning without redeployment.
4. Alert Publishing Flow: When a threshold is breached, the user is flagged in Redis (with a TTL to prevent alert spam) and a FraudAlertEvent is published to the raud.exchange for downstream reaction by Auth or Analytics services.
