package tech.provokedynamic.gymcrmworkload.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tech.provokedynamic.gymcrmworkload.dto.response.TrainerWorkloadResponse;
import tech.provokedynamic.gymcrmworkload.mapper.WorkloadResponseMapper;
import tech.provokedynamic.gymcrmworkload.service.WorkloadService;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/trainers/workload")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Workload", description = "Trainer workload aggregation endpoints")
public class WorkloadController {

    private final WorkloadService workloadService;
    private final WorkloadResponseMapper responseMapper;

    @Operation(summary = "Get aggregated workload summary for a trainer")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Summary found",
                    content = @Content(schema = @Schema(implementation = TrainerWorkloadResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid bearer token", content = @Content),
            @ApiResponse(responseCode = "404", description = "No summary exists for this trainer", content = @Content)
    })
    @GetMapping("/{username}")
    public ResponseEntity<TrainerWorkloadResponse> getWorkload(@PathVariable String username) {
        log.info("GET /api/trainers/workload/{} - fetching workload summary", username);

        var document = workloadService.getSummary(username);
        if (document == null) {
            log.warn("GET /api/trainers/workload/{} - no summary found", username);
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(responseMapper.toResponse(document));
    }
}
