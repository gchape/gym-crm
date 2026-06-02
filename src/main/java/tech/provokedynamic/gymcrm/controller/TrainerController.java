package tech.provokedynamic.gymcrm.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tech.provokedynamic.gymcrm.dto.Request;
import tech.provokedynamic.gymcrm.dto.Response;
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
                    content = @Content(schema = @Schema(implementation = Response.CreatedUser.class))),
            @ApiResponse(responseCode = "400", description = "Validation error", content = @Content)
    })
    @PostMapping
    public ResponseEntity<Response.CreatedUser> register(@Valid @RequestBody Request.CreateTrainer body) {
        log.info("POST /api/trainers - registering trainer firstName={} lastName={}", body.firstName(), body.lastName());

        var request = new Request.CreateTrainer(body.firstName(), body.lastName(), body.specialization());
        var profile = trainerService.create(request);

        log.info("POST /api/trainers - trainer registered username={}", profile.username());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new Response.CreatedUser(profile.username(), null));
    }

    @Operation(summary = "Get trainer profile by username")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile found",
                    content = @Content(schema = @Schema(implementation = Response.TrainerProfile.class))),
            @ApiResponse(responseCode = "404", description = "Trainer not found", content = @Content)
    })
    @GetMapping("/{username}")
    public ResponseEntity<Response.TrainerProfile> getProfile(@PathVariable String username) {
        log.info("GET /api/trainers/{} - fetching profile", username);

        var profile = trainerService.getProfile(username);
        var response = Response.TrainerProfile.from(profile);

        log.info("GET /api/trainers/{} - profile returned", username);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update trainer profile")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile updated"),
            @ApiResponse(responseCode = "400", description = "Validation error", content = @Content),
            @ApiResponse(responseCode = "401", description = "Authentication failed", content = @Content),
            @ApiResponse(responseCode = "404", description = "Trainer not found", content = @Content)
    })
    @PutMapping("/{username}")
    public ResponseEntity<Response.TrainerProfile> update(
            @PathVariable String username,
            @Valid @RequestBody Request.UpdateTrainer body) {
        log.info("PUT /api/trainers/{} - updating profile", username);

        var request = new Request.UpdateTrainer(
                username, body.password(),
                body.firstName(), body.lastName(),
                body.specialization());
        var profile = trainerService.update(request);

        log.info("PUT /api/trainers/{} - profile updated", username);
        return ResponseEntity.ok(Response.TrainerProfile.from(profile));
    }

    @Operation(summary = "Change trainer password")
    @PutMapping("/{username}/password")
    public ResponseEntity<Void> changePassword(
            @PathVariable String username,
            @Valid @RequestBody Request.ChangePassword body) {
        log.info("PUT /api/trainers/{}/password - changing password", username);

        trainerService.changePassword(new Request.ChangePassword(username, body.password(), body.newPassword()));

        log.info("PUT /api/trainers/{}/password - password changed", username);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Activate or deactivate a trainer")
    @PatchMapping("/{username}/active")
    public ResponseEntity<Void> toggleActive(
            @PathVariable String username,
            @Valid @RequestBody Request.ToggleActive2 body) {
        log.info("PATCH /api/trainers/{}/active - isActive={}", username, body.isActive());

        var req = new Request.ToggleActive(username, body.password());
        if (body.isActive()) {
            trainerService.activate(req);
        } else {
            trainerService.deactivate(req);
        }

        log.info("PATCH /api/trainers/{}/active - status updated to {}", username, body.isActive());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Get trainer's training list")
    @GetMapping("/{username}/trainings")
    public ResponseEntity<List<Response.TrainingSummary>> getTrainings(
            @PathVariable String username,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @Nullable LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @Nullable LocalDate to,
            @RequestParam(required = false) @Nullable String traineeUsername) {
        log.info("GET /api/trainers/{}/trainings", username);

        var trainings = trainerService.getTrainings(username, from, to, traineeUsername);
        var response = trainings.stream().map(Response.TrainingSummary::fromTrainer).toList();

        log.info("GET /api/trainers/{}/trainings - {} results", username, response.size());
        return ResponseEntity.ok(response);
    }
}
