package ks.com.budgetmanagementproject.feature.expenditure.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ExpenditureRecommendResponse {

    List<ExpenditureRecommend> recommendList;

    private long todayExpenditurePossibleTotal;

    private String message;

}