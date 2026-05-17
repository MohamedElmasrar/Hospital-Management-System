package com.hms.reception_service.repository;

import com.hms.reception_service.model.DiagnosticReservationDetail;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiagnosticReservationDetailRepository
    extends JpaRepository<DiagnosticReservationDetail, UUID> {}
