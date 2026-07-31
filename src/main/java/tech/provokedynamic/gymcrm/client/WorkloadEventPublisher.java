package tech.provokedynamic.gymcrm.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import tech.provokedynamic.gymcrmcommon.event.WorkloadEvent;
import tech.provokedynamic.gymcrmcommon.event.WorkloadTopics;

import java.nio.charset.StandardCharsets;
import java.util.List;
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

    /**
     * Registers a single event to be published only after the current
     * transaction commits. No-op (with a debug log) if there's no active
     * transaction synchronization — e.g. a plain Mockito unit test calling
     * the owning service method directly, outside a Spring transaction proxy.
     * Publishing before commit would leak an uncommitted change's workload.
     */
    public void publishAfterCommit(WorkloadEvent event) {
        publishAfterCommit(List.of(event));
    }

    /**
     * Batched variant of {@link #publishAfterCommit(WorkloadEvent)} for
     * callers that need to notify on multiple events from one transaction
     * (e.g. cascading deletes).
     */
    public void publishAfterCommit(List<WorkloadEvent> events) {
        if (events.isEmpty()) {
            return;
        }

        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            log.debug("No active transaction synchronization; skipping after-commit publish for {} event(s)",
                    events.size());
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                events.forEach(WorkloadEventPublisher.this::publish);
            }
        });
    }

    private String resolveTransactionId() {
        String existing = MDC.get(TRANSACTION_ID_HEADER);
        return existing != null ? existing : UUID.randomUUID().toString();
    }
}
