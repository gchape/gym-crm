package tech.provokedynamic.gymcrm.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tech.provokedynamic.gymcrm.dto.Request;
import tech.provokedynamic.gymcrm.service.TrainingService;

@Slf4j
@RestController
@RequestMapping("/api/trainings")
@RequiredArgsConstructor
@Tag(name = "Training", description = "Training management endpoints")
public class TrainingController {

    private final TrainingService trainingService;

    @Operation(summary = "Add a new training session")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Training added"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "401", description = "Authentication failed"),
            @ApiResponse(responseCode = "404", description = "Trainee or trainer not found")
    })
    @PostMapping
    public ResponseEntity<Void> add(@Valid @RequestBody Request.AddTraining body) {
        log.info("POST /api/trainings - trainee={} trainer={} name={} date={} duration={}min",
                body.traineeUsername(), body.trainerUsername(), body.trainingName(),
                body.trainingDate(), body.trainingDuration());

        trainingService.add(body);

        log.info("POST /api/trainings - added successfully for trainee={} trainer={}",
                body.traineeUsername(), body.trainerUsername());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Cancel a training session")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Training cancelled"),
            @ApiResponse(responseCode = "404", description = "Training not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancel(@PathVariable Long id) {
        log.info("DELETE /api/trainings/{} - cancelling training", id);
        trainingService.cancel(new Request.CancelTraining(id));
        log.info("DELETE /api/trainings/{} - cancelled successfully", id);
        return ResponseEntity.ok().build();
    }
}
