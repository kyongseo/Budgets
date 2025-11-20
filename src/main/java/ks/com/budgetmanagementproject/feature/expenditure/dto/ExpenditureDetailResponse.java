package ks.com.budgetmanagementproject.feature.expenditure.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ExpenditureDetailResponse {

    @Size(max = 200, message = "메모는 200자를 초과할 수 없습니다.")
    private String memo;

    @NotNull(message = "지출 일시는 필수입니다.")
    private LocalDate period;

    @NotBlank(message = "카테고리 이름은 필수입니다.")
    private String categoryName;

    @NotNull(message = "합계 제외 여부는 필수입니다.")
    private boolean excludingTotal;

    @NotNull(message = "지출 금액은 필수입니다.")
    @DecimalMin(value = "0.0", inclusive = false, message = "지출 금액은 0보다 커야 합니다.")
    private BigDecimal money;
}