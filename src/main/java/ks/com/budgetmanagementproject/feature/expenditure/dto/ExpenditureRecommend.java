package ks.com.budgetmanagementproject.feature.expenditure.dto;

import ks.com.budgetmanagementproject.feature.budget.entity.BudgetCategory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Setter
public class ExpenditureRecommend {

    private BudgetCategory category;

    private long todayExpenditurePossibleMoney;

}
