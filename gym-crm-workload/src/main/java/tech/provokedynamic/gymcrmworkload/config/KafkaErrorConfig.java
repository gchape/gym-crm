package tech.provokedynamic.gymcrmworkload.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.TopicPartition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;
import tech.provokedynamic.gymcrmcommon.event.WorkloadTopics;
import tech.provokedynamic.gymcrmworkload.exception.InvalidWorkloadEventException;

@Configuration(proxyBeanMethods = false)
public class KafkaErrorConfig {
    @Bean
    DefaultErrorHandler workloadErrorHandler(KafkaTemplate<Object, Object> template) {
        var recoverer = new DeadLetterPublishingRecoverer(template, (record, _) ->
                new TopicPartition(WorkloadTopics.TRAINER_WORKLOAD_DEAD_LETTER, record.partition()));
        recoverer.setFailIfSendResultIsError(true);
        var handler = new DefaultErrorHandler(recoverer, new FixedBackOff(1000L, 3L));
        handler.addNotRetryableExceptions(InvalidWorkloadEventException.class);
        return handler;
    }

    @Bean
    NewTopic workloadDeadLetterTopic() {
        return TopicBuilder.name(WorkloadTopics.TRAINER_WORKLOAD_DEAD_LETTER)
                .partitions(1).replicas(1).build();
    }
}
