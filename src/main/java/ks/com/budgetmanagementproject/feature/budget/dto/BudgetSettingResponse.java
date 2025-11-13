package ks.com.budgetmanagementproject.feature.budget.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import ks.com.budgetmanagementproject.feature.budget.entity.Budget;
import ks.com.budgetmanagementproject.feature.budget.entity.BudgetCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BudgetSettingResponse {

    private Long id;
    private String categoryName;
    private BigDecimal money;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate period;

    public static BudgetSettingResponse from(Budget budget) {
        return BudgetSettingResponse.builder()
                .id(budget.getId())
                .categoryName(budget.getCategory().getName())
                .money(budget.getMoney())
                .period(budget.getPeriod())
                .build();
    }

    public static BudgetSettingResponse of(Budget budget, BudgetCategory category) {
        return BudgetSettingResponse.builder()
                .id(budget.getId())
                .categoryName(category.getName())
                .money(budget.getMoney())
                .period(budget.getPeriod())
                .build();
    }
}