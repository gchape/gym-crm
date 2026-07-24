package tech.provokedynamic.gymcrmworkload.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tech.provokedynamic.gymcrmcommon.event.WorkloadEvent;
import tech.provokedynamic.gymcrmcommon.event.WorkloadTopics;
import tech.provokedynamic.gymcrmworkload.service.WorkloadService;

@Slf4j
@Component
@RequiredArgsConstructor
public class WorkloadEventListener {

    private final WorkloadService workloadService;

    @KafkaListener(topics = WorkloadTopics.TRAINER_WORKLOAD_EVENTS,
            groupId = "${spring.kafka.consumer.group-id}")
    public void onWorkloadEvent(ConsumerRecord<String, WorkloadEvent> record) {
        WorkloadEvent event = record.value();
        log.info("Received workload event - key=[{}], trainer=[{}], action=[{}], partition=[{}], offset=[{}]",
                record.key(), event.trainerUsername(), event.actionType(), record.partition(), record.offset());

        try {
            workloadService.processWorkload(event);
        } catch (Exception ex) {
            log.error("Failed to process workload event - trainer=[{}], action=[{}]: {}",
                    event.trainerUsername(), event.actionType(), ex.getMessage(), ex);
        }
    }
}
