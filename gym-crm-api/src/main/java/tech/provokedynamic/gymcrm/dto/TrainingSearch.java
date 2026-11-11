package tech.provokedynamic.gymcrm.dto;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import tech.provokedynamic.gymcrm.exception.InvalidRequestException;
import java.time.LocalDate;

public record TrainingSearch(LocalDate from, LocalDate to, String traineeUsername,
                             String trainerUsername, String trainingType, int page, int size) {
    public TrainingSearch {
        if (page < 0 || size < 1 || size > 100) {
            throw new InvalidRequestException("Page must be non-negative and size must be between 1 and 100");
        }
        if (from != null && to != null && from.isAfter(to)) {
            throw new InvalidRequestException("The start date must be on or before the end date");
        }
    }

    public PageRequest pageable() {
        return PageRequest.of(page, size, Sort.by("trainingDate").descending().and(Sort.by("id").descending()));
    }
}
