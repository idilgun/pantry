// CookingLogRepository.java
package com.localbanana.pantry.domain.repository;

import com.localbanana.pantry.domain.entity.CookingLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CookingLogRepository extends JpaRepository<CookingLog, Long> {
    List<CookingLog> findByHouseholdIdOrderByCookedAtDesc(Long householdId);
}