package tech.provokedynamic.gymcrm.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tech.provokedynamic.gymcrm.dto.*;
import tech.provokedynamic.gymcrm.dto.request.*;
import tech.provokedynamic.gymcrm.dto.response.*;
import tech.provokedynamic.gymcrm.service.TrainingService;
import java.net.URI;
import java.security.Principal;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/trainings")
@RequiredArgsConstructor
@Tag(name = "Training", description = "Training management endpoints")
public class TrainingController {
    private final TrainingService trainingService;

    @Operation(summary = "Schedule a training as the assigned trainer")
    @PostMapping
    @PreAuthorize("#body.trainerUsername() == authentication.name")
    public ResponseEntity<TrainingDetails> add(@Valid @RequestBody ScheduleTrainingRequest body) {
        var training = trainingService.scheduleTraining(body);
        return ResponseEntity.created(URI.create("/api/trainings/" + training.id())).body(training);
    }

    @Operation(summary = "Get a training you participate in")
    @GetMapping("/{id}")
    public TrainingDetails get(@PathVariable @Positive long id, Principal principal) {
        return trainingService.getTraining(id, principal.getName());
    }

    @Operation(summary = "Search your trainings, newest first; page size is limited to 100")
    @GetMapping
    public PageResponse<TrainingDetails> search(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) String traineeUsername,
            @RequestParam(required = false) String trainerUsername,
            @RequestParam(required = false) String trainingType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Principal principal) {
        return trainingService.searchTrainings(principal.getName(),
                new TrainingSearch(from, to, traineeUsername, trainerUsername, trainingType, page, size));
    }

    @Operation(summary = "Cancel a training as its assigned trainer")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancel(@PathVariable @Positive Long id, Principal principal) {
        trainingService.cancelTraining(new CancelTrainingRequest(id), principal.getName());
        return ResponseEntity.noContent().build();
    }
}
