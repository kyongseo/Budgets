package ks.com.budgetmanagementproject.feature.budget.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import ks.com.budgetmanagementproject.feature.budget.dto.BudgetRecommendListResponse;
import ks.com.budgetmanagementproject.feature.budget.dto.BudgetSettingRequest;
import ks.com.budgetmanagementproject.feature.budget.dto.BudgetUpdateRequest;
import ks.com.budgetmanagementproject.feature.budget.dto.BudgetUpdateResponse;
import ks.com.budgetmanagementproject.feature.budget.service.BudgetService;
import ks.com.budgetmanagementproject.feature.user.entity.User;
import ks.com.budgetmanagementproject.global.common.logger.BaseResponse;
import ks.com.budgetmanagementproject.global.common.logger.BaseResponseStatus;
import ks.com.budgetmanagementproject.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/budgets")
@Tag(name = "Budget", description = "Budget API")
public class BudgetController {

    private final BudgetService budgetService;

    @Operation(summary = "✅ 예산 설정", description = "카테고리별 예산을 설정합니다. 먼저 /api/categories로 카테고리 목록을 조회하세요.")
    @PostMapping
    public ResponseEntity<?> budgetSetting_endpoint(
            @Validated @RequestBody BudgetSettingRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        String username = userDetails.getUsername();
        budgetService.createBudget(request, username);

        return ResponseEntity
                .status(BaseResponseStatus.BUDGET_CREATE_SUCCESS.getStatus())
                .body(BaseResponse.of(BaseResponseStatus.BUDGET_CREATE_SUCCESS));
    }

    @Operation(summary = "✅ 예산 수정", description = "예산 수정")
    @PatchMapping("/{budgetId}")
    public ResponseEntity<?> budgetUpdate_endpoint(
            @PathVariable Long budgetId,
            @Validated @RequestBody BudgetUpdateRequest request
    ) {

        BudgetUpdateResponse response = budgetService.budgetUpdate(budgetId, request);

        return ResponseEntity
                .status(BaseResponseStatus.BUDGET_UPDATE_SUCCESS.getStatus())
                .body(BaseResponse.of(BaseResponseStatus.BUDGET_UPDATE_SUCCESS, response));
    }

    @Operation(summary = "✅ 예산 삭제(soft)", description = "예산 삭제(soft)")
    @DeleteMapping("/soft-delete/{budgetId}")
    public ResponseEntity<?> budgetSoftDelete_endpoint(
            @PathVariable Long budgetId) {

        budgetService.budgetSoftDelete(budgetId);

        return ResponseEntity
                .status(BaseResponseStatus.BUDGET_DELETE_SUCCESS.getStatus())
                .body(BaseResponse.of(BaseResponseStatus.BUDGET_DELETE_SUCCESS));
    }

    @Operation(summary = "✅ 예산 삭제(hard)", description = "예산 삭제(hard)")
    @DeleteMapping("/hard-delete/{budgetId}")
    public ResponseEntity<?> budgetHardDelete_endpoint(
            @PathVariable Long budgetId) {

        budgetService.budgetHardDelete(budgetId);

        return ResponseEntity
                .status(BaseResponseStatus.BUDGET_DELETE_SUCCESS.getStatus())
                .body(BaseResponse.of(BaseResponseStatus.BUDGET_DELETE_SUCCESS));
    }

    @Operation(summary = "✅ 예산 추천", description = "예산 추천")
    @GetMapping("/recommend")
    public ResponseEntity<?> budgetRecommend(@RequestParam Long totalAmount) {

        BudgetRecommendListResponse response = budgetService.budgetRecommend(totalAmount);

        return ResponseEntity
                .status(BaseResponseStatus.BUDGET_RECOMMEND_SUCCESS.getStatus())
                .body(BaseResponse.of(BaseResponseStatus.BUDGET_RECOMMEND_SUCCESS, response));
    }
}