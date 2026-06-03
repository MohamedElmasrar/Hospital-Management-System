package com.hms.notification_service.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;

@Configuration
public class KafkaTopicConfig {
  @Bean
  NewTopic notificationTopic(@Value("${notification.kafka.topic}") String name) {
    return new NewTopic(name, 3, (short) 1);
  }
}
