package tech.provokedynamic.gymcrm.cucumber;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tech.provokedynamic.gymcrm.repository.TraineeRepository;
import tech.provokedynamic.gymcrm.testsupport.TestJson;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

public class TraineeSteps {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TraineeRepository traineeRepository;

    private MvcResult lastResult;

    @Given("no trainee is registered with the name {string} {string}")
    public void noTraineeRegisteredWithName(String firstName, String lastName) {
        assertThat(traineeRepository.existsByUsernameIncludingDeleted(firstName + "." + lastName)).isFalse();
    }

    @Given("a trainee is already registered with first name {string} and last name {string}")
    public void aTraineeIsAlreadyRegistered(String firstName, String lastName) throws Exception {
        registerTrainee(firstName, lastName, null);
    }

    @When("I register a trainee with first name {string}, last name {string}, date of birth {string} and no address")
    public void registerTraineeWithDob(String firstName, String lastName, String dob) throws Exception {
        registerTrainee(firstName, lastName, dob);
    }

    @When("I register a trainee with first name {string}, last name {string}, no date of birth and no address")
    public void registerTraineeWithoutDob(String firstName, String lastName) throws Exception {
        registerTrainee(firstName, lastName, null);
    }

    private void registerTrainee(String firstName, String lastName, String dob) throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("firstName", firstName);
        body.put("lastName", lastName);
        body.put("dateOfBirth", dob);
        body.put("address", null);

        lastResult = mockMvc.perform(post("/api/trainees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestJson.MAPPER.writeValueAsString(body)))
                .andReturn();
    }

    @Then("the registration response status is {int}")
    public void theRegistrationResponseStatusIs(int status) {
        assertThat(lastResult.getResponse().getStatus()).isEqualTo(status);
    }

    @Then("the response contains a generated username starting with {string}")
    public void theResponseContainsAGeneratedUsernameStartingWith(String prefix) throws Exception {
        var json = TestJson.MAPPER.readTree(lastResult.getResponse().getContentAsString());
        assertThat(json.get("username").asText()).startsWith(prefix);
    }

    @And("the response contains a generated password of 10 characters")
    public void theResponseContainsAGeneratedPasswordOfCharacters() throws Exception {
        var json = TestJson.MAPPER.readTree(lastResult.getResponse().getContentAsString());
        assertThat(json.get("password").asText()).hasSize(10);
    }
}
