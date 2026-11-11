# Dependency and container upgrades

Verified on **2026-11-11** using official project release pages, Maven Central metadata, Docker Hub tag metadata, and image pulls. Stable releases are used; Spring Boot 4.2 release candidates and Maven 4 release candidates are excluded. Java remains on the requested JDK 25 line.

## Selected versions

| Component | Selected version | Verification source |
| --- | --- | --- |
| Spring Boot | 4.1.2 | [System requirements and stable releases](https://docs.spring.io/spring-boot/system-requirements.html) |
| Spring Cloud | 2025.1.4 | [Release train compatibility](https://spring.io/projects/spring-cloud/) |
| springdoc-openapi | 3.2.0 | [Maintainer documentation](https://springdoc.org/) |
| Cucumber JVM | 8.0.4 | [Installation documentation](https://cucumber.io/docs/installation/java/) and [BOM metadata](https://repo.maven.apache.org/maven2/io/cucumber/cucumber-bom/maven-metadata.xml) |
| Maven Wrapper / build image | 3.10.0 | [Release notes](https://maven.apache.org/docs/3.10.0/release-notes.html) |
| Temurin runtime | 25.0.5+8, Ubuntu Noble JRE | [Official image tags](https://hub.docker.com/_/eclipse-temurin/tags?name=25.0.5) |
| PostgreSQL | 18.6, Alpine variant | [Official image tags](https://hub.docker.com/_/postgres) |
| MongoDB | 9.0.4 | [Official image registry](https://hub.docker.com/_/mongo/tags?name=9.0.4) |
| Kafka broker | 4.4.0 | [Apache release list](https://kafka.apache.org/community/downloads/) |
| Consul | 2.1.0 | [HashiCorp release list](https://releases.hashicorp.com/consul/) |
| GitHub Actions | checkout 7.0.1, setup-java 6.2.0, upload-artifact 7.0.2 | [checkout](https://github.com/actions/checkout/releases/tag/v7.0.1), [setup-java](https://github.com/actions/setup-java/releases/tag/v6.2.0), [upload-artifact](https://github.com/actions/upload-artifact/releases/tag/v7.0.2) |

Spring Boot's dependency management remains authoritative for Spring Framework, Security, Data, Hibernate, Jackson, Flyway, JDBC/MongoDB/Kafka clients, JUnit, Mockito, and Testcontainers. Those versions form a tested platform; individual newer upstream versions are not automatically forced over the BOM. Broker/server releases and client-library releases have independent version numbers. Cucumber modules share their own imported BOM.

The Boot 3 AOP starter has been replaced with the Boot 4 AspectJ starter. The Testcontainers 1.x Kafka artifact override has been removed in favor of Boot-managed Testcontainers 2.x. Kafka JSON serialization now uses Jackson 3 serializers. MongoDB connection URIs use `spring.mongodb.uri` rather than the former `spring.data.mongodb.uri` namespace.

## Reproducible container inputs

Application Dockerfiles pin the build and runtime manifest digests. Compose pins infrastructure digests as well. Exact test version tags match the corresponding infrastructure releases.

| Image | Manifest digest |
| --- | --- |
| `maven:3.10.0-eclipse-temurin-25-noble` | `sha256:d4bb5796a17ab2422f609b764ba16ddcbd2e6ba5b639168eb9e99e6de8f1f431` |
| `eclipse-temurin:25.0.5_8-jre-noble` | `sha256:a98128a57de944a72bd31fbc79b898edd5a5864d3652071d5bbd2160ab7d7dce` |
| `postgres:18.6-alpine` | `sha256:123b17998ccaf47e53b502e8c6a1e79afd8f579c6f3eec83c563da026f957395` |
| `mongo:9.0.4` | `sha256:452bdaf2127b8ff851e31410a267809b9829eded219979f2210e7c7efb85a058` |
| `apache/kafka:4.4.0` | `sha256:833b867a9585a141da455dc2b57431c65cbccb27f11c91f694444cca9f777e0f` |
| `hashicorp/consul:2.1.0` | `sha256:259fd02f2544fe1aa03fa49efd3b105c8dd67106d710a72f5e898110266356f7` |

The Maven download cache is shared through a locked BuildKit cache mount. Runtime images run as the `gymcrm` user, and health checks use an explicitly installed `curl`.

## Existing database volumes

The new image defaults are suitable for fresh local databases. Upgrading a tag does not migrate database files. This work does not start the new server versions against existing user volumes.

For an existing installation:

1. Record server versions, MongoDB feature compatibility version, and the names of existing volumes. Stop application writes and take verified logical backups.
2. Rehearse restore and application verification using fresh volumes or a separate environment. Keep the old volumes and images available for rollback.
3. For MongoDB, follow the topology-specific supported upgrade path and FCV requirements. The official [MongoDB 9.0 standalone upgrade procedure](https://www.mongodb.com/docs/manual/release-notes/9.0-upgrade-standalone/) starts from **8.3**; older 7.x/8.0 deployments need the documented intermediate upgrade steps. Do not attach the former 7.0 volume directly to MongoDB 9.0. Check the [Java driver compatibility matrix](https://www.mongodb.com/docs/drivers/compatibility/?driver-language=java&java-driver-framework=java-sync) for production feature requirements.
4. PostgreSQL remains within major version 18. If your actual database predates 18, use a supported logical restore or `pg_upgrade` procedure. PostgreSQL 18 Compose data is mounted at `/var/lib/postgresql`, matching the official image layout.
5. Run Flyway and schema validation against the restored relational database, verify stored workload documents, and compare workload totals against source sessions before routing writes to the new services.

Historical Flyway V1–V3 files remain byte-for-byte unchanged. New catalog/index changes are additive in V4. Workload bucket class names and Java accessors have changed, but the existing nested MongoDB `trainingsSummaryDuration` field remains mapped and covered by a compatibility test.

## Ongoing updates

Dependabot checks Maven, Dockerfiles, and GitHub Actions weekly. Compose image pins and matching Testcontainers tags should be reviewed together. There is no automatic merge policy.

For each update, check the Spring Boot/Cloud compatibility matrix, update version pins and digests, run `./mvnw clean verify` on JDK 25 with Docker, build both application images, and smoke-test the affected runtime. A successful build is evidence for this application's exercised paths, not a substitute for database upgrade rehearsal or a production feature compatibility review.

## Validation of this upgrade

The final `./mvnw -B clean verify` run on Temurin **25.0.5+8** and Maven **3.10.0** passed **187 tests**: 164 in the CRM API and 23 in the workload module, including its Failsafe integration suite. There were no failures or errors.

Both application Dockerfiles built successfully. A fresh isolated full stack passed real registration and JWT login, session creation, PostgreSQL persistence, Kafka delivery, MongoDB workload projection to 45 minutes, cancellation, and projection back to zero. Both application containers reported healthy. The API also served the renamed request/response schemas through OpenAPI. The Consul 2.1.0 development agent started and reported a live member.

These checks used temporary review containers and volumes, not existing application data. They validate a fresh installation and the covered persistence mappings; they do not claim an in-place MongoDB major-version upgrade was performed.
