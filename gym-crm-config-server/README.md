# gym-crm-config-server

Spring Cloud Config Server — the single source of configuration for every other service in the
system.

Port: **8071**

## Responsibilities

Serves YAML configuration files from its own classpath (`native` profile — no separate Git repo) to
every other module at startup, via `spring.config.import: configserver:http://localhost:8071` in
each consuming service's `application.yaml`.

This must be **the first service started** in any environment — every other module fails to start
without it (or falls back to its own bare-bones `application.yaml` defaults, which is intentionally
minimal).

## Tech stack

- Spring Boot 4.1, Java 25
- `spring-cloud-config-server` (native backend)

## Layout

```
src/main/resources/config/
├── gym-crm.yaml                        → gym-crm (core service)
├── gym-crm-authorization-server.yaml   → gym-crm-authorization-server
└── gym-crm-workload.yaml               → gym-crm-workload
```

Each file's name must exactly match the consuming service's `spring.application.name`. Each file uses
Spring's multi-document YAML format (`---` separators) with `spring.config.activate.on-profile` to
provide `dev` and `prod` variants — the config server just serves the whole file; profile resolution
happens client-side based on the requesting service's active profile.

## Configuration served (summary)

| File                                | Key settings                                                                                    |
|-------------------------------------|-------------------------------------------------------------------------------------------------|
| `gym-crm.yaml`                      | Server port (8081), Postgres datasource, JWT issuer-uri, Kafka producer, CORS, Consul discovery |
| `gym-crm-authorization-server.yaml` | Server port (9000), Postgres datasource, OAuth2 issuer                                          |
| `gym-crm-workload.yaml`             | Server port (8082), JWT issuer-uri, Kafka consumer, Consul discovery                            |

See each service's own README for the full property list and environment variables used in `prod`.

## Running

```bash
mvn spring-boot:run
```

No external dependencies (no database, no Kafka) — this service only serves static config from its
own classpath, so it's always the fastest to start and should always be started first.

## Verifying config is being served correctly

```bash
curl http://localhost:8071/gym-crm/dev
curl http://localhost:8071/gym-crm-workload/prod
```

Returns the resolved property source for that application/profile combination as JSON — useful for
confirming a config change actually took effect before restarting a downstream service.

## Adding a new service

1. Create `src/main/resources/config/<spring.application.name>.yaml`.
2. Add `dev` and `prod` profile documents as needed.
3. In the new service's own `application.yaml`, set `spring.application.name` to match and add
   `spring.config.import: "configserver:http://localhost:8071"`.

## Running tests

```bash
mvn test
```