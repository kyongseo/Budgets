package ks.com.budgetmanagementproject.feature.budget.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BudgetRecommendListResponse {

    List<BudgetRecommendResponse> responseList;
}