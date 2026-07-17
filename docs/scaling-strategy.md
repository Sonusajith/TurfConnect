# Enterprise Scalability & Multi-Region Strategy

This document outlines the architectural roadmap for scaling TurfConnect to support millions of users across multiple geographic regions, ensuring high availability (HA), low latency, and robust disaster recovery (DR).

---

## 1. High-Level Architecture & Request Flow

The following diagram illustrates how user traffic moves through the edge network, API Gateway, microservices, databases, caching layers, and asynchronous messaging queues.

```mermaid
graph TD
    Client[Client Apps (Web/Mobile)] -->|HTTPS / WSS| CDN[CDN & Cloudflare WAF]
    CDN --> LB[Cloud Load Balancer]
    LB --> Gateway[API Gateway & WebSocket Gateway]
    
    subgraph Microservices Layer
        Gateway -->|REST| Auth[Auth Service]
        Gateway -->|REST| Turf[Turf Service]
        Gateway -->|REST| Booking[Booking Service]
        Gateway -->|REST| Payment[Payment Service]
    end
    
    subgraph Caching & State
        Turf --> Redis[Redis Cluster]
        Booking --> Redis
        Gateway -->|Rate Limits & WS Pub/Sub| Redis
    end
    
    subgraph Data Layer
        Auth --> MongoDBAuth[(Auth DB Replica)]
        Turf --> MongoDBTurf[(Turf DB Sharded)]
        Booking --> MongoDBBook[(Booking DB Sharded)]
    end
    
    subgraph Async Messaging
        Booking -->|Events| RabbitMQ[RabbitMQ / Kafka]
        RabbitMQ --> Notify[Notification Service]
        RabbitMQ --> Analytics[Analytics Service]
    end
```

---

## 2. API Gateway & Microservices Scaling

### Load Balancing & Auto-Scaling
* **API Gateway:** Deployed as the single entry point behind a Cloud Load Balancer. It handles SSL termination, JWT validation, and per-IP rate limiting (backed by Redis).
* **Horizontal Pod Autoscaling (HPA):** All stateless microservices scale automatically based on CPU and memory utilization metrics.
* **WebSocket Horizontal Scaling:** WebSockets maintain sticky, stateful connections. Scaling the `websocket-gateway` horizontally requires using Redis Pub/Sub to fan out messages. If user A connects to Gateway 1 and user B connects to Gateway 2, an event published to Redis by a backend service will reach both gateways and broadcast to both users.

---

## 3. Database Strategy: Sharding & Read Replicas

### Read Replicas for High Availability
Every microservice's MongoDB database runs as a 3-node Replica Set (1 Primary, 2 Secondaries). 
* Write operations (bookings, payments) always target the Primary node for strict consistency.
* Heavy read operations (reporting, dashboards) are routed to Secondary nodes to prevent transactional lock contention.

### Sharding for Horizontal Data Growth
As the platform expands to multiple countries, collections like `turfs` and `bookings` will outgrow a single Replica Set.
* **Shard Key Candidate:** `country_code` or `city_id`. Since 99% of queries are localized (e.g., users in Mumbai only search for turfs in Mumbai), geographic sharding ensures queries are routed to a specific shard, avoiding scatter-gather overhead.
* **Architecture:** The application connects to `mongos` query routers, which consult config servers to route the query to the correct Shard Replica Set.

---

## 4. Asynchronous Messaging & Queueing

### RabbitMQ High Availability
* RabbitMQ is deployed in a clustered configuration with Quorum Queues to ensure message durability and high availability in the event of a node failure.
* Dead Letter Queues (DLQs) are configured for strict event reprocessing (e.g., failed payment webhooks).

### Kafka Alternative for High Throughput
* While RabbitMQ excels at complex routing and fire-and-forget notifications, **Apache Kafka** is planned as a future replacement when the platform transitions to an event-sourcing architecture or needs to process high-velocity telemetry data (e.g., millions of IoT sensor events from turnstiles at sports venues).

---

## 5. Performance: Caching & Edge Delivery

### Global CDN
* Static assets, turf images, and frontend bundles are cached aggressively at the edge via Cloudflare or AWS CloudFront, significantly reducing the load on the backend.

### Redis Caching Strategy
* **Cache-Aside Pattern:** Turf details, search results, and reviews are cached in a distributed Redis Cluster.
* **Eviction:** Wildcard scanning or TTL-based eviction ensures data consistency upon write updates.

---

## 6. Multi-Region Active-Passive Setup & Disaster Recovery

To achieve 99.99% uptime, the platform utilizes an Active-Passive disaster recovery model.

* **Primary Region:** E.g., AWS `ap-south-1` (Mumbai) handles all live traffic.
* **Standby (DR) Region:** E.g., AWS `ap-southeast-1` (Singapore).
* **Data Replication:** 
  * MongoDB uses asynchronous cross-region replication to maintain a warm standby replica set in the DR region.
  * Redis enterprise cross-cluster replication mirrors session data.
* **Failover:** In the event of a catastrophic regional failure, DNS routing (Route53) automatically redirects traffic to the DR region. The Recovery Time Objective (RTO) is < 15 minutes, and Recovery Point Objective (RPO) is < 1 minute.

---

## 7. Monitoring, Observability, and Security

### Observability Stack
* **Metrics:** Prometheus scrapes JVM, Spring Boot Actuator, and custom application metrics. Grafana provides real-time visualization of queue depth, memory usage, and endpoint latency.
* **Distributed Tracing:** OpenTelemetry is used to inject trace IDs across HTTP and RabbitMQ boundaries, allowing engineers to trace a booking request across 5 different microservices.
* **Centralized Logging:** Logs are shipped via Filebeat to an ELK (Elasticsearch, Logstash, Kibana) or OpenSearch cluster for centralized querying.

### Enterprise Security
* **TLS:** End-to-end encryption. Internal service-to-service traffic is secured using mTLS within the service mesh.
* **Secrets Management:** Database passwords, JWT signing keys, and Razorpay API keys are never hardcoded. They are injected at runtime via HashiCorp Vault or AWS Secrets Manager.
* **Backups:** Automated, encrypted snapshots of all MongoDB clusters are taken daily and stored in WORM (Write Once Read Many) cloud storage to protect against ransomware.
