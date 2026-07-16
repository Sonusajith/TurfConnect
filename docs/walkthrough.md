# Walkthrough — Modules 5, 6 & 7

We have implemented the Core Booking Service, the Payment microservice, and the Minimal Frontend client interface.

---

## Module 5: Booking Service & Redis Lock

### 1. DTO & Shared Layer (`/backend/shared`)
- Added [BookingStatus.java](file:///c:/Users/rohit/TurfConnect/backend/shared/src/main/java/com/turfconnect/shared/dto/booking/BookingStatus.java) enum: `PENDING`, `CONFIRMED`, `CANCELLED`.
- Added [BookingCreateRequest.java](file:///c:/Users/rohit/TurfConnect/backend/shared/src/main/java/com/turfconnect/shared/dto/booking/BookingCreateRequest.java) to request slot booking checkout.
- Added [BookingResponse.java](file:///c:/Users/rohit/TurfConnect/backend/shared/src/main/java/com/turfconnect/shared/dto/booking/BookingResponse.java) representing a read DTO of bookings.

### 2. Turf Service Internal API Extension
- Added an internal GET endpoint `GET /api/v1/internal/slots/{slotId}` in [SlotController.java](file:///c:/Users/rohit/TurfConnect/backend/turf-service/src/main/java/com/turfconnect/turf/controller/SlotController.java) to retrieve slot metadata.

### 3. Booking Database Model & Repositories
- Created [Booking.java](file:///c:/Users/rohit/TurfConnect/backend/booking-service/src/main/java/com/turfconnect/booking/model/Booking.java) document mapping to `bookings` collection with unique index on `slotId`.
- Added `lockToken` to the Booking entity to track ownership of the Redis distributed lock.
- Created [BookingRepository.java](file:///c:/Users/rohit/TurfConnect/backend/booking-service/src/main/java/com/turfconnect/booking/repository/BookingRepository.java).

### 4. Distributed Redis Lock Service
- Created [RedisLockService.java](file:///c:/Users/rohit/TurfConnect/backend/booking-service/src/main/java/com/turfconnect/booking/service/RedisLockService.java).
- Implemented `acquireLock` utilizing a 5-minute TTL.
- Implemented `releaseLock` using an atomic Lua script key validation check.

---

## Module 6: Payment Service

We built the core Payment microservice with support for mock and production gateways.

### 1. Pluggable Gateway Strategies
- Created [PaymentGatewayStrategy.java](file:///c:/Users/rohit/TurfConnect/backend/payment-service/src/main/java/com/turfconnect/payment/strategy/PaymentGatewayStrategy.java) representing the pluggable contract.
- Implemented [StripePaymentStrategy.java](file:///c:/Users/rohit/TurfConnect/backend/payment-service/src/main/java/com/turfconnect/payment/strategy/StripePaymentStrategy.java):
  - Integrates the Stripe Java SDK.
  - Converts prices to cents.
  - Validates webhook signatures via `Webhook.Signature.verifyHeader`.
  - Parses event payloads with lightweight, version-agnostic Jackson object mappings.
- Implemented [RazorpayPaymentStrategy.java](file:///c:/Users/rohit/TurfConnect/backend/payment-service/src/main/java/com/turfconnect/payment/strategy/RazorpayPaymentStrategy.java):
  - Computes HMAC-SHA256 digests in pure Java cryptography to verify webhook signatures.
- Implemented [MockPaymentStrategy.java](file:///c:/Users/rohit/TurfConnect/backend/payment-service/src/main/java/com/turfconnect/payment/strategy/MockPaymentStrategy.java):
  - Used when configuration credentials are missing, enabling easy offline local development and testing.

### 2. Idempotency & Database Integrity
- Configured [Payment.java](file:///c:/Users/rohit/TurfConnect/backend/payment-service/src/main/java/com/turfconnect/payment/model/Payment.java) with a unique sparse index on `idempotencyKey`.
- Implemented check logic in [PaymentService.java](file:///c:/Users/rohit/TurfConnect/backend/payment-service/src/main/java/com/turfconnect/payment/service/PaymentService.java) to catch duplicate submissions and return the already established transaction directly.

### 3. Secured Microservice REST Communication
- Added checks on `BookingController.java` to validate a custom token header `X-Internal-Token`.
- Secured endpoints:
  - `PUT /api/v1/bookings/{id}/confirm`
  - `PUT /api/v1/bookings/{id}/cancel`
- Configured the REST template in [PaymentService.java](file:///c:/Users/rohit/TurfConnect/backend/payment-service/src/main/java/com/turfconnect/payment/service/PaymentService.java) to inject this secret header on successful transactions.

---

## Module 7: Minimal Frontend (Antigravity)

We built a clean, scalable Vite + React frontend dashboard implementing the Athletic Synergy design language.

### 1. Folder Structure & Features Segmentation
Organized the project using a robust feature-oriented layout:
- `/src/components/` — Reusable elements (Buttons, Input cards, Modals, Spinner loaders, Badges, skeletons)
- `/src/features/` — Domain components (Auth LoginForm, TurfCard, TurfList, TurfSearch, SlotCard, SlotGrid, CheckoutModal, PaymentModal)
- `/src/services/` — Centralized API wrapper client (`apiClient.js` with auto token headers and refresh rotation interceptors)
- `/src/hooks/` — State and event management (`useSlots.js`, `useTurfs.js`, `useBookings.js`, and `useSlotSocket.js` websocket listener)
- `/src/contexts/` — Provider modules (`AuthContext.jsx` and `ToastContext.jsx`)

### 2. Real-Time Slot Broadcasting via WebSockets
- Configured `WebSocketConfig.java` in `turf-service` to start a simple in-memory STOMP broker at endpoint `/ws` with SockJS support.
- Configured `SlotBroadcaster.java` to send slot updates when slot status changes are made.
- Created `useSlotSocket.js` using `@stomp/stompjs` to merge real-time slot state changes directly in the client UI.

### 3. Verification & Core Flows
The client successfully drives the full transactional booking pipeline:
1. **User Authentication:** Login forms submit credentials to Gateway and securely store access/refresh tokens.
2. **Dashboard Search:** Filters turf listings by sports and cities.
3. **Slot Picking Grid:** Fetches daily slots. Color-coded buttons show statuses (`AVAILABLE`, `LOCKED`, `BOOKED`).
4. **Checkout & Mock Pay:** Spawns transaction orders, triggers simulated card checkouts, hits mock webhook handlers, and validates confirmed states.
5. **My Bookings History:** Tracks confirmation details dynamically.

---

## Testing Results

### 1. Backend Integration Build
```text
[INFO] Reactor Summary for turfconnect-parent 0.1.0-SNAPSHOT:
[INFO] 
[INFO] turfconnect-parent ................................. SUCCESS [  0.201 s]
[INFO] shared ............................................. SUCCESS [  4.475 s]
[INFO] api-gateway ........................................ SUCCESS [ 16.769 s]
[INFO] auth-service ....................................... SUCCESS [ 13.014 s]
[INFO] turf-service ....................................... SUCCESS [ 13.399 s]
[INFO] booking-service .................................... SUCCESS [ 12.954 s]
[INFO] payment-service .................................... SUCCESS [ 11.877 s]
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
```

### 2. Frontend Unit Tests (Vitest + JSDOM)
```text
> frontend@0.0.0 test
> vitest run

 RUN  v4.1.10 C:/Users/rohit/TurfConnect/frontend

 ✓ src/tests/webSocket.test.js (2 tests) 34ms
 ✓ src/tests/protectedRoutes.test.jsx (2 tests) 76ms
 ✓ src/tests/slotSelection.test.jsx (2 tests) 297ms
 ✓ src/tests/turfListing.test.jsx (4 tests) 355ms
 ✓ src/tests/bookingFlow.test.jsx (2 tests) 390ms
 ✓ src/tests/auth.test.jsx (3 tests) 389ms
 ✓ src/tests/paymentFlow.test.jsx (2 tests) 454ms

 Test Files  7 passed (7)
      Tests  17 passed (17)
   Start at  22:09:42
   Duration  4.07s
```

---

## Module 9: RabbitMQ & Notification Service

We have introduced asynchronous, message-driven notification support using **RabbitMQ** to decouple notifications (email, SMS, push) from core booking and payment transaction flows.

### 1. Shared Event Layer (`/backend/shared`)
- Created [BookingEvent.java](file:///c:/Users/rohit/TurfConnect/backend/shared/src/main/java/com/turfconnect/shared/dto/event/BookingEvent.java) to encapsulate booking transitions.
- Created [PaymentEvent.java](file:///c:/Users/rohit/TurfConnect/backend/shared/src/main/java/com/turfconnect/shared/dto/event/PaymentEvent.java) to encapsulate transaction outcomes.

### 2. Event Publishing Setup
- Enabled RabbitMQ connection configurations inside `booking-service` and `payment-service` pom files.
- Configured [RabbitMQConfig.java](file:///c:/Users/rohit/TurfConnect/backend/booking-service/src/main/java/com/turfconnect/booking/config/RabbitMQConfig.java) in `booking-service` declaring `booking.exchange`.
- Configured [RabbitMQConfig.java](file:///c:/Users/rohit/TurfConnect/backend/payment-service/src/main/java/com/turfconnect/payment/config/RabbitMQConfig.java) in `payment-service` declaring `payment.exchange`.
- Wired publishers in [BookingService.java](file:///c:/Users/rohit/TurfConnect/backend/booking-service/src/main/java/com/turfconnect/booking/service/BookingService.java) to broadcast booking event payload updates (`CREATED`, `CONFIRMED`, `CANCELLED`).
- Wired publishers in [PaymentService.java](file:///c:/Users/rohit/TurfConnect/backend/payment-service/src/main/java/com/turfconnect/payment/service/PaymentService.java) to broadcast payment event payload updates (`SUCCESS`, `FAILED`).

### 3. Notification Microservice (`/backend/notification-service`)
- Created a new microservice mapping to port `8085`.
- Configured [RabbitMQConfig.java](file:///c:/Users/rohit/TurfConnect/backend/notification-service/src/main/java/com/turfconnect/notification/config/RabbitMQConfig.java) to define:
  - Main topic bindings mapping `booking.notification.queue` to `booking.exchange` via `booking.#` routing key.
  - Main topic bindings mapping `payment.notification.queue` to `payment.exchange` via `payment.#` routing key.
  - Dead Letter Exchange (`notification.dlx`) and Dead Letter Queues (`booking.notification.dlq` / `payment.notification.dlq`) to capture failed/undelivered notifications.
- Created [NotificationListener.java](file:///c:/Users/rohit/TurfConnect/backend/notification-service/src/main/java/com/turfconnect/notification/listener/NotificationListener.java) consuming from these event queues asynchronously and executing simulated dispatches.

### 4. Resiliency & Actuator Adjustments
- Added `management.health.rabbit.enabled: false` and `management.health.redis.enabled: false` to the centralized `application-dev.yml` settings to prevent Actuator health checks from marking services as DOWN (503 Service Unavailable) when brokers are starting up or run offline.

---

## Testing Results (Module 9 Update)

### 1. Backend Integration Build
```text
[INFO] Reactor Summary for turfconnect-parent 0.1.0-SNAPSHOT:
[INFO] 
[INFO] turfconnect-parent ................................. SUCCESS [  0.171 s]
[INFO] shared ............................................. SUCCESS [  4.897 s]
[INFO] api-gateway ........................................ SUCCESS [  4.317 s]
[INFO] auth-service ....................................... SUCCESS [  3.901 s]
[INFO] turf-service ....................................... SUCCESS [  4.093 s]
[INFO] booking-service .................................... SUCCESS [  3.556 s]
[INFO] payment-service .................................... SUCCESS [  3.560 s]
[INFO] notification-service ............................... SUCCESS [  1.408 s]
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
```

### 2. Unit Tests
- **Notification Service Tests (`NotificationListenerTest.java`):**
  ```text
  [INFO] Running com.turfconnect.notification.listener.NotificationListenerTest
  14:11:09.704 [main] INFO com.turfconnect.notification.listener.NotificationListener -- ✉️ [NOTIFICATION DISPATCHED] To Recipient: u-1 | Message: Booking Confirmed! You are ready to play at Elite Football Arena on 2026-07-16 (18:00 - 19:00). See you there!
  [INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.903 s
  ```
- **Booking Service Concurrency Tests:**
  ```text
  [INFO] Running com.turfconnect.booking.service.BookingServiceConcurrencyTest
  2026-07-16 14:12:28 [pool-5-thread-7] INFO  c.t.booking.service.BookingService - Successfully published booking event CREATED for booking booking-generated-id
  [INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.082 s
  ```
- **Payment Service Tests:**
  ```text
  [INFO] Running com.turfconnect.payment.service.PaymentServiceTest
  [INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.175 s
  ```

