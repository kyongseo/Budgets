package ks.com.budgetmanagementproject.feature.expenditure.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExpenditureListRequest {

    @NotNull(message = "시작 날짜는 필수입니다.")
    @PastOrPresent(message = "시작 날짜는 과거 또는 현재 날짜여야 합니다.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate minPeriod;

    @NotNull(message = "종료 날짜는 필수입니다.")
    @PastOrPresent(message = "종료 날짜는 과거 또는 현재 날짜여야 합니다.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate maxPeriod;

    @Size(max = 50, message = "카테고리명은 50자를 초과할 수 없습니다.")
    private String categoryName;

    @DecimalMin(value = "0.0", inclusive = true, message = "최소 금액은 0원 이상이어야 합니다.")
    @Digits(integer = 10, fraction = 2, message = "금액 형식이 올바르지 않습니다.")
    private BigDecimal minMoney;

    @DecimalMin(value = "0.0", inclusive = true, message = "최대 금액은 0원 이상이어야 합니다.")
    @Digits(integer = 10, fraction = 2, message = "금액 형식이 올바르지 않습니다.")
    private BigDecimal maxMoney;

    @AssertTrue(message = "종료 날짜는 시작 날짜보다 이후여야 합니다.")
    public boolean isValidPeriod() {
        if (minPeriod == null || maxPeriod == null) {
            return true;
        }
        return !maxPeriod.isBefore(minPeriod);
    }

    @AssertTrue(message = "최대 금액은 최소 금액보다 크거나 같아야 합니다.")
    public boolean isValidMoneyRange() {
        if (minMoney == null || maxMoney == null) {
            return true;
        }
        return maxMoney.compareTo(minMoney) >= 0;
    }
}