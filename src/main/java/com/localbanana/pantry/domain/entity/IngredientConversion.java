package com.localbanana.pantry.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Entity
@Table(name = "ingredient_conversion")
@Getter
@Setter
public class IngredientConversion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ingredient_id", nullable = false)
    private Ingredient ingredient;

    @Column(nullable = false)
    private String fromUnit;

    @Column(nullable = false)
    private BigDecimal valueInCanonicalUnit;
}