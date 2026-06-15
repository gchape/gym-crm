package tech.provokedynamic.gymcrm.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tech.provokedynamic.gymcrm.security.JwtService;
import tech.provokedynamic.gymcrm.security.LoginAttemptService;
import tech.provokedynamic.gymcrm.security.TokenBlacklist;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    AuthenticationManager authenticationManager;
    @Mock
    JwtService jwtService;
    @Mock
    TokenBlacklist tokenBlacklist;
    @Mock
    LoginAttemptService loginAttemptService;

    MockMvc mockMvc;
    ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new AuthController(
                        authenticationManager, jwtService, tokenBlacklist, loginAttemptService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        mapper = new ObjectMapper();
    }

    // ─── POST /api/login ──────────────────────────────────────────────────────

    @Test
    void login_validCredentials_returns200WithToken() throws Exception {
        when(loginAttemptService.isBlocked("john.doe")).thenReturn(false);
        when(authenticationManager.authenticate(any())).thenReturn(
                new UsernamePasswordAuthenticationToken("john.doe", null));
        when(jwtService.generateToken("john.doe")).thenReturn("jwt.token.here");

        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"john.doe","password":"pass123456"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt.token.here"));

        verify(loginAttemptService).onSuccess("john.doe");
    }

    @Test
    void login_badCredentials_returns401AndRecordsFailure() throws Exception {
        when(loginAttemptService.isBlocked("john.doe")).thenReturn(false);
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("bad"));

        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"john.doe","password":"wrong"}
                                """))
                .andExpect(status().isUnauthorized());

        verify(loginAttemptService).onFailure("john.doe");
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void login_blockedUser_returns429WithoutAttemptingAuth() throws Exception {
        when(loginAttemptService.isBlocked("john.doe")).thenReturn(true);

        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"john.doe","password":"pass123456"}
                                """))
                .andExpect(status().isTooManyRequests());

        verifyNoInteractions(authenticationManager);
        verifyNoInteractions(jwtService);
    }

    @Test
    void login_blankUsername_returns400() throws Exception {
        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"","password":"pass123456"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_blankPassword_returns400() throws Exception {
        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"john.doe","password":""}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_missingBody_returns400() throws Exception {
        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    // ─── POST /api/logout ─────────────────────────────────────────────────────

    @Test
    void logout_validToken_returns200AndBlacklists() throws Exception {
        mockMvc.perform(post("/api/logout")
                        .header("Authorization", "Bearer my.jwt.token"))
                .andExpect(status().isOk());

        verify(tokenBlacklist).blacklist("my.jwt.token");
    }

    @Test
    void logout_missingAuthHeader_returns400() throws Exception {
        mockMvc.perform(post("/api/logout"))
                .andExpect(status().isBadRequest());
    }
}
