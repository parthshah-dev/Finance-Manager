package com.example.moneymanager.controller;

import com.example.moneymanager.dto.CategoryDto;
import com.example.moneymanager.enums.Type;
import com.example.moneymanager.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    public ResponseEntity<CategoryDto> saveCategory(@RequestBody CategoryDto categoryDto){
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.saveCategory(categoryDto));
    }

    @GetMapping
    public ResponseEntity<List<CategoryDto>> getCategories(){
        List<CategoryDto> categories = categoryService.getCategoryForCurrentUser();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/{type}")
    public ResponseEntity<List<CategoryDto>> getCategoriesByType(@PathVariable Type type){
        List<CategoryDto> categories = categoryService.getCategoriesByTypeForCurrentUser(type);
        return ResponseEntity.ok(categories);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryDto> updateCategory(@PathVariable Long id, @RequestBody CategoryDto categoryDto){
        CategoryDto updatedCategory = categoryService.updateCategory(id, categoryDto);
        return ResponseEntity.ok(updatedCategory);
    }
}
