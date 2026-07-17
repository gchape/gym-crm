# gym-crm

Core domain service for the Gym CRM system: trainee, trainer, and training management.

Port: **8081** · Swagger: http://localhost:8081/swagger-ui.html

## Responsibilities

- Registers and manages **trainees** and **trainers** (shared `user` table, JOINED inheritance,
  soft-deleted via `is_active`).
- Manages **trainings** (a trainee/trainer/date/duration/type booking).
- Owns the trainee↔trainer many-to-many assignment.
- Publishes `WorkloadEvent`s to Kafka on training add/cancel and trainee deletion, so
  `gym-crm-workload` can maintain aggregated per-trainer workload without a synchronous call.
- Validates JWTs issued by `gym-crm-authorization-server` as an OAuth2 resource server.

## Tech stack

- Spring Boot 4.1, Java 25
- Spring Data JPA + Hibernate (snake_case naming strategy), Flyway migrations
- Postgres
- Spring Security (OAuth2 resource server)
- Spring Kafka (producer)
- Spring Cloud Consul (service discovery) + Config Client
- springdoc-openapi (Swagger UI)
- AspectJ (`@Around` request/response logging, `@Before` request validation)

## Domain model

```
User (abstract, table "user", JOINED inheritance, discriminator "u_type")
├── Trainee (dob, address, many-to-many → Trainer via trainee_trainer)
└── Trainer (specialization → TrainingType, many-to-many → Trainee)

Training
├── trainee, trainer, trainingType (all FK)
├── trainingName, trainingDate, trainingDuration

TrainingType (immutable reference data)
```

`User` uses Hibernate's `@SoftDelete` (`is_active` column) — trainers are soft-deleted (deactivated),
while a trainee hard-delete (`DELETE /api/trainees/{username}`) cascades and removes their trainings
via `OnDelete(CASCADE)`.

## API overview

| Endpoint                                           | Auth   | Notes                                              |
|----------------------------------------------------|--------|----------------------------------------------------|
| `POST /api/trainees`                               | public | Self-registration, returns generated credentials   |
| `GET/PUT/DELETE /api/trainees/{username}`          | JWT    |                                                    |
| `PUT /api/trainees/{username}/password`            | JWT    |                                                    |
| `PATCH /api/trainees/{username}/active`            | JWT    | Not idempotent — 409 if already in requested state |
| `GET /api/trainees/{username}/trainings`           | JWT    | Filterable by date range, trainer, type            |
| `GET /api/trainees/{username}/unassigned-trainers` | JWT    | Active trainers not yet assigned                   |
| `PUT /api/trainees/{username}/trainers`            | JWT    | Replaces the trainee's trainer list                |
| `POST /api/trainers`                               | public | Self-registration                                  |
| `GET/PUT /api/trainers/{username}`                 | JWT    |                                                    |
| `PUT /api/trainers/{username}/password`            | JWT    |                                                    |
| `PATCH /api/trainers/{username}/active`            | JWT    |                                                    |
| `GET /api/trainers/{username}/trainings`           | JWT    |                                                    |
| `POST /api/trainings`                              | JWT    | Cannot be scheduled in the past                    |
| `DELETE /api/trainings/{id}`                       | JWT    |                                                    |
| `PUT /api/password`                                | JWT    | Generic password change for either role            |
| `GET /api/training-types`                          | public |                                                    |

Full request/response schemas: see Swagger UI or `dto/Request.java` / `dto/Response.java`.

## Validation

Request DTOs (`dto/Request.java`, a sealed interface) carry Jakarta Bean Validation annotations.
Two layers enforce them:

1. `@Valid` on controller method parameters — standard Spring MVC validation, returns 400 with a
   field-error map (`GlobalExceptionHandler`).
2. A custom `@Validate` annotation + AspectJ aspect (`RequestValidationAspect`) re-validates any
   `Request` argument entering the **service layer**, so validation isn't bypassable by calling a
   service method directly (e.g. from another aspect, a scheduled job, or a test that skips the
   controller).

## Logging

- Every request through `controller/**` is logged on entry/exit by `OperationLoggingAspect`,
  including a redacted view of arguments — any DTO carrying a secret must implement the `Sensitive`
  marker interface (see `dto/Sensitive.java`) so passwords never hit the logs.
- Every request gets an `X-Transaction-Id` header (generated if absent) propagated through MDC, so
  all log lines for one request can be correlated (`filter/TransactionIdFilter`).

## Kafka

Publishes to the `trainer-workload-events` topic (name defined once in
`gym-crm-common`'s `WorkloadTopics`), keyed by `trainerUsername` so all of one trainer's events land
on the same partition and stay ordered.

Publishing happens via `TransactionSynchronizationManager.registerSynchronization(...).afterCommit()`
in `TraineeServiceImpl` and `TrainingServiceImpl` — the event is only sent once the owning DB
transaction has actually committed, so a rolled-back request never produces a stray workload update.

## Configuration

Config is pulled from `gym-crm-config-server` (`configserver:http://localhost:8071`) at startup —
see `gym-crm-config-server/src/main/resources/config/gym-crm.yaml` for the full set of properties,
and the root README for environment variables used in `prod`.

Locally relevant defaults (`dev` profile):

- Postgres: `jdbc:postgresql://localhost:5432/postgres`
- `ddl-auto: create-drop` (schema is recreated on every restart — see **Database** below)
- CORS allowed origins: `http://localhost:3000`, `http://localhost:4200`

## Database

Flyway migrations live in `src/main/resources/db/migration`. In `prod`, `ddl-auto` is `validate`
(schema must match migrations exactly); in `dev` it's `create-drop` for fast iteration, which means
**Flyway migrations are not exercised in dev** — always test against `prod`-like `ddl-auto: validate`
before shipping a schema change.

## Running tests

```bash
mvn test
```

Repository tests (`src/test/.../repository/`) use Testcontainers Postgres, shared across the test
class via `BaseRepositoryTest` + `SharedPostgres` (a single container reused across all repository
test classes in the module for speed). Service and controller tests are pure Mockito/MockMvc, no
container required.