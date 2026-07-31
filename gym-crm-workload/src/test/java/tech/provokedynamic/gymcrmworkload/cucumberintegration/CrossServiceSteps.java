package tech.provokedynamic.gymcrmworkload.cucumberintegration;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.awaitility.Awaitility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tech.provokedynamic.gymcrmcommon.event.WorkloadEvent;
import tech.provokedynamic.gymcrmcommon.event.WorkloadTopics;
import tech.provokedynamic.gymcrmworkload.repository.TrainerWorkloadRepository;
import tech.provokedynamic.gymcrmworkload.testsupport.TestJson;
import tech.provokedynamic.gymcrmworkload.testsupport.TrainerJwt;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

/**
 * Publishes messages exactly the way gym-crm's
 * {@code tech.provokedynamic.gymcrm.client.WorkloadEventPublisher} does
 * (topic, partition key = trainerUsername, transactionId header, JSON
 * value), so this suite fails if either side of the contract drifts —
 * without needing gym-crm's fat jar on this module's test classpath.
 */
public class CrossServiceSteps {

    private static final String TRANSACTION_ID_HEADER = "transactionId";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private TrainerWorkloadRepository workloadRepository;

    private KafkaTemplate<String, WorkloadEvent> producer;
    private MvcResult lastResult;

    private KafkaTemplate<String, WorkloadEvent> producer() {
        if (producer == null) {
            Map<String, Object> config = new HashMap<>();
            config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                    IntegrationCucumberSpringConfiguration.KAFKA.getBootstrapServers());
            config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
            config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JacksonJsonSerializer.class);
            producer = new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(config));
        }
        return producer;
    }

    @Given("no workload summary exists in gym-crm-workload for trainer {string}")
    public void noWorkloadSummaryExistsInGymCrmWorkloadForTrainer(String username) {
        assertThat(workloadRepository.existsByTrainerUsername(username)).isFalse();
    }

    @When("gym-crm publishes a workload ADD event with transaction id {string} for trainer {string} \\({string} {string}, active\\) on {string} for {int} minutes")
    public void gymCrmPublishesAnAddEvent(String txId, String username, String first, String last,
                                          String date, int duration) {
        publishLikeGymCrm(txId, username, first, last, date, duration, WorkloadEvent.ActionType.ADD);
    }

    @When("gym-crm publishes a workload DELETE event with transaction id {string} for trainer {string} \\({string} {string}, active\\) on {string} for {int} minutes")
    public void gymCrmPublishesADeleteEvent(String txId, String username, String first, String last,
                                            String date, int duration) {
        publishLikeGymCrm(txId, username, first, last, date, duration, WorkloadEvent.ActionType.DELETE);
    }

    private void publishLikeGymCrm(String txId, String username, String first, String last,
                                   String date, int duration, WorkloadEvent.ActionType action) {
        var event = new WorkloadEvent(username, first, last, true, LocalDate.parse(date), duration, action);

        var message = MessageBuilder.withPayload(event)
                .setHeader(KafkaHeaders.TOPIC, WorkloadTopics.TRAINER_WORKLOAD_EVENTS)
                .setHeader(KafkaHeaders.KEY, username)
                .setHeader(TRANSACTION_ID_HEADER, txId.getBytes(StandardCharsets.UTF_8))
                .build();

        producer().send(message);
    }

    @Then("within {int} seconds gym-crm-workload's API reports {int} minutes for trainer {string} in {word}")
    public void withinSecondsGymCrmWorkloadsApiReportsMinutesForTrainerIn(int seconds, int minutes, String username,
                                                                          String yearMonth) {
        String[] parts = yearMonth.split("-");
        int year = Integer.parseInt(parts[0]);
        int month = Integer.parseInt(parts[1]);

        Awaitility.await().atMost(Duration.ofSeconds(seconds)).untilAsserted(() -> {
            var result = mockMvc.perform(get("/api/trainers/workload/{username}", username)
                            .with(TrainerJwt.trainerToken()))
                    .andReturn();
            assertThat(result.getResponse().getStatus()).isEqualTo(200);

            var json = TestJson.MAPPER.readTree(result.getResponse().getContentAsString());
            var months = json.get("years").elements();
            int actual = -1;
            while (months.hasNext()) {
                var yearNode = months.next();
                if (yearNode.get("year").asInt() == year) {
                    var monthIter = yearNode.get("months").elements();
                    while (monthIter.hasNext()) {
                        var monthNode = monthIter.next();
                        if (monthNode.get("month").asInt() == month) {
                            actual = monthNode.get("trainingSummaryDuration").asInt();
                        }
                    }
                }
            }
            assertThat(actual).isEqualTo(minutes);
        });
    }

    @Then("gym-crm-workload still has no workload summary for trainer {string} after {int} seconds")
    public void gymCrmWorkloadStillHasNoWorkloadSummaryForTrainerAfterSeconds(String username, int seconds)
            throws InterruptedException {
        Thread.sleep(Duration.ofSeconds(seconds).toMillis());
        assertThat(workloadRepository.existsByTrainerUsername(username)).isFalse();
    }
}
