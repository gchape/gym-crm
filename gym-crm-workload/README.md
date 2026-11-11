# Trainer workload service

Kafka-driven, **MongoDB-persisted** monthly training totals. Uses Spring Boot 4.1.2 and Java 25. See the [repository README](../README.md) for setup, architecture, and tests.

- Port: **8082**
- API: `GET /api/trainers/workload/{username}` (JWT with trainer role)
- Swagger UI: <http://localhost:8082/swagger-ui.html>
- Topic: `trainer-workload-events`, partition key: trainer username
- Dead-letter topic: `trainer-workload-events.DLT`
- Storage: one versioned MongoDB document per trainer with nested year/month totals

`ADD` increments monthly minutes; `DELETE` decrements them, floored at zero. Changes survive service restarts because they are stored in MongoDB. Kafka offsets determine which events are consumed next; `auto-offset-reset: earliest` does not replay acknowledged records on every restart.

The listener propagates processing failures to a `DefaultErrorHandler`. It retries transient processing failures three times before dead-letter publishing; invalid decoded events are dead-lettered immediately. Failed dead-letter publication is propagated. Duplicate deliveries are not yet deduplicated, and malformed JSON needs a deserialization recovery policy.

Run the full local stack from the repository root:

```bash
docker compose -f docker-compose.full.yml up --build
```

The `standalone` workload profile is a legacy startup configuration and still needs MongoDB/Kafka for useful operations. The infrastructure-free standalone demo runs the CRM API only.

Run all tests with `./mvnw verify` from the root. Cucumber integration tests use actual Kafka and MongoDB containers and reproduce the CRM producer's event contract; they do not launch the CRM API process.
