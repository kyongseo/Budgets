package ks.com.budgetmanagementproject.feature.expenditure.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Setter
public class ExpenditureGuideResponse {

    private List<ExpenditureGuide> guideList;

    private long totalAmount;
}