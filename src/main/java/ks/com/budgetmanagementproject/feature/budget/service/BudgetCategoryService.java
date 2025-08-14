package ks.com.budgetmanagementproject.feature.budget.service;

import ks.com.budgetmanagementproject.feature.budget.dto.BudgetCategoryResponse;
import ks.com.budgetmanagementproject.feature.budget.entity.BudgetCategory;
import ks.com.budgetmanagementproject.feature.budget.repository.BudgetCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BudgetCategoryService {

    private final BudgetCategoryRepository categoryRepository;

    /**
     * 예산 카테고리 목록 조회
     * 전체 예산 카테고리 목록을 불러와서 BudgetCategoryResponse를 반환한다.
     * @return
     */
    public BudgetCategoryResponse categoryList() {
        List<BudgetCategory> list = categoryRepository.findAll();

        BudgetCategoryResponse response = new BudgetCategoryResponse(list);

        return response;
    }
}