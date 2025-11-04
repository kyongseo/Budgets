package ks.com.budgetmanagementproject.feature.expenditure.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
    private BigDecimal money;

    @Schema(description = "지출 카테고리", example = "식비")
    @NotBlank(message = "카테고리를 입력해주세요")
    private String categoryName;

    @Schema(description = "지출 일시", example = "2023-11-13")
    @NotNull(message = "지출 일시를 설정해주세요.")
    private LocalDate period;

    @Schema(description = "메모", example = "저녁값으로 지출")
    private String memo;
}
