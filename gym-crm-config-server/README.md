# Configuration server

Optional Spring Cloud Config Server using the native classpath backend. Spring Boot 4.1.2, Spring Cloud 2025.1.4, Java 25; port **8071**.

Serves configuration from `src/main/resources/config/gym-crm.yaml` and `gym-crm-workload.yaml`. Each includes shared settings and `dev`/`prod` overrides. The API issues JWTs itself; there is no separate authorization-server module.

The standalone API and full Docker stack disable Config Server and Consul. For IDE development with the `dev` profile:

```bash
# From the repository root:
./mvnw -DskipTests package
java -jar gym-crm-config-server/target/gym-crm-config-server-1.0.0-SNAPSHOT.jar
```

Inspect resolved configuration:

```bash
curl http://localhost:8071/gym-crm/dev
curl http://localhost:8071/gym-crm-workload/dev
```

Clients default to an optional import from `http://localhost:8071`. If using centralized `dev`/`prod` configuration, start this server before the clients. Production values come from environment placeholders in the configuration documents; do not commit deployment secrets.

See the [repository README](../README.md) for the full build, tests, and local infrastructure commands.
