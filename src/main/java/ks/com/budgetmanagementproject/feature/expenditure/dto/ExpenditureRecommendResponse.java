package ks.com.budgetmanagementproject.feature.expenditure.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ExpenditureRecommendResponse {

    @NotNull(message = "추천 목록은 필수입니다.")
    @Valid
    List<ExpenditureRecommend> recommendList;

    @Min(value = 0, message = "오늘 지출 가능 총액은 0 이상이어야 합니다.")
    private long todayExpenditurePossibleTotal;

    @NotBlank(message = "메시지는 필수입니다.")
    @Size(max = 500, message = "메시지는 500자를 초과할 수 없습니다.")
    private String message;
}