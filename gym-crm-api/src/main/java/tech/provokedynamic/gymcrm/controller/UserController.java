package tech.provokedynamic.gymcrm.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import tech.provokedynamic.gymcrm.dto.Request;
import tech.provokedynamic.gymcrm.dto.Response;
import tech.provokedynamic.gymcrm.repository.TrainingTypeRepository;
import tech.provokedynamic.gymcrm.service.UserService;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "User", description = "Shared user endpoints")
public class UserController {

    private final UserService userService;
    private final TrainingTypeRepository trainingTypeRepository;

    @Operation(summary = "Change login password")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Password changed successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "401", description = "Authentication failed"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PutMapping("/api/password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody Request.ChangePassword body) {
        log.info("PUT /api/password - changing password username={}", body.username());

        userService.updatePassword(body);

        log.info("PUT /api/password - password changed username={}", body.username());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Get all training types")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Training types returned")
    })
    @GetMapping("/api/training-types")
    public ResponseEntity<List<Response.TrainingType>> getTrainingTypes() {
        log.info("GET /api/training-types");

        var types = trainingTypeRepository.findAll()
                .stream()
                .map(t -> new Response.TrainingType(t.getId(), t.getTrainingTypeName()))
                .toList();

        log.info("GET /api/training-types - {} types returned", types.size());
        return ResponseEntity.ok(types);
    }
}
