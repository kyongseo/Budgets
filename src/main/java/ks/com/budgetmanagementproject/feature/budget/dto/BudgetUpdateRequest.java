package ks.com.budgetmanagementproject.feature.budget.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BudgetUpdateRequest {

    @Schema(description = "설정 예산", example = "100000")
    @NotNull(message = "예산을 입력해주세요.")
    @DecimalMin(value = "0.0", inclusive = false, message = "예산은 0보다 커야 합니다.")
    @Digits(integer = 10, fraction = 2, message = "예산은 최대 10자리 정수와 2자리 소수로 입력해주세요.")
    private BigDecimal money;
}
