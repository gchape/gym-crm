package tech.provokedynamic.gymcrmworkload.cucumberintegration;

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

@CucumberContextConfiguration
@SpringBootTest(
        classes = GymCrmWorkloadApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        properties = {
                "spring.security.oauth2.resourceserver.jwt.public-key-location=classpath:keys/gym-crm-public.pem",
                "spring.kafka.consumer.group-id=gym-crm-workload-integration-test",
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
public class IntegrationCucumberSpringConfiguration {

    @Container
    @ServiceConnection
    static final MongoDBContainer MONGO = new MongoDBContainer("mongo:8.4");

    @Container
    @ServiceConnection
    static final KafkaContainer KAFKA = new KafkaContainer(DockerImageName.parse("apache/kafka:4.3.1"));
}
