// IngredientRepository.java
package com.localbanana.pantry.domain.repository;

import com.localbanana.pantry.domain.entity.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface IngredientRepository extends JpaRepository<Ingredient, Long> {
    List<Ingredient> findByHouseholdId(Long householdId);
}