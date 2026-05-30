package com.hms.analytics_service.repository;

import com.hms.analytics_service.model.AnalyticsEvent;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AnalyticsEventRepository
    extends JpaRepository<AnalyticsEvent, UUID>, JpaSpecificationExecutor<AnalyticsEvent> {
  boolean existsByEventId(UUID eventId);
}
