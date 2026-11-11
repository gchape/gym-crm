package tech.provokedynamic.gymcrm.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tech.provokedynamic.gymcrm.testsupport.TestJson;
import java.time.LocalDate;
import java.util.Map;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {"app.auth.login-rate-limit=100", "spring.jpa.open-in-view=false"})
@AutoConfigureMockMvc
@ActiveProfiles("standalone")
@Transactional
class TrainingApiTest {
    @Autowired MockMvc mvc;
    String trainerToken;
    String traineeToken;
    String strangerToken;

    @BeforeEach
    void registerUsers() throws Exception {
        trainerToken = registerAndLogin("/api/trainers", Map.of(
                "firstName", "Dana", "lastName", "Ford", "specialization", "CARDIO"));
        traineeToken = registerAndLogin("/api/trainees", Map.of("firstName", "Chris", "lastName", "Miller"));
        strangerToken = registerAndLogin("/api/trainers", Map.of(
                "firstName", "Other", "lastName", "Trainer", "specialization", "CARDIO"));
    }

    String registerAndLogin(String path, Map<String, String> body) throws Exception {
        var result = mvc.perform(post(path).contentType(MediaType.APPLICATION_JSON)
                .content(TestJson.MAPPER.writeValueAsString(body))).andExpect(status().isCreated()).andReturn();
        var credentials = TestJson.MAPPER.readTree(result.getResponse().getContentAsString());
        var login = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content(TestJson.MAPPER.writeValueAsString(Map.of("username", credentials.get("username").asText(),
                        "password", credentials.get("password").asText()))))
                .andExpect(status().isOk()).andReturn();
        return TestJson.MAPPER.readTree(login.getResponse().getContentAsString()).get("accessToken").asText();
    }

    String trainingBody() throws Exception {
        return TestJson.MAPPER.writeValueAsString(Map.of("traineeUsername", "Chris.Miller",
                "trainerUsername", "Dana.Ford", "trainingName", "Cardio Blast",
                "trainingDate", LocalDate.now().plusDays(2).toString(), "trainingDuration", 45));
    }

    long schedule() throws Exception {
        var result = mvc.perform(post("/api/trainings").header("Authorization", "Bearer " + trainerToken)
                .contentType(MediaType.APPLICATION_JSON).content(trainingBody()))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.trainingType").value("CARDIO")).andReturn();
        var id = TestJson.MAPPER.readTree(result.getResponse().getContentAsString()).get("id").asLong();
        assertThat(result.getResponse().getHeader("Location")).isEqualTo("/api/trainings/" + id);
        return id;
    }

    @Test
    void participantsCanFindTrainingAndOnlyAssignedTrainerCanCancelIt() throws Exception {
        var id = schedule();
        mvc.perform(get("/api/trainings/{id}", id).header("Authorization", "Bearer " + traineeToken))
                .andExpect(status().isOk()).andExpect(jsonPath("$.trainerUsername").value("Dana.Ford"));
        mvc.perform(get("/api/trainings").param("trainingType", "CARDIO").param("size", "1")
                .header("Authorization", "Bearer " + traineeToken))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].id").value(id));
        mvc.perform(get("/api/trainings").header("Authorization", "Bearer " + strangerToken))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(0));
        mvc.perform(get("/api/trainings/{id}", id).header("Authorization", "Bearer " + strangerToken))
                .andExpect(status().isForbidden());
        mvc.perform(delete("/api/trainings/{id}", id).header("Authorization", "Bearer " + strangerToken))
                .andExpect(status().isForbidden());
        mvc.perform(delete("/api/trainings/{id}", id).header("Authorization", "Bearer " + traineeToken))
                .andExpect(status().isForbidden());
        mvc.perform(delete("/api/trainings/{id}", id).header("Authorization", "Bearer " + trainerToken))
                .andExpect(status().isNoContent());
        mvc.perform(get("/api/trainings/{id}", id).header("Authorization", "Bearer " + trainerToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void schedulingCannotImpersonateAnotherTrainer() throws Exception {
        mvc.perform(post("/api/trainings").header("Authorization", "Bearer " + strangerToken)
                .contentType(MediaType.APPLICATION_JSON).content(trainingBody()))
                .andExpect(status().isForbidden());
    }

    @Test
    void invalidSearchParametersReturnClientErrors() throws Exception {
        for (var query : new String[]{"size=0", "size=101", "page=-1", "size=wrong",
                "from=2027-02-01&to=2027-01-01", "from=wrong"}) {
            mvc.perform(get("/api/trainings?" + query).header("Authorization", "Bearer " + trainerToken))
                    .andExpect(status().isBadRequest());
        }
    }

    @Test
    void correlationHeaderIsPreservedAndAuthenticationIsRequired() throws Exception {
        mvc.perform(get("/api/trainings").header("X-Transaction-Id", "demo-request-1"))
                .andExpect(status().isUnauthorized()).andExpect(header().string("X-Transaction-Id", "demo-request-1"));
    }
}
