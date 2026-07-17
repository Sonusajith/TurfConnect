# Developer Log â€” TurfConnect

This file is a running log of decisions, debug sessions, and important configurations resolved per module.

---

## Stage 0 â€” Setup (Day 0)
- **Decision:** Directory layout organized with `/docs` containing structured design specs, `/backend` containing Spring Boot 3.3.x Maven projects, and `/config` containing centralized application configuration profiles.
- **Port Strategy:** Standardized port assignments across all services from 8080 (API Gateway) to 8091 (Org Admin) to avoid development clashes.
- **Centralized Configuration:** Configured services to optionally look at files in the shared `/config` root folder.

---

## Module 7 â€” Minimal Frontend
- **Framework & Structure:** Selected Vite + React for lightweight rendering and standard state hooks. Implemented a robust features-based directory structure (`features/`, `components/`, `services/`, `hooks/`, `contexts/`) ensuring clean codebase isolation and readiness for Redux/Zustand scaling.
- **Centralized API Client:** Created an Axios interceptor client that dynamically appends Bearer JWTs to incoming gateway requests and handles auto-refresh tokens seamlessly when catching 401 statuses.
- **Real-Time WebSocket Hook:** Wired `WebSocketConfig` and `SlotBroadcaster` into the `turf-service` backend, utilizing standard `spring-boot-starter-websocket`. Built `useSlotSocket.js` on SockJS + STOMP in the frontend to trigger slot updates without long-polling.
- **Design system:** Applied CSS variables for colors (Athletic Deep Green `#1B5E20`, CTA Energetic Orange `#FF6D00`) and rounded borders (`rounded-2xl` for widgets) to adhere to the Athletic Synergy specifications.
- **Testing:** Enabled Vitest + JSDOM to validate authentication, protected paths, slot search parameters, booking modals, and WebSocket handlers. Checked off Stage 1 MVP milestone.

---

## Module 9 â€” RabbitMQ & Notification Service
- **Broker Topology:** Declared topic exchange configurations (`booking.exchange` and `payment.exchange`) and queue bindings (`booking.notification.queue`, `payment.notification.queue`) routing by prefix event keys. 
- **Resiliency & Error Handling:** Configured Dead Letter Exchange (`notification.dlx`) and direct Dead Letter Queues (`booking.notification.dlq`, `payment.notification.dlq`) to capture unacknowledged or toxic messages.
- **Actuator Health Adjustment:** Configured health checks for `rabbit` and `redis` to run as disabled in `application-dev.yml` to prevent entire microservices from failing with 503 SERVICE_UNAVAILABLE codes when AMQP brokers are not active locally during developmental offline launches.
- **Testing:** Wrote unit tests for `NotificationListener` confirming message processing routes correctly. Verified all backend modules compile successfully with Maven.

---

## Module 10 â€” Reviews & Ratings
- **Decentralized Reviews Microservice:** Created the new `review-service` microservice, utilizing MongoDB (`turfconnect_reviews` database) for storage, adhering to the database-per-service paradigm.
- **Review Submission Restrictions:** Secured review submissions to require a validated CONFIRMED booking belonging to the active user retrieved dynamically via REST call to the `booking-service`. Added checks to prevent duplicate reviews for the same booking.
- **Event-Driven Aggregation:** Built aggregation pipelines using Spring Data MongoDB to calculate the average rating and total review counts per turf. Emitted `ReviewEvent` with UUID, eventType, timestamp, version, and metrics to `review.exchange` on MongoDB writes.
- **Automatic Turf Rating Propagation:** Configured `turf-service` to consume `ReviewEvent` via RabbitMQ binding, update the local turf entity's `averageRating` cache-aside store, and invalidate cached turf queries.
- **Future Readiness & Enhanced Fields:** Enhanced the `Review` entity with future-proofing fields: soft delete (`isDeleted`), edited status (`isEdited`), `updatedAt`, `status` (`ACTIVE`, `HIDDEN`, `DELETED`), and `ownerReply` placeholders to avoid future schema changes.
- **Testing:** Wrote exhaustive unit tests in `review-service` checking validations, duplicate submissions, and aggregation math. Wrote AMQP listener tests in `turf-service` verifying event consumption and rating updates. Verified end-to-end routing and validation using Python test script.

---

## Module 11 — Refunds & Payment State Extension (2026-07-16)

### Key Decisions

- **Embedded Refund sub-document:** Refund is embedded inside Payment (not a separate collection) to keep refund lifecycle atomically tied to its payment. MongoDB document-level atomicity handles all state transitions.
- **Idempotency keyed on bookingId:** Duplicate refund calls return the current refund state without re-calling the gateway. Guard: isRefundLifecycleActive() checks all four refund states.
- **Refund state machine:** SUCCESS -> REFUND_INITIATED -> REFUND_PROCESSING -> REFUNDED / REFUND_FAILED. Each transition is immediately persisted before the next step.
- **Non-blocking cancellation:** cancelBooking always completes regardless of refund outcome. 	riggerRefundIfApplicable() wraps payment-service REST call in try-catch.
- **Internal-only refund endpoint:** POST /api/v1/payments/refund is protected by X-Internal-Token. External users cannot reach it.
- **Partial refund readiness:** Refund.refundAmount and Refund.remainingAmount modelled now. PARTIAL is schema-ready for a future module.
- **PaymentStatus extended:** Added REFUND_INITIATED, REFUND_PROCESSING, REFUND_FAILED to the shared enum.
- **RabbitMQ event extension:** PaymentEvent extended with efundAmount and efundReference. Notification-service handles REFUNDED and REFUND_FAILED event types.

### Test Results

- RefundServiceTest: 10 tests (idempotency, state machine, event publishing, concurrent race) — ALL PASS
- PaymentServiceTest: 5 tests — ALL PASS
- NotificationListenerTest: 5 tests — ALL PASS
- Full multi-module build: 9/9 SUCCESS
---

## Module 12 - Redis Caching (Turf Search, Turf Details, and Reviews) (2026-07-16)

### Key Decisions

- **Cache-Aside Pattern:** Implemented the Cache-Aside pattern in TurfCacheService and ReviewServiceImpl. Redis handles reads while MongoDB remains the source of truth.
- **Centralized Versioned Keys:** Created CacheKeyUtil in the shared module to generate standardized, versioned cache keys (e.g., 1:cache:turf:{id}, 1:cache:reviews:turf:{id}).
- **Polymorphic JSON Serialization:** Configured RedisTemplate to use Jackson2JsonRedisSerializer with JavaTimeModule for standard JSON, making cache values portable and easy to debug. Configured a BasicPolymorphicTypeValidator to restrict deserialization to allowed packages.
- **SCAN-based Eviction:** Used SCAN instead of KEYS for wildcard cache invalidation (e.g., 1:cache:turfs:* for search results). This prevents blocking the Redis single-threaded event loop during high-volume evictions.
- **Graceful Degradation:** Built a fail-open mechanism wrapped in try-catch blocks. If Redis goes down, the system logs the failure and seamlessly falls back to MongoDB for reads/writes, ensuring uninterrupted user experience.
- **Externalized TTL:** Moved TTL values to application properties, bound via @ConfigurationProperties in CacheProperties.
- **Review Cache Invalidation:** Hooked into review creation, update, deletion, and owner replies to evict the corresponding turf's review cache. Turf cache is also invalidated when its average rating is updated.
- **Suppressed Health Indicators:** Disabled Redis health checks in the development profile to prevent Spring Boot Actuator from marking the service as DOWN when Redis is unreachable (matching the fail-open design).

### Test Results

- TurfCacheServiceTest: Tests for cache hits, misses, timeouts, graceful fallback, and concurrent threads - ALL PASS
- ReviewCacheTest: Tests for review reads, writes, modifications, and graceful degradation - ALL PASS
- Unit tests modified to support the new caching components (via mocking TurfCacheService and RedisTemplate).
- Full multi-module build (mvn test -pl turf-service,review-service): SUCCESS

## Module 13 - Teams & Invitations (community-service) (2026-07-17)

### Key Decisions

- **New Microservice (community-service):** Bootstrapped the community-service to manage teams and invitations independently of turf-service and booking-service, adhering to the database-per-service pattern.
- **Header-Based Auth Pattern:** Configured community-service to rely on X-User-Id headers passed by the API Gateway instead of validating JWTs directly, saving resources and standardizing security.
- **Internal User Lookup:** Exposed a secure internal endpoint /api/v1/auth/internal/user on auth-service (protected by X-Internal-Token) to allow community-service to resolve a user's ID by email without breaking bounded contexts.
- **RabbitMQ Integration:** Configured community-service to publish TeamInvitationEvent to a durable exchange with DLQ configured. Updated notification-service to listen to community.notification.queue and send mock email notifications.
- **Advanced Validations:** Enforced business rules for team invitations (max members, unique pending invites, captain-only invites, no expired invites allowed).
- **Unit Testing:** Implemented full test suites for TeamServiceImpl and InvitationServiceImpl achieving 100% test pass rate for all edge cases.

### Test Results

- All 23 unit tests across community-service passed.
- Integrated RabbitMQ publisher and listener tested end-to-end.
- Full multi-module parent build successful.


### Module 14: Matches
- Added TeamMatch, MatchStatus, MatchType domains.
- Integrated BookingServiceClient using RestTemplate to enforce booking ownership and confirmed status.
- Added MatchNotificationEvent to shared module.
- Validated state machine transitions and prevented self challenges.
- Verified all functionality with unit tests and multi-module build.

### Module 15: Tournaments & Leaderboard
- Scaffolded `tournament-service` with MongoDB, Redis, and Eureka Client.
- Designed `Tournament` and `TournamentRegistration` domains with structured state machines (`DRAFT` -> `OPEN_FOR_REGISTRATION` -> `IN_PROGRESS` -> `COMPLETED`).
- Implemented `LeaderboardService` using Redis Sorted Sets (`ZADD`, `ZINCRBY`, `ZREVRANGE`) with deterministic tie-breaking (lexicographical sorting by default in Redis for same score).
- Points are also persisted to `TournamentRegistration` for DB rebuilding in case of Redis failure.
- Used `CommunityServiceClient` (via `RestTemplate`) to synchronously check `community-service` if a team exists before allowing tournament registration.
- Added comprehensive unit tests for business rules (state transitions, duplicate registration prevention, full tournament checks, Redis mock operations).

### Module 16: Recommendation Service
- Set up event-driven materialized view for fast recommendation lookups.
- Configured RabbitMQ consumers with DLQ and retries for resilience.
- Implemented heuristic scoring algorithm integrating popularity and ratings.
- Verified behavior with automated tests.

### Module 17: Fraud-Signal Service
- Implemented real-time anomaly detection using pure Redis INCR/EXPIRE counters.
- Added FraudAlertEvent publishing for downstream action.
- Addressed concurrent atomic race conditions in Redis by testing parallel thread increments.

### Module 18: Admin Analytics Dashboard
- Added analytics-service.
- Implemented event-driven aggregation for Bookings and Fraud signals using RabbitMQ.
- Applied strict RBAC for Platform-wide vs Turf-specific metrics.
- Used MongoDB atomic operations for high-performance idempotency and aggregation.
