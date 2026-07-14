package tech.provokedynamic.gymcrmworkload.model;

import lombok.Getter;

import java.util.concurrent.atomic.AtomicInteger;

public class MonthSummary {

    @Getter
    private final int month;

    private final AtomicInteger trainingSummaryDuration;

    public MonthSummary(int month) {
        this.month = month;
        this.trainingSummaryDuration = new AtomicInteger(0);
    }

    public int getTrainingSummaryDuration() {
        return trainingSummaryDuration.get();
    }

    public void addDuration(int duration) {
        trainingSummaryDuration.addAndGet(duration);
    }

    public void subtractDuration(int duration) {
        trainingSummaryDuration.updateAndGet(current -> Math.max(0, current - duration));
    }
}
