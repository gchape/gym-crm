package tech.provokedynamic.gymcrm.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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
import tech.provokedynamic.gymcrm.exception.AlreadyActivatedException;
import tech.provokedynamic.gymcrm.exception.AlreadyDeactivatedException;
import tech.provokedynamic.gymcrm.exception.AuthenticationException;
import tech.provokedynamic.gymcrm.exception.UserDoesNotExistException;
import tech.provokedynamic.gymcrm.service.TrainerService;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TrainerControllerTest {

    @Mock
    TrainerService trainerService;

    MockMvc mockMvc;
    ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new TrainerController(trainerService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        mapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private Profile.Trainer sampleTrainerProfile() {
        return new Profile.Trainer(
                "Jane", "Smith", "jane.smith", "Yoga", true,
                List.of(new Profile.Trainee("John", "Doe", "john.doe",
                        LocalDate.of(1990, 1, 1), null, true, List.of()))
        );
    }

    private Summary.Training sampleTraining() {
        return new Summary.Training(
                "Morning Yoga", LocalDate.now(), "Yoga", 60, "jane.smith", "john.doe"
        );
    }

    // ─── POST /api/trainers ───────────────────────────────────────────────────

    @Test
    void register_validBody_returns201WithCredentials() throws Exception {
        var body = new Request.CreateTrainer("Jane", "Smith", "Yoga");
        var credentials = new Response.CreatedUser("jane.smith", "pass123456");
        when(trainerService.create(any())).thenReturn(credentials);

        mockMvc.perform(post("/api/trainers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("jane.smith"))
                .andExpect(jsonPath("$.password").value("pass123456"));
    }

    @Test
    void register_blankSpecialization_returns400() throws Exception {
        var body = new Request.CreateTrainer("Jane", "Smith", "");

        mockMvc.perform(post("/api/trainers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_tooShortFirstName_returns400() throws Exception {
        var body = new Request.CreateTrainer("J", "Smith", "Yoga");

        mockMvc.perform(post("/api/trainers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_invalidCharactersInName_returns400() throws Exception {
        var body = new Request.CreateTrainer("Jane123", "Smith", "Yoga");

        mockMvc.perform(post("/api/trainers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    // ─── GET /api/trainers/{username} ─────────────────────────────────────────

    @Test
    void getProfile_existingTrainer_returns200() throws Exception {
        when(trainerService.getProfile("jane.smith")).thenReturn(sampleTrainerProfile());

        mockMvc.perform(get("/api/trainers/jane.smith"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Jane"))
                .andExpect(jsonPath("$.lastName").value("Smith"))
                .andExpect(jsonPath("$.specialization").value("Yoga"))
                .andExpect(jsonPath("$.isActive").value(true))
                .andExpect(jsonPath("$.trainees[0].username").value("john.doe"));
    }

    @Test
    void getProfile_unknownTrainer_returns404() throws Exception {
        when(trainerService.getProfile("ghost")).thenThrow(new UserDoesNotExistException("Not found"));

        mockMvc.perform(get("/api/trainers/ghost"))
                .andExpect(status().isNotFound());
    }

    // ─── PUT /api/trainers/{username} ─────────────────────────────────────────

    @Test
    void update_validBody_returns200WithProfile() throws Exception {
        var body = new Request.UpdateTrainer(
                "jane.smith", "oldpassword", "Jane", "Smith", "Yoga", true);
        when(trainerService.update(any())).thenReturn(sampleTrainerProfile());

        mockMvc.perform(put("/api/trainers/jane.smith")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("jane.smith"))
                .andExpect(jsonPath("$.specialization").value("Yoga"));
    }

    @Test
    void update_wrongPassword_returns401() throws Exception {
        var body = new Request.UpdateTrainer(
                "jane.smith", "wrongpass", "Jane", "Smith", "Yoga", true);
        when(trainerService.update(any())).thenThrow(new AuthenticationException("Bad credentials"));

        mockMvc.perform(put("/api/trainers/jane.smith")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(body)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void update_missingIsActive_returns400() throws Exception {
        String rawBody = """
                {"username":"jane.smith","password":"pass","firstName":"Jane","lastName":"Smith","specialization":"Yoga"}
                """;

        mockMvc.perform(put("/api/trainers/jane.smith")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(rawBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void update_unknownTrainer_returns404() throws Exception {
        var body = new Request.UpdateTrainer(
                "ghost", "pass123456", "Ghost", "User", "Yoga", true);
        when(trainerService.update(any())).thenThrow(new UserDoesNotExistException("Not found"));

        mockMvc.perform(put("/api/trainers/ghost")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(body)))
                .andExpect(status().isNotFound());
    }

    // ─── PUT /api/trainers/{username}/password ─────────────────────────────────

    @Test
    void changePassword_validBody_returns200() throws Exception {
        var body = new Request.ChangePassword("jane.smith", "oldpassword", "newpass123");
        doNothing().when(trainerService).changePassword(any());

        mockMvc.perform(put("/api/trainers/jane.smith/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(body)))
                .andExpect(status().isOk());
    }

    @Test
    void changePassword_newPasswordNot10Chars_returns400() throws Exception {
        var body = new Request.ChangePassword("jane.smith", "oldpassword", "tooshort");

        mockMvc.perform(put("/api/trainers/jane.smith/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void changePassword_wrongCurrentPassword_returns401() throws Exception {
        var body = new Request.ChangePassword("jane.smith", "wrongpass", "newpass123");
        doThrow(new AuthenticationException("Bad credentials"))
                .when(trainerService).changePassword(any());

        mockMvc.perform(put("/api/trainers/jane.smith/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(body)))
                .andExpect(status().isUnauthorized());
    }

    // ─── PATCH /api/trainers/{username}/active ─────────────────────────────────

    @Test
    void setActive_activate_callsActivate() throws Exception {
        var body = new Request.ToggleActive("jane.smith", "pass123456", true);
        doNothing().when(trainerService).activate(any());

        mockMvc.perform(patch("/api/trainers/jane.smith/active")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(body)))
                .andExpect(status().isOk());

        verify(trainerService).activate(any());
        verify(trainerService, never()).deactivate(any());
    }

    @Test
    void setActive_deactivate_callsDeactivate() throws Exception {
        var body = new Request.ToggleActive("jane.smith", "pass123456", false);
        doNothing().when(trainerService).deactivate(any());

        mockMvc.perform(patch("/api/trainers/jane.smith/active")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(body)))
                .andExpect(status().isOk());

        verify(trainerService).deactivate(any());
        verify(trainerService, never()).activate(any());
    }

    @Test
    void setActive_alreadyActivated_returns409() throws Exception {
        var body = new Request.ToggleActive("jane.smith", "pass123456", true);
        doThrow(new AlreadyActivatedException("Already active"))
                .when(trainerService).activate(any());

        mockMvc.perform(patch("/api/trainers/jane.smith/active")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(body)))
                .andExpect(status().isConflict());
    }

    @Test
    void setActive_alreadyDeactivated_returns409() throws Exception {
        var body = new Request.ToggleActive("jane.smith", "pass123456", false);
        doThrow(new AlreadyDeactivatedException("Already inactive"))
                .when(trainerService).deactivate(any());

        mockMvc.perform(patch("/api/trainers/jane.smith/active")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(body)))
                .andExpect(status().isConflict());
    }

    // ─── GET /api/trainers/{username}/trainings ────────────────────────────────

    @Test
    void getTrainings_noFilters_returnsAll() throws Exception {
        when(trainerService.getTrainings(eq("jane.smith"), isNull(), isNull(), isNull()))
                .thenReturn(List.of(sampleTraining()));

        mockMvc.perform(get("/api/trainers/jane.smith/trainings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].trainingName").value("Morning Yoga"))
                .andExpect(jsonPath("$[0].traineeName").value("john.doe"))
                .andExpect(jsonPath("$[0].trainerName").doesNotExist());
    }

    @Test
    void getTrainings_withFilters_passesAllParams() throws Exception {
        when(trainerService.getTrainings(
                eq("jane.smith"),
                eq(LocalDate.of(2025, 1, 1)),
                eq(LocalDate.of(2025, 12, 31)),
                eq("john.doe")))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/trainers/jane.smith/trainings")
                        .param("from", "2025-01-01")
                        .param("to", "2025-12-31")
                        .param("traineeUsername", "john.doe"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getTrainings_unknownTrainer_returns404() throws Exception {
        when(trainerService.getTrainings(eq("ghost"), any(), any(), any()))
                .thenThrow(new UserDoesNotExistException("Not found"));

        mockMvc.perform(get("/api/trainers/ghost/trainings"))
                .andExpect(status().isNotFound());
    }
}
