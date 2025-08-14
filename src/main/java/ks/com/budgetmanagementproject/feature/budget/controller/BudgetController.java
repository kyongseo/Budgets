package ks.com.budgetmanagementproject.feature.budget.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import ks.com.budgetmanagementproject.feature.budget.dto.BudgetSettingRequest;
import ks.com.budgetmanagementproject.feature.budget.dto.BudgetUpdateRequest;
import ks.com.budgetmanagementproject.feature.budget.service.BudgetService;
import ks.com.budgetmanagementproject.feature.user.entity.User;
import ks.com.budgetmanagementproject.global.common.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/budgets/")
@Tag(name = "Budgets", description = "Budgets API")
public class BudgetController {

    private final BudgetService budgetService;


    @Operation(operationId = "01-create-budget", summary = "✅ 예산 설정", responses = {
            @ApiResponse(responseCode = "201")
    })
    @Tag(name = "Budgets")
    @PostMapping
    public ResponseEntity<?> budgetSetting(@Validated @RequestBody BudgetSettingRequest request, @AuthenticationPrincipal User user) {
        budgetService.budgetSetting(request, user);

        return ResponseEntity.ok().body(new BaseResponse<>(200, "예산 설정에 성공했습니다."));
    }

    @Operation(operationId = "02-update-budget", summary = "✅ 예산 수정", responses = {
            @ApiResponse(responseCode = "200")
    })
    @Tag(name = "Budgets")
    @PatchMapping("/{budgetId}")
    public ResponseEntity<?> budgetUpdate(@PathVariable Long budgetId, @Validated @RequestBody BudgetUpdateRequest request, @AuthenticationPrincipal User user) {
        budgetService.budgetUpdate(budgetId, request, user);

        return ResponseEntity.ok().body(new BaseResponse<>(200, "예산 수정에 성공했습니다."));
    }

//    @Operation(summary = "Budget 추천 API", responses = {
//            @ApiResponse(responseCode = "200")
//    })
//    @Tag(name = "Budgets")
//    @GetMapping("/recommend")
//    public ResponseEntity<?> budgetRecommend(@RequestParam long totalAmount) {
//        BudgetRecommendListResponse budgetRecommendListResponse = budgetService.budgetRecommend(totalAmount);
//
//        return ResponseEntity.ok().body(new BaseResponse<>(200, "예산 추천에 성공했습니다.", budgetRecommendListResponse));
//    }

}