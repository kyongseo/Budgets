package ks.com.budgetmanagementproject.feature.budget.dto;


import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.YearMonth;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class BudgetSettingRequest {

    @Schema(description = "설정 예산", example = "100000")
    @NotNull(message = "예산을 입력해주세요.")
    private long money;

    @Schema(description = "예산 카테고리", example = "식비")
    @NotBlank(message = "카테고리를 입력해주세요")
    private String categoryName;

    @Schema(description = "기간", example = "2023-11")
    @NotNull(message = "기간을 설정해주세요.")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM", timezone = "Asia/Seoul")
    private YearMonth period;
}
