package com.example.moneymanager.service;

import com.example.moneymanager.dto.ExpenseDto;
import com.example.moneymanager.dto.IncomeDto;
import com.example.moneymanager.entity.Category;
import com.example.moneymanager.entity.Expense;
import com.example.moneymanager.entity.Income;
import com.example.moneymanager.entity.Profile;
import com.example.moneymanager.repository.CategoryRepository;
import com.example.moneymanager.repository.ExpenseRepository;
import com.example.moneymanager.repository.IncomeRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final CategoryRepository categoryRepository;
    private final ProfileService profileService;
    private final ModelMapper modelMapper;

    public ExpenseDto saveExpense(ExpenseDto expenseDto){
        Profile currentProfile = profileService.getCurrentProfile();
        Category category = categoryRepository.findById(expenseDto.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + expenseDto.getCategoryId()));

        Expense newExpense = modelMapper.map(expenseDto, Expense.class);
        newExpense.setProfile(currentProfile);
        newExpense.setCategory(category);
        newExpense = expenseRepository.save(newExpense);

        return modelMapper.map(newExpense, ExpenseDto.class);

    }

    public List<ExpenseDto> getCurrentMonthExpenseOfCurrentUser() {
        Profile currentProfile = profileService.getCurrentProfile();
        LocalDate now = LocalDate.now();
        LocalDate startDate = now.withDayOfMonth(1);
        LocalDate endDate = now.withDayOfMonth(now.lengthOfMonth());
        List<Expense> expenses = expenseRepository.findByProfileIdAndDateBetween(currentProfile.getId(), startDate, endDate);
        return expenses.stream()
                .map(expense -> modelMapper.map(expense, ExpenseDto.class))
                .collect(Collectors.toList());
    }

    public void deletedExpenseById(Long id){
        Profile profile = profileService.getCurrentProfile();
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Expense not found with id: " + id));

        if(!expense.getProfile().getId().equals(profile.getId())){
            throw new RuntimeException("You are not authorized to delete this expense");
        }
        expenseRepository.delete(expense);
    }

    public List<ExpenseDto> getLatestFiveExpenses(){
        Profile profile = profileService.getCurrentProfile();

        List<Expense> expenses = expenseRepository.findTop5ByProfileIdOrderByDateDesc(profile.getId());

        return expenses.stream().map((element) -> modelMapper.map(element, ExpenseDto.class))
                .collect(Collectors.toList());

    }

    public BigDecimal getTotalExpenses(){
        Profile profile = profileService.getCurrentProfile();
        BigDecimal total = expenseRepository.findTotalExpenseByProfileId(profile.getId());

        return total != null ? total : BigDecimal.ZERO;
    }
}
