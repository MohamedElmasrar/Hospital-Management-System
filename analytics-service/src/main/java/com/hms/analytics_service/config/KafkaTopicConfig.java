package com.hms.analytics_service.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;

@Configuration
@EnableKafka
public class KafkaTopicConfig {

  @Bean
  NewTopic analyticsTopic(@Value("${analytics.kafka.topic}") String topicName) {
    return new NewTopic(topicName, 3, (short) 1);
  }
}
