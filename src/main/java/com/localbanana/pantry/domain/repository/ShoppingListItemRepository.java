// ShoppingListItemRepository.java
package com.localbanana.pantry.domain.repository;

import com.localbanana.pantry.domain.entity.ShoppingListItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ShoppingListItemRepository extends JpaRepository<ShoppingListItem, Long> {
    List<ShoppingListItem> findByHouseholdIdAndStatus(Long householdId, String status);
    List<ShoppingListItem> findByHouseholdIdAndStatusIn(Long householdId, List<String> statuses);
}