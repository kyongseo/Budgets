package ks.com.budgetmanagementproject.feature.expenditure.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
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

    @NotNull(message = "카테고리는 필수입니다.")
    @Valid
    private BudgetCategory category;

    @Min(value = 0, message = "오늘 지출 가능 금액은 0 이상이어야 합니다.")
    private long todayExpenditurePossibleMoney;
}
