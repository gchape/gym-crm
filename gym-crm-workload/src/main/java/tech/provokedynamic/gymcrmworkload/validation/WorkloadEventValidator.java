package tech.provokedynamic.gymcrmworkload.validation;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import tech.provokedynamic.gymcrmcommon.event.WorkloadEvent;
import tech.provokedynamic.gymcrmworkload.exception.InvalidWorkloadEventException;

/**
 * WorkloadEvent is a shared DTO in gym-crm-common with no bean-validation
 * annotations (it must stay dependency-light for producer & consumer alike),
 * so inbound events are validated explicitly here before touching Mongo.
 */
@Component
public class WorkloadEventValidator {

    public void validate(WorkloadEvent event) {
        if (event == null) {
            throw new InvalidWorkloadEventException("Workload event must not be null");
        }
        if (!StringUtils.hasText(event.trainerUsername())) {
            throw new InvalidWorkloadEventException("trainerUsername is required");
        }
        if (!StringUtils.hasText(event.trainerFirstName())) {
            throw new InvalidWorkloadEventException("trainerFirstName is required");
        }
        if (!StringUtils.hasText(event.trainerLastName())) {
            throw new InvalidWorkloadEventException("trainerLastName is required");
        }
        if (event.trainingDate() == null) {
            throw new InvalidWorkloadEventException("trainingDate is required");
        }
        if (event.trainingDuration() <= 0) {
            throw new InvalidWorkloadEventException("trainingDuration must be greater than zero");
        }
        if (event.actionType() == null) {
            throw new InvalidWorkloadEventException("actionType is required");
        }
    }
}
