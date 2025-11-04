package ks.com.budgetmanagementproject.feature.expenditure.dto;

import ks.com.budgetmanagementproject.feature.budget.entity.BudgetCategory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Setter
public class ExpenditureGuide {

    private BudgetCategory category;

    private BigDecimal todayExpenditureAmount;

    private BigDecimal todayAppropriateExpenditureAmount;

    private String risk;
}
