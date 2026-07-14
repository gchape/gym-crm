package tech.provokedynamic.gymcrmworkload.model;

import lombok.Getter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class YearSummary {

    private final int year;
    private final Map<Integer, MonthSummary> months = new ConcurrentHashMap<>();

    public YearSummary(int year) {
        this.year = year;
    }

    public MonthSummary getOrCreateMonth(int month) {
        return months.computeIfAbsent(month, MonthSummary::new);
    }
}
