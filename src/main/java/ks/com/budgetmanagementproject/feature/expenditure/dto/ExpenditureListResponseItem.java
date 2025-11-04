package ks.com.budgetmanagementproject.feature.expenditure.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpenditureListResponseItem {

    private Long expenditureId;
    private String memo;
    private LocalDate period;
    private String categoryName;
    private boolean excludingTotal;
    private long money;

    public static ExpenditureListResponseItem from(ExpenditureList entity) {
        return ExpenditureListResponseItem.builder()
                .memo(entity.getMemo())
                .period(entity.getPeriod())
                .categoryName(entity.getCategory().getName())
                .excludingTotal(entity.isExcludingTotal())
                .money(entity.getMoney().longValue())
                .build();
    }
}
