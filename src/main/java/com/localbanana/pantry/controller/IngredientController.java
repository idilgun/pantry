package com.localbanana.pantry.controller;

import com.localbanana.pantry.domain.entity.Ingredient;
import com.localbanana.pantry.dto.IngredientDto;
import com.localbanana.pantry.service.IngredientService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/households/{householdId}/ingredients")
public class IngredientController {

    private final IngredientService ingredientService;

    public IngredientController(IngredientService ingredientService) {
        this.ingredientService = ingredientService;
    }

    @GetMapping
    public List<IngredientDto> getIngredients(@PathVariable Long householdId) {
        return ingredientService.getIngredientsByHousehold(householdId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @GetMapping("/{id}")
    public IngredientDto getIngredient(@PathVariable Long householdId,
                                       @PathVariable Long id) {
        return toDto(ingredientService.getIngredientById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public IngredientDto addIngredient(@PathVariable Long householdId,
                                       @RequestBody AddIngredientRequest request) {
        return toDto(ingredientService.addIngredient(
                householdId,
                request.categoryId(),
                request.name(),
                request.trackingType(),
                request.location(),
                request.quantity(),
                request.canonicalUnit(),
                request.customShelfLifeDays()
        ));
    }

    @PatchMapping("/{id}/quantity")
    public IngredientDto updateQuantity(@PathVariable Long householdId,
                                        @PathVariable Long id,
                                        @RequestBody UpdateQuantityRequest request) {
        return toDto(ingredientService.updateQuantity(id, request.quantity()));
    }

    @PatchMapping("/{id}/mark-as-out")
    public IngredientDto markAsOut(@PathVariable Long householdId,
                                   @PathVariable Long id) {
        return toDto(ingredientService.markAsOut(id));
    }

    private IngredientDto toDto(Ingredient ingredient) {
        return new IngredientDto(
                ingredient.getId(),
                ingredient.getName(),
                ingredient.getTrackingType(),
                ingredient.getLocation(),
                ingredient.getQuantity(),
                ingredient.getCanonicalUnit(),
                ingredient.getCategory().getName(),
                ingredient.getCustomShelfLifeDays(),
                ingredient.getIsAvailable(),
                ingredient.getIsFrozen(),
                ingredient.getAddedAt()
        );
    }

    public record AddIngredientRequest(
            Long categoryId,
            String name,
            String trackingType,
            String location,
            BigDecimal quantity,
            String canonicalUnit,
            Integer customShelfLifeDays
    ) {}

    public record UpdateQuantityRequest(BigDecimal quantity) {}
}