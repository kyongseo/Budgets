package ks.com.budgetmanagementproject.feature.expenditure.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
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

    @NotNull(message = "지출 목록은 필수입니다.")
    @Valid
    private List<ExpenditureListResponseItem> expenditureLists;

    @Min(value = 0, message = "조회 금액 합계는 0 이상이어야 합니다.")
    private long viewMoneyTotal;

    @Min(value = 0, message = "전체 카테고리 금액 합계는 0 이상이어야 합니다.")
    private long totalCategoryMoneyTotal;

    public static ExpenditureListResponse of(List<ExpenditureList> source, long viewTotal, long categoryTotal) {
        List<ExpenditureListResponseItem> list = source.stream()
                .map(ExpenditureListResponseItem::from)
                .toList();

        return new ExpenditureListResponse(list, viewTotal, categoryTotal);
    }
}
