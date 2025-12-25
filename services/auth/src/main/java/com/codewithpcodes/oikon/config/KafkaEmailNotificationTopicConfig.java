package com.codewithpcodes.oikon.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaEmailNotificationTopicConfig {

    @Bean
    public NewTopic emailNotificationTopic() {
        return TopicBuilder
                .name("notification.emails")
                .partitions(3)
                .replicas(1)
                .build();
    }
}
