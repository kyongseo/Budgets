package ks.com.budgetmanagementproject.feature.expenditure.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import ks.com.budgetmanagementproject.feature.budget.entity.BudgetCategory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Setter
public class ExpenditureGuide {

    @NotNull(message = "카테고리는 필수입니다.")
    @Valid
    private String categoryName;

    @NotNull(message = "오늘 지출 금액은 필수입니다.")
    @DecimalMin(value = "0.0", message = "오늘 지출 금액은 0 이상이어야 합니다.")
    private BigDecimal todaySpent;

    @NotNull(message = "오늘 적정 지출 금액은 필수입니다.")
    @DecimalMin(value = "0.0", message = "오늘 적정 지출 금액은 0 이상이어야 합니다.")
    private BigDecimal appropriateAmount;

    @NotBlank(message = "위험도는 필수입니다.")
    @Size(max = 10, message = "위험도는 10자를 초과할 수 없습니다.")
    private int risk;
}