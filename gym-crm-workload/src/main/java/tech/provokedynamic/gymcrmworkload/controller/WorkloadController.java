package tech.provokedynamic.gymcrmworkload.controller;

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
public class WorkloadController {

    private final WorkloadService workloadService;
    private final WorkloadResponseMapper responseMapper;

    public WorkloadController(WorkloadService workloadService, WorkloadResponseMapper responseMapper) {
        this.workloadService = workloadService;
        this.responseMapper = responseMapper;
    }

    @PostMapping
    public ResponseEntity<Void> submitWorkload(@Valid @RequestBody WorkloadRequest request) {
        workloadService.processWorkload(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{username}")
    public ResponseEntity<TrainerWorkloadResponse> getWorkload(@PathVariable String username) {
        TrainerWorkloadSummary summary = workloadService.getSummary(username);
        if (summary == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(responseMapper.toResponse(summary));
    }
}
