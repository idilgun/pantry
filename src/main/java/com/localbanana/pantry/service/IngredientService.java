package com.localbanana.pantry.service;

import com.localbanana.pantry.domain.entity.Category;
import com.localbanana.pantry.domain.entity.Household;
import com.localbanana.pantry.domain.entity.Ingredient;
import com.localbanana.pantry.domain.repository.CategoryRepository;
import com.localbanana.pantry.domain.repository.HouseholdRepository;
import com.localbanana.pantry.domain.repository.IngredientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class IngredientService {

    private final IngredientRepository ingredientRepository;
    private final HouseholdRepository householdRepository;
    private final CategoryRepository categoryRepository;

    public IngredientService(IngredientRepository ingredientRepository,
                             HouseholdRepository householdRepository,
                             CategoryRepository categoryRepository) {
        this.ingredientRepository = ingredientRepository;
        this.householdRepository = householdRepository;
        this.categoryRepository = categoryRepository;
    }

    public Ingredient addIngredient(Long householdId,
                                    Long categoryId,
                                    String name,
                                    String trackingType,
                                    String location,
                                    BigDecimal quantity,
                                    String canonicalUnit,
                                    Integer customShelfLifeDays) {

        Household household = householdRepository.findById(householdId)
                .orElseThrow(() -> new IllegalArgumentException("Household not found: " + householdId));

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found: " + categoryId));

        Ingredient ingredient = new Ingredient();
        ingredient.setHousehold(household);
        ingredient.setCategory(category);
        ingredient.setName(name);
        ingredient.setTrackingType(trackingType);
        ingredient.setLocation(location);
        ingredient.setQuantity(quantity);
        ingredient.setCanonicalUnit(canonicalUnit);
        ingredient.setCustomShelfLifeDays(customShelfLifeDays);
        ingredient.setIsAvailable(true);
        ingredient.setIsFrozen(false);
        ingredient.setAddedAt(LocalDateTime.now());

        return ingredientRepository.save(ingredient);
    }

    @Transactional(readOnly = true)
    public List<Ingredient> getIngredientsByHousehold(Long householdId) {
        return ingredientRepository.findByHouseholdId(householdId);
    }

    @Transactional(readOnly = true)
    public Ingredient getIngredientById(Long id) {
        return ingredientRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ingredient not found: " + id));
    }

    public Ingredient updateQuantity(Long ingredientId, BigDecimal newQuantity) {
        Ingredient ingredient = getIngredientById(ingredientId);
        ingredient.setQuantity(newQuantity);
        return ingredientRepository.save(ingredient);
    }

    @Transactional(readOnly = true)
    public List<Ingredient> searchByName(Long householdId, String query) {
        return ingredientRepository.searchByNameFuzzy(householdId, query);
    }

    public Ingredient markAsOut(Long ingredientId) {
        Ingredient ingredient = getIngredientById(ingredientId);
        ingredient.setIsAvailable(false);
        if (ingredient.getTrackingType().equals("stocked")) {
            ingredient.setQuantity(null);
        } else {
            ingredient.setQuantity(BigDecimal.ZERO);
        }
        return ingredientRepository.save(ingredient);
    }
}