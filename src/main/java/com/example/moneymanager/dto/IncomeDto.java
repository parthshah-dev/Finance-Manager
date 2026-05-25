package com.example.moneymanager.dto;

import com.example.moneymanager.entity.Category;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class IncomeDto {

    private Long id;
    private String name;
    private String icon;
    private BigDecimal amount;
    private String categoryName;
    private Long categoryId;
    private LocalDate date;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
