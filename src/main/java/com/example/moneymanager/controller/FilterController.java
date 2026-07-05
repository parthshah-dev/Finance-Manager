package com.example.moneymanager.controller;

import com.example.moneymanager.dto.ExpenseDto;
import com.example.moneymanager.dto.FilterDto;
import com.example.moneymanager.dto.IncomeDto;
import com.example.moneymanager.service.ExpenseService;
import com.example.moneymanager.service.IncomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

@RestController
@RequiredArgsConstructor
@RequestMapping("/filter")
public class FilterController {

    private final ExpenseService expenseService;
    private final IncomeService incomeService;

    @PostMapping
    public ResponseEntity<?> filterTransactions(@RequestBody FilterDto filterDto){
        LocalDate startDate = filterDto.getStartDate() != null ? filterDto.getStartDate() : LocalDate.MIN;
        LocalDate endDate = filterDto.getEndDate() != null ? filterDto.getEndDate() : LocalDate.now();
        String key = filterDto.getKey() != null ? filterDto.getKey() : "";
        String sortField = filterDto.getSortField() != null ? filterDto.getSortField() : "date";
        Sort.Direction sortOrder = "desc".equalsIgnoreCase(filterDto.getSortOrder()) ? Sort.Direction.DESC : Sort.Direction.ASC;

        if("income".equalsIgnoreCase(filterDto.getType().toString())){
            List<IncomeDto> incomes = incomeService.filterIncomes(startDate, endDate, key, Sort.by(sortOrder, sortField));
            return ResponseEntity.ok(incomes);
        }else if("expense".equalsIgnoreCase(filterDto.getType().toString())){
            List<ExpenseDto> expenses = expenseService.filterExpenses(startDate, endDate, key, Sort.by(sortOrder, sortField));
            return ResponseEntity.ok(expenses);
        }else{
            return ResponseEntity.badRequest().body("Invalid type. Must be 'income' or 'expense'.");
        }
    }

}
