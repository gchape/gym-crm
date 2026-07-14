# gym-crm-workload

Trainer workload tracking service for the `gym-crm` microservices ecosystem — maintains an in-memory summary of
each trainer's monthly training hours, updated via events reported by `gym-crm`.

## Purpose

`gym-crm` reports a workload delta (`ADD` or `DELETE`) to this service every time a training session is added,
cancelled, or a trainee (with trainings) is deleted. This service aggregates those deltas per trainer, per
year, per month, and exposes the running totals over a small REST API. It does not persist to a database — all
state lives in memory for the lifetime of the process.

## Tech Stack

- Java 25
- Spring Boot 4.1.0 (Web MVC, Validation, Actuator, AOP)
- Spring Cloud (2025.1.2): Netflix Eureka Client, Config Client, Circuit Breaker (Resilience4j)
- jjwt — JWT verification for service-to-service auth
- Lombok
- JUnit 5 for testing

## How It Works

```
gym-crm ──POST /api/trainers/workload──► gym-crm-workload
                                              │
                                    BearerAuthFilter validates
                                    service-to-service JWT
                                              │
                                    TransactionIdFilter propagates
                                    X-Transaction-Id via MDC
                                              │
                                    OperationLoggingAspect logs
                                    request/response/status
                                              │
                                    WorkloadServiceImpl updates
                                    in-memory trainer summary
```

Workload data is stored as a nested, thread-safe in-memory structure:

```
TrainerWorkloadSummary (per trainer, keyed by username)
└── YearSummary (per year)
    └── MonthSummary (per month) — holds an AtomicInteger of total training minutes
```

`ADD` actions increase a month's duration; `DELETE` actions decrease it (never below zero).

## API

### `POST /api/trainers/workload`

Reports a workload delta for a trainer. Called by `gym-crm` whenever a training is added or removed.

Request body (`WorkloadRequest`):

| Field              | Type              | Notes                      |
|--------------------|-------------------|----------------------------|
| `trainerUsername`  | String            | required                   |
| `trainerFirstName` | String            | required                   |
| `trainerLastName`  | String            | required                   |
| `isActive`         | Boolean           | required                   |
| `trainingDate`     | LocalDate         | required                   |
| `trainingDuration` | Integer           | required, must be positive |
| `actionType`       | `ADD` \| `DELETE` | required                   |

Returns `200 OK` on success.

### `GET /api/trainers/workload/{username}`

Returns the aggregated workload summary for a trainer, broken down by year and month.

Returns `200 OK` with a `TrainerWorkloadResponse`, or `404 Not Found` if no summary exists for that username yet.

```json
{
  "trainerUsername": "jane.smith",
  "trainerFirstName": "Jane",
  "trainerLastName": "Smith",
  "trainerStatus": true,
  "years": [
    {
      "year": 2025,
      "months": [
        {
          "month": 8,
          "trainingSummaryDuration": 180
        }
      ]
    }
  ]
}
```

## Authentication

- All requests must include a valid `Bearer` JWT in the `Authorization` header (`BearerAuthFilter`).
- Tokens are signed with a shared secret (`jwt.secret`) that **must match** the one used by `gym-crm` to issue
  the service-to-service token — both are sourced from `gym-crm-config-server`.
- Unlike `gym-crm`, this service performs no user login of its own; it only validates tokens issued elsewhere.

## Cross-cutting Concerns

- **`TransactionIdFilter`** (`@Order(0)`) — reads or generates an `X-Transaction-Id` header and puts it in the
  SLF4J MDC, so logs here can be correlated with the originating request in `gym-crm`.
- **`BearerAuthFilter`** (`@Order(1)`) — rejects any request without a valid bearer token with `401 Unauthorized`.
- **`OperationLoggingAspect`** — logs method entry, exit, and status for everything in `controller..*`.

## Running Locally

Start dependencies first:

```bash
# 1. Config server (port 8071)
cd gym-crm-config-server && ./mvnw spring-boot:run

# 2. Eureka server (port 8761)
cd gym-crm-eureka-server && ./mvnw spring-boot:run

# 3. This service (port 8082)
./mvnw spring-boot:run
```

Verify it's up:

```bash
curl http://localhost:8082/actuator/health
```

## Notes

- **In-memory only**: workload data is lost on restart. There is no persistence layer or database dependency for
  this service.
- Called by `gym-crm` through a **load-balanced RestClient** (Eureka-resolved) wrapped in a **Resilience4j circuit
  breaker** (`workloadService` instance, configured in `gym-crm-config-server`), so transient failures here don't
  fail the caller's request — `gym-crm` logs a fallback and moves on.
- Registers with Eureka and fetches its configuration from `gym-crm-config-server`, matching the pattern used by
  `gym-crm`.