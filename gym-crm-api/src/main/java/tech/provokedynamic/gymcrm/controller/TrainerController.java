package tech.provokedynamic.gymcrm.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tech.provokedynamic.gymcrm.dto.request.*;
import tech.provokedynamic.gymcrm.dto.response.*;
import tech.provokedynamic.gymcrm.service.TrainerService;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/trainers")
@RequiredArgsConstructor
@Tag(name = "Trainer", description = "Trainer management endpoints")
public class TrainerController {

    private final TrainerService trainerService;

    @Operation(summary = "Register a new trainer")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Trainer registered successfully",
                    content = @Content(schema = @Schema(implementation = RegistrationCredentials.class))),
            @ApiResponse(responseCode = "400", description = "Validation error", content = @Content)
    })
    @PostMapping
    public ResponseEntity<RegistrationCredentials> register(@Valid @RequestBody RegisterTrainerRequest body) {
        log.info("POST /api/trainers - registering trainer firstName={} lastName={}", body.firstName(), body.lastName());

        var credentials = trainerService.create(body);

        log.info("POST /api/trainers - trainer registered username={}", credentials.username());
        return ResponseEntity.status(HttpStatus.CREATED).body(credentials);
    }

    @Operation(summary = "Get trainer profile by username")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "UserProfile found",
                    content = @Content(schema = @Schema(implementation = TrainerProfileResponse.class))),
            @ApiResponse(responseCode = "404", description = "Trainer not found", content = @Content)
    })
    @GetMapping("/{username}")
    public ResponseEntity<TrainerProfileResponse> getProfile(@PathVariable String username) {
        log.info("GET /api/trainers/{} - fetching profile", username);

        var profile = trainerService.getProfile(username);

        log.info("GET /api/trainers/{} - profile returned", username);
        return ResponseEntity.ok(TrainerProfileResponse.from(profile));
    }

    @Operation(summary = "Update trainer profile")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "UserProfile updated",
                    content = @Content(schema = @Schema(implementation = TrainerProfileResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error", content = @Content),
            @ApiResponse(responseCode = "401", description = "Authentication failed", content = @Content),
            @ApiResponse(responseCode = "404", description = "Trainer not found", content = @Content)
    })
    @PutMapping("/{username}")
    public ResponseEntity<TrainerProfileResponse> update(
            @PathVariable String username,
            @Valid @RequestBody UpdateTrainerRequest body) {
        log.info("PUT /api/trainers/{} - updating profile", username);

        var request = new UpdateTrainerRequest(
                username,
                body.firstName(), body.lastName(),
                body.specialization(), body.isActive());
        var profile = trainerService.update(request);

        log.info("PUT /api/trainers/{} - profile updated", username);
        return ResponseEntity.ok(TrainerProfileResponse.from(profile));
    }

    @Operation(summary = "Change trainer password")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Password changed"),
            @ApiResponse(responseCode = "400", description = "Validation error", content = @Content),
            @ApiResponse(responseCode = "401", description = "Authentication failed", content = @Content)
    })
    @PutMapping("/{username}/password")
    public ResponseEntity<Void> changePassword(
            @PathVariable String username,
            @Valid @RequestBody ChangePasswordRequest body) {
        log.info("PUT /api/trainers/{}/password - changing password", username);

        trainerService.changePassword(new ChangePasswordRequest(username, body.password(), body.newPassword()));

        log.info("PUT /api/trainers/{}/password - password changed", username);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Activate or deactivate a trainer (not idempotent)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status updated"),
            @ApiResponse(responseCode = "401", description = "Authentication failed", content = @Content),
            @ApiResponse(responseCode = "404", description = "Trainer not found", content = @Content),
            @ApiResponse(responseCode = "409", description = "Already in requested state", content = @Content)
    })
    @PatchMapping("/{username}/active")
    public ResponseEntity<Void> setActive(
            @PathVariable String username,
            @Valid @RequestBody ChangeActiveStatusRequest body) {
        log.info("PATCH /api/trainers/{}/active - isActive={}", username, body.isActive());

        var req = new ChangeActiveStatusRequest(username, body.isActive());
        if (body.isActive()) {
            trainerService.activate(req);
        } else {
            trainerService.deactivate(req);
        }

        log.info("PATCH /api/trainers/{}/active - status updated to {}", username, body.isActive());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Get trainer's training list")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Trainings returned"),
            @ApiResponse(responseCode = "404", description = "Trainer not found", content = @Content)
    })
    @GetMapping("/{username}/trainings")
    public ResponseEntity<List<TrainingSummaryResponse>> getTrainings(
            @PathVariable String username,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) String traineeUsername) {
        log.info("GET /api/trainers/{}/trainings", username);

        var trainings = trainerService.getTrainings(username, from, to, traineeUsername);
        var response = trainings.stream().map(TrainingSummaryResponse::fromTrainer).toList();

        log.info("GET /api/trainers/{}/trainings - {} results", username, response.size());
        return ResponseEntity.ok(response);
    }
}
