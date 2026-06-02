package tech.provokedynamic.gymcrm.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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
            @ApiResponse(responseCode = "404", description = "Trainee, trainer, or training type not found")
    })
    @PostMapping
    public ResponseEntity<Void> add(@Valid @RequestBody Request.AddTraining body) {
        log.info("POST /api/trainings - trainee={} trainer={} name={}",
                body.traineeUsername(), body.trainerUsername(), body.trainingName());

        trainingService.add(new Request.AddTraining(
                body.traineeUsername(),
                body.traineePassword(),
                body.trainerUsername(),
                body.trainingName(),
                body.trainingType(),
                body.trainingDate(),
                body.trainingDuration()
        ));

        log.info("POST /api/trainings - added successfully");
        return ResponseEntity.ok().build();
    }
}
