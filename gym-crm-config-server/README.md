# gym-crm-config-server

Centralized configuration server for the `gym-crm` microservices ecosystem, built with Spring Cloud Config Server.

## Purpose

Provides distributed configuration management for all `gym-crm` microservices. Instead of each service holding its
own environment-specific settings (database credentials, JWT secrets, feature flags, Eureka registration details,
etc.) locally, they fetch this configuration remotely from this server at startup.

## Tech Stack

- Spring Boot 4.1.0
- Spring Cloud Config Server 2025.1.2
- Native (file-based / classpath) configuration repository — no Git backend required

## How It Works

This server reads configuration files from `src/main/resources/config/` and serves them over HTTP to any client
that requests them, based on:

- **Application name** (`spring.application.name` on the client)
- **Active profile** (`spring.profiles.active` on the client)

Example request/response flow:

```
Client "gym-crm" with profile "dev" starts up
        │
        ▼
GET http://localhost:8071/gym-crm/dev
        │
        ▼
Config Server reads config/gym-crm.yaml,
merges base block + "dev" profile block
        │
        ▼
Returns merged config as JSON
        │
        ▼
Client applies it to its own Environment
```

## Configuration Files

Located under `src/main/resources/config/`:

| File                    | Served to clients named...                |
|-------------------------|-------------------------------------------|
| `application.yaml`      | All clients (shared defaults, if present) |
| `gym-crm.yaml`          | `gym-crm`                                 |
| `gym-crm-workload.yaml` | `gym-crm-workload`                        |

Each file can contain multiple `---`-separated YAML documents, using `spring.config.activate.on-profile` to scope
sections to specific profiles (`dev`, `prod`, etc.).

### What each client gets

**`gym-crm.yaml`** (served to `gym-crm`):

- Server port (`8081`), Eureka client registration flags
- Resilience4j circuit breaker settings for the `workloadService` instance (sliding window, failure threshold,
  wait duration, half-open call permits)
- springdoc/Swagger paths
- Hibernate snake-case naming strategy, PostgreSQL driver class
- `dev` profile: local Postgres connection, `ddl-auto: create-drop`, local Eureka URL, fast heartbeat intervals
- `prod` profile: connection details and JWT secret sourced from environment variables
  (`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`), `ddl-auto: validate`, Eureka URL defaulting to the
  in-cluster hostname

**`gym-crm-workload.yaml`** (served to `gym-crm-workload`):

- Server port (`8082`), Eureka client registration flags
- `dev` profile: hardcoded JWT secret, local Eureka URL, fast heartbeat intervals
- `prod` profile: `JWT_SECRET` and `EUREKA_URL` sourced from environment variables

⚠️ Both clients' JWT secrets **must match** (they validate/sign the same service-to-service tokens used for the
workload API), so keep the `dev` values in sync and set the same `JWT_SECRET` environment variable for both in
`prod`.

## Running Locally

```bash
./mvnw spring-boot:run
```

Server starts on **port 8071** by default.

Verify it's serving config correctly:

```bash
curl http://localhost:8071/gym-crm/dev
curl http://localhost:8071/gym-crm-workload/dev
```

## Client Setup

Any Spring Boot service that wants to consume config from this server needs:

**Dependency** (`pom.xml`):

```xml

<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-config</artifactId>
</dependency>
```

**Local `application.yaml`:**

```yaml
spring:
  application:
    name: gym-crm       # must match a filename in config/
  profiles:
    active: dev
  config:
    import: "configserver:http://localhost:8071"
```

## Environment Variables (Production)

Production profile blocks reference environment variables instead of hardcoded values:

| Variable      | Used for                                                         | Required by                   |
|---------------|------------------------------------------------------------------|-------------------------------|
| `DB_URL`      | Datasource JDBC URL                                              | `gym-crm`                     |
| `DB_USERNAME` | Database username                                                | `gym-crm`                     |
| `DB_PASSWORD` | Database password                                                | `gym-crm`                     |
| `JWT_SECRET`  | JWT signing secret                                               | `gym-crm`, `gym-crm-workload` |
| `EUREKA_URL`  | Eureka server address (defaults to in-cluster hostname if unset) | `gym-crm`, `gym-crm-workload` |

⚠️ These must be set on the **client's** runtime environment (e.g. `gym-crm`'s or `gym-crm-workload`'s
host/container), not on this config server — the config server only serves the raw `${VAR}` placeholder text;
resolution happens client-side.

## Notes

- This server does not register with Eureka (kept simple — clients use a hardcoded URL to reach it).
- Uses `native` profile (`spring.profiles.active: native`) to read from the local classpath instead of a Git
  repository.
- Actuator is on the classpath (via `spring-boot-starter-actuator`) for health/monitoring endpoints, but no custom
  actuator configuration is currently applied here.