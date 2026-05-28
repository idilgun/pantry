// CategoryRepository.java
package com.localbanana.pantry.domain.repository;

import com.localbanana.pantry.domain.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
}