package com.codewithpcodes.oikon.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class kafkaUserCreationTopicConfig {

    @Bean
    public NewTopic userCreationTopic() {
        return TopicBuilder
                .name("user.creation")
                .partitions(3)
                .replicas(1)
                .build();
    }
}
