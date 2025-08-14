package ks.com.budgetmanagementproject.feature.budget.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import ks.com.budgetmanagementproject.feature.budget.dto.BudgetCategoryResponse;
import ks.com.budgetmanagementproject.feature.budget.service.BudgetCategoryService;
import ks.com.budgetmanagementproject.global.common.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/budget/categories")
@Tag(name = "budget-category", description = "Budget-Category API")
public class BudgetCategoryController {

    private final BudgetCategoryService categoryService;

    @Operation(operationId = "01-list-budget", summary = "✅ 예산 카테고리 목록 조회", responses = {
            @ApiResponse(responseCode = "200")
    })
    @Tag(name = "budget-category")
    @GetMapping
    public ResponseEntity<?> categoryList() {
        BudgetCategoryResponse response = categoryService.categoryList();

        return ResponseEntity.ok().body(new BaseResponse<>(200, "예산 카테고리 목록 조회에 성공했습니다."));
    }
}
