package com.localbanana.pantry.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record IngredientDto(
        Long id,
        String name,
        String trackingType,
        String location,
        BigDecimal quantity,
        String canonicalUnit,
        String categoryName,
        Integer customShelfLifeDays,
        Boolean isAvailable,
        Boolean isFrozen,
        LocalDateTime addedAt
) {}