package ks.com.budgetmanagementproject.feature.expenditure.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpenditureListResponse {

    private List<ExpenditureListResponseItem> expenditureLists;
    private long viewMoneyTotal;
    private long totalCategoryMoneyTotal;

    public static ExpenditureListResponse of(List<ExpenditureList> source, long viewTotal, long categoryTotal) {
        List<ExpenditureListResponseItem> list = source.stream()
                .map(ExpenditureListResponseItem::from)
                .toList();

        return new ExpenditureListResponse(list, viewTotal, categoryTotal);
    }
}
