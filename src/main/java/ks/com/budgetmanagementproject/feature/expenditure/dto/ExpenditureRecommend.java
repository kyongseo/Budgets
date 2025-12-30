package ks.com.budgetmanagementproject.feature.expenditure.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import ks.com.budgetmanagementproject.feature.budget.entity.BudgetCategory;
import ks.com.budgetmanagementproject.feature.expenditure.entity.Expenditure;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Setter @Builder
public class ExpenditureRecommend {

    @NotNull(message = "카테고리는 필수입니다.")
    @Valid
    private BudgetCategory category;

    @Min(value = 0, message = "오늘 지출 가능 금액은 0 이상이어야 합니다.")
    private long todayExpenditurePossibleMoney;

    public static ExpenditureRecommend from(Expenditure expenditure) {
        return ExpenditureRecommend.builder()
                .category(expenditure.getCategory())
                .todayExpenditurePossibleMoney(expenditure.getMoney().longValue())
                .build();
    }
}
