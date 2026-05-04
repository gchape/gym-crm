# Gym CRM System

A Spring-based CRM module for managing gym trainees, trainers, and training sessions.

---

## Tech Stack

- **Java 25**
- **Spring Boot** (Spring Core, Spring AOP)
- **Jackson 3** (`tools.jackson`) for JSON deserialization
- **Jakarta Bean Validation** with Hibernate Validator
- **Logback** with dev/prod profiles
- **JUnit 5 + Mockito** (planned)

---

## Architecture

```
┌─────────────────────────────────────────────┐
│                   GymFacade                  │
└────────────┬────────────┬───────────────────┘
             │            │
     ┌───────▼──┐  ┌──────▼──┐  ┌─────────────┐
     │ Trainee  │  │ Trainer │  │  Training   │
     │ Service  │  │ Service │  │   Service   │
     └───────┬──┘  └──────┬──┘  └──────┬──────┘
             │            │            │
     ┌───────▼──┐  ┌──────▼──┐  ┌─────▼───────┐
     │ Trainee  │  │ Trainer │  │  Training   │
     │   DAO    │  │   DAO   │  │    DAO      │
     └───────┬──┘  └──────┬──┘  └──────┬──────┘
             │            │            │
             └────────────▼────────────┘
                   ┌──────────────┐
                   │ InMemory     │
                   │ Storage      │
                   │ (single map) │
                   └──────────────┘
```

---

## Project Structure

```
src/
├── main/
│   ├── java/tech/provokedynamic/gymcrm/
│   │   ├── annotations/
│   │   │   └── Validate.java                 # method-level validation trigger
│   │   ├── aspect/
│   │   │   └── ValidationAspect.java         # AOP aspect for @Validate
│   │   ├── component/
│   │   │   ├── CredentialGenerator.java      # username + password generation
│   │   │   └── InMemoryStorage.java          # shared ConcurrentHashMap storage
│   │   ├── config/
│   │   │   └── JacksonConfig.java            # JsonMapper bean with JavaTimeModule
│   │   ├── dao/
│   │   │   ├── AbstractDao.java              # generic CRUD via Storage
│   │   │   ├── CrudDao.java                  # generic CRUD interface
│   │   │   ├── TraineeDao.java
│   │   │   ├── TrainerDao.java
│   │   │   └── TrainingDao.java
│   │   ├── dto/
│   │   │   ├── Request.java                  # marker interface
│   │   │   ├── TraineeRequest.java           # abstract base + Create/Update nested classes
│   │   │   ├── TrainerRequest.java           # abstract base + Create/Update nested classes
│   │   │   └── TrainingRequest.java          # abstract base + Create nested class
│   │   ├── entity/
│   │   │   ├── Entity.java                   # sealed interface
│   │   │   ├── User.java                     # base class with generic builder
│   │   │   ├── Trainee.java                  # extends User, implements Entity
│   │   │   ├── Trainer.java                  # extends User, implements Entity
│   │   │   └── Training.java                 # record, implements Entity
│   │   ├── facade/
│   │   │   └── GymFacade.java                # single entry point, constructor injection
│   │   ├── model/
│   │   │   ├── Address.java                  # record
│   │   │   ├── Specialization.java           # enum
│   │   │   └── TrainingType.java             # enum (same values as Specialization)
│   │   ├── service/
│   │   │   ├── TraineeService.java           # interface
│   │   │   ├── TrainerService.java           # interface
│   │   │   ├── TrainingService.java          # interface
│   │   │   ├── TraineeServiceImpl.java
│   │   │   ├── TrainerServiceImpl.java
│   │   │   └── TrainingServiceImpl.java
│   │   ├── storage/
│   │   │   └── Storage.java                  # storage interface
│   │   └── validation/
│   │       └── BeanValidator.java            # enum singleton validator
│   └── resources/
│       ├── application.yaml                  # dev/prod profiles
│       └── logback.xml                       # dev + prod log profiles
```

---

## Domain Model

### User (base class)

| Field     | Type    | Notes                           |
|-----------|---------|---------------------------------|
| firstName | String  | required                        |
| lastName  | String  | required                        |
| username  | String  | auto-generated: `First.Last`    |
| password  | String  | auto-generated: 10 random chars |
| isActive  | boolean | defaults to true                |

### Trainee extends User

| Field       | Type      | Notes                         |
|-------------|-----------|-------------------------------|
| id          | long      | auto-generated                |
| dateOfBirth | LocalDate | optional, must be past        |
| address     | Address   | optional, cascades validation |

### Trainer extends User

| Field          | Type           | Notes          |
|----------------|----------------|----------------|
| id             | long           | auto-generated |
| specialization | Specialization | required       |

### Training (record)

| Field            | Type         | Notes                     |
|------------------|--------------|---------------------------|
| traineeId        | long         | required, positive        |
| trainerId        | long         | required, positive        |
| trainingName     | String       | required                  |
| trainingType     | TrainingType | required                  |
| trainingDate     | LocalDate    | required, today or future |
| trainingDuration | Duration     | required, min 30 minutes  |

### Address (record)

| Field      | Type   | Notes       |
|------------|--------|-------------|
| street     | String | required    |
| city       | String | required    |
| country    | String | required    |
| postalCode | String | 4-10 digits |

---

## Request DTOs

Each request type uses an abstract base class with nested `Create` and `Update` static classes to share validation and
separate concerns:

```
TraineeRequest
├── Create(firstName, lastName, dateOfBirth, address)
└── Update(firstName, lastName, dateOfBirth, address, isActive)

TrainerRequest
├── Create(firstName, lastName, specialization)
└── Update(firstName, lastName, specialization, isActive)

TrainingRequest
└── Create(traineeId, trainerId, trainingName, trainingType, trainingDate, trainingDuration)
```

Validation annotations live on the base class fields — no duplication between `Create` and `Update`.

---

## Username & Password Rules

- **Username**: `firstName.lastName` (e.g. `John.Smith`)
- **Duplicate names**: suffix with serial number (e.g. `John.Smith1`, `John.Smith2`)
- **Password**: randomly generated 10-character alphanumeric string

---

## Storage

Single shared `ConcurrentHashMap` bean (`InMemoryStorage`) used by all DAOs. Entities are separated by namespace key
prefix:

```
trainee:1  → Trainee
trainee:2  → Trainee
trainer:1  → Trainer
training:1 → Training
```

---

## Validation

Single-layer validation via AOP:

- **`@Validate`** — placed on service interface methods that accept `Request` arguments
- **`ValidationAspect`** — intercepts annotated methods, checks if any argument is a `Request`, validates via
  `BeanValidator.INSTANCE`
- **`BeanValidator`** — enum singleton wrapping Jakarta `Validator`, usable outside Spring context

Entity builders are public due to Java's lack of cross-package encapsulation without JPMS. The service layer is the
enforced entry point — direct builder usage bypasses validation intentionally left as a known limitation.

---

## Configuration

Logback profiles:

- **dev**: DEBUG level, logs to console + file
- **prod**: INFO/WARN level, logs to file only

Activate with:

```
-Dspring.profiles.active=dev
```

---

## Injection Strategy

Per task requirements:

- **Constructor injection**: DAO → Service (required dependencies)
- **Setter injection**: `CredentialGenerator` → Service (auxiliary dependency)
- **Constructor injection**: Services → Facade

---

## Completed ✅

- Domain model (`User`, `Trainee`, `Trainer`, `Training`, `Address`)
- Generic builder pattern with `public` access (JPMS needed for true encapsulation)
- Sealed `Entity` interface
- `Request` marker interface with abstract base classes (`TraineeRequest`, `TrainerRequest`, `TrainingRequest`)
- `InMemoryStorage` with namespace support
- `AbstractDao` with generic CRUD
- `TraineeDao`, `TrainerDao`, `TrainingDao`
- `TraineeService`, `TrainerService`, `TrainingService` with interfaces
- `CredentialGenerator` (username + password generation)
- `BeanValidator` enum singleton
- `@Validate` annotation + `ValidationAspect`
- `GymFacade` with constructor injection
- `JacksonConfig` with `JsonMapper`
- Logback with dev/prod profiles

---

## In Progress 🔧

- Storage initialization — load seed data from file on startup via `@PostConstruct`

---

## TODO 📋

### Required by task

- [ ] **Storage initialization** — `StorageInitializer` with `@PostConstruct`, reads `init-data.json`
- [ ] **Property placeholder** — configure file path via `application.yaml`
- [ ] **Unit tests** — cover all service and DAO methods with JUnit 5 + Mockito

### Technical debt

- [ ] Merge `Specialization` and `TrainingType` — they are identical enums
- [ ] `AtomicLong` ID counters reset on restart — seed data IDs may collide with generated IDs
- [ ] `Storage.get()` returns raw entity instead of `Optional` — risk of null pointer
- [ ] `TrainerRequest` missing `implements Request`
- [ ] `Request` interface should be `sealed` permitting `TraineeRequest`, `TrainerRequest`, `TrainingRequest`
- [ ] `Trainer` constructor should be `private` not `public`

### Future enhancements

- [ ] JPMS (`module-info.java`) for true cross-package encapsulation
- [ ] Add pagination to `findAll()` methods
- [ ] Add `findByUsername()` to Trainee/Trainer services
- [ ] Add `isActive` toggle (activate/deactivate trainee or trainer)
- [ ] Custom exception hierarchy (`TraineeNotFoundException`, `TrainerNotFoundException`, etc.)
