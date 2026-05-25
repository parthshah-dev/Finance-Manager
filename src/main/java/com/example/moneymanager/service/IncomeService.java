package com.example.moneymanager.service;

import com.example.moneymanager.dto.ExpenseDto;
import com.example.moneymanager.dto.IncomeDto;
import com.example.moneymanager.entity.Category;
import com.example.moneymanager.entity.Expense;
import com.example.moneymanager.entity.Income;
import com.example.moneymanager.entity.Profile;
import com.example.moneymanager.repository.CategoryRepository;
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
public class IncomeService {

    private final IncomeRepository incomeRepository;
    private final CategoryRepository categoryRepository;
    private final ProfileService profileService;
    private final ModelMapper modelMapper;

    public IncomeDto saveIncome(IncomeDto incomeDto){
        Profile currentProfile = profileService.getCurrentProfile();
        Category category = categoryRepository.findById(incomeDto.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + incomeDto.getCategoryId()));

        Income newIncome = modelMapper.map(incomeDto, Income.class);
        newIncome.setProfile(currentProfile);
        newIncome.setCategory(category);
        newIncome = incomeRepository.save(newIncome);

        return modelMapper.map(newIncome, IncomeDto.class);

    }

    public List<IncomeDto> getCurrentMonthIncomeOfCurrentUser() {
        Profile currentProfile = profileService.getCurrentProfile();
        LocalDate now = LocalDate.now();
        LocalDate startDate = now.withDayOfMonth(1);
        LocalDate endDate = now.withDayOfMonth(now.lengthOfMonth());
        List<Income> incomes = incomeRepository.findByProfileIdAndDateBetween(currentProfile.getId(), startDate, endDate);
        return incomes.stream()
                .map(income -> modelMapper.map(income, IncomeDto.class))
                .collect(Collectors.toList());
    }

    public void deletedIncomeById(Long id){
        Profile profile = profileService.getCurrentProfile();
        Income income = incomeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Income not found with id: " + id));

        if(!income.getProfile().getId().equals(profile.getId())){
            throw new RuntimeException("You are not authorized to delete this income");
        }
        incomeRepository.delete(income);
    }

    public List<IncomeDto> getLatestFiveIncomes(){
        Profile profile = profileService.getCurrentProfile();

        List<Income> incomes = incomeRepository.findTop5ByProfileIdOrderByDateDesc(profile.getId());

        return incomes.stream().map((element) -> modelMapper.map(element, IncomeDto.class))
                .collect(Collectors.toList());

    }

    public BigDecimal getTotalIncomes(){
        Profile profile = profileService.getCurrentProfile();
        BigDecimal total = incomeRepository.findTotalIncomeByProfileId(profile.getId());

        return total != null ? total : BigDecimal.ZERO;
    }
}
