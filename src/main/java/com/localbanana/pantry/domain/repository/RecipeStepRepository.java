// RecipeStepRepository.java
package com.localbanana.pantry.domain.repository;

import com.localbanana.pantry.domain.entity.RecipeStep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RecipeStepRepository extends JpaRepository<RecipeStep, Long> {
    List<RecipeStep> findByRecipeIdOrderByStepNumber(Long recipeId);
}