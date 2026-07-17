# TurfConnect

TurfConnect is a premium sports turf booking platform (Playo/Hudle-style) designed to connect athletes with sports facilities. The project is organized as a Spring Boot microservice architecture for the backend and a React + Vite + Tailwind CSS application for the frontend.

## 🚀 Final Release (v1.0-submission)
This repository represents the completed academic submission for the TurfConnect platform. It encompasses a fully functional 5-phase backend architecture (Core Booking, Enhanced Features, Community, AI Analytics, and Enterprise Multi-Region scaling) and a premium, responsive Frontend UI designed with the Athletic Synergy design system.

## Tech Stack
- **Backend:** Spring Boot 3.3.x (Java 17), Spring Cloud Gateway, Spring Security
- **Database:** MongoDB (Strict Database-per-service architecture)
- **Distributed Caching & Locks:** Redis
- **Message Broker:** RabbitMQ
- **Real-Time Updates:** WebSockets (STOMP over SockJS)
- **Frontend:** React, Vite, Tailwind CSS v3

---

## Directory Structure

```text
TurfConnect/
├── .env.example          # Environment variable placeholders
├── .gitignore            # Git ignore rules
├── AGENTS.md             # Active agent execution instructions
├── README.md             # This instructions file
├── config/               # Centralized configuration profiles
├── docs/                 # Project documentation (SRS, Architecture, Progress)
├── backend/              # Java backend microservices
│   ├── shared/           # Common library module
│   ├── api-gateway/      # API Gateway
│   ├── auth-service/     # Authentication & Authorization
│   ├── turf-service/     # Turf CRUD & Search
│   ├── booking-service/  # Booking & Locks
│   ├── payment-service/  # Integration with payments
│   ├── review-service/   # Reviews and Ratings
│   ├── community-service/# Teams and Invites
│   ├── tournament-service/ # Tournaments and Leaderboards
│   ├── analytics-service/# Admin Analytics
│   ├── audit-service/    # Audit Logging
│   └── (other modules)
└── frontend/             # React + Vite web application
```

---

## Getting Started

### Prerequisites
- **Java 17 Development Kit (JDK)**
- **Node.js (v18+) and npm**
- **MongoDB** (running locally on port 27017 or Atlas cloud instance)
- **Redis** (running locally on port 6379 or Upstash instance)
- **RabbitMQ** (running locally on port 5672)

### Local Environment Setup
1. Copy `.env.example` to `.env` in the root directory:
   ```bash
   cp .env.example .env
   ```
2. Adjust connection strings and keys in `.env`.

---

## Build & Test Instructions

### Running the Backend (Low-RAM Core Profile)
To launch the core services needed for booking without exhausting local RAM, use the provided batch script:
```bash
./start-core.bat
```
*(This starts the Gateway, Auth, Turf, Booking, and Payment services).*

Alternatively, build and run manually:
```bash
cd backend
./mvnw clean install
cd <service-name>
../mvnw spring-boot:run
```

### Running the Frontend
```bash
cd frontend
npm install
npm run dev
```
Navigate to `http://localhost:5173` to view the TurfConnect UI.

---

## Documentation & Evidence
For the academic viva, please refer to:
- `docs/DEV_LOG.md`: Running log of architectural decisions.
- `docs/srs.md`: Complete requirement specifications.
- `docs/scaling-strategy.md`: Module 22 scaling documentation.
