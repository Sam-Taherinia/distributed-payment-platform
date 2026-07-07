# Distributed Payment Platform (DPF)

A fintech-grade **distributed wallet and payment system** built as a Spring Boot microservices monorepo. The project demonstrates industry patterns: bounded contexts, idempotent transactions, optimistic locking, double-entry ledger, and event-driven architecture (Kafka-ready).

> **Status:** `dpf-wallet-service` is ~40% complete and actively developed. Other services are scaffolded but not yet implemented.

## Goals

- Financially correct money movement (no double spending)
- Clear microservice boundaries (user, wallet, notification, gateway)
- Idempotent, auditable transaction processing
- Portfolio-grade backend engineering practices



## Monorepo structure


| Path                        | Description                                | Status     |
| --------------------------- | ------------------------------------------ | ---------- |
| `dpf-wallet-service/`       | Wallets, transactions, ledger, idempotency | **Active** |
| `dpf-user-service/`         | Registration, login, JWT, RBAC             | Planned    |
| `dpf-notification-service/` | Email/SMS via Kafka events                 | Planned    |
| `dpf-gateway/`              | Routing, auth, rate limiting               | Planned    |
| `infrastructure/`           | Docker Compose, Kafka, K8s manifests       | Planned    |
| `shared/`                   | Common DTOs, events, security libs         | Planned    |




## Quick start



### Prerequisites

- Java 21
- Docker (for PostgreSQL)
- Maven (or use included wrapper)



### Run wallet service

```bash
cd dpf-wallet-service
docker compose up -d          # PostgreSQL on localhost:5432
./mvnw spring-boot:run        # API on http://localhost:8080
```



### API

Base path: `/api/v1/wallets`


| Method | Endpoint                 | Notes                    |
| ------ | ------------------------ | ------------------------ |
| `POST` | `/wallets`               | Create wallet            |
| `GET`  | `/wallets/{id}`          | Get by wallet ID         |
| `GET`  | `/wallets/user/{userId}` | Get by user ID           |
| `POST` | `/wallets/{id}/deposit`  | Deposit funds            |
| `POST` | `/wallets/{id}/withdraw` | Withdraw funds           |
| `POST` | `/wallets/transfer`      | Transfer between wallets |


All `POST` requests require an `Idempotency-Key` header (max 255 chars).

See `[dpf-wallet-service/POSTMAN.md](dpf-wallet-service/POSTMAN.md)` for request/response examples.

### Health check

```bash
curl http://localhost:8080/actuator/health
```



## Tech stack

- **Java 21** · **Spring Boot 4** · **Spring Data JPA** · **PostgreSQL 17**
- **Lombok** · **Jakarta Validation** · **Spring Actuator**
- Planned: Spring Security (JWT), Kafka, Redis, Resilience4j, Testcontainers



## Architecture (target)

```text
Client → API Gateway → [user-service | wallet-service | notification-service]
                              ↓
                         Kafka (events)
                              ↓
                    PostgreSQL (per service)
```

