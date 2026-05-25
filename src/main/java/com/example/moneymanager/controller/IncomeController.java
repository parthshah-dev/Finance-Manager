package com.example.moneymanager.controller;

import com.example.moneymanager.dto.ExpenseDto;
import com.example.moneymanager.dto.IncomeDto;
import com.example.moneymanager.entity.Income;
import com.example.moneymanager.service.IncomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/incomes")
@RequiredArgsConstructor
public class IncomeController {

    private final IncomeService incomeService;

    @PostMapping
    public ResponseEntity<IncomeDto> addIncome(@RequestBody IncomeDto incomeDto){
        IncomeDto savedIncome = incomeService.saveIncome(incomeDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedIncome);
    }

    @GetMapping
    public ResponseEntity<List<IncomeDto>> getIncomes(){
        return ResponseEntity.ok(incomeService.getCurrentMonthIncomeOfCurrentUser());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteIncome(@PathVariable Long id){
        incomeService.deletedIncomeById(id);
        return ResponseEntity.noContent().build();
    }
}
