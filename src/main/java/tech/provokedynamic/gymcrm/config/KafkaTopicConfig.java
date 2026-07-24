package tech.provokedynamic.gymcrm.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import tech.provokedynamic.gymcrmcommon.event.WorkloadTopics;

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
