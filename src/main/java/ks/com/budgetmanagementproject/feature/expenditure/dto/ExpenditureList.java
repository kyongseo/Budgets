package ks.com.budgetmanagementproject.feature.expenditure.dto;

import ks.com.budgetmanagementproject.feature.budget.entity.BudgetCategory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ExpenditureList {

    private String memo;

    private LocalDate period;

    private BudgetCategory category;

    private boolean excludingTotal;

    private long money;
}