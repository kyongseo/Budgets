package ks.com.budgetmanagementproject.feature.budget.service;

import ks.com.budgetmanagementproject.feature.budget.dto.*;
import ks.com.budgetmanagementproject.feature.budget.entity.Budget;
import ks.com.budgetmanagementproject.feature.budget.entity.BudgetCategory;
import ks.com.budgetmanagementproject.feature.budget.repository.BudgetCategoryRepository;
import ks.com.budgetmanagementproject.feature.budget.repository.BudgetRepository;
import ks.com.budgetmanagementproject.feature.user.entity.User;
import ks.com.budgetmanagementproject.global.common.logger.BaseException;
import ks.com.budgetmanagementproject.global.common.logger.BaseExceptionStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final BudgetCategoryRepository categoryRepository;

    /**
     * 예산 설정
     * @param request money, categoryName, period
     * @param user    사용자
     */
    @Transactional
    public void createBudget(BudgetSettingRequest request, User user) {

        BudgetCategory budgets = categoryRepository.findByName(request.getCategoryName())
                .orElseThrow(() -> new BaseException(BaseExceptionStatus.NON_EXISTENT_CATEGORY));

//        Budget existingBudget = budgetRepository.findByCategoryAndPeriodAndUser(
//                budgets, LocalDate.from(request.getPeriod()), user);

        LocalDate period = request.getPeriod().atDay(1);

        Budget existingBudget = budgetRepository.findByCategoryAndPeriodAndUser(
                budgets, period, user);

        Budget budget = Budget.builder()
                .category(budgets)
                .money(request.getMoney())
                .period(period)
                .user(user)
                .build();

        if (existingBudget != null) {
            throw new BaseException(BaseExceptionStatus.DUPLICATE_BUDGET);
        }
        budgetRepository.save(budget);
    }

    /**
     * 예산 수정
     * @param budgetId 예산 아이디
     * @param request  : money
     * @param user     사용자
     */
    @Transactional
    public void budgetUpdate(Long budgetId, BudgetUpdateRequest request, User user) {

        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new BaseException(BaseExceptionStatus.NON_EXISTENT_BUDGET));

        if (!budget.getUser().getId().equals(user.getId())) {
            throw new BaseException(BaseExceptionStatus.FORBIDDEN_USER);
        }

        budget.updateBudget(request.getMoney());
        BudgetUpdateResponse.from(budget);
    }

    /**
     * 예산 Soft 삭제
     * @param budgetId 예산 아이디
     */
    public void budgetSoftDelete(Long budgetId) {

        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new BaseException(BaseExceptionStatus.NON_EXISTENT_BUDGET));

        budget.softDeleted();
        budgetRepository.save(budget);
    }

    /**
     * 예산 Hard 삭제
     * @param budgetId 예산 아이디
     */
    public void budgetHardDelete(Long budgetId) {

        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new BaseException(BaseExceptionStatus.NON_EXISTENT_BUDGET));

        budgetRepository.delete(budget);
    }

    /**
     * 예산 추천
     * @return list
     */
    @Transactional(readOnly = true)
    public BudgetRecommendListResponse budgetRecommend(Long totalAmount) {
        // 1. 모든 예산 데이터 조회 (카테고리 정보 포함) - N+1 방지
        List<Budget> allBudgets = budgetRepository.findAllWithCategory();

        // 2. 예산 데이터가 없는 경우 빈 리스트 반환
        if (allBudgets.isEmpty()) {
            return BudgetRecommendListResponse.from(Collections.emptyList());
        }

        // 3. 전체 예산 합계 계산
        BigDecimal totalBudgetSum = allBudgets.stream()
                .map(Budget::getMoney)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 4. 카테고리별로 그룹화하고 합계 계산
        Map<BudgetCategory, BigDecimal> categorySum = allBudgets.stream()
                .collect(Collectors.groupingBy(
                        Budget::getCategory,
                        Collectors.reducing(
                                BigDecimal.ZERO,
                                Budget::getMoney,
                                BigDecimal::add
                        )
                ));

        // 5. 카테고리별 추천 금액 계산
        List<BudgetRecommendResponse> responseList = categorySum.entrySet().stream()
                .map(entry -> {
                    BudgetCategory category = entry.getKey();
                    BigDecimal categoryMoney = entry.getValue();

                    // 비율 계산: (카테고리 금액 / 전체 금액) * 사용자 총 예산
                    BigDecimal ratio = categoryMoney.divide(totalBudgetSum, 10, RoundingMode.HALF_UP);
                    long recommendedAmount = BigDecimal.valueOf(totalAmount)
                            .multiply(ratio)
                            .setScale(0, RoundingMode.HALF_UP)
                            .longValue();

                    return BudgetRecommendResponse.builder()
                            .category(BudgetCategoryResponse.from(category))
                            .average(recommendedAmount)
                            .build();
                })
                .sorted(Comparator.comparing(r -> r.getCategory().getId())) // ID 순으로 정렬
                .toList();

        return BudgetRecommendListResponse.from(responseList);
    }
}
