package tech.provokedynamic.gymcrm.cucumber;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tech.provokedynamic.gymcrm.entity.TrainingType;
import tech.provokedynamic.gymcrm.repository.TraineeRepository;
import tech.provokedynamic.gymcrm.repository.TrainingRepository;
import tech.provokedynamic.gymcrm.repository.TrainingTypeRepository;
import tech.provokedynamic.gymcrm.testsupport.TestJson;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

/**
 * Kafka publishing is stubbed out here — this suite verifies gym-crm's own
 * REST/JPA behavior in isolation. The cross-service effect of these events
 * on gym-crm-workload is covered by the gym-crm-integration-tests module.
 */
public class TrainingSteps {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TrainingTypeRepository trainingTypeRepository;

    @Autowired
    private TrainingRepository trainingRepository;

    @Autowired
    private TraineeRepository traineeRepository;

    private MvcResult lastResult;
    private Long lastTrainingId;

    @Given("the training type {string} exists")
    public void theTrainingTypeExists(String name) {
        if (trainingTypeRepository.findByTrainingTypeName(name).isEmpty()) {
            trainingTypeRepository.save(new TrainingType(name));
        }
    }

    @Given("a trainee {string} {string} is registered")
    public void aTraineeIsRegistered(String firstName, String lastName) throws Exception {
        registerTrainee(firstName, lastName);
    }

    @Given("a trainer {string} {string} is registered with specialization {string}")
    public void aTrainerIsRegistered(String firstName, String lastName, String specialization) throws Exception {
        registerTrainer(firstName, lastName, specialization);
    }

    private void registerTrainee(String firstName, String lastName) throws Exception {
        Map<String, Object> body = Map.of("firstName", firstName, "lastName", lastName);
        mockMvc.perform(post("/api/trainees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestJson.MAPPER.writeValueAsString(body)));
    }

    private void registerTrainer(String firstName, String lastName, String specialization) throws Exception {
        Map<String, Object> body = Map.of(
                "firstName", firstName, "lastName", lastName, "specialization", specialization);
        mockMvc.perform(post("/api/trainers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestJson.MAPPER.writeValueAsString(body)));
    }

    @When("trainer {string} schedules a {string} training with trainee {string} on {string} for {int} minutes")
    public void trainerSchedulesATraining(String trainerUsername, String name, String traineeUsername,
                                          String date, int duration) throws Exception {
        scheduleTraining(traineeUsername, trainerUsername, name, date, duration, true);
    }

    @Given("trainer {string} has scheduled a {string} training with trainee {string} on {string} for {int} minutes")
    public void trainerHasScheduledATraining(String trainerUsername, String name, String traineeUsername,
                                             String date, int duration) throws Exception {
        scheduleTraining(traineeUsername, trainerUsername, name, date, duration, true);
        lastTrainingId = trainingRepository.findAllByTraineeUsernameWithTrainer(traineeUsername).stream()
                .filter(t -> t.getTrainingName().equals(name))
                .reduce((first, second) -> second) // keep the most recently added match
                .orElseThrow()
                .getId();
    }

    @When("an unauthenticated caller schedules a {string} training with trainee {string} and trainer {string} on {string} for {int} minutes")
    public void anUnauthenticatedCallerSchedulesATraining(String name, String traineeUsername, String trainerUsername,
                                                          String date, int duration) throws Exception {
        scheduleTraining(traineeUsername, trainerUsername, name, date, duration, false);
    }

    private void scheduleTraining(String traineeUsername, String trainerUsername, String name,
                                  String date, int duration, boolean authenticated) throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("traineeUsername", traineeUsername);
        body.put("trainerUsername", trainerUsername);
        body.put("trainingName", name);
        body.put("trainingDate", date);
        body.put("trainingDuration", duration);

        var request = post("/api/trainings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestJson.MAPPER.writeValueAsString(body));

        if (authenticated) {
            request = request.with(TestJwtSupport.trainerToken("ROLE_TRAINER"));
        }

        lastResult = mockMvc.perform(request).andReturn();
    }

    @When("trainer {string} cancels that training")
    public void trainerCancelsThatTraining(String trainerUsername) throws Exception {
        cancelTraining(lastTrainingId);
    }

    @When("trainer {string} cancels training id {long}")
    public void trainerCancelsTrainingId(String trainerUsername, long id) throws Exception {
        cancelTraining(id);
    }

    private void cancelTraining(long id) throws Exception {
        lastResult = mockMvc.perform(delete("/api/trainings/{id}", id)
                        .with(TestJwtSupport.trainerToken("ROLE_TRAINER")))
                .andReturn();
    }

    @Then("the training response status is {int}")
    public void theTrainingResponseStatusIs(int status) {
        assertThat(lastResult.getResponse().getStatus()).isEqualTo(status);
    }

    @And("trainee {string} has {int} training(s) on record")
    public void traineeHasTrainingsOnRecord(String traineeUsername, int count) throws Exception {
        var result = mockMvc.perform(get("/api/trainees/{username}/trainings", traineeUsername)
                        .with(TestJwtSupport.trainerToken("ROLE_TRAINEE")))
                .andReturn();
        var json = TestJson.MAPPER.readTree(result.getResponse().getContentAsString());
        assertThat(json).hasSize(count);
    }
}
