package ks.com.budgetmanagementproject.feature.budget.service;

import ks.com.budgetmanagementproject.feature.budget.dto.BudgetSettingRequest;
import ks.com.budgetmanagementproject.feature.budget.dto.BudgetUpdateRequest;
import ks.com.budgetmanagementproject.feature.budget.entity.Budget;
import ks.com.budgetmanagementproject.feature.budget.entity.BudgetCategory;
import ks.com.budgetmanagementproject.feature.budget.repository.BudgetCategoryRepository;
import ks.com.budgetmanagementproject.feature.budget.repository.BudgetRepository;
import ks.com.budgetmanagementproject.feature.user.entity.User;
import ks.com.budgetmanagementproject.global.common.BaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static ks.com.budgetmanagementproject.global.common.BaseExceptionStatus.*;

@Service
@RequiredArgsConstructor
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final BudgetCategoryRepository categoryRepository;

    /**
     * 예산 설정
     * request에서 받은 categoryName으로 카테고리를 조회 후 존재하지 않은 카테고리면 예외 처리
     * @param request money, categoryName, period
     * @param user
     */
    @Transactional
    public void budgetSetting(BudgetSettingRequest request, User user) {
        BudgetCategory category = categoryRepository.findByName(request.getCategoryName()).orElseThrow(() -> new BaseException(NON_EXISTENT_CATEGORY));
        existsByBudget(request, user, category);
        LocalDate date = LocalDate.of(request.getPeriod().getYear(), request.getPeriod().getMonth(), 1);
        Budget budget = Budget.builder()
                .category(category)
                .money(request.getMoney())
                .period(date)
                .user(user)
                .build();
        budgetRepository.save(budget);
    }
    private void existsByBudget(BudgetSettingRequest request, User user, BudgetCategory category) {
        LocalDate date = LocalDate.of(request.getPeriod().getYear(), request.getPeriod().getMonth(), 1);
        Budget exists = budgetRepository.findByCategoryAndPeriodAndUser(category, date, user);
        if (exists != null) {
            throw new BaseException(DUPLICATE_BUDGET);
        }
    }


    @Transactional
    public void budgetUpdate(Long budgetId, BudgetUpdateRequest request, User user) {
        Budget budget = budgetRepository.findById(budgetId).orElseThrow(() -> new BaseException(NON_EXISTENT_BUDGET));
        if (!budget.getUser().getId().equals(user.getId())) {
            throw new BaseException(FORBIDDEN_USER);
        }
        budget.updateBudget(request.getMoney());
    }


//    @Transactional(readOnly = true)
//    public BudgetRecommendListResponse budgetRecommend(long totalAmount) {
//        List<BudgetRecommendResponse> responseList = budgetRepository.findByAverage(totalAmount);
//
//        return new BudgetRecommendListResponse(responseList);
//    }


}
