# gym-crm-eureka-server

Service discovery server for the `gym-crm` microservices ecosystem, built with Spring Cloud Netflix Eureka.

## Purpose

Acts as the central registry where all `gym-crm` microservices register themselves on startup and discover each
other by service name — instead of relying on hardcoded hostnames/ports. This is what allows, for example,
`gym-crm` to call `gym-crm-workload` (via a load-balanced `RestClient` pointed at `http://gym-crm-workload`)
without knowing its exact network location in advance.

## Tech Stack

- Spring Boot 4.1.0
- Spring Cloud Netflix Eureka Server 2025.1.2

## How It Works

```
gym-crm          ─┐
                   ├──► registers with ──► gym-crm-eureka-server (port 8761)
gym-crm-workload  ─┘

gym-crm ──► "where is gym-crm-workload?" ──► gym-crm-eureka-server
gym-crm-eureka-server ──► returns current instance address(es)
gym-crm ──► calls gym-crm-workload directly using that address
```

This server does **not** register with itself and does **not** fetch a registry from elsewhere — it *is* the
registry.

Both `gym-crm` and `gym-crm-workload` fetch their Eureka client settings (server URL, heartbeat/lease intervals)
from `gym-crm-config-server` rather than hardcoding them locally, so this server's address only needs to be
correct in one place.

## Running Locally

```bash
./mvnw spring-boot:run
```

Server starts on **port 8761** by default.

Open the dashboard in a browser:

```
http://localhost:8761
```

You should see a list of currently registered service instances once `gym-crm` and `gym-crm-workload` are up and
pointed at this server.

## Configuration

`src/main/resources/application.yaml`:

```yaml
server:
  port: 8761
spring:
  application:
    name: gym-crm-eureka-server
  profiles:
    active: dev
eureka:
  client:
    register-with-eureka: false
    fetch-registry: false
```

Profile-specific blocks control self-preservation behavior:

```yaml
---
spring:
  config:
    activate:
      on-profile: dev
eureka:
  server:
    enable-self-preservation: false
---
spring:
  config:
    activate:
      on-profile: prod
eureka:
  server:
    enable-self-preservation: true
```

| Setting                       | Purpose                                                                                                                                                                                                                         |
|-------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `register-with-eureka: false` | Prevents this server from registering itself as a client                                                                                                                                                                        |
| `fetch-registry: false`       | Prevents this server from fetching a registry from another node                                                                                                                                                                 |
| `enable-self-preservation`    | Controls whether Eureka protects registrations during perceived network issues. Disabled in `dev` for faster, more predictable local testing; enabled in `prod` to avoid false-positive evictions under real network conditions |

## Client Setup

Any service that wants to register with this Eureka server needs:

**Dependency** (`pom.xml`):

```xml

<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>
```

**Configuration** (typically supplied via `gym-crm-config-server`, not hardcoded locally):

```yaml
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
    registry-fetch-interval-seconds: 10
  instance:
    lease-renewal-interval-in-seconds: 10
    lease-expiration-duration-in-seconds: 30
```

In production, `defaultZone` should point to the actual Eureka server host, typically supplied via an environment
variable rather than hardcoded:

```yaml
eureka:
  client:
    service-url:
      defaultZone: ${EUREKA_URL:http://gym-crm-eureka-server:8761/eureka/}
```

## Notes

- This server does not itself fetch configuration from `gym-crm-config-server` — it is kept intentionally
  standalone and simple, since it needs to be one of the first things available when the rest of the system
  starts up.
- Faster heartbeat/lease timings are used in `dev` (via each client's config) to make the dashboard reflect
  service start/stop activity quickly during local testing; these are relaxed to Eureka's safer defaults in
  production.
- Actuator is on the classpath for health/monitoring endpoints, alongside the Eureka server's own dashboard UI at
  the root path.