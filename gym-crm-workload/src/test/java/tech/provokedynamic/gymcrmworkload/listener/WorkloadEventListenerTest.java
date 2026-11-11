package tech.provokedynamic.gymcrmworkload.listener;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import tech.provokedynamic.gymcrmcommon.event.WorkloadEvent;
import tech.provokedynamic.gymcrmworkload.service.TrainerWorkloadService;
import java.time.LocalDate;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

class WorkloadEventListenerTest {
    @Test
    void processingFailuresReachTheContainerErrorHandlerAndClearMdc() {
        var service = mock(TrainerWorkloadService.class);
        var failure = new IllegalStateException("Mongo temporarily unavailable");
        doThrow(failure).when(service).applyEvent(any(), anyString());
        var event = new WorkloadEvent("Dana.Ford", "Dana", "Ford", true, LocalDate.now(), 45, WorkloadEvent.ActionType.ADD);
        var record = new ConsumerRecord<String, WorkloadEvent>("trainer-workload-events", 0, 1, "Dana.Ford", event);
        assertThatThrownBy(() -> new WorkloadEventListener(service).onWorkloadEvent(record)).isSameAs(failure);
        assertThat(MDC.get("transactionId")).isNull();
    }
}
