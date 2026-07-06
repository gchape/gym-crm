package tech.provokedynamic.gymcrm.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import tech.provokedynamic.gymcrm.security.JwtService;
import tech.provokedynamic.gymcrm.security.LoginAttemptService;
import tech.provokedynamic.gymcrm.security.TokenBlacklist;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@NullMarked
@Tag(name = "Auth", description = "Login and logout")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final TokenBlacklist tokenBlacklist;
    private final LoginAttemptService loginAttemptService;

    @Operation(summary = "Login — returns JWT token")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login successful"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials"),
            @ApiResponse(responseCode = "429", description = "Account temporarily blocked")
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest body) {
        log.info("POST /api/login - username={}", body.username());

        if (loginAttemptService.isBlocked(body.username())) {
            log.warn("POST /api/login - account blocked username={}", body.username());
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(body.username(), body.password()));
        } catch (BadCredentialsException e) {
            loginAttemptService.onFailure(body.username());
            log.warn("POST /api/login - bad credentials username={}", body.username());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        loginAttemptService.onSuccess(body.username());
        var token = jwtService.generateToken(body.username());

        log.info("POST /api/login - success username={}", body.username());
        return ResponseEntity.ok(new LoginResponse(token));
    }

    @Operation(summary = "Logout — invalidates JWT token")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Logged out successfully"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid token")
    })
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String authHeader) {
        log.info("POST /api/logout");

        var token = authHeader.substring(7);
        tokenBlacklist.blacklist(token);

        log.info("POST /api/logout - token blacklisted");
        return ResponseEntity.ok().build();
    }

    public record LoginRequest(@NotBlank String username, @NotBlank String password) {
    }

    public record LoginResponse(String token) {
    }
}
