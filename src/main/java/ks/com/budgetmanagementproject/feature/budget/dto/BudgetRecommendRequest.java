package ks.com.budgetmanagementproject.feature.budget.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BudgetRecommendRequest {

    @Schema(description = "총 예산 금액", example = "1000000", required = true)
    @NotNull(message = "총 예산 금액은 필수입니다.")
    @Min(value = 1, message = "총 예산 금액은 1 이상이어야 합니다.")
    private Long totalAmount;
}