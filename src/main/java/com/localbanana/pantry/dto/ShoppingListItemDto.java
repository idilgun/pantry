package com.localbanana.pantry.dto;

import java.time.LocalDateTime;

public record ShoppingListItemDto(
        Long id,
        Long ingredientId,
        String ingredientName,
        String trackingType,
        Long addedById,
        String reason,
        String status,
        LocalDateTime addedAt,
        LocalDateTime boughtAt
) {}
