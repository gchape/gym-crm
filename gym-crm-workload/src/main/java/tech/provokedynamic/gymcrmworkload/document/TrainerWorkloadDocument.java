package tech.provokedynamic.gymcrmworkload.document;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.ArrayList;
import java.util.List;

/**
 * One document per trainer. "years" holds nested year -> months -> duration
 * summaries, matching the required schema.
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@Document(collection = "trainer_workload")
@CompoundIndexes({

        @CompoundIndex(name = "trainer_name_idx", def = "{'trainerFirstName': 1, 'trainerLastName': 1}")
})
public class TrainerWorkloadDocument {

    @Id
    private String id;

    @NotBlank(message = "Trainer username is required")
    @Indexed(unique = true, name = "trainer_username_idx")
    @Field("trainerUsername")
    private String trainerUsername;

    @NotBlank(message = "Trainer first name is required")
    @Field("trainerFirstName")
    private String trainerFirstName;

    @NotBlank(message = "Trainer last name is required")
    @Field("trainerLastName")
    private String trainerLastName;

    @NotNull(message = "Trainer status is required")
    @Field("trainerStatus")
    private Boolean trainerStatus;

    @Valid
    @Field("years")
    private List<YearlyWorkload> years = new ArrayList<>();

    @Version
    private Long version;

    public TrainerWorkloadDocument(String trainerUsername, String trainerFirstName,
                                   String trainerLastName, Boolean trainerStatus) {
        this.trainerUsername = trainerUsername;
        this.trainerFirstName = trainerFirstName;
        this.trainerLastName = trainerLastName;
        this.trainerStatus = trainerStatus;
    }

    /** Updates trainer metadata and delegates a session change to its calendar month. */
    public void applyEvent(tech.provokedynamic.gymcrmcommon.event.WorkloadEvent event) {
        if (!trainerUsername.equals(event.trainerUsername())) {
            throw new IllegalArgumentException("A workload event must belong to this trainer");
        }
        trainerFirstName = event.trainerFirstName();
        trainerLastName = event.trainerLastName();
        trainerStatus = event.isActive();
        int year = event.trainingDate().getYear();
        int month = event.trainingDate().getMonthValue();
        var yearly = years.stream().filter(value -> value.getYear() == year).findFirst().orElseGet(() -> {
            var created = new YearlyWorkload(year, new ArrayList<>());
            years.add(created);
            return created;
        });
        var monthly = yearly.getMonths().stream().filter(value -> value.getMonth() == month).findFirst().orElseGet(() -> {
            var created = new MonthlyWorkload(month, 0);
            yearly.getMonths().add(created);
            return created;
        });
        monthly.applyChange(event.actionType(), event.trainingDuration());
    }
}
