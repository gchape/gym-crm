package tech.provokedynamic.gymcrm.repository;

import jakarta.annotation.Nullable;
import tech.provokedynamic.gymcrm.dto.Summary;

import java.time.LocalDate;
import java.util.List;

public interface TraineeRepositoryCustom {

    List<Summary.Training> findTrainingsByUsername(
            String username,
            @Nullable LocalDate from,
            @Nullable LocalDate to,
            @Nullable String trainer,
            @Nullable String type
    );
}
