package tech.provokedynamic.gymcrmworkload.document;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "year")
public class YearSummary {

    @NotNull(message = "Year is required")
    private Integer year;

    @Valid
    private List<MonthSummary> months = new ArrayList<>();
}
