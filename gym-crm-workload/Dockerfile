FROM maven:3-eclipse-temurin-26-noble@sha256:69a6c37f0a91fca2c9d0cdd684c9c0ea7b1bc00d4196749c24440b2e02ad163d AS build
WORKDIR /workspace
COPY . .
RUN --mount=type=cache,target=/root/.m2,sharing=locked mvn -B --no-transfer-progress -pl gym-crm-workload -am -Dmaven.test.skip=true package

FROM eclipse-temurin:25.0.5_8-jre-noble@sha256:a98128a57de944a72bd31fbc79b898edd5a5864d3652071d5bbd2160ab7d7dce
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
