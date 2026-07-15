# Developer Log — TurfConnect

This file is a running log of decisions, debug sessions, and important configurations resolved per module.

---

## Stage 0 — Setup (Day 0)
- **Decision:** Directory layout organized with `/docs` containing structured design specs, `/backend` containing Spring Boot 3.3.x Maven projects, and `/config` containing centralized application configuration profiles.
- **Port Strategy:** Standardized port assignments across all services from 8080 (API Gateway) to 8091 (Org Admin) to avoid development clashes.
- **Centralized Configuration:** Configured services to optionally look at files in the shared `/config` root folder.
