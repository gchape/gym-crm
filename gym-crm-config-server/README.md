# gym-crm-config-server

Centralized configuration server for the `gym-crm` microservices ecosystem, built with Spring Cloud Config Server.

## Purpose

Provides distributed configuration management for all `gym-crm` microservices. Instead of each service holding its own
environment-specific settings (database credentials, JWT secrets, feature flags, etc.) locally, they fetch this
configuration remotely from this server at startup.

## Tech Stack

- Spring Boot 4.1.0
- Spring Cloud Config Server 2025.1.2
- Native (file-based / classpath) configuration repository — no Git backend required

## How It Works

This server reads configuration files from `src/main/resources/config/` and serves them over HTTP to any client that
requests them, based on:

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

| Variable      | Used for            |
|---------------|---------------------|
| `DB_URL`      | Datasource JDBC URL |
| `DB_USERNAME` | Database username   |
| `DB_PASSWORD` | Database password   |
| `JWT_SECRET`  | JWT signing secret  |

⚠️ These must be set on the **client's** runtime environment (e.g. `gym-crm`'s host/container), not on this config
server — the config server only serves the raw `${VAR}` placeholder text; resolution happens client-side.

## Notes

- This server does not register with Eureka (kept simple — clients use a hardcoded URL to reach it).
- Uses `native` profile (`spring.profiles.active: native`) to read from the local classpath instead of a Git repository.