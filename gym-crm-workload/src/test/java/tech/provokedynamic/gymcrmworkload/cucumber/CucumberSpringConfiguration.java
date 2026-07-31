package tech.provokedynamic.gymcrmworkload.cucumber;

import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.mongodb.MongoDBContainer;
import org.testcontainers.utility.DockerImageName;
import tech.provokedynamic.gymcrmworkload.GymCrmWorkloadApplication;

/**
 * Uses @ServiceConnection instead of manual @DynamicPropertySource: Spring
 * Boot's Testcontainers integration detects the container type (Mongo,
 * Kafka) and wires the right spring.data.mongodb.* / spring.kafka.*
 * properties itself, so there is no property-key typo to get wrong and no
 * risk of the container silently not being picked up.
 * <p>
 * JWTs are validated against a static public key (see
 * spring.security.oauth2.resourceserver.jwt.public-key-location below)
 * instead of an issuer, since there's no longer a separate authorization
 * server to discover. Boot auto-configures a real JwtDecoder bean from
 * that property, so no manual/dummy decoder bean is needed here anymore —
 * MockMvc requests still authenticate via
 * SecurityMockMvcRequestPostProcessors.jwt(), which injects a
 * pre-authenticated token directly and never actually invokes the decoder.
 */
@CucumberContextConfiguration
@SpringBootTest(
        classes = GymCrmWorkloadApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        properties = {
                "spring.security.oauth2.resourceserver.jwt.public-key-location=classpath:keys/gym-crm-public.pem",
                "spring.kafka.consumer.group-id=gym-crm-workload-component-test",
                "spring.kafka.consumer.key-deserializer=org.apache.kafka.common.serialization.StringDeserializer",
                "spring.kafka.consumer.value-deserializer=org.springframework.kafka.support.serializer.JsonDeserializer",
                "spring.kafka.consumer.properties.spring.json.trusted.packages=tech.provokedynamic.gymcrmcommon.event",
                "spring.kafka.consumer.properties.spring.json.value.default.type=tech.provokedynamic.gymcrmcommon.event.WorkloadEvent",
                "spring.kafka.consumer.properties.spring.json.use.type.headers=false",
                "spring.kafka.consumer.auto-offset-reset=earliest"

        }
)
@AutoConfigureMockMvc
@Testcontainers
public class CucumberSpringConfiguration {

    @Container
    @ServiceConnection
    static final MongoDBContainer MONGO = new MongoDBContainer("mongo:8.4");

    @Container
    @ServiceConnection
    static final KafkaContainer KAFKA = new KafkaContainer(DockerImageName.parse("apache/kafka:4.3.1"));
}
