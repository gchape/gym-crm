// tech/provokedynamic/gymcrmworkload/listener/WorkloadEventListener.java
package tech.provokedynamic.gymcrmworkload.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tech.provokedynamic.gymcrmcommon.event.WorkloadEvent;
import tech.provokedynamic.gymcrmcommon.event.WorkloadTopics;
import tech.provokedynamic.gymcrmworkload.service.WorkloadService;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class WorkloadEventListener {

    private static final String TRANSACTION_ID_HEADER = "transactionId";
    private static final String MDC_KEY = "transactionId";

    private final WorkloadService workloadService;

    @KafkaListener(topics = WorkloadTopics.TRAINER_WORKLOAD_EVENTS,
            groupId = "${spring.kafka.consumer.group-id}")
    public void onWorkloadEvent(ConsumerRecord<String, WorkloadEvent> record) {
        String transactionId = extractTransactionId(record);
        MDC.put(MDC_KEY, transactionId);

        try {
            WorkloadEvent event = record.value();

            // Transaction-level log: receipt of the message off the topic.
            log.info("[txId={}] Received workload event - key=[{}], trainer=[{}], action=[{}], partition=[{}], offset=[{}]",
                    transactionId, record.key(), event.trainerUsername(), event.actionType(),
                    record.partition(), record.offset());

            workloadService.processWorkload(event, transactionId);
        } catch (Exception ex) {
            log.error("[txId={}] Failed to process workload event - trainer=[{}]: {}",
                    transactionId, record.value() != null ? record.value().trainerUsername() : "unknown",
                    ex.getMessage(), ex);
        } finally {
            MDC.remove(MDC_KEY);
        }
    }

    private String extractTransactionId(ConsumerRecord<String, WorkloadEvent> record) {
        Header header = record.headers().lastHeader(TRANSACTION_ID_HEADER);
        if (header == null) {
            return UUID.randomUUID().toString();
        }
        return new String(header.value(), StandardCharsets.UTF_8);
    }
}
