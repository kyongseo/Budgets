package ks.com.budgetmanagementproject.feature.expenditure.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Setter @Builder
public class ExpenditureGuideResponse {

    @NotNull(message = "가이드 목록은 필수입니다.")
    @Valid
    private List<ExpenditureGuide> guideList;

    @NotNull(message = "총 금액은 필수입니다.")
    @DecimalMin(value = "0.0", message = "총 금액은 0 이상이어야 합니다.")
    private BigDecimal totalAmount;

    public static ExpenditureGuideResponse of(
            List<ExpenditureGuide> guideList,
            BigDecimal totalAmount) {
        return ExpenditureGuideResponse.builder()
                .guideList(guideList)
                .totalAmount(totalAmount)
                .build();
    }
}