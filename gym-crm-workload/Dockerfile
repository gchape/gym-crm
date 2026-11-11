FROM maven:3.10.0-eclipse-temurin-25-noble@sha256:d4bb5796a17ab2422f609b764ba16ddcbd2e6ba5b639168eb9e99e6de8f1f431 AS build
WORKDIR /workspace
COPY . .
RUN --mount=type=cache,target=/root/.m2,sharing=locked mvn -B --no-transfer-progress -pl gym-crm-workload -am -Dmaven.test.skip=true package

FROM eclipse-temurin:26.0.2.1_1-jre-noble@sha256:adfd44c114916dc79fa9a24087fbac34252096edb9892d1c2d9d82db3f8947b3
RUN apt-get update && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && useradd --system --uid 10001 gymcrm
WORKDIR /app
COPY --from=build /workspace/gym-crm-workload/target/gym-crm-workload-*.jar app.jar
USER gymcrm
ENV SPRING_PROFILES_ACTIVE=docker
EXPOSE 8082
HEALTHCHECK --interval=15s --timeout=5s --start-period=45s --retries=5 \
  CMD curl --fail --silent http://localhost:8082/actuator/health || exit 1
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
