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
