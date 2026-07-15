# Software Requirements Specification (SRS) — TurfConnect

## 1. Introduction

### 1.1 Document Purpose
This document specifies the software requirements for **TurfConnect**, a premium sports turf booking platform that connects athletes with sports venues. It defines the functional requirements, user interfaces, system architecture, database design patterns, and security constraints.

### 1.2 Scope of the System
TurfConnect allows users to register, search for sports pitches (football, cricket, badminton, etc.), view live slot availability, select timeslots, and complete bookings via a payment gateway. The system also supports team organization, match scheduling, and venue administration.

---

## 2. Product Features

### 2.1 User Authentication (auth-service)
- **Email Registration & Password Login:** Secure registration using BCrypt for password hashing.
- **Social Auth:** Google Sign-In support.
- **Token-Based Sessions:** JWT access tokens (15-min lifespan) paired with rotation-ready refresh tokens.
- **Role-Based Access Control (RBAC):** Roles include `PLAYER`, `TURF_OWNER`, `FRANCHISE_ADMIN`, `ORG_ADMIN`, and `SUPER_ADMIN`.

### 2.2 Venue & Search Management (turf-service)
- **Browse Venues:** Responsive list and interactive map views of nearby turfs.
- **Search Filters:** Filter by sport type (Soccer, Cricket, Badminton, etc.), price range, rating, date, and amenities (Floodlights, Parking, Changing Rooms).
- **Slot Generation:** Automated slots generated daily based on venue configurations.

### 2.3 Slot Locking & Bookings (booking-service)
- **Slot Lock Mechanism:** Temporary lock of 5 minutes in Redis upon selecting a slot to prevent race conditions during checkout.
- **State Machine Transitions:**
  - `PENDING`: Slot is locked; payment is initiated.
  - `CONFIRMED`: Payment succeeds; slot is permanently booked.
  - `CANCELLED`: Payment fails, lock expires, or user cancels booking.

### 2.4 Payment Service (payment-service)
- **Razorpay/Stripe Integration:** Running in **test mode only**.
- **Webhooks:** Handling callbacks asynchronously to trigger booking confirmation.
- **Idempotency:** Payment events deduplicated using a combination of `bookingId` and payment provider transaction reference.

### 2.5 User Dashboard (user-dashboard-service / frontend)
- **Metrics View:** Total bookings, upcoming matches, reward points, and favorite venues.
- **Calendar & Schedule:** List of upcoming matches with map routing links.
- **Recommendations:** Personalized venue recommendations based on location and sport preferences.

---

## 3. UI/UX Design System (Athletic Synergy)

The application follows the **Athletic Synergy** design system:
- **Primary Color (Deep Athletic Green):** `#1B5E20` / `#00450d` (active states, brand identity)
- **Secondary Color (Vibrant Grass Green):** `#4CAF50` / `#006e1c` (success badges, status indicators)
- **Action CTA Color (Energetic Orange):** `#FF6D00` / `#ff9800` (Book Now, View Details, checkout triggers)
- **Surface Color:** `#F4FAFF` (main app background)
- **Typography:** Inter (Sans-serif)
- **Rounding:** `8px` (`rounded-lg`) standard for inputs/buttons, `16px`/`24px` (`rounded-xl` / `rounded-2xl`) for layout containers and cards.

---

## 4. Technical Constraints & Architecture
- **Backend Stack:** Java 17, Spring Boot 3.3.x, Spring Cloud Gateway, Spring Security.
- **Database:** MongoDB (Database-per-service isolation; collections never queried cross-service).
- **Caching & Locks:** Redis Cluster for temporary locks, rate limits, and sorted set leaderboards.
- **Asynchronous Messaging:** RabbitMQ/Kafka for notifications and analytics events.
- **Stateless Services:** All services must be horizontal-scaling ready.
