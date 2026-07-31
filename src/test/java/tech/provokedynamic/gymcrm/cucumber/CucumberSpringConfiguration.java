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
 * One real Postgres (via Testcontainers) backs every scenario in this
 * module's Cucumber suite. Config-server / service-discovery lookups are
 * disabled so the context boots standalone. JWTs are now issued/validated
 * with gym-crm's own keystore (see JwtConfig) — no more separate
 * authorization-server issuer to disable or stub out. Kafka publishing is
 * stubbed out per-step-class with a Mockito bean (see {@link TrainingSteps})
 * because verifying the workload event itself is the job of the dedicated
 * integration-test module, not this component suite.
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
                "spring.jpa.hibernate.ddl-auto=create-drop",
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
            new PostgreSQLContainer(DockerImageName.parse("postgres:18.4"));

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
