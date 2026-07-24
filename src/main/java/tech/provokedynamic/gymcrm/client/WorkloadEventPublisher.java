package tech.provokedynamic.gymcrm.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import tech.provokedynamic.gymcrmcommon.event.WorkloadEvent;
import tech.provokedynamic.gymcrmcommon.event.WorkloadTopics;

@Slf4j
@Component
@RequiredArgsConstructor
public class WorkloadEventPublisher {

    private final KafkaTemplate<String, WorkloadEvent> kafkaTemplate;

    public void publish(WorkloadEvent event) {
        kafkaTemplate.send(WorkloadTopics.TRAINER_WORKLOAD_EVENTS, event.trainerUsername(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish workload event - trainer=[{}], action=[{}]: {}",
                                event.trainerUsername(), event.actionType(), ex.getMessage(), ex);
                    } else {
                        var metadata = result.getRecordMetadata();
                        log.info("Published workload event - trainer=[{}], action=[{}], partition=[{}], offset=[{}]",
                                event.trainerUsername(), event.actionType(),
                                metadata.partition(), metadata.offset());
                    }
                });
    }
}
