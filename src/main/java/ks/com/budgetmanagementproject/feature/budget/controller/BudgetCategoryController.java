package ks.com.budgetmanagementproject.feature.budget.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import ks.com.budgetmanagementproject.feature.budget.dto.BudgetCategoryResponse;
import ks.com.budgetmanagementproject.feature.budget.service.BudgetCategoryService;
import ks.com.budgetmanagementproject.global.common.logger.BaseResponse;
import ks.com.budgetmanagementproject.global.common.logger.BaseResponseStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/budget/categories")
@Tag(name = "Budget-category", description = "Budget-Category API")
public class BudgetCategoryController {

    private final BudgetCategoryService categoryService;

    @GetMapping
    @Operation(summary = "✅ 예산 카테고리 목록 조회", description = "예산 카테고리 목록 조회")
    public ResponseEntity<?> categoryList() {

        List<BudgetCategoryResponse> response = categoryService.categoryList();

        return ResponseEntity
                .status(BaseResponseStatus.BUDGET_CATEGORY_LIST_SUCCESS.getStatus())
                .body(BaseResponse.of(BaseResponseStatus.BUDGET_CATEGORY_LIST_SUCCESS, response));
    }
}
