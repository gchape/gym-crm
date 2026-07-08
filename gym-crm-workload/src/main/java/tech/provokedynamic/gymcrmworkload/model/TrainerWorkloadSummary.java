package tech.provokedynamic.gymcrmworkload.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class TrainerWorkloadSummary {

    private final String trainerUsername;

    private final Map<Integer, YearSummary> years = new ConcurrentHashMap<>();

    @Setter
    private volatile String trainerFirstName;

    @Setter
    private volatile String trainerLastName;

    @Setter
    private volatile boolean trainerStatus;

    public TrainerWorkloadSummary(String trainerUsername, String trainerFirstName,
                                  String trainerLastName, boolean trainerStatus) {
        this.trainerStatus = trainerStatus;
        this.trainerLastName = trainerLastName;
        this.trainerFirstName = trainerFirstName;
        this.trainerUsername = trainerUsername;
    }

    public YearSummary getOrCreateYear(int year) {
        return years.computeIfAbsent(year, YearSummary::new);
    }
}
