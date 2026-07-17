package tech.provokedynamic.gymcrmworkload.model;

import lombok.Getter;
import lombok.Setter;

import java.time.YearMonth;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Getter
public class TrainerWorkloadSummary {

    private final String trainerUsername;
    private final Map<YearMonth, AtomicInteger> monthlyDurations = new ConcurrentHashMap<>();

    @Setter
    private volatile String trainerFirstName;
    @Setter
    private volatile String trainerLastName;
    @Setter
    private volatile boolean trainerStatus;

    public TrainerWorkloadSummary(String trainerUsername, String trainerFirstName,
                                  String trainerLastName, boolean trainerStatus) {
        this.trainerUsername = trainerUsername;
        this.trainerFirstName = trainerFirstName;
        this.trainerLastName = trainerLastName;
        this.trainerStatus = trainerStatus;
    }

    public void addDuration(YearMonth month, int duration) {
        monthlyDurations.computeIfAbsent(month, k -> new AtomicInteger())
                .addAndGet(duration);
    }

    public void subtractDuration(YearMonth month, int duration) {
        monthlyDurations.computeIfAbsent(month, k -> new AtomicInteger())
                .updateAndGet(current -> Math.max(0, current - duration));
    }
}
