package com.codewithpcodes.oikon.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaMfaTopicConfig {

    @Bean
    public NewTopic mfaTopic() {
        return TopicBuilder
                .name("otp-notification-topic")
                .build();
    }
}
