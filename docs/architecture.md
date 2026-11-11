# Architecture decisions and pattern review

Reviewed on **2026-11-11** against Martin Fowler's catalog and *Enterprise Integration Patterns*. The decisions below distinguish code that exists from proposed work. Pattern selection is based on the current registration, training, and workload use cases.

## Application and persistence boundaries

| Pattern | Application here | Decision |
| --- | --- | --- |
| [Service Layer](https://martinfowler.com/eaaCatalog/serviceLayer.html) | `TrainingApplicationService`, `TraineeApplicationService`, `TrainerApplicationService`, and `TrainerWorkloadApplicationService` coordinate use cases. | Keep transaction and validation boundaries in these services. HTTP controllers adapt requests and responses. |
| [Repository](https://martinfowler.com/eaaCatalog/repository.html) | Spring Data repositories own persistence access; `TrainingSpecifications` composes query criteria. | Keep participant visibility inside the query rather than loading and filtering every session in memory. Avoid a second generic CRUD abstraction over Spring Data. |
| [Domain Model](https://martinfowler.com/eaaCatalog/domainModel.html) | `TrainerWorkloadDocument.applyEvent` locates a calendar bucket; `MonthlyWorkload.applyChange` owns duration arithmetic. | Move arithmetic out of the application service. Enforce non-negative totals and checked addition in the model. Persistence annotations remain on this small model rather than duplicating its entire object graph. |
| [Data Transfer Object](https://martinfowler.com/eaaCatalog/dataTransferObject.html) | Named request/response records and separate internal projections. | Keep JSON contracts independent from JPA entities. Map lazy relationships inside service transactions; Open Session in View is disabled. |

Straightforward profile CRUD remains application-service logic. A rich domain hierarchy, generic command bus, or extra factory layer would not simplify those operations. Add domain types when scheduling gains rules such as timeslots, room capacity, and attendance transitions.

The workload projection uses a separate read model, an aspect of [CQRS](https://martinfowler.com/bliki/CQRS.html). This does not make the system event sourced: PostgreSQL owns session state, Kafka has finite retention, and MongoDB stores a mutable projection. Rebuilding that projection requires an explicit reconciliation process, not merely setting `auto-offset-reset=earliest` on an existing consumer group.

## Messaging patterns in use

| Pattern | Concrete implementation | Remaining constraint |
| --- | --- | --- |
| [Messaging Gateway](https://www.enterpriseintegrationpatterns.com/patterns/messaging/MessagingGateway.html) | `WorkloadEventPublisher` encapsulates Kafka topic, key, correlation headers, and send completion logging. | It sends after commit rather than persisting a delivery intent. |
| [Event-Driven Consumer](https://www.enterpriseintegrationpatterns.com/patterns/messaging/EventDrivenConsumer.html) | `WorkloadEventListener` validates through the application service, then persists the projection. | Offset recovery and MongoDB writes do not form one transaction. |
| [Dead Letter Channel](https://www.enterpriseintegrationpatterns.com/patterns/messaging/DeadLetterChannel.html) | `KafkaErrorConfig` retries transient processing failures three times and routes exhausted/invalid decoded events to the explicit `.DLT` destination. | Operators need monitoring and a replay procedure. Malformed JSON needs deserialization recovery. |
| [Correlation Identifier](https://www.enterpriseintegrationpatterns.com/patterns/messaging/CorrelationIdentifier.html) | CRM HTTP correlation IDs propagate to the Kafka `transactionId` header and consumer logs. | This adapts the correlation idea to tracing a one-way event; it is not a request/reply protocol or message identity. |

A running monthly sum is a read projection. It is not a complete implementation of the EIP Aggregator, which also needs a correlation and completion policy for a set of related messages. There is no need to introduce a general aggregation framework for these totals.

## Highest-priority follow-up: atomic delivery and duplicate handling

A process can commit a session and crash before the after-commit send. Sending before commit would instead expose rolled-back state. The [Transactional Outbox](https://microservices.io/patterns/data/transactional-outbox.html) addresses this dual-write gap by persisting delivery intent alongside the business change.

A concrete next implementation should:

1. Store a versioned event envelope (`eventId`, `schemaVersion`, trainer key, creation time, payload) in an `outbox_event` table in the same transaction as the session change.
2. Use a relay with bounded batches, a durable claim/lease, retry scheduling, and broker acknowledgements. Preserve ordering per trainer across API instances; do not let two relays publish a trainer's batches out of order.
3. Retain failed delivery records for inspection, expose age/backlog metrics, and clean acknowledged records through a retention policy.
4. Give cancellation events the same session identity as their corresponding addition, so a projection can reconcile session state rather than relying only on arithmetic deltas.

The relay can publish an event twice if it crashes after the broker acknowledges but before it records delivery. Pair it with an [Idempotent Receiver](https://www.enterpriseintegrationpatterns.com/patterns/messaging/IdempotentReceiver.html). Persist an inbox receipt and the projection update atomically, or use a single atomic conditional document update with a defined receipt-retention policy. A separate `exists(eventId)` check followed by a write is insufficient under concurrency. An unbounded list of processed IDs inside each trainer document would eventually hit MongoDB document-size limits.

MongoDB multi-document transactions require a replica set. An inbox design must therefore include local/production topology changes and crash/replay tests, rather than adding an annotation to the existing standalone Mongo container. Kafka transactions alone cannot make the PostgreSQL and MongoDB writes atomic; see [Transactional Client](https://www.enterpriseintegrationpatterns.com/patterns/messaging/TransactionalClient.html).

These delivery changes are **recommended follow-up work**, not guarantees of the current code.

## Further improvements, in order

| Priority | Improvement | Evidence required before calling it complete |
| --- | --- | --- |
| High | Finish member/staff authorization across legacy profile and workload endpoints. | Cross-account read/write denial tests for every route, plus an explicit staff role model. |
| High | Separate activation, deletion, and personal-data erasure policies. | Tests for inactive participants, retained sessions, reactivation, and workload reconciliation. |
| High | Deserialization recovery for malformed Kafka records. | Send invalid bytes; verify the original bytes and failure metadata reach a recovery channel without blocking subsequent valid events. |
| Medium | Outbox/inbox and projection reconciliation. | Crash at each commit/send/ack boundary, redeliver duplicates, run competing consumers, and verify exact totals. |
| Medium | Monitoring and recovery runbooks. | Alert on oldest undelivered event, DLT arrivals, repeated retries, and projection lag; demonstrate controlled replay. |
| Medium | Scheduling domain expansion. | Define start/end time, timezone, trainer availability, room capacity, and cancellation rules before adding conflict detection. |

A Saga or distributed process manager is unnecessary for today's single-database session command and asynchronous reporting projection. Revisit it only when a command coordinates independent business transactions such as paid membership, payment authorization, and capacity reservation.
