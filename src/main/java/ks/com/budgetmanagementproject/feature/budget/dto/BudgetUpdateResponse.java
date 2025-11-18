package ks.com.budgetmanagementproject.feature.budget.dto;

import ks.com.budgetmanagementproject.feature.budget.entity.Budget;
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
public class BudgetUpdateResponse {

    private Long budgetId;
    private String category;
    private BigDecimal money;
    private LocalDate period;

    public static BudgetUpdateResponse from(Budget budget) {
        return BudgetUpdateResponse.builder()
                .budgetId(budget.getId())
                .category(budget.getCategory().getName())
                .money(budget.getMoney())
                .period(budget.getPeriod())
                .build();
    }
}
