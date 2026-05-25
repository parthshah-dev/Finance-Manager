package com.example.moneymanager.repository;

import com.example.moneymanager.entity.Category;
import com.example.moneymanager.enums.Type;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByProfileId(Long profileId);

    Optional<Category> findByIdAndProfileId(Long id, Long profileId);

    List<Category> findByTypeAndProfileId(Type type, Long profileId);

    Boolean existsByNameAndProfileId(String name, Long profileId);
}
