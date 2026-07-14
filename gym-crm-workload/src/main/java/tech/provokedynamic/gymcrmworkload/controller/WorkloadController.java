package tech.provokedynamic.gymcrmworkload.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tech.provokedynamic.gymcrmworkload.dto.WorkloadRequest;
import tech.provokedynamic.gymcrmworkload.dto.response.TrainerWorkloadResponse;
import tech.provokedynamic.gymcrmworkload.mapper.WorkloadResponseMapper;
import tech.provokedynamic.gymcrmworkload.model.TrainerWorkloadSummary;
import tech.provokedynamic.gymcrmworkload.service.WorkloadService;

@RestController
@RequestMapping("/api/trainers/workload")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Workload", description = "Trainer workload aggregation endpoints")
public class WorkloadController {

    private final WorkloadService workloadService;
    private final WorkloadResponseMapper responseMapper;

    public WorkloadController(WorkloadService workloadService, WorkloadResponseMapper responseMapper) {
        this.workloadService = workloadService;
        this.responseMapper = responseMapper;
    }

    @Operation(summary = "Submit a workload delta for a trainer")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Workload updated"),
            @ApiResponse(responseCode = "400", description = "Validation error", content = @Content),
            @ApiResponse(responseCode = "401", description = "Missing or invalid bearer token", content = @Content)
    })
    @PostMapping
    public ResponseEntity<Void> submitWorkload(@Valid @RequestBody WorkloadRequest request) {
        workloadService.processWorkload(request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Get aggregated workload summary for a trainer")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Summary found",
                    content = @Content(schema = @Schema(implementation = TrainerWorkloadResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid bearer token", content = @Content),
            @ApiResponse(responseCode = "404", description = "No summary exists for this trainer", content = @Content)
    })
    @GetMapping("/{username}")
    public ResponseEntity<TrainerWorkloadResponse> getWorkload(
            @Parameter(description = "Trainer username") @PathVariable String username) {
        TrainerWorkloadSummary summary = workloadService.getSummary(username);
        if (summary == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(responseMapper.toResponse(summary));
    }
}
