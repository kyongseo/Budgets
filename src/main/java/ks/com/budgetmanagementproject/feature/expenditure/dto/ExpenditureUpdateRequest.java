package ks.com.budgetmanagementproject.feature.expenditure.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExpenditureUpdateRequest {

    @Schema(description = "지출 금액", example = "20000")
    @NotNull(message = "지출 금액을 입력해주세요.")
    @DecimalMin(value = "0.0", inclusive = false, message = "지출 금액은 0보다 커야 합니다.")
    @Digits(integer = 10, fraction = 2, message = "지출 금액은 최대 10자리 정수와 2자리 소수로 입력해주세요.")
    private BigDecimal money;

    @Schema(description = "지출 카테고리", example = "식비")
    @NotBlank(message = "카테고리를 입력해주세요")
    @Size(max = 20, message = "카테고리 이름은 20자를 초과할 수 없습니다.")
    private String categoryName;

    @Schema(description = "지출 일시", example = "2025-01-01")
    @NotNull(message = "지출 일시를 설정해주세요.")
    @PastOrPresent(message = "지출 일시는 현재 또는 과거 날짜여야 합니다.")
    private LocalDate period;

    @Schema(description = "메모", example = "저녁값으로 지출")
    @Size(max = 200, message = "메모는 200자를 초과할 수 없습니다.")
    private String memo;
}
