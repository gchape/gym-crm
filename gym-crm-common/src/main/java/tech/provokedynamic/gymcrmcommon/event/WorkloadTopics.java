package tech.provokedynamic.gymcrmcommon.event;

public final class WorkloadTopics {
    public static final String TRAINER_WORKLOAD_EVENTS = "trainer-workload-events";

    public static final String TRAINER_WORKLOAD_DEAD_LETTER = TRAINER_WORKLOAD_EVENTS + ".DLT";

    private WorkloadTopics() {
    }
}
