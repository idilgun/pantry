// RecipeRepository.java
package com.localbanana.pantry.domain.repository;

import com.localbanana.pantry.domain.entity.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecipeRepository extends JpaRepository<Recipe, Long> {
}