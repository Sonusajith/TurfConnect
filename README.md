# TurfConnect

TurfConnect is a premium sports turf booking platform (Playo/Hudle-style) designed to connect athletes with sports facilities. The project is organized as a Spring Boot microservice architecture for the backend and a React + Vite + Tailwind CSS application for the frontend.

## Tech Stack
- **Backend:** Spring Boot 3.3.x (Java 17), Spring Cloud Gateway, Spring Security
- **Database:** MongoDB (Database-per-service architecture)
- **Distributed Caching & Locks:** Redis
- **Message Broker:** RabbitMQ
- **Real-Time Updates:** WebSockets (STOMP over SockJS)
- **Frontend:** React, Vite, Tailwind CSS

---

## Directory Structure

```text
TurfConnect/
├── .env.example          # Environment variable placeholders
├── .gitignore            # Git ignore rules
├── AGENTS.md             # Active agent execution instructions
├── README.md             # This instructions file
├── config/               # Centralized configuration profiles
│   ├── application-dev.yml
│   ├── application-test.yml
│   └── application-prod.yml
├── docs/                 # Project documentation
│   ├── srs.md            # Software Requirements Specification
│   ├── backend-architecture.md
│   ├── module-roadmap.md
│   ├── PROGRESS.md       # Stage tracker
│   └── DEV_LOG.md        # Running development log
├── backend/              # Java backend microservices
│   ├── pom.xml           # Parent Maven POM
│   ├── mvnw / mvnw.cmd   # Custom Maven wrapper
│   ├── shared/           # Common library module
│   ├── api-gateway/      # API Gateway
│   ├── auth-service/     # Authentication & Authorization
│   ├── turf-service/     # Turf CRUD & Search
│   ├── booking-service/  # Booking & Locks
│   ├── payment-service/  # Integration with payments
│   └── (notification-service, review-service, etc.) # Placeholder modules
└── Designs/              # UI/UX design resources and mockups
```

---

## Port Allocation Strategy

```text
8080: API Gateway
8081: Auth Service
8082: Turf Service
8083: Booking Service
8084: Payment Service
8085: Notification Service (Placeholder)
8086: Review Service (Placeholder)
8087: Community Service (Placeholder)
8088: Tournament Service (Placeholder)
8089: Recommendation Service (Placeholder)
8090: Analytics Service (Placeholder)
8091: Org Admin Service (Placeholder)
```

---

## Getting Started

### Prerequisites
- **Java 17 Development Kit (JDK)**
- **MongoDB** (running locally on port 27017 or Atlas cloud instance)
- **Redis** (running locally on port 6379 or Upstash instance)

### Local Environment Setup
1. Copy `.env.example` to `.env` in the root directory:
   ```bash
   cp .env.example .env
   ```
2. Adjust connection strings and keys in `.env`.

### central Configuration
Centralized configuration is managed inside the `/config` folder:
- **`application-dev.yml`**: Shared development settings.
- **`application-test.yml`**: Shared test settings.
- **`application-prod.yml`**: Shared production-grade connection profiles.

Downstream microservices import these configurations dynamically on startup based on their active Spring profile.

---

## Build & Test Instructions

Build the parent and all active modules inside the `/backend` folder:
```bash
cd backend
# Windows command prompt
mvnw.cmd clean install
# Unix shell
./mvnw clean install
```

To run a specific service:
```bash
cd backend/<service-name>
# Windows
../mvnw.cmd spring-boot:run
# Unix
../mvnw spring-boot:run
```

---

## Git Workflow & Branching
- **Branch Naming:** `feature/module-<number>-<short-name>`
- **Commit Message Format:** `feat(<service>): <what> — <why>`
- **Tagging:** After completing each milestone, create a tag (e.g. `v0.1-setup`) before pushing.
