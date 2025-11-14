package ks.com.budgetmanagementproject.feature.budget.dto;

import ks.com.budgetmanagementproject.feature.budget.entity.BudgetCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BudgetCategoryResponse {

    private Long id;
    private String name;

    public static BudgetCategoryResponse from(BudgetCategory category) {
        return BudgetCategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .build();
    }
}