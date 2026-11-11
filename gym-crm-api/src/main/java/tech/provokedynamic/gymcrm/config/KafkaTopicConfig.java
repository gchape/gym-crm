package tech.provokedynamic.gymcrm.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import tech.provokedynamic.gymcrmcommon.event.WorkloadTopics;

@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(name = "app.workload.events-enabled", havingValue = "true", matchIfMissing = true)
@Configuration(proxyBeanMethods = false)
public class KafkaTopicConfig {

    @Bean
    public NewTopic workloadEventsTopic() {
        return TopicBuilder.name(WorkloadTopics.TRAINER_WORKLOAD_EVENTS)
                .partitions(1)
                .replicas(1)
                .build();
    }
}
