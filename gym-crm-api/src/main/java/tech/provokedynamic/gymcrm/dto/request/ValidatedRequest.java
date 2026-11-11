package tech.provokedynamic.gymcrm.dto.request;

/** Marker for commands that receive Bean Validation at the application service boundary. */
public sealed interface ValidatedRequest permits
        CancelTrainingRequest,
        LoginRequest,
        RegisterTraineeRequest,
        UpdateTraineeRequest,
        DeleteTraineeRequest,
        ReplaceTrainerAssignmentsRequest,
        RegisterTrainerRequest,
        UpdateTrainerRequest,
        ChangePasswordRequest,
        ChangeActiveStatusRequest,
        ScheduleTrainingRequest {
}
