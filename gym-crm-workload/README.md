# gym-crm-workload

Secondary microservice responsible for tracking and calculating trainers' monthly working hours, built with Spring Boot.

## Purpose

Every time a training session is planned or cancelled for a trainer in the main `gym-crm` application, that event is
sent to this microservice. It maintains an in-memory summary of each trainer's total training duration, broken down by
year and month, and exposes an endpoint to query that summary on demand.

## Tech Stack

- Spring Boot 4.1.0
- Spring Cloud Netflix Eureka Client 2025.1.2
- Spring Cloud Circuit Breaker (Resilience4j) 2025.1.2
- Spring AOP (for operation-level logging)
- In-memory storage — `ConcurrentHashMap` + `AtomicInteger` (no database)

## Architecture

```
gym-crm ──► POST /api/trainers/workload ──► gym-crm-workload
                                                    │
                                                    ▼
                                     ConcurrentHashMap<username, TrainerWorkloadSummary>
                                                    │
gym-crm ──► GET /api/trainers/workload/{username} ─┘
```

- Registers with `gym-crm-eureka-server` for service discovery.
- Fetches its own configuration (port, Eureka settings) from `gym-crm-config-server`.
- Does not persist data to a database — all workload data lives in memory and is lost on restart, per the task's "
  in-memory saved structure" requirement.

## REST API

Follows Richardson Maturity Level 2: proper HTTP verbs, resource-based URLs, meaningful status codes.

### Submit a workload event

```
POST /api/trainers/workload
Content-Type: application/json
```

**Request body:**

```json
{
  "trainerUsername": "john.smith",
  "trainerFirstName": "John",
  "trainerLastName": "Smith",
  "isActive": true,
  "trainingDate": "2026-07-08",
  "trainingDuration": 60,
  "actionType": "ADD"
}
```

- `actionType`: `ADD` (training scheduled) or `DELETE` (training cancelled) — `DELETE` subtracts the duration from that
  month's total rather than removing history.

**Response:** `200 OK` (empty body)

### Get a trainer's monthly summary

```
GET /api/trainers/workload/{username}
```

**Response:**

```json
{
  "trainerUsername": "john.smith",
  "trainerFirstName": "John",
  "trainerLastName": "Smith",
  "trainerStatus": true,
  "years": [
    {
      "year": 2026,
      "months": [
        {
          "month": 7,
          "trainingSummaryDuration": 120
        }
      ]
    }
  ]
}
```

Returns `404 Not Found` if the trainer has no recorded workload data.

## Thread Safety

- Top-level trainer storage uses `ConcurrentHashMap` with atomic `computeIfAbsent` for safe concurrent trainer creation.
- Per-month duration accumulation uses `AtomicInteger` to avoid race conditions when multiple requests update the same
  trainer/month concurrently.

## Transaction Logging (Two-Level)

**Transaction level** — `TransactionIdFilter`:

- Reads an incoming `X-Transaction-Id` header if present (propagated from `gym-crm`), or generates a new UUID if absent.
- Places it in SLF4J MDC under `transactionId`, so every log line during the request automatically includes it.
- Echoes it back in the response header.

**Operation level** — `OperationLoggingAspect`:

- Wraps all controller methods.
- Logs the endpoint called, the incoming request payload, and the resulting response status (200) or error message on
  failure.

Console log pattern includes the transaction ID:

```
2026-07-08 10:14:15 [http-nio-8082-exec-1] INFO  [txId=3f9a1b2c-...] t.p.g.controller.WorkloadController - ...
```

## Running Locally

Requires `gym-crm-eureka-server` (port 8761) and `gym-crm-config-server` (port 8071) to be running first.

```bash
./mvnw spring-boot:run
```

Starts on **port 8082** (configured via the config server).

## Configuration

This service holds almost no local configuration — everything comes from `gym-crm-config-server`'s
`config/gym-crm-workload.yaml`:

```yaml
spring:
  application:
    name: gym-crm-workload
  profiles:
    active: dev
  config:
    import: "configserver:http://localhost:8071"
```

## Not Yet Implemented

- JWT bearer token validation on incoming requests (currently unauthenticated)
- Circuit breaker usage (dependency present, not yet wired to any outbound call — this service doesn't call anything
  downstream yet)