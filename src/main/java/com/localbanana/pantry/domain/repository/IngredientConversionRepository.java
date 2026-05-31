// IngredientConversionRepository.java
package com.localbanana.pantry.domain.repository;

import com.localbanana.pantry.domain.entity.IngredientConversion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface IngredientConversionRepository extends JpaRepository<IngredientConversion, Long> {
    List<IngredientConversion> findByIngredientId(Long ingredientId);
    Optional<IngredientConversion> findByIngredientIdAndFromUnit(Long ingredientId, String fromUnit);
}