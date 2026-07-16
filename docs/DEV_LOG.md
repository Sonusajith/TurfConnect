# Developer Log — TurfConnect

This file is a running log of decisions, debug sessions, and important configurations resolved per module.

---

## Stage 0 — Setup (Day 0)
- **Decision:** Directory layout organized with `/docs` containing structured design specs, `/backend` containing Spring Boot 3.3.x Maven projects, and `/config` containing centralized application configuration profiles.
- **Port Strategy:** Standardized port assignments across all services from 8080 (API Gateway) to 8091 (Org Admin) to avoid development clashes.
- **Centralized Configuration:** Configured services to optionally look at files in the shared `/config` root folder.

---

## Module 7 — Minimal Frontend
- **Framework & Structure:** Selected Vite + React for lightweight rendering and standard state hooks. Implemented a robust features-based directory structure (`features/`, `components/`, `services/`, `hooks/`, `contexts/`) ensuring clean codebase isolation and readiness for Redux/Zustand scaling.
- **Centralized API Client:** Created an Axios interceptor client that dynamically appends Bearer JWTs to incoming gateway requests and handles auto-refresh tokens seamlessly when catching 401 statuses.
- **Real-Time WebSocket Hook:** Wired `WebSocketConfig` and `SlotBroadcaster` into the `turf-service` backend, utilizing standard `spring-boot-starter-websocket`. Built `useSlotSocket.js` on SockJS + STOMP in the frontend to trigger slot updates without long-polling.
- **Design system:** Applied CSS variables for colors (Athletic Deep Green `#1B5E20`, CTA Energetic Orange `#FF6D00`) and rounded borders (`rounded-2xl` for widgets) to adhere to the Athletic Synergy specifications.
- **Testing:** Enabled Vitest + JSDOM to validate authentication, protected paths, slot search parameters, booking modals, and WebSocket handlers. Checked off Stage 1 MVP milestone.

---

## Module 9 — RabbitMQ & Notification Service
- **Broker Topology:** Declared topic exchange configurations (`booking.exchange` and `payment.exchange`) and queue bindings (`booking.notification.queue`, `payment.notification.queue`) routing by prefix event keys. 
- **Resiliency & Error Handling:** Configured Dead Letter Exchange (`notification.dlx`) and direct Dead Letter Queues (`booking.notification.dlq`, `payment.notification.dlq`) to capture unacknowledged or toxic messages.
- **Actuator Health Adjustment:** Configured health checks for `rabbit` and `redis` to run as disabled in `application-dev.yml` to prevent entire microservices from failing with 503 SERVICE_UNAVAILABLE codes when AMQP brokers are not active locally during developmental offline launches.
- **Testing:** Wrote unit tests for `NotificationListener` confirming message processing routes correctly. Verified all backend modules compile successfully with Maven.

---

## Module 10 — Reviews & Ratings
- **Decentralized Reviews Microservice:** Created the new `review-service` microservice, utilizing MongoDB (`turfconnect_reviews` database) for storage, adhering to the database-per-service paradigm.
- **Review Submission Restrictions:** Secured review submissions to require a validated CONFIRMED booking belonging to the active user retrieved dynamically via REST call to the `booking-service`. Added checks to prevent duplicate reviews for the same booking.
- **Event-Driven Aggregation:** Built aggregation pipelines using Spring Data MongoDB to calculate the average rating and total review counts per turf. Emitted `ReviewEvent` with UUID, eventType, timestamp, version, and metrics to `review.exchange` on MongoDB writes.
- **Automatic Turf Rating Propagation:** Configured `turf-service` to consume `ReviewEvent` via RabbitMQ binding, update the local turf entity's `averageRating` cache-aside store, and invalidate cached turf queries.
- **Future Readiness & Enhanced Fields:** Enhanced the `Review` entity with future-proofing fields: soft delete (`isDeleted`), edited status (`isEdited`), `updatedAt`, `status` (`ACTIVE`, `HIDDEN`, `DELETED`), and `ownerReply` placeholders to avoid future schema changes.
- **Testing:** Wrote exhaustive unit tests in `review-service` checking validations, duplicate submissions, and aggregation math. Wrote AMQP listener tests in `turf-service` verifying event consumption and rating updates. Verified end-to-end routing and validation using Python test script.
