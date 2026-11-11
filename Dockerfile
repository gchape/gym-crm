FROM maven:3-eclipse-temurin-26-noble@sha256:69a6c37f0a91fca2c9d0cdd684c9c0ea7b1bc00d4196749c24440b2e02ad163d AS build
WORKDIR /workspace
COPY . .
RUN --mount=type=cache,target=/root/.m2,sharing=locked mvn -B --no-transfer-progress -pl gym-crm-api -am -Dmaven.test.skip=true package

FROM eclipse-temurin:26.0.2.1_1-jre-noble@sha256:adfd44c114916dc79fa9a24087fbac34252096edb9892d1c2d9d82db3f8947b3
RUN apt-get update && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && useradd --system --uid 10001 gymcrm
WORKDIR /app
COPY --from=build /workspace/gym-crm-api/target/gym-crm-api-*.jar app.jar
USER gymcrm
ENV SPRING_PROFILES_ACTIVE=standalone
EXPOSE 8081
HEALTHCHECK --interval=15s --timeout=5s --start-period=45s --retries=5 \
  CMD curl --fail --silent http://localhost:8081/actuator/health || exit 1
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
