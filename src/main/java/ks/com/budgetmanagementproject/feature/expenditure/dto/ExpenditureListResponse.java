package ks.com.budgetmanagementproject.feature.expenditure.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExpenditureListResponse {

    private List<ExpenditureList> expenditureLists;

    private long viewMoneyTotal;

    private long totalCategoryMoneyTotal;
}
