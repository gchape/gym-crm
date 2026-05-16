# Gym CRM System

A Spring-based CRM system for managing gym trainees, trainers, and training sessions.

---

## Tech Stack

- **Java 25**
- **Spring Framework 7** (Spring Core, Spring AOP, Spring TX)
- **Hibernate ORM 7** (JPA provider)
- **HikariCP** (connection pooling)
- **Jakarta Bean Validation** with Hibernate Validator
- **Lombok** (`@SuperBuilder`, `@Getter`) for entity boilerplate
- **JSpecify** for null safety annotations
- **Logback** for logging
- **JUnit 5 + AssertJ** for testing

---

## Architecture

```
┌──────────────────────────────────────────────────────┐
│                     Service Layer                    │
│       TraineeService  TrainerService  TrainingService│
└────────────┬────────────┬───────────────┬────────────┘
             │            │               │
      ┌──────▼──┐  ┌──────▼──┐  ┌────────▼────┐
      │Trainee  │  │Trainer  │  │  Training   │
      │  DAO    │  │  DAO    │  │    DAO      │
      └──────┬──┘  └──────┬──┘  └────────┬────┘
             │            │               │
             └────────────▼───────────────┘
                   ┌──────────────┐
                   │ EntityManager│
                   │  (Hibernate) │
                   └──────────────┘
                          ▲
                   ┌──────┴──────┐
                   │  HikariCP   │
                   │ DataSource  │
                   └─────────────┘
```

### Cross-cutting Concerns (AOP)

```
Request → @Validate → RequestValidationAspect (order=2)
        → @Authenticated → AuthenticationValidationAspect (order=1)
        → Service Method
```

Authentication runs before validation — invalid credentials are rejected first.

---

## Project Structure

```
src/
├── main/
│   └── java/tech/provokedynamic/gymcrm/
│       ├── annotation/
│       │   ├── Authenticated.java        # triggers auth aspect
│       │   └── Validate.java             # triggers validation aspect
│       ├── aspect/
│       │   ├── AuthenticationValidationAspect.java
│       │   ├── RequestValidationAspect.java
│       │   └── pointcuts/
│       │       ├── AnnotationPointcuts.java
│       │       └── ServicePointcuts.java
│       ├── config/
│       │   └── PersistenceConfig.java    # DataSource, EntityManagerFactory, TX
│       ├── dao/
│       │   ├── UserDao.java
│       │   ├── TraineeDao.java
│       │   ├── TrainerDao.java
│       │   ├── TrainingDao.java
│       │   ├── TrainingTypeDao.java
│       │   └── impl/
│       │       ├── UserDaoImpl.java
│       │       ├── TraineeDaoImpl.java
│       │       ├── TrainerDaoImpl.java
│       │       ├── TrainingDaoImpl.java
│       │       └── TrainingTypeDaoImpl.java
│       ├── dto/
│       │   ├── Request.java              # sealed interface with nested records
│       │   ├── Profile.java              # sealed interface (Trainee, Trainer records)
│       │   └── Summary.java              # sealed interface (Training record)
│       ├── entity/
│       │   ├── User.java                 # base @Entity with @Inheritance
│       │   ├── Trainee.java              # extends User
│       │   ├── Trainer.java              # extends User
│       │   ├── Training.java
│       │   └── TrainingType.java         # @Immutable
│       ├── exception/
│       │   ├── AuthenticationException.java
│       │   ├── AlreadyActivatedException.java
│       │   ├── AlreadyDeactivatedException.java
│       │   ├── UserDoesNotExistException.java
│       │   └── TrainingTypeNotFoundException.java
│       ├── model/
│       │   └── Address.java              # @Embeddable record
│       ├── service/
│       │   ├── TraineeService.java
│       │   ├── TrainerService.java
│       │   ├── TrainingService.java
│       │   └── impl/
│       │       ├── TraineeServiceImpl.java
│       │       ├── TrainerServiceImpl.java
│       │       └── TrainingServiceImpl.java
│       ├── util/
│       │   └── CredentialGenerator.java
│       └── validation/
│           ├── RequestValidator.java
│           └── impl/
│               └── RequestValidatorImpl.java
└── test/
    └── java/tech/provokedynamic/gymcrm/
        └── service/
            ├── TraineeServiceImplTest.java
            ├── TrainerServiceImplTest.java
            └── TrainingServiceImplTest.java
```

---

## Domain Model

### User (base entity, single-table inheritance)

| Field     | Type    | Notes                           |
|-----------|---------|---------------------------------|
| id        | Long    | auto-generated PK               |
| firstName | String  | required                        |
| lastName  | String  | required                        |
| username  | String  | auto-generated: `First.Last`    |
| password  | String  | auto-generated: 10 random chars |
| isActive  | boolean | defaults to true                |

### Trainee extends User

| Field       | Type      | Notes                     |
|-------------|-----------|---------------------------|
| dateOfBirth | LocalDate | optional, past or present |
| address     | Address   | optional, embedded        |
| trainers    | Set       | many-to-many with Trainer |

### Trainer extends User

| Field          | Type         | Notes        |
|----------------|--------------|--------------|
| specialization | TrainingType | required, FK |

### Training

| Field            | Type         | Notes                     |
|------------------|--------------|---------------------------|
| id               | Long         | auto-generated PK         |
| trainee          | Trainee      | required, FK              |
| trainer          | Trainer      | required, FK              |
| trainingName     | String       | required, max 100 chars   |
| trainingType     | TrainingType | required, FK              |
| trainingDate     | LocalDate    | required, today or future |
| trainingDuration | Integer      | required, 1-480 minutes   |

### Address (embeddable)

| Field   | Type   | Notes    |
|---------|--------|----------|
| street  | String | optional |
| city    | String | optional |
| country | String | optional |

### TrainingType (immutable lookup table)

| Field            | Type   | Notes                        |
|------------------|--------|------------------------------|
| id               | Long   | auto-generated PK            |
| trainingTypeName | String | constant values, not updated |

---

## Request DTOs

All requests implement the sealed `Request` interface. Authenticated requests
additionally implement `Request.Authenticated` which exposes `username()` and `password()`.

```
Request (sealed interface)
├── CreateTrainee(firstName, lastName, dateOfBirth?, address?)
├── CreateTrainer(firstName, lastName, specialization)
├── UpdateTrainee(username, password, firstName, lastName, dateOfBirth?, address?)
├── UpdateTrainer(username, password, firstName, lastName, specialization)
├── UpdateTraineeTrainers(username, password, trainerUsernames)
├── ChangePassword(username, password, newPassword)
├── ToggleActive(username, password)
├── DeleteTrainee(username, password)
└── AddTraining(traineeUsername, traineePassword, trainerUsername,
                trainingName, trainingType, trainingDate, trainingDuration)
```

## Response DTOs

```
Profile (sealed interface)
├── Trainee(firstName, lastName, username, dateOfBirth, address)
└── Trainer(firstName, lastName, username, specialization)

Summary (sealed interface)
└── Training(trainingName, trainingDate, trainingDuration, trainerUsername/traineeUsername)
```

Static `from(entity)` factory methods on `Profile.Trainee` and `Profile.Trainer`
map from entity to DTO.

---

## Username & Password Generation

- **Username**: `firstName.lastName` (e.g. `John.Smith`)
- **Duplicates**: suffix with incrementing number (`John.Smith1`, `John.Smith2`)
- **Uniqueness check**: queries against all users including soft-deleted ones
- **Password**: 10 random bytes encoded as UTF-8 string via `SecureRandom.getInstanceStrong()`

---

## Authentication

All operations except `create` require credentials. The `AuthenticationValidationAspect`
intercepts methods annotated with `@Authenticated`, extracts the `Request.Authenticated`
argument, and validates username/password against the database before the method executes.

Authentication runs at `order=1`, validation at `order=2` — invalid credentials are
rejected before validation runs.

---

## Validation

- **`@Validate`** — placed on service impl methods accepting `Request` arguments
- **`RequestValidationAspect`** — intercepts annotated methods, validates via `RequestValidatorImpl`
- **`RequestValidatorImpl`** — wraps Jakarta `Validator`, throws `ConstraintViolationException` on failure, logs at
  `warn`

---

## Implemented Requirements

| #  | Requirement                            | Status |
|----|----------------------------------------|--------|
| 1  | Create Trainer profile                 | ✅      |
| 2  | Create Trainee profile                 | ✅      |
| 3  | Trainee username/password matching     | ✅      |
| 4  | Trainer username/password matching     | ✅      |
| 5  | Select Trainer profile by username     | ✅      |
| 6  | Select Trainee profile by username     | ✅      |
| 7  | Trainee password change                | ✅      |
| 8  | Trainer password change                | ✅      |
| 9  | Update Trainer profile                 | ✅      |
| 10 | Update Trainee profile                 | ✅      |
| 11 | Activate/Deactivate Trainee            | ✅      |
| 12 | Activate/Deactivate Trainer            | ✅      |
| 13 | Delete Trainee (hard delete + cascade) | ✅      |
| 14 | Get Trainee trainings with criteria    | ✅      |
| 15 | Get Trainer trainings with criteria    | ✅      |
| 16 | Add training                           | ✅      |
| 17 | Get unassigned trainers for trainee    | ✅      |
| 18 | Update Trainee's trainers list         | ✅      |

---

## Persistence

- **Hibernate** as the JPA provider, configured via `PersistenceConfig`
- **Single-table inheritance** for `User`/`Trainee`/`Trainer` with discriminator column
- **Snake case** physical naming strategy via `PhysicalNamingStrategySnakeCaseImpl`
- **HikariCP** connection pool with configurable timeout and pool size
- **`@OnDelete(CASCADE)`** on `Trainee` for DB-level cascade deletion of trainings
- **`@Immutable`** on `TrainingType` — never updated from application
- All queries written manually via `EntityManager` — no annotation processor
- `HINT_READ_ONLY` applied to SELECT queries for first-level cache optimization
- `HINT_NATIVE_SPACES` applied to native UPDATE queries for cache synchronization

---

## Transaction Management

- `@Transactional` on all write operations
- `@Transactional(readOnly = true)` on all read operations
- Combined with `HINT_READ_ONLY` at the query level for full optimization

---

## Logging

| Level   | Used for                                               |
|---------|--------------------------------------------------------|
| `trace` | Aspect interception (validation firing)                |
| `debug` | Method entry/exit, fetched result counts               |
| `info`  | Successful mutations (created, updated, deleted)       |
| `warn`  | Expected failures (auth failure, already active, etc.) |

---

## Injection Strategy

- **Constructor injection** throughout — services, DAOs, aspects, utilities
- `EntityManager` injected via constructor (Spring provides scoped proxy automatically)
- `@PersistenceContext` retained only on `UserDaoImpl` field for abstract base class inheritance

---

## TODO 📋

- [ ] Unit tests for DAO layer
- [ ] Unit tests for aspects
- [ ] Unit tests for `CredentialGenerator`
- [ ] Integration tests for service layer
- [ ] Add pagination to training list queries