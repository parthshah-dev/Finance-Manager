package com.example.moneymanager.controller;

import com.example.moneymanager.dto.ExpenseDto;
import com.example.moneymanager.service.ExpenseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    @PostMapping
    public ResponseEntity<ExpenseDto> addExpense(@RequestBody ExpenseDto expenseDto){
        ExpenseDto savedExpense = expenseService.saveExpense(expenseDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedExpense);
    }

    @GetMapping
    public ResponseEntity<List<ExpenseDto>> getExpenses(){
        return ResponseEntity.ok(expenseService.getCurrentMonthExpenseOfCurrentUser());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExpense(@PathVariable Long id){
        expenseService.deletedExpenseById(id);
        return ResponseEntity.noContent().build();
    }

}
