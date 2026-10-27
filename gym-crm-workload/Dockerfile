FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /workspace

COPY gym-crm-common gym-crm-common
RUN mvn -f gym-crm-common/pom.xml -q -Dmaven.test.skip=true install

COPY gym-crm-workload gym-crm-workload
RUN mvn -f gym-crm-workload/pom.xml -q -Dmaven.test.skip=true package && \
    cp gym-crm-workload/target/*.jar /workspace/app.jar

FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=build /workspace/app.jar app.jar

ENV SPRING_PROFILES_ACTIVE=standalone
EXPOSE 8082

HEALTHCHECK --interval=15s --timeout=5s --start-period=45s --retries=5 \
  CMD wget -qO- http://localhost:8082/actuator/health | grep -q '"status":"UP"' || exit 1

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
