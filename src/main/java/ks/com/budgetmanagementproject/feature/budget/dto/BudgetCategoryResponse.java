package ks.com.budgetmanagementproject.feature.budget.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import ks.com.budgetmanagementproject.feature.budget.entity.BudgetCategory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BudgetCategoryResponse {

    @NotNull(message = "카테고리 목록은 필수입니다.")
    @Valid
    private List<BudgetCategory> categories;
}