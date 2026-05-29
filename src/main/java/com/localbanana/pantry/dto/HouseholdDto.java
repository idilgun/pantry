package com.localbanana.pantry.dto;

import java.time.LocalDateTime;

public record HouseholdDto(
        Long id,
        String name,
        LocalDateTime createdAt
) {}