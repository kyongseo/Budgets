package ks.com.budgetmanagementproject.feature.budget.dto;

import ks.com.budgetmanagementproject.feature.budget.entity.BudgetCategory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BudgetRecommendResponse {

    private BudgetCategory category;
    private Long average;

    public static BudgetRecommendResponse from(BudgetCategory category, Long average) {
        return new BudgetRecommendResponse(category, average);
    }
}
