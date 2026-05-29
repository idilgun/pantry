package com.localbanana.pantry.controller;

import com.localbanana.pantry.domain.entity.Category;
import com.localbanana.pantry.dto.CategoryDto;
import com.localbanana.pantry.service.CategoryService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public List<CategoryDto> getAllCategories() {
        return categoryService.getAllCategories()
                .stream()
                .map(this::toDto)
                .toList();
    }

    @GetMapping("/{id}")
    public CategoryDto getCategoryById(@PathVariable Long id) {
        return toDto(categoryService.getCategoryById(id));
    }

    private CategoryDto toDto(Category category) {
        return new CategoryDto(
                category.getId(),
                category.getName(),
                category.getDefaultShelfLifeDays()
        );
    }
}