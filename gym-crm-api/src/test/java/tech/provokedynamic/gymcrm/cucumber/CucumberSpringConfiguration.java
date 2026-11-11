package tech.provokedynamic.gymcrm.cucumber;

import io.cucumber.spring.CucumberContextConfiguration;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;
import tech.provokedynamic.gymcrm.GymCrmApplication;
import tech.provokedynamic.gymcrm.client.WorkloadEventPublisher;

/**
 * PostgreSQL-backed API scenarios with workload publishing mocked.
 * HTTP authorization uses pre-authenticated test JWTs; the separate H2 workflow
 * tests exercise real login and token decoding. Workload Kafka integration is
 * verified by the workload module's Failsafe suite.
 */
@CucumberContextConfiguration
@SpringBootTest(
        classes = GymCrmApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        properties = {
                "spring.config.import=",
                "spring.cloud.discovery.enabled=false",
                "spring.cloud.consul.enabled=false",
                "spring.cloud.consul.discovery.enabled=false",
                "spring.jpa.hibernate.ddl-auto=validate",
                "app.workload.events-enabled=false",
                "app.cors.allowed-origins=http://localhost:3000",
                "gym-crm.auth.issuer=http://localhost:8081",
                "gym-crm.auth.access-token-ttl=PT30M",
                "gym-crm.auth.key-store.location=classpath:keystore/dev-keystore.p12",
                "gym-crm.auth.key-store.password=dev-keystore-pass",
                "gym-crm.auth.key-store.alias=gym-crm-auth",
                "gym-crm.auth.key-store.key-password=dev-keystore-pass"
        }
)
@Import(CucumberSpringConfiguration.MockConfig.class)
@AutoConfigureMockMvc
@ActiveProfiles("cucumber")
@Testcontainers
public class CucumberSpringConfiguration {

    static final PostgreSQLContainer POSTGRES =
            new PostgreSQLContainer(DockerImageName.parse("postgres:18.6-alpine"));

    static {
        POSTGRES.start();
    }

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @TestConfiguration
    static class MockConfig {

        @Bean
        WorkloadEventPublisher workloadEventPublisher() {
            return Mockito.mock(WorkloadEventPublisher.class);
        }
    }
}
