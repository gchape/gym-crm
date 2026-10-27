package tech.provokedynamic.gymcrmworkload.cucumber;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.awaitility.Awaitility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tech.provokedynamic.gymcrmcommon.event.WorkloadEvent;
import tech.provokedynamic.gymcrmcommon.event.WorkloadTopics;
import tech.provokedynamic.gymcrmworkload.repository.TrainerWorkloadRepository;
import tech.provokedynamic.gymcrmworkload.testsupport.TrainerJwt;

import java.time.Duration;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

public class WorkloadSteps {

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
                    CucumberSpringConfiguration.KAFKA.getBootstrapServers());
            config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
            config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JacksonJsonSerializer.class);
            producer = new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(config));
        }
        return producer;
    }

    @Given("no workload summary exists for trainer {string}")
    public void noWorkloadSummaryExistsForTrainer(String username) {
        assertThat(workloadRepository.existsByTrainerUsername(username)).isFalse();
    }

    @Given("an ADD workload event has already been processed for trainer {string} \\({string} {string}, active\\) on {string} for {int} minutes")
    public void anAddWorkloadEventHasAlreadyBeenProcessed(String username, String first, String last,
                                                          String date, int duration) {
        publish(username, first, last, true, date, duration, WorkloadEvent.ActionType.ADD);
        Awaitility.await().atMost(Duration.ofSeconds(10))
                .untilAsserted(() -> assertThat(workloadRepository.existsByTrainerUsername(username)).isTrue());
    }

    @When("an ADD workload event is published for trainer {string} \\({string} {string}, active\\) on {string} for {int} minutes")
    public void anAddWorkloadEventIsPublished(String username, String first, String last, String date, int duration) {
        publish(username, first, last, true, date, duration, WorkloadEvent.ActionType.ADD);
    }

    @When("a DELETE workload event is published for trainer {string} \\({string} {string}, active\\) on {string} for {int} minutes")
    public void aDeleteWorkloadEventIsPublished(String username, String first, String last, String date, int duration) {
        publish(username, first, last, true, date, duration, WorkloadEvent.ActionType.DELETE);
    }

    private void publish(String username, String first, String last, boolean active,
                         String date, int duration, WorkloadEvent.ActionType action) {
        var event = new WorkloadEvent(username, first, last, active, LocalDate.parse(date), duration, action);
        producer().send(WorkloadTopics.TRAINER_WORKLOAD_EVENTS, username, event);
    }

    @Then("within {int} seconds trainer {string} has a workload summary")
    public void withinSecondsTrainerHasAWorkloadSummary(int seconds, String username) {
        Awaitility.await().atMost(Duration.ofSeconds(seconds))
                .untilAsserted(() -> assertThat(workloadRepository.existsByTrainerUsername(username)).isTrue());
    }

    @Then("within {int} seconds the summary for trainer {string} year {int} month {int} shows {int} minutes")
    public void withinSecondsTheSummaryForTrainerYearMonthShowsMinutes(int seconds, String username, int year,
                                                                       int month, int minutes) {
        Awaitility.await().atMost(Duration.ofSeconds(seconds)).untilAsserted(() -> {
            var doc = workloadRepository.findByTrainerUsername(username).orElseThrow();
            int actual = doc.getYears().stream()
                    .filter(y -> y.getYear() == year)
                    .flatMap(y -> y.getMonths().stream())
                    .filter(m -> m.getMonth() == month)
                    .findFirst()
                    .orElseThrow()
                    .getTrainingsSummaryDuration();
            assertThat(actual).isEqualTo(minutes);
        });
    }

    @When("I request the workload summary for trainer {string}")
    public void iRequestTheWorkloadSummaryForTrainer(String username) throws Exception {
        lastResult = mockMvc.perform(get("/api/trainers/workload/{username}", username)
                        .with(TrainerJwt.trainerToken()))
                .andReturn();
    }

    @Then("the workload response status is {int}")
    public void theWorkloadResponseStatusIs(int status) {
        assertThat(lastResult.getResponse().getStatus()).isEqualTo(status);
    }

    @Then("trainer {string} still has no workload summary after {int} seconds")
    public void trainerStillHasNoWorkloadSummaryAfterSeconds(String username, int seconds) throws InterruptedException {
        Thread.sleep(Duration.ofSeconds(seconds).toMillis());
        assertThat(workloadRepository.existsByTrainerUsername(username)).isFalse();
    }
}
