package tech.provokedynamic.gymcrmworkload.document;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "month")
public class MonthSummary {

    @NotNull(message = "Month is required")
    @Min(value = 1, message = "Month must be between 1 and 12")
    @Max(value = 12, message = "Month must be between 1 and 12")
    private Integer month;

    @NotNull(message = "Trainings summary duration is required")
    @Min(value = 0, message = "Trainings summary duration cannot be negative")
    private Integer trainingsSummaryDuration = 0;
}
