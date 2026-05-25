package com.example.moneymanager.dto;

import com.example.moneymanager.enums.Type;
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
public class RecentTransactionDto {

    private Long id;
    private Long profileId;
    private String name;
    private BigDecimal amount;
    private String icon;
    private LocalDate date;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Type type;
}
