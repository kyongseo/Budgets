package ks.com.budgetmanagementproject.feature.expenditure.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ExpenditureExcludeRequest {

    @NotNull(message = "합계 제외 여부는 필수입니다.")
    private Boolean excludingTotal;
}
