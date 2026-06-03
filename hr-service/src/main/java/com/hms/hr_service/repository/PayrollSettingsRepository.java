package com.hms.hr_service.repository;

import com.hms.hr_service.model.PayrollSettings;
import java.util.*;
import org.springframework.data.jpa.repository.*;

public interface PayrollSettingsRepository
    extends JpaRepository<PayrollSettings, UUID>, JpaSpecificationExecutor<PayrollSettings> {}
