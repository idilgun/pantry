package com.localbanana.pantry.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Entity
@Table(name = "cooking_log_item")
@Getter
@Setter
public class CookingLogItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cooking_log_id", nullable = false)
    private CookingLog cookingLog;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ingredient_id", nullable = false)
    private Ingredient ingredient;

    private BigDecimal actualQuantity;

    private String actualUnit;

    @Column(nullable = false)
    private String trackingTypeAtTime;
}