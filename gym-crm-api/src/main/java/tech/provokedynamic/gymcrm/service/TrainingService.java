package tech.provokedynamic.gymcrm.service;

import tech.provokedynamic.gymcrm.dto.PageResponse;
import tech.provokedynamic.gymcrm.dto.TrainingDetails;
import tech.provokedynamic.gymcrm.dto.TrainingSearch;
import tech.provokedynamic.gymcrm.dto.request.CancelTrainingRequest;
import tech.provokedynamic.gymcrm.dto.request.ScheduleTrainingRequest;

/** Transactional application boundary for training sessions and participant-scoped queries. */
public interface TrainingService {
    /** Creates a validated session and arranges its workload notification after commit. */
    TrainingDetails scheduleTraining(ScheduleTrainingRequest request);

    /** Cancels an existing session after verifying that the caller is its assigned trainer. */
    void cancelTraining(CancelTrainingRequest request, String trainerUsername);

    /** Returns a session only when the caller is one of its participants. */
    TrainingDetails getTraining(long id, String username);

    /** Searches only sessions visible to the caller, returning an explicit, stable page contract. */
    PageResponse<TrainingDetails> searchTrainings(String username, TrainingSearch search);
}
