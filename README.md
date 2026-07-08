# Gym CRM System

A Spring Boot REST API for managing gym trainees, trainers, and training sessions — the core service in a small
`gym-crm` microservices ecosystem (alongside `gym-crm-config-server`, `gym-crm-eureka-server`, and
`gym-crm-workload`).

---

## Tech Stack

- **Java 25**
- **Spring Boot 4.1.0** (Web MVC, Validation, Actuator, AOP)
- **Spring Security** — stateless JWT authentication
- **Spring Data JPA** + **Hibernate ORM** (joined-table inheritance for `User`/`Trainee`/`Trainer`)
- **PostgreSQL** + **HikariCP**
- **Flyway** — schema migrations
- **Spring Cloud** (2025.1.2): Netflix Eureka Client, Config Client, Circuit Breaker (Resilience4j), LoadBalancer
- **jjwt** — JWT issuing/parsing
- **springdoc-openapi** — Swagger UI / OpenAPI docs
- **Lombok** (`@SuperBuilder`, `@Getter`, `@Slf4j`) for boilerplate
- **Jspecify** for null safety annotations
- **Logback** for logging (console in dev, rolling file in prod)
- **JUnit 5 + AssertJ + Mockito** for testing
- **Testcontainers** (PostgreSQL) for repository integration tests

---

## Architecture

```
                         ┌──────────────────────────┐
                         │  gym-crm-eureka-server   │  (service discovery, :8761)
                         └────────────▲─────────────┘
                                      │ register / discover
┌──────────────────────────┐         │        ┌───────────────────────────┐
│ gym-crm-config-server    │◄────────┼───────►│     gym-crm-workload      │
│ (centralized config,     │  fetch  │        │ (trainer hours tracker,  │
│  :8071)                  │  config │        │  :8082, in-memory)       │
└──────────────────────────┘         │        └─────────────▲─────────────┘
                                      │                      │ POST /api/trainers/workload
                         ┌────────────┴─────────────┐        │ (JWT + circuit breaker)
                         │         gym-crm           ├────────┘
                         │   (this service, :8081)   │
                         └────────────┬──────────────┘
                                      │
                               ┌──────▼───────┐
                               │  PostgreSQL  │
                               └──────────────┘
```

`gym-crm` fetches its configuration from `gym-crm-config-server` at startup, registers with
`gym-crm-eureka-server`, and — on every training add/cancel or trainee delete — reports the affected trainer's
workload delta to `gym-crm-workload` via a load-balanced, circuit-breaker-wrapped REST call.

### Cross-cutting Concerns

```
Request → JwtFilter (Spring Security filter chain — authenticates via Bearer token)
        → TransactionIdFilter (assigns/propagates X-Transaction-Id via SLF4J MDC)
        → OperationLoggingAspect (@Around on controller..*, logs request/response/status)
        → @Validate → RequestValidationAspect (order=2, validates Request DTOs)
        → Service Method
```

- **Authentication** is handled by Spring Security's filter chain (`JwtFilter` + `GymCRMUserDetailsService`), not
  a custom method-level aspect. JWTs are issued at `POST /api/login` and required as a `Bearer` token on every
  endpoint except trainee/trainer registration, login, and `GET /api/training-types`.
- **Request validation** (`@Validate` on service impl methods) is a separate concern, handled by
  `RequestValidationAspect`, which validates any `Request` argument via Jakarta Bean Validation before the
  service method body runs.
- **Transaction correlation**: `TransactionIdFilter` generates or propagates an `X-Transaction-Id` header and puts
  it in the logging MDC, so every log line for a request — and the downstream call to `gym-crm-workload` — carries
  the same ID.

---

## Project Structure

```
src/
├── main/
│   └── java/tech/provokedynamic/gymcrm/
│       ├── annotation/
│       │   └── Validate.java              # triggers request-validation aspect
│       ├── aspect/
│       │   ├── OperationLoggingAspect.java
│       │   ├── RequestValidationAspect.java
│       │   └── pointcuts/
│       │       ├── AnnotationPointcuts.java
│       │       └── ServicePointcuts.java
│       ├── client/
│       │   ├── WorkloadClient.java             # interface
│       │   ├── WorkloadClientImpl.java         # RestClient + @CircuitBreaker call to gym-crm-workload
│       │   └── WorkloadRequest.java
│       ├── config/
│       │   ├── JwtProperties.java
│       │   ├── OpenApiConfig.java
│       │   ├── RestClientConfig.java           # load-balanced RestClient.Builder bean
│       │   └── SecurityConfig.java             # filter chain, CORS, auth provider
│       ├── controller/
│       │   ├── AuthController.java             # /api/login, /api/logout
│       │   ├── GlobalExceptionHandler.java
│       │   ├── TraineeController.java
│       │   ├── TrainerController.java
│       │   ├── TrainingController.java
│       │   └── UserController.java             # shared password change, training types
│       ├── dto/
│       │   ├── Request.java               # sealed interface with nested records
│       │   ├── Response.java               # sealed interface with nested records
│       │   ├── Profile.java                # sealed interface (Trainee, Trainer records)
│       │   └── Summary.java                # sealed interface (Training record)
│       ├── entity/
│       │   ├── User.java                   # base @Entity, joined-table inheritance, soft delete
│       │   ├── Trainee.java                # extends User
│       │   ├── Trainer.java                # extends User
│       │   ├── Training.java
│       │   └── TrainingType.java           # @Immutable
│       ├── exception/
│       │   ├── AuthenticationException.java
│       │   ├── AlreadyActivatedException.java
│       │   ├── AlreadyDeactivatedException.java
│       │   ├── UserDoesNotExistException.java
│       │   ├── TrainingNotFoundException.java
│       │   └── TrainingTypeNotFoundException.java
│       ├── filter/
│       │   └── TransactionIdFilter.java
│       ├── model/
│       │   └── Address.java                # @Embeddable record
│       ├── repository/
│       │   ├── BaseUserRepository.java     # shared JPA + native-query operations
│       │   ├── UserRepository.java
│       │   ├── TraineeRepository.java / TraineeRepositoryCustom(Impl)
│       │   ├── TrainerRepository.java / TrainerRepositoryCustom(Impl)
│       │   ├── TrainingRepository.java
│       │   └── TrainingTypeRepository.java
│       ├── security/
│       │   ├── GymCRMUserDetails.java / GymCRMUserDetailsService.java
│       │   ├── JwtFilter.java / JwtService.java
│       │   ├── LoginAttemptService.java    # brute-force lockout (3 attempts / 5 min block)
│       │   └── TokenBlacklist.java         # logout invalidation
│       ├── service/
│       │   ├── TraineeService.java / TrainerService.java / TrainingService.java / UserService.java
│       │   └── impl/…
│       ├── util/
│       │   ├── CredentialGenerator.java
│       │   └── DBCredentialGenerator.java
│       └── validation/
│           ├── Validator.java
│           └── RequestValidator.java
└── test/
    └── java/tech/provokedynamic/gymcrm/
        ├── aspect/RequestValidationAspectTest.java
        ├── controller/…ControllerTest.java     # MockMvc standalone tests per controller
        ├── repository/
        │   ├── BaseRepositoryTest.java          # shared @DataJpaTest base, EntityManager
        │   ├── SharedPostgres.java              # singleton Testcontainers PostgreSQL instance
        │   └── …RepositoryTest.java
        ├── security/LoginAttemptServiceTest.java
        ├── service/impl/…ServiceImplTest.java   # Mockito unit tests
        ├── util/CredentialGeneratorTest.java
        └── validation/impl/RequestValidatorImplTest.java
```

---

## Domain Model

### User (base entity, joined-table inheritance, soft delete)

| Field     | Type    | Notes                                  |
|-----------|---------|----------------------------------------|
| id        | Long    | auto-generated PK                      |
| firstName | String  | required                               |
| lastName  | String  | required                               |
| username  | String  | unique, auto-generated: `First.Last`   |
| password  | String  | BCrypt-hashed                          |
| isActive  | boolean | backs `@SoftDelete` — defaults to true |

### Trainee extends User

| Field       | Type      | Notes                               |
|-------------|-----------|-------------------------------------|
| dateOfBirth | LocalDate | optional, past or present           |
| address     | Address   | optional, embedded                  |
| trainers    | Set       | many-to-many with Trainer           |
| trainings   | Set       | one-to-many, `mappedBy = "trainee"` |

### Trainer extends User

| Field          | Type         | Notes                                 |
|----------------|--------------|---------------------------------------|
| specialization | TrainingType | required, FK, lazy                    |
| trainees       | Set          | many-to-many, `mappedBy = "trainers"` |
| trainings      | Set          | one-to-many, `mappedBy = "trainer"`   |

### Training

| Field            | Type         | Notes                        |
|------------------|--------------|------------------------------|
| id               | Long         | auto-generated PK (IDENTITY) |
| trainee          | Trainee      | required, FK                 |
| trainer          | Trainer      | required, FK                 |
| trainingType     | TrainingType | required, FK                 |
| trainingName     | String       | required, max 100 chars      |
| trainingDate     | LocalDate    | required, today or future    |
| trainingDuration | Integer      | required, 1–480 minutes      |

### Address (embeddable)

| Field      | Type   | Notes                        |
|------------|--------|------------------------------|
| street     | String | required in DTO validation   |
| city       | String | required in DTO validation   |
| country    | String | required in DTO validation   |
| postalCode | String | required, 4–10 digit pattern |

### TrainingType (immutable lookup table)

| Field            | Type   | Notes                                                  |
|------------------|--------|--------------------------------------------------------|
| id               | Long   | auto-generated PK                                      |
| trainingTypeName | String | unique, seeded/managed data — not updated from the app |

---

## Request / Response DTOs

`Request` and `Response` are sealed interfaces with nested records:

```
Request (sealed interface)
├── CreateTrainee(firstName, lastName, dateOfBirth?, address?)
├── CreateTrainer(firstName, lastName, specialization)
├── UpdateTrainee(username, firstName, lastName, dateOfBirth?, address?, isActive)
├── UpdateTrainer(username, firstName, lastName, specialization, isActive)
├── UpdateTraineeTrainers(username, trainerUsernames)
├── ChangePassword(username, password, newPassword)
├── ToggleActive(username, isActive)
├── DeleteTrainee(username)
├── AddTraining(traineeUsername, trainerUsername, trainingName, trainingDate, trainingDuration)
└── CancelTraining(trainingId)

Response (sealed interface)
├── CreatedUser(username, password)
├── TraineeProfile(firstName, lastName, dateOfBirth, address, isActive, trainers[])
├── TrainerProfile(username, firstName, lastName, specialization, isActive, trainees[])
├── TrainingSummary(trainingName, trainingDate, trainingType, trainingDuration, trainerName?, traineeName?)
└── TrainingType(id, trainingTypeName)
```

`Profile` (sealed interface, `Trainee` / `Trainer` records) sits between entities and `Response`, with static
`from(entity)` / `fromEntity(entity)` factory methods mapping from JPA entities.

`Summary.Training` is the flat projection returned directly by the custom criteria-query repository methods
(`findTrainingsByUsername`), constructed via `CriteriaBuilder.construct(...)`.

---

## Username & Password Generation

- **Username**: `firstName.lastName` (e.g. `John.Smith`)
- **Duplicates**: suffix with incrementing number (`John.Smith1`, `John.Smith2`, …)
- **Uniqueness check**: `existsByUsernameIncludingDeleted` — queries against all users including soft-deleted ones
- **Password**: 10 random alphanumeric characters generated via `SecureRandom`, returned once (raw) to the caller
  and stored BCrypt-encoded

---

## Authentication & Authorization

- Stateless JWT auth via Spring Security (`SessionCreationPolicy.STATELESS`).
- `POST /api/login` authenticates against `GymCRMUserDetailsService` (backed by `UserRepository`) and returns a
  signed JWT (`JwtService`, `io.jsonwebtoken`).
- `JwtFilter` runs before `UsernamePasswordAuthenticationFilter`, validates the `Bearer` token, and populates the
  `SecurityContext`.
- `POST /api/logout` adds the token to an in-memory `TokenBlacklist`, which `JwtService.isValid` checks on every
  request.
- `LoginAttemptService` blocks a username for 5 minutes after 3 consecutive failed login attempts
  (`POST /api/login` returns `429 Too Many Requests`).
- Public endpoints (no token required): `POST /api/trainees`, `POST /api/trainers`, `POST /api/login`,
  `GET /api/training-types`, Swagger UI / OpenAPI docs. Everything else requires a valid Bearer token.

---

## Validation

- **`@Validate`** — placed on service impl methods accepting `Request` arguments
- **`RequestValidationAspect`** — `@Before` advice intercepting annotated methods, invokes `RequestValidator`
- **`RequestValidator`** — wraps Jakarta `Validator`, throws `ConstraintViolationException` on failure, logs at
  `warn`
- Controller-level `@Valid @RequestBody` also enforces Jakarta Bean Validation constraints, so invalid payloads are
  rejected with `400` before even reaching the service layer in most cases; `@Validate` provides a second line of
  defense at the service boundary.
- `GlobalExceptionHandler` (`@RestControllerAdvice`) maps validation, auth, and domain exceptions to appropriate
  `ProblemDetail` responses (`400`, `401`, `404`, `409`, `429`, `500`).

---

## Persistence

- **Hibernate** as the JPA provider (Spring Data JPA repositories + hand-written `EntityManager`/Criteria queries
  for filtered training lookups)
- **Joined-table inheritance** for `User` / `Trainee` / `Trainer` with a `u_type` discriminator column
- **Snake case** physical naming strategy via `PhysicalNamingStrategySnakeCaseImpl`
- **HikariCP** connection pool (via Spring Boot autoconfiguration)
- **`@OnDelete(CASCADE)`** on `Trainee` for DB-level cascade deletion of trainings
- **`@Immutable`** on `TrainingType` — never updated from the application
- **`@SoftDelete(strategy = ACTIVE, columnName = "is_active")`** on `User` — deactivation is a soft-delete flag, not
  a row deletion
- Native SQL used for activate/deactivate (`BaseUserRepository.activateByUsername` / `deactivateByUsername`) with
  `org.hibernate.query.native.spaces` query hints to keep the first-level cache in sync
- **Flyway** manages schema migrations (`src/main/resources/db/migration`), including an early migration that
  switches `IDENTITY` generation away from manual sequences

---

## Transaction Management

- `@Transactional` on all write operations
- `@Transactional(readOnly = true)` on all read operations
- Deleting a trainee first reads their trainings (with trainer eagerly fetched), deletes the trainee (cascading to
  trainings at the DB level), then reports a `DELETE` workload event per affected training to `gym-crm-workload`

---

## Downstream Integration — `gym-crm-workload`

Whenever a training is added, cancelled, or a trainee (with trainings) is deleted, `gym-crm` calls
`gym-crm-workload` to keep trainer monthly-hours totals in sync:

- Uses a **load-balanced `RestClient`** (`@LoadBalanced RestClient.Builder`, resolved via Eureka against
  `http://gym-crm-workload`)
- Wrapped in a **Resilience4j circuit breaker** (`workloadService` instance, configured in
  `gym-crm-config-server`'s `gym-crm.yaml`); on failure, `sendWorkloadFallback` logs the error rather than failing
  the calling request
- Carries a service-to-service JWT (`JwtService.generateToken("gym-crm-service")`) and propagates the current
  `X-Transaction-Id` for cross-service log correlation

---

## Logging

| Level   | Used for                                                                                                  |
|---------|-----------------------------------------------------------------------------------------------------------|
| `trace` | Aspect interception (validation firing)                                                                   |
| `debug` | Method entry/exit, fetched result counts                                                                  |
| `info`  | Successful mutations (created, updated, deleted), request/response summaries via `OperationLoggingAspect` |
| `warn`  | Expected failures (auth failure, already active, validation errors, etc.)                                 |
| `error` | Unhandled exceptions, workload-delivery failures                                                          |

Console logs (dev profile) include the transaction ID and colorized level via `logback-spring.xml`; the prod
profile writes plain, rolling daily log files instead.

---

## Injection Strategy

- **Constructor injection** throughout — services, repositories, aspects, filters, security components
  (`@RequiredArgsConstructor`)
- Custom repository implementations (`TraineeRepositoryCustomImpl`, `TrainerRepositoryCustomImpl`) inject
  `EntityManager` via constructor for hand-written Criteria queries

---

## Running Locally

Start dependencies first, in order:

```bash
# 1. Config server (port 8071)
cd gym-crm-config-server && ./mvnw spring-boot:run

# 2. Eureka server (port 8761)
cd gym-crm-eureka-server && ./mvnw spring-boot:run

# 3. Workload service (port 8082)
cd gym-crm-workload && ./mvnw spring-boot:run

# 4. This service (port 8081) — requires a local PostgreSQL on 5432 (dev profile)
./mvnw spring-boot:run
```

Once running:

- Swagger UI: `http://localhost:8081/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8081/api-docs`

---

## Testing

- **Repository tests** (`@DataJpaTest`) run against a real PostgreSQL instance via Testcontainers. A single
  container is started once per JVM in `SharedPostgres` and shared across all test classes through
  `BaseRepositoryTest`. Each test runs inside a transaction that's rolled back afterward, keeping tests isolated
  without truncating tables between runs.
- **Service tests** use Mockito (`@InjectMocks`/`@Mock`) to unit-test business logic in isolation.
- **Controller tests** use `MockMvc` in standalone mode (`MockMvcBuilders.standaloneSetup(...)`) with the real
  `GlobalExceptionHandler` wired in, so status-code mapping for domain exceptions is verified alongside request
  validation.
- **Aspect tests** (`RequestValidationAspectTest`) load a minimal Spring context with `@EnableAspectJAutoProxy` to
  verify the validation aspect actually fires around service methods.