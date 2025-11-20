package ks.com.budgetmanagementproject.feature.budget.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import ks.com.budgetmanagementproject.feature.budget.entity.BudgetCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BudgetRecommendResponse {

    @Schema(description = "카테고리 정보")
    private BudgetCategoryResponse category;

    @Schema(description = "추천 금액", example = "150000")
    private Long average;

    public static BudgetRecommendResponse of(BudgetCategory category, Long average) {
        return BudgetRecommendResponse.builder()
                .category(BudgetCategoryResponse.from(category))
                .average(average)
                .build();
    }
}
