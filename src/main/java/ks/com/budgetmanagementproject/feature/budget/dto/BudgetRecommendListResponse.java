package ks.com.budgetmanagementproject.feature.budget.dto;


import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BudgetRecommendListResponse {

    @NotNull(message = "추천 목록은 필수입니다.")
    @Valid
    private List<BudgetRecommendResponse> responseList;

    public static BudgetRecommendListResponse from(List<BudgetRecommendResponse> list) {
        return new BudgetRecommendListResponse(list);
    }
}