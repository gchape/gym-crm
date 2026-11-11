package tech.provokedynamic.gymcrmworkload.document;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * A calendar month's non-negative training duration, expressed in minutes.
 * The legacy MongoDB storage field is retained even though the Java name is clearer.
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "month")
public class MonthlyWorkload {

    @NotNull(message = "Month is required")
    @Min(value = 1, message = "Month must be between 1 and 12")
    @Max(value = 12, message = "Month must be between 1 and 12")
    private Integer month;

    @NotNull(message = "Trainings summary duration is required")
    @Min(value = 0, message = "Trainings summary duration cannot be negative")
    @org.springframework.data.mongodb.core.mapping.Field("trainingsSummaryDuration")
    private Integer durationMinutes = 0;

    /** Applies one validated session change, preserving a non-negative monthly total. */
    public void applyChange(tech.provokedynamic.gymcrmcommon.event.WorkloadEvent.ActionType action, int minutes) {
        if (minutes <= 0) {
            throw new IllegalArgumentException("Training duration must be positive");
        }
        durationMinutes = switch (action) {
            case ADD -> Math.addExact(durationMinutes, minutes);
            case DELETE -> Math.max(0, durationMinutes - minutes);
        };
    }
}
