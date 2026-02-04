package com.codewithpcodes.oikon.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaUserUpdateTopicConfig {

    @Bean
    public NewTopic userUpdateTopic() {
        return TopicBuilder
                .name("user.updates")
                .partitions(3)
                .replicas(1)
                .build();
    }
}
