package tech.provokedynamic.gymcrm.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tech.provokedynamic.gymcrm.dto.Request;
import tech.provokedynamic.gymcrm.entity.TrainingType;
import tech.provokedynamic.gymcrm.exception.AuthenticationException;
import tech.provokedynamic.gymcrm.exception.GlobalExceptionHandler;
import tech.provokedynamic.gymcrm.exception.UserDoesNotExistException;
import tech.provokedynamic.gymcrm.repository.TrainingTypeRepository;
import tech.provokedynamic.gymcrm.service.UserService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    UserService userService;
    @Mock
    TrainingTypeRepository trainingTypeRepository;

    MockMvc mockMvc;
    ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new UserController(userService, trainingTypeRepository))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        mapper = new ObjectMapper();
    }

    // ─── PUT /api/password ────────────────────────────────────────────────────

    @Test
    void changePassword_validBody_returns200() throws Exception {
        var body = new Request.ChangePassword("john.doe", "oldpassword", "newpass123");
        doNothing().when(userService).updatePassword(any());

        mockMvc.perform(put("/api/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(body)))
                .andExpect(status().isOk());
    }

    @Test
    void changePassword_wrongCurrentPassword_returns401() throws Exception {
        var body = new Request.ChangePassword("john.doe", "wrongpass", "newpass123");
        doThrow(new AuthenticationException("Bad credentials"))
                .when(userService).updatePassword(any());

        mockMvc.perform(put("/api/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(body)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void changePassword_userNotFound_returns404() throws Exception {
        var body = new Request.ChangePassword("ghost", "oldpassword", "newpass123");
        doThrow(new UserDoesNotExistException("Not found"))
                .when(userService).updatePassword(any());

        mockMvc.perform(put("/api/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(body)))
                .andExpect(status().isNotFound());
    }

    @Test
    void changePassword_newPasswordTooShort_returns400() throws Exception {
        var body = new Request.ChangePassword("john.doe", "oldpassword", "short");

        mockMvc.perform(put("/api/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void changePassword_blankUsername_returns400() throws Exception {
        String rawBody = """
                {"username":"","password":"oldpassword","newPassword":"newpass123"}
                """;

        mockMvc.perform(put("/api/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(rawBody))
                .andExpect(status().isBadRequest());
    }

    // ─── GET /api/training-types ──────────────────────────────────────────────

    @Test
    void getTrainingTypes_returnsAll() throws Exception {
        when(trainingTypeRepository.findAll()).thenReturn(List.of(
                new TrainingType("Yoga"),
                new TrainingType("CrossFit")
        ));

        mockMvc.perform(get("/api/training-types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].trainingTypeName").value("Yoga"))
                .andExpect(jsonPath("$[1].trainingTypeName").value("CrossFit"));
    }

    @Test
    void getTrainingTypes_emptyRepo_returnsEmptyArray() throws Exception {
        when(trainingTypeRepository.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/training-types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
