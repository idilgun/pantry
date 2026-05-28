// CookingLogItemRepository.java
package com.localbanana.pantry.domain.repository;

import com.localbanana.pantry.domain.entity.CookingLogItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CookingLogItemRepository extends JpaRepository<CookingLogItem, Long> {
    List<CookingLogItem> findByCookingLogId(Long cookingLogId);
}