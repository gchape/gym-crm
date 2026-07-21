// src/main/java/tech/provokedynamic/gymcrm/client/WorkloadEventPublisher.java
package tech.provokedynamic.gymcrm.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import tech.provokedynamic.gymcrmcommon.event.WorkloadEvent;
import tech.provokedynamic.gymcrmcommon.event.WorkloadTopics;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class WorkloadEventPublisher {

    public static final String TRANSACTION_ID_HEADER = "transactionId";

    private final KafkaTemplate<String, WorkloadEvent> kafkaTemplate;

    public void publish(WorkloadEvent event) {
        String transactionId = resolveTransactionId();

        var message = MessageBuilder.withPayload(event)
                .setHeader(KafkaHeaders.TOPIC, WorkloadTopics.TRAINER_WORKLOAD_EVENTS)
                .setHeader(KafkaHeaders.KEY, event.trainerUsername())
                .setHeader(TRANSACTION_ID_HEADER, transactionId.getBytes(StandardCharsets.UTF_8))
                .build();

        kafkaTemplate.send(message)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("[txId={}] Failed to publish workload event - trainer=[{}], action=[{}]: {}",
                                transactionId, event.trainerUsername(), event.actionType(), ex.getMessage(), ex);
                    } else {
                        var metadata = result.getRecordMetadata();
                        log.info("[txId={}] Published workload event - trainer=[{}], action=[{}], partition=[{}], offset=[{}]",
                                transactionId, event.trainerUsername(), event.actionType(),
                                metadata.partition(), metadata.offset());
                    }
                });
    }

    private String resolveTransactionId() {
        String existing = MDC.get(TRANSACTION_ID_HEADER);
        return existing != null ? existing : UUID.randomUUID().toString();
    }
}
