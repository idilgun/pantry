// IngredientRepository.java
package com.localbanana.pantry.domain.repository;

import com.localbanana.pantry.domain.entity.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface IngredientRepository extends JpaRepository<Ingredient, Long> {
    List<Ingredient> findByHouseholdId(Long householdId);

    @Query(value = """
            SELECT * FROM ingredient
            WHERE household_id = :householdId
            AND (
                name ILIKE '%' || :query || '%'
                OR similarity(name, :query) > 0.3
            )
            ORDER BY similarity(name, :query) DESC
            """, nativeQuery = true)
    List<Ingredient> searchByNameFuzzy(@Param("householdId") Long householdId,
                                       @Param("query") String query);
}
