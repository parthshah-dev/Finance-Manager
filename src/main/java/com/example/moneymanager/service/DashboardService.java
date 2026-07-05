package com.example.moneymanager.service;

import com.example.moneymanager.dto.ExpenseDto;
import com.example.moneymanager.dto.IncomeDto;
import com.example.moneymanager.dto.RecentTransactionDto;
import com.example.moneymanager.entity.Income;
import com.example.moneymanager.entity.Profile;
import com.example.moneymanager.enums.Type;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Stream.concat;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final ExpenseService expenseService;
    private final IncomeService incomeService;
    private final ProfileService profileService;
    private final ModelMapper modelMapper;

    public Map<String, Object> getDashboardData(){
        Profile profile = profileService.getCurrentProfile();
        Map<String, Object> returnValue = new LinkedHashMap<>();

        List<IncomeDto> latestIncomes = incomeService.getLatestFiveIncomes();
        List<ExpenseDto> latestExpenses = expenseService.getLatestFiveExpenses();

        List<RecentTransactionDto> recentTransactions = concat(latestIncomes.stream().map(
                (income) -> modelMapper.map(income, RecentTransactionDto.class))
                .peek(dto -> {
                    dto.setProfileId(profile.getId());
                    dto.setType(Type.INCOME);
                }),
                latestExpenses.stream().map(
                        (expense) -> modelMapper.map(expense, RecentTransactionDto.class))
                .peek(dto -> {
                    dto.setProfileId(profile.getId());
                    dto.setType(Type.EXPENSE);
                })
        ).sorted((a, b) -> {
            int cmp = a.getDate().compareTo(b.getDate());
            if (cmp == 0) {
                return a.getCreatedAt().compareTo(b.getCreatedAt());
            }
            return cmp;
        }).collect(Collectors.toList());

        returnValue.put("totalBalance",
                incomeService.getTotalIncomes().subtract(expenseService.getTotalExpenses())
        );

        returnValue.put("totalIncome",
                incomeService.getTotalIncomes()
        );
        returnValue.put("totalExpense",
                expenseService.getTotalExpenses()
        );
        returnValue.put("recent5Expenses", latestExpenses);
        returnValue.put("recent5Incomes", latestIncomes);
        returnValue.put("recentTransactions", recentTransactions);

        return returnValue;
    }
}
