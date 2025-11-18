package ks.com.budgetmanagementproject.feature.budget.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BudgetRecommendListResponse {

    @Schema(description = "카테고리별 추천 예산 목록")
    private List<BudgetRecommendResponse> responseList;

    public static BudgetRecommendListResponse from(List<BudgetRecommendResponse> list) {
        return new BudgetRecommendListResponse(list);
    }
}