package com.example.moneymanager.dto;

import com.example.moneymanager.enums.Type;
import lombok.Data;

import java.time.LocalDate;

@Data
public class FilterDto {

    private Type type;
    private LocalDate startDate;
    private LocalDate endDate;
    private String key;
    private String sortField;
    private String sortOrder;
}
