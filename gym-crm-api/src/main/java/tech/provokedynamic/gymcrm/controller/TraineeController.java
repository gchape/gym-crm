package tech.provokedynamic.gymcrm.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import tech.provokedynamic.gymcrm.service.TraineeService;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/trainees")
@RequiredArgsConstructor
@Tag(name = "Trainee", description = "Trainee management endpoints")
public class TraineeController {

    private final TraineeService traineeService;

    @Operation(summary = "Register a new trainee")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Trainee registered successfully",
                    content = @Content(schema = @Schema(implementation = RegistrationCredentials.class))),
            @ApiResponse(responseCode = "400", description = "Validation error", content = @Content)
    })
    @PostMapping
    public ResponseEntity<RegistrationCredentials> register(@Valid @RequestBody RegisterTraineeRequest body) {
        log.info("POST /api/trainees - registering trainee firstName={} lastName={}", body.firstName(), body.lastName());

        var credentials = traineeService.create(body);

        log.info("POST /api/trainees - trainee registered username={}", credentials.username());
        return ResponseEntity.status(HttpStatus.CREATED).body(credentials);
    }

    @Operation(summary = "Get trainee profile by username")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "UserProfile found",
                    content = @Content(schema = @Schema(implementation = TraineeProfileResponse.class))),
            @ApiResponse(responseCode = "404", description = "Trainee not found", content = @Content)
    })
    @GetMapping("/{username}")
    public ResponseEntity<TraineeProfileResponse> getProfile(
            @Parameter(description = "Trainee username") @PathVariable String username) {
        log.info("GET /api/trainees/{} - fetching profile", username);

        var profile = traineeService.getProfile(username);
        var response = TraineeProfileResponse.from(profile);

        log.info("GET /api/trainees/{} - profile returned", username);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update trainee profile")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "UserProfile updated",
                    content = @Content(schema = @Schema(implementation = TraineeProfileResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error", content = @Content),
            @ApiResponse(responseCode = "401", description = "Authentication failed", content = @Content),
            @ApiResponse(responseCode = "404", description = "Trainee not found", content = @Content)
    })
    @PutMapping("/{username}")
    public ResponseEntity<TraineeProfileResponse> update(
            @PathVariable String username,
            @Valid @RequestBody UpdateTraineeRequest body) {
        log.info("PUT /api/trainees/{} - updating profile", username);

        var request = new UpdateTraineeRequest(
                username,
                body.firstName(), body.lastName(),
                body.dateOfBirth(), body.address(),
                body.isActive());
        var profile = traineeService.update(request);

        log.info("PUT /api/trainees/{} - profile updated", username);
        return ResponseEntity.ok(TraineeProfileResponse.from(profile));
    }

    @Operation(summary = "Delete trainee profile (hard delete, cascades trainings)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Trainee deleted"),
            @ApiResponse(responseCode = "401", description = "Authentication failed", content = @Content),
            @ApiResponse(responseCode = "404", description = "Trainee not found", content = @Content)
    })
    @DeleteMapping("/{username}")
    public ResponseEntity<Void> delete(@PathVariable String username) {
        log.info("DELETE /api/trainees/{} - deleting", username);

        traineeService.delete(new DeleteTraineeRequest(username));

        log.info("DELETE /api/trainees/{} - deleted", username);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Change trainee password")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Password changed"),
            @ApiResponse(responseCode = "400", description = "Validation error", content = @Content),
            @ApiResponse(responseCode = "401", description = "Authentication failed", content = @Content)
    })
    @PutMapping("/{username}/password")
    public ResponseEntity<Void> changePassword(
            @PathVariable String username,
            @Valid @RequestBody ChangePasswordRequest body) {
        log.info("PUT /api/trainees/{}/password - changing password", username);

        traineeService.changePassword(new ChangePasswordRequest(username, body.password(), body.newPassword()));

        log.info("PUT /api/trainees/{}/password - password changed", username);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Activate or deactivate a trainee (not idempotent)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status updated"),
            @ApiResponse(responseCode = "401", description = "Authentication failed", content = @Content),
            @ApiResponse(responseCode = "404", description = "Trainee not found", content = @Content),
            @ApiResponse(responseCode = "409", description = "Already in requested state", content = @Content)
    })
    @PatchMapping("/{username}/active")
    public ResponseEntity<Void> setActive(
            @PathVariable String username,
            @Valid @RequestBody ChangeActiveStatusRequest body) {
        log.info("PATCH /api/trainees/{}/active - isActive={}", username, body.isActive());

        var req = new ChangeActiveStatusRequest(username, body.isActive());
        if (body.isActive()) {
            traineeService.activate(req);
        } else {
            traineeService.deactivate(req);
        }

        log.info("PATCH /api/trainees/{}/active - status updated to {}", username, body.isActive());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Get trainee's training list")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Trainings returned"),
            @ApiResponse(responseCode = "404", description = "Trainee not found", content = @Content)
    })
    @GetMapping("/{username}/trainings")
    public ResponseEntity<List<TrainingSummaryResponse>> getTrainings(
            @PathVariable String username,
            @Parameter(description = "Filter from date (yyyy-MM-dd)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @Parameter(description = "Filter to date (yyyy-MM-dd)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) String trainerUsername,
            @RequestParam(required = false) String trainingType) {
        log.info("GET /api/trainees/{}/trainings", username);

        var trainings = traineeService.getTrainings(username, from, to, trainerUsername, trainingType);
        var response = trainings.stream().map(TrainingSummaryResponse::fromTrainee).toList();

        log.info("GET /api/trainees/{}/trainings - {} results", username, response.size());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get active trainers not assigned to this trainee")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Unassigned trainers returned"),
            @ApiResponse(responseCode = "404", description = "Trainee not found", content = @Content)
    })
    @GetMapping("/{username}/unassigned-trainers")
    public ResponseEntity<List<TrainerSummaryResponse>> getUnassignedTrainers(
            @PathVariable String username) {
        log.info("GET /api/trainees/{}/unassigned-trainers", username);

        var trainers = traineeService.getUnassignedTrainers(username)
                .stream().map(TrainerSummaryResponse::from).toList();

        log.info("GET /api/trainees/{}/unassigned-trainers - {} results", username, trainers.size());
        return ResponseEntity.ok(trainers);
    }

    @Operation(summary = "Update trainee's trainer list")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Trainer list updated"),
            @ApiResponse(responseCode = "400", description = "Validation error", content = @Content),
            @ApiResponse(responseCode = "401", description = "Authentication failed", content = @Content),
            @ApiResponse(responseCode = "404", description = "Trainee or trainer not found", content = @Content)
    })
    @PutMapping("/{username}/trainers")
    public ResponseEntity<List<TrainerSummaryResponse>> updateTrainers(
            @PathVariable String username,
            @Valid @RequestBody ReplaceTrainerAssignmentsRequest body) {
        log.info("PUT /api/trainees/{}/trainers", username);

        var request = new ReplaceTrainerAssignmentsRequest(username, body.trainerUsernames());
        var trainers = traineeService.updateTrainers(request)
                .stream().map(TrainerSummaryResponse::from).toList();

        log.info("PUT /api/trainees/{}/trainers - updated {} trainers", username, trainers.size());
        return ResponseEntity.ok(trainers);
    }
}
