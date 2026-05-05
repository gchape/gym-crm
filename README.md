# Gym CRM System

A Spring-based CRM module for managing gym trainees, trainers, and training sessions.

---

## Tech Stack

- **Java 25**
- **Spring Boot** (Spring Core, Spring AOP)
- **Jackson 3** (`tools.jackson`) for JSON deserialization
- **Jakarta Bean Validation** with Hibernate Validator
- **Logback** with dev/prod profiles
- **JUnit 5 + AssertJ**

---

## Architecture

```
┌─────────────────────────────────────────────┐
│                   GymFacade                 │
└────────────┬────────────┬───────────┬───────┘
             │            │           │
     ┌───────▼──┐  ┌──────▼──┐  ┌─────▼───────┐
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
                         ▲
                  ┌──────┴──────┐
                  │  Storage    │
                  │ Initializer │
                  │ (on startup)│
                  └─────────────┘
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
│   │   │   ├── InMemoryStorage.java          # shared ConcurrentHashMap storage
│   │   │   └── StorageInitializer.java       # loads init-data.json on startup
│   │   ├── config/
│   │   │   └── JacksonConfig.java            # JsonMapper bean with JavaTimeModule
│   │   ├── dao/
│   │   │   ├── AbstractDao.java              # generic CRUD via Storage
│   │   │   ├── CrudDao.java                  # generic CRUD interface
│   │   │   ├── TraineeDao.java
│   │   │   ├── TrainerDao.java
│   │   │   └── TrainingDao.java
│   │   ├── dto/
│   │   │   ├── Request.java                  # sealed marker interface
│   │   │   ├── Response.java                 # sealed marker interface
│   │   │   ├── TraineeRequest.java           # sealed interface + Create/Update records
│   │   │   ├── TrainerRequest.java           # sealed interface + Create/Update records
│   │   │   ├── TrainingRequest.java          # sealed interface + Create record
│   │   │   ├── TraineeResponse.java          # sealed interface + Summary/Detail records
│   │   │   ├── TrainerResponse.java          # sealed interface + Summary/Detail records
│   │   │   └── TrainingResponse.java         # sealed interface + Summary/Detail records
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
│       ├── application.yaml                  # dev/prod profiles + storage path
│       ├── logback.xml                       # dev + prod log profiles
│       └── data/
│           └── init-data.json                # seed data loaded on startup
└── test/
    └── java/tech/provokedynamic/gymcrm/
        ├── component/
        │   ├── CredentialGeneratorTest.java  # unit tests, no Spring context
        │   ├── InMemoryStorageTest.java      # unit tests, no Spring context
        │   └── StorageInitializerTest.java   # sliced context, startup loading tests
        ├── dao/
        │   ├── AbstractDaoTest.java          # unit tests, no Spring context
        │   └── TestDao.java                  # test-only AbstractDao subclass
        └── service/
            ├── TraineeServiceImplTest.java   # sliced context, validation + CRUD tests
            ├── TrainerServiceImplTest.java   # sliced context, validation + CRUD tests
            └── TrainingServiceImplTest.java  # sliced context, validation + CRUD tests
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
| dateOfBirth | LocalDate | must be in the past           |
| address     | Address   | required, cascades validation |

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

| Field      | Type   | Notes                 |
|------------|--------|-----------------------|
| street     | String | required              |
| city       | String | required              |
| country    | String | required              |
| postalCode | String | required, 4-10 digits |

---

## Request DTOs

Each request type is a sealed interface with nested `Create` and `Update` records.
Validation annotations live on the record components of each permit:

```
TraineeRequest (sealed interface)
├── Create(firstName, lastName, dateOfBirth, address)
└── Update(firstName, lastName, dateOfBirth, address, active)

TrainerRequest (sealed interface)
├── Create(firstName, lastName, specialization)
└── Update(firstName, lastName, specialization, active)

TrainingRequest (sealed interface)
└── Create(traineeId, trainerId, trainingName, trainingType, trainingDate, trainingDuration)
```

## Response DTOs

Each response type is a sealed interface with nested `Summary` and `Detail` records,
exposing only the fields relevant to each use case. Static `from()` factory methods
map from entity to response:

```
TraineeResponse (sealed interface)
├── Summary(id, username, firstName, lastName, isActive)
└── Detail(id, username, firstName, lastName, isActive, dateOfBirth, address)

TrainerResponse (sealed interface)
├── Summary(id, username, firstName, lastName, isActive)
└── Detail(id, username, firstName, lastName, isActive, specialization)

TrainingResponse (sealed interface)
├── Summary(traineeId, trainerId, trainingName, trainingType)
└── Detail(traineeId, trainerId, trainingName, trainingType, trainingDate, trainingDuration)
```

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

### Initialization

On application startup, `StorageInitializer` reads `init-data.json` via `@PostConstruct` and populates the storage map.
The file path is configured via `application.yaml`:

```yaml
storage:
  data:
    path: classpath:data/init-data.json
```

Jackson 3 deserializes entities using `@JsonDeserialize(builder = ...)` on `Trainee` and `Trainer`, and `@JsonProperty`
on `Training` record components.

---

## Validation

Single-layer validation via AOP:

- **`@Validate`** — placed on service impl methods that accept `Request` arguments
- **`ValidationAspect`** — intercepts annotated methods, checks if any argument is a `Request`, validates via
  `BeanValidator.INSTANCE`, scoped to `tech.provokedynamic.gymcrm.service..*`
- **`BeanValidator`** — enum singleton wrapping Jakarta `Validator`, usable outside Spring context

`@Validate` must be placed on the concrete implementation class, not the interface. Spring's `@annotation` pointcut
resolves annotations against the target class method, not the proxy interface method — the same limitation applies to
`@Transactional` and other Spring AOP-driven annotations.

Entity builders are public due to Java's lack of cross-package encapsulation without JPMS. The service layer is the
enforced entry point — direct builder usage bypasses validation, intentionally left as a known limitation.

---

## Testing

**Unit tests** (no Spring context, plain instantiation):

- `CredentialGeneratorTest` — username generation, suffix incrementing, password length and uniqueness
- `InMemoryStorageTest` — key generation, put/get/delete, namespace isolation
- `AbstractDaoTest` — save, findById, findAll, update, delete via a test-only `TestDao` subclass

**Integration tests** (sliced Spring context):

Service tests load only the classes needed for each slice, keeping startup fast

- `TraineeServiceImplTest` — valid/invalid requests, address and postal code validation, boundary values, update,
  findById, findAll, delete, not-found
- `TraineeServiceImplTest` — valid/invalid requests, specialization validation, username uniqueness, update, findById,
  findAll, delete, not-found
- `TrainingServiceImplTest` — valid/invalid requests, date and duration validation, boundary values, ID constraints,
  findById, findAll
- `StorageInitializerTest` — entity count per namespace, type correctness, field values, namespace isolation, duplicate
  username handling

`GymFacade` is not tested directly — it is pure delegation with no logic of its own. Service tests cover all behavior
end-to-end.

### Coverage

| Metric | Coverage      |
|--------|---------------|
| Class  | 100% (33/33)  |
| Method | 85% (109/127) |
| Line   | 90% (311/344) |
| Branch | 82% (28/34)   |

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
- `Request` sealed marker interface with sealed interfaces and records (`TraineeRequest`, `TrainerRequest`,
  `TrainingRequest`)
- `Response` sealed marker interface with sealed interfaces and records (`TraineeResponse`, `TrainerResponse`,
  `TrainingResponse`), with static `from()` factory methods
- `InMemoryStorage` with namespace support
- `AbstractDao` with generic CRUD
- `TraineeDao`, `TrainerDao`, `TrainingDao`
- `TraineeService`, `TrainerService`, `TrainingService` with interfaces and implementations
- `CredentialGenerator` (username + password generation)
- `BeanValidator` enum singleton
- `@Validate` annotation + `ValidationAspect` (scoped to service package, on impl methods)
- `GymFacade` with constructor injection
- `JacksonConfig` with `JsonMapper`
- `StorageInitializer` with `@PostConstruct` loading from `init-data.json`
- Property placeholder for file path via `application.yaml`
- Logback with dev/prod profiles
- Unit tests for `CredentialGenerator`, `InMemoryStorage`, `AbstractDao`
- Sliced context integration tests for `TraineeService`, `TrainerService`, `TrainingService`
- Sliced context integration tests for `StorageInitializer`
- `GymFacade` not tested — pure delegation, no logic to verify

---

## TODO 📋

### Technical debt

- [ ] JPMS (`module-info.java`) for true cross-package encapsulation

### Future enhancements

- [ ] Add pagination to `findAll()` methods
- [ ] Add `findByUsername()` to Trainee/Trainer services
- [ ] Custom exception hierarchy (`TraineeNotFoundException`, `TrainerNotFoundException`, etc.)