package com.example.moneymanager.service;

import com.example.moneymanager.dto.CategoryDto;
import com.example.moneymanager.entity.Category;
import com.example.moneymanager.enums.Type;
import com.example.moneymanager.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final ProfileService profileService;
    private final CategoryRepository categoryRepository;
    private final ModelMapper modelMapper;

    public CategoryDto saveCategory(CategoryDto categoryDto){
        if(categoryRepository.existsByNameAndProfileId(categoryDto.getName(), profileService.getCurrentProfile().getId())){
            throw new RuntimeException("Category with the same name already exists");
        }

        Category newCategory = modelMapper.map(categoryDto, Category.class);
        newCategory.setProfile(profileService.getCurrentProfile());
        return modelMapper.map(categoryRepository.save(newCategory), CategoryDto.class);
    }

    public List<CategoryDto> getCategoryForCurrentUser() {
        List<Category> categoryDtoList = categoryRepository.findByProfileId(
                profileService.getCurrentProfile().getId()
        );

        return categoryDtoList.stream()
                .map((element) -> modelMapper.map(element, CategoryDto.class))
                .collect(Collectors.toList());
    }

    public List<CategoryDto> getCategoriesByTypeForCurrentUser(Type type) {
        List<Category> categoryList = categoryRepository.findByTypeAndProfileId(
                type,
                profileService.getCurrentProfile().getId()
        );

        return categoryList.stream().
                map((element) -> modelMapper.map(element, CategoryDto.class))
                .collect(Collectors.toList());
    }

    public CategoryDto updateCategory(Long id, CategoryDto categoryDto){
        Category existingCategory = categoryRepository.findByIdAndProfileId(id, profileService.getCurrentProfile().getId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        existingCategory.setName(categoryDto.getName());
        existingCategory.setType(categoryDto.getType());
        existingCategory.setIcon(categoryDto.getIcon());
        categoryRepository.save(existingCategory);

        return modelMapper.map(existingCategory, CategoryDto.class);

    }

}
