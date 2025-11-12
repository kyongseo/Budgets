package ks.com.budgetmanagementproject.feature.expenditure.dto;

import jakarta.validation.constraints.*;
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

    @NotNull(message = "지출 ID는 필수입니다.")
    @Positive(message = "지출 ID는 양수여야 합니다.")
    private Long expenditureId;

    @Size(max = 200, message = "메모는 200자를 초과할 수 없습니다.")
    private String memo;

    @NotNull(message = "지출 일시는 필수입니다.")
    private LocalDate period;

    @NotBlank(message = "카테고리 이름은 필수입니다.")
    private String categoryName;

    @NotNull(message = "합계 제외 여부는 필수입니다.")
    private boolean excludingTotal;

    @Min(value = 1, message = "지출 금액은 1 이상이어야 합니다.")
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
