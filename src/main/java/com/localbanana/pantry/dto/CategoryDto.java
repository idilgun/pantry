package com.localbanana.pantry.dto;

public record CategoryDto(
        Long id,
        String name,
        Integer defaultShelfLifeDays
) {}