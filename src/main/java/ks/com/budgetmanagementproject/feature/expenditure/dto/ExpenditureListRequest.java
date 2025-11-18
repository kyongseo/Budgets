package ks.com.budgetmanagementproject.feature.expenditure.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ExpenditureListRequest {

    @NotNull(message = "시작 날짜는 필수입니다.")
    private LocalDate minPeriod;

    @NotNull(message = "종료 날짜는 필수입니다.")
    private LocalDate maxPeriod;

    private String categoryName;

    private BigDecimal minMoney;

    private BigDecimal maxMoney;

    @AssertTrue(message = "종료 날짜는 시작 날짜보다 이후여야 합니다.")
    public boolean isValidPeriod() {
        if (minPeriod == null || maxPeriod == null) {
            return true;
        }
        return !maxPeriod.isBefore(minPeriod);
    }
}