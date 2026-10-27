package tech.provokedynamic.gymcrm.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tech.provokedynamic.gymcrm.dto.Profile;
import tech.provokedynamic.gymcrm.dto.Request;
import tech.provokedynamic.gymcrm.dto.Response;
import tech.provokedynamic.gymcrm.dto.Summary;
import tech.provokedynamic.gymcrm.exception.*;
import tech.provokedynamic.gymcrm.service.TraineeService;
import tech.provokedynamic.gymcrm.testsupport.TestJson;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TraineeControllerTest {

    @Mock
    TraineeService traineeService;

    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new TraineeController(traineeService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private Profile.Trainee sampleTraineeProfile() {
        return new Profile.Trainee(
                "John", "Doe", "john.doe",
                LocalDate.of(1990, 1, 15), null, true,
                List.of(new Profile.Trainer("Jane", "Smith", "jane.smith", "Yoga", true, List.of()))
        );
    }

    private Summary.Training sampleTraining() {
        return new Summary.Training(
                "Morning Yoga", LocalDate.now(), "Yoga", 60, "jane.smith", "john.doe"
        );
    }

    // ─── POST /api/trainees ──────────────────────────────────────────────────

    @Test
    void register_validBody_returns201WithCredentials() throws Exception {
        var body = new Request.CreateTrainee("John", "Doe", null, null);
        var credentials = new Response.CreatedUser("john.doe", "pass123456");
        when(traineeService.create(any())).thenReturn(credentials);

        mockMvc.perform(post("/api/trainees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestJson.MAPPER.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("john.doe"))
                .andExpect(jsonPath("$.password").value("pass123456"));
    }

    @Test
    void register_blankFirstName_returns400() throws Exception {
        var body = new Request.CreateTrainee("", "Doe", null, null);

        mockMvc.perform(post("/api/trainees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestJson.MAPPER.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_blankLastName_returns400() throws Exception {
        var body = new Request.CreateTrainee("John", "  ", null, null);

        mockMvc.perform(post("/api/trainees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestJson.MAPPER.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_futureDateOfBirth_returns400() throws Exception {
        var body = new Request.CreateTrainee("John", "Doe", LocalDate.now().plusDays(1), null);

        mockMvc.perform(post("/api/trainees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestJson.MAPPER.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    // ─── GET /api/trainees/{username} ────────────────────────────────────────

    @Test
    void getProfile_existingUser_returns200() throws Exception {
        when(traineeService.getProfile("john.doe")).thenReturn(sampleTraineeProfile());

        mockMvc.perform(get("/api/trainees/john.doe"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.isActive").value(true))
                .andExpect(jsonPath("$.trainers[0].username").value("jane.smith"));
    }

    @Test
    void getProfile_unknownUser_returns404() throws Exception {
        when(traineeService.getProfile("ghost")).thenThrow(new UserDoesNotExistException("Not found"));

        mockMvc.perform(get("/api/trainees/ghost"))
                .andExpect(status().isNotFound());
    }

    // ─── PUT /api/trainees/{username} ────────────────────────────────────────

    @Test
    void update_validBody_returns200WithProfile() throws Exception {
        var body = new Request.UpdateTrainee(
                "john.doe", "John", "Doe",
                LocalDate.of(1990, 1, 15), null, true);
        when(traineeService.update(any())).thenReturn(sampleTraineeProfile());

        mockMvc.perform(put("/api/trainees/john.doe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestJson.MAPPER.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("John"));
    }

    @Test
    void update_unknownUser_returns404() throws Exception {
        var body = new Request.UpdateTrainee(
                "ghost", "Ghost", "User",
                LocalDate.of(1990, 1, 15), null, true);
        when(traineeService.update(any())).thenThrow(new UserDoesNotExistException("Not found"));

        mockMvc.perform(put("/api/trainees/ghost")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestJson.MAPPER.writeValueAsString(body)))
                .andExpect(status().isNotFound());
    }

    @Test
    void update_missingIsActive_returns400() throws Exception {
        String rawBody = """
                {"username":"john.doe","firstName":"John","lastName":"Doe"}
                """;

        mockMvc.perform(put("/api/trainees/john.doe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(rawBody))
                .andExpect(status().isBadRequest());
    }

    // ─── DELETE /api/trainees/{username} ─────────────────────────────────────

    @Test
    void delete_validUsername_returns200() throws Exception {
        doNothing().when(traineeService).delete(any());

        mockMvc.perform(delete("/api/trainees/john.doe"))
                .andExpect(status().isOk());
    }

    @Test
    void delete_authenticationFailure_returns401() throws Exception {
        doThrow(new AuthenticationException("Bad credentials"))
                .when(traineeService).delete(any());

        mockMvc.perform(delete("/api/trainees/john.doe"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void delete_unknownUser_returns404() throws Exception {
        doThrow(new UserDoesNotExistException("Not found"))
                .when(traineeService).delete(any());

        mockMvc.perform(delete("/api/trainees/ghost"))
                .andExpect(status().isNotFound());
    }

    // ─── PUT /api/trainees/{username}/password ────────────────────────────────

    @Test
    void changePassword_validBody_returns200() throws Exception {
        var body = new Request.ChangePassword("john.doe", "oldpassword", "newpass123");
        doNothing().when(traineeService).changePassword(any());

        mockMvc.perform(put("/api/trainees/john.doe/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestJson.MAPPER.writeValueAsString(body)))
                .andExpect(status().isOk());
    }

    @Test
    void changePassword_shortNewPassword_returns400() throws Exception {
        var body = new Request.ChangePassword("john.doe", "oldpassword", "short");

        mockMvc.perform(put("/api/trainees/john.doe/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestJson.MAPPER.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    // ─── PATCH /api/trainees/{username}/active ────────────────────────────────

    @Test
    void setActive_activate_returns200() throws Exception {
        var body = new Request.ToggleActive("john.doe", true);
        doNothing().when(traineeService).activate(any());

        mockMvc.perform(patch("/api/trainees/john.doe/active")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestJson.MAPPER.writeValueAsString(body)))
                .andExpect(status().isOk());

        verify(traineeService).activate(any());
        verify(traineeService, never()).deactivate(any());
    }

    @Test
    void setActive_deactivate_returns200() throws Exception {
        var body = new Request.ToggleActive("john.doe", false);
        doNothing().when(traineeService).deactivate(any());

        mockMvc.perform(patch("/api/trainees/john.doe/active")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestJson.MAPPER.writeValueAsString(body)))
                .andExpect(status().isOk());

        verify(traineeService).deactivate(any());
        verify(traineeService, never()).activate(any());
    }

    @Test
    void setActive_alreadyActivated_returns409() throws Exception {
        var body = new Request.ToggleActive("john.doe", true);
        doThrow(new AlreadyActivatedException("Already active"))
                .when(traineeService).activate(any());

        mockMvc.perform(patch("/api/trainees/john.doe/active")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestJson.MAPPER.writeValueAsString(body)))
                .andExpect(status().isConflict());
    }

    @Test
    void setActive_alreadyDeactivated_returns409() throws Exception {
        var body = new Request.ToggleActive("john.doe", false);
        doThrow(new AlreadyDeactivatedException("Already inactive"))
                .when(traineeService).deactivate(any());

        mockMvc.perform(patch("/api/trainees/john.doe/active")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestJson.MAPPER.writeValueAsString(body)))
                .andExpect(status().isConflict());
    }

    // ─── GET /api/trainees/{username}/trainings ───────────────────────────────

    @Test
    void getTrainings_noFilters_returnsAll() throws Exception {
        when(traineeService.getTrainings(eq("john.doe"), isNull(), isNull(), isNull(), isNull()))
                .thenReturn(List.of(sampleTraining()));

        mockMvc.perform(get("/api/trainees/john.doe/trainings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].trainingName").value("Morning Yoga"))
                .andExpect(jsonPath("$[0].trainerName").value("jane.smith"))
                .andExpect(jsonPath("$[0].traineeName").doesNotExist());
    }

    @Test
    void getTrainings_withDateFilter_passesParams() throws Exception {
        when(traineeService.getTrainings(
                eq("john.doe"),
                eq(LocalDate.of(2025, 1, 1)),
                eq(LocalDate.of(2025, 12, 31)),
                isNull(), isNull()))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/trainees/john.doe/trainings")
                        .param("from", "2025-01-01")
                        .param("to", "2025-12-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getTrainings_unknownUser_returns404() throws Exception {
        when(traineeService.getTrainings(eq("ghost"), any(), any(), any(), any()))
                .thenThrow(new UserDoesNotExistException("Not found"));

        mockMvc.perform(get("/api/trainees/ghost/trainings"))
                .andExpect(status().isNotFound());
    }

    // ─── GET /api/trainees/{username}/unassigned-trainers ────────────────────

    @Test
    void getUnassignedTrainers_returns200WithList() throws Exception {
        var trainer = new Profile.Trainer("Jane", "Smith", "jane.smith", "Yoga", true, List.of());
        when(traineeService.getUnassignedTrainers("john.doe")).thenReturn(List.of(trainer));

        mockMvc.perform(get("/api/trainees/john.doe/unassigned-trainers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("jane.smith"))
                .andExpect(jsonPath("$[0].specialization").value("Yoga"));
    }

    // ─── PUT /api/trainees/{username}/trainers ────────────────────────────────

    @Test
    void updateTrainers_validBody_returns200() throws Exception {
        var body = new Request.UpdateTraineeTrainers("john.doe", List.of("jane.smith"));
        var trainer = new Profile.Trainer("Jane", "Smith", "jane.smith", "Yoga", true, List.of());
        when(traineeService.updateTrainers(any())).thenReturn(List.of(trainer));

        mockMvc.perform(put("/api/trainees/john.doe/trainers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestJson.MAPPER.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("jane.smith"));
    }

    @Test
    void updateTrainers_emptyList_returns400() throws Exception {
        var body = new Request.UpdateTraineeTrainers("john.doe", List.of());

        mockMvc.perform(put("/api/trainees/john.doe/trainers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestJson.MAPPER.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }
}
