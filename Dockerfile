# Build context MUST be the repo root (contains gym-crm-common/ and pom.xml)
#   docker build -f Dockerfile -t gym-crm:local .

FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /workspace

# gym-crm-common is a local dependency - install it first.
# -Dmaven.test.skip=true (not just -DskipTests) so test sources don't even
# need to compile inside the image - keeps the build image lean and fast.
COPY gym-crm-common gym-crm-common
RUN mvn -f gym-crm-common/pom.xml -q -Dmaven.test.skip=true install

COPY pom.xml .
COPY src src
RUN mvn -q -Dmaven.test.skip=true package && \
    cp target/*.jar /workspace/app.jar

FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=build /workspace/app.jar app.jar

# Default to the infra-free "standalone" profile. Override at `docker run`
# time (-e SPRING_PROFILES_ACTIVE=docker) for the fully-wired setup.
ENV SPRING_PROFILES_ACTIVE=standalone
EXPOSE 8081

HEALTHCHECK --interval=15s --timeout=5s --start-period=45s --retries=5 \
  CMD wget -qO- http://localhost:8081/actuator/health | grep -q '"status":"UP"' || exit 1

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
