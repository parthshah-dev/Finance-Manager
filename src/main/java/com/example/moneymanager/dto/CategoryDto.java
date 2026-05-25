package com.example.moneymanager.dto;

import com.example.moneymanager.entity.Profile;
import com.example.moneymanager.enums.Type;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CategoryDto {
    private Long id;
    private String name;
    private Long profileId;
    private String icon;
    private Type type;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
