# gym-crm-workload

Consumes trainer workload events from Kafka and exposes an aggregated, per-trainer, per-month
duration summary via a read-only REST endpoint.

Port: **8082** · Swagger: http://localhost:8082/swagger-ui.html

## Responsibilities

- Consumes `WorkloadEvent`s published by `gym-crm` on the `trainer-workload-events` Kafka topic.
- Maintains an **in-memory** running total of training minutes per trainer, grouped by calendar
  month (`YearMonth`).
- Exposes `GET /api/trainers/workload/{username}` returning that trainer's workload broken down by
  year and month.
- Validates JWTs issued by `gym-crm-authorization-server` as an OAuth2 resource server.

This service deliberately does **not** talk to Postgres or own any persistent state — it's a
read-optimized projection rebuilt entirely from the Kafka event stream.

## Tech stack

- Spring Boot 4.1, Java 25
- Spring Kafka (consumer)
- Spring Security (OAuth2 resource server)
- Spring Cloud Consul (service discovery) + Config Client
- springdoc-openapi (Swagger UI)
- AspectJ (`@Around` request/response logging)

## Data model

```java
TrainerWorkloadSummary
├──trainerUsername,trainerFirstName,trainerLastName,trainerStatus
└──
Map<YearMonth, AtomicInteger> monthlyDurations
```

A single flat `Map<YearMonth, AtomicInteger>` per trainer — `WorkloadResponseMapper` groups this back
into a year → month tree only at the API boundary, when building the response DTO. Internal storage
stays as simple as the data actually is.

`WorkloadServiceImpl` holds one `ConcurrentHashMap<String, TrainerWorkloadSummary>` for all trainers,
process-wide, in memory.

## Kafka

- **Topic**: `trainer-workload-events` (constant defined once in `gym-crm-common`'s
  `WorkloadTopics`, shared with the producer in `gym-crm` — never duplicated as a raw string).
- **Consumer group**: `gym-crm-workload`
- **Deserialization**: JSON, trusting only `tech.provokedynamic.gymcrmcommon.event` package, typed
  directly to `WorkloadEvent` (`spring.json.use.type.headers: false` — the type isn't inferred from
  a header, both sides just agree on the DTO shape via the shared `gym-crm-common` dependency).
- **`auto-offset-reset: earliest`** — on a cold start (or after wiping in-memory state), the consumer
  replays the entire topic from the beginning to rebuild every trainer's summary. This only works
  within the topic's configured retention window.

`ADD` events increment a month's duration; `DELETE` events decrement it, floored at zero
(`Math.max(0, current - duration)`) so a duplicate or out-of-order delete can't push a total
negative.

### ⚠️ Known limitation: no dead-letter handling

`WorkloadEventListener` catches and logs any exception thrown while processing an event, but the
Kafka offset still commits regardless. A malformed or unexpected event is silently dropped rather
than retried or routed to a DLT — acceptable for a side project, but worth adding a dead-letter topic
before this handles anything that matters.

### ⚠️ Known limitation: state is lost on restart

Because `TrainerWorkloadSummary` lives only in a `ConcurrentHashMap`, restarting this service resets
all workload data to zero until Kafka replays events from `auto-offset-reset: earliest`. Confirm your
topic's retention period is long enough to fully reconstruct history before relying on this in an
environment where restarts are frequent.

## API

| Endpoint                                | Auth | Response                                                                                        |
|-----------------------------------------|------|-------------------------------------------------------------------------------------------------|
| `GET /api/trainers/workload/{username}` | JWT  | `200` with `TrainerWorkloadResponse`, or `404` if no events have been seen for that trainer yet |

## Configuration

Config is pulled from `gym-crm-config-server` — see
`gym-crm-config-server/src/main/resources/config/gym-crm-workload.yaml`.

In `prod`, `issuer-uri`, Consul host/port, and `KAFKA_BOOTSTRAP_SERVERS` are sourced from
`OAUTH2_ISSUER_URI`, `CONSUL_HOST`/`CONSUL_PORT`, and `KAFKA_BOOTSTRAP_SERVERS` respectively.

## Running tests

```bash
mvn test
```