package ks.com.budgetmanagementproject.feature.budget.service;

import ks.com.budgetmanagementproject.feature.budget.dto.*;
import ks.com.budgetmanagementproject.feature.budget.entity.Budget;
import ks.com.budgetmanagementproject.feature.budget.entity.BudgetCategory;
import ks.com.budgetmanagementproject.feature.budget.repository.BudgetCategoryRepository;
import ks.com.budgetmanagementproject.feature.budget.repository.BudgetRepository;
import ks.com.budgetmanagementproject.feature.user.entity.User;
import ks.com.budgetmanagementproject.feature.user.repository.UserRepository;
import ks.com.budgetmanagementproject.global.common.logger.BaseException;
import ks.com.budgetmanagementproject.global.common.logger.BaseExceptionStatus;
import ks.com.budgetmanagementproject.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class BudgetService {

    private final UserRepository userRepository;
    private final BudgetRepository budgetRepository;
    private final BudgetCategoryRepository categoryRepository;

    /**
     * 예산 설정
     * @param request money, categoryName, period
     * @param username 사용자명
     */
    @Transactional
    public void createBudget(BudgetSettingRequest request, String username) {

        User user = findUserByUsername(username);
        BudgetCategory category = findCategoryByName(request.getCategoryName());
        LocalDate period = request.getPeriod().atDay(1);

        validateDuplicateBudget(category, period, user);

        Budget budget = Budget.builder()
                .category(category)
                .money(request.getMoney())
                .period(period)
                .user(user)
                .build();

        budgetRepository.save(budget);
    }

    /**
     * 예산 수정
     * @param budgetId 예산 아이디
     * @param request  : money
     */
    @Transactional
    public BudgetUpdateResponse budgetUpdate(Long budgetId, BudgetUpdateRequest request) {

        Budget budget = findBudgetById(budgetId);
        budget.updateBudget(request.getMoney());
        return BudgetUpdateResponse.from(budget);
    }

    /**
     * 예산 Soft 삭제
     * @param budgetId 예산 아이디
     */
    @Transactional
    public void budgetSoftDelete(Long budgetId) {

        Budget budget = findBudgetById(budgetId);
        budget.softDeleted();
        budgetRepository.save(budget);
    }

    /**
     * 예산 Hard 삭제
     * @param budgetId 예산 아이디
     */
    @Transactional
    public void budgetHardDelete(Long budgetId) {

        Budget budget = findBudgetById(budgetId);
        budgetRepository.delete(budget);
    }

    /**
     * 예산 추천
     * @return list
     */
    @Transactional(readOnly = true)
    public BudgetRecommendListResponse budgetRecommend(Long totalAmount) {
        List<Budget> allBudgets = budgetRepository.findAllWithCategory();

        if (allBudgets.isEmpty()) {
            return BudgetRecommendListResponse.from(Collections.emptyList());
        }

        BigDecimal totalBudgetSum = calculateTotalBudgetSum(allBudgets);
        Map<BudgetCategory, BigDecimal> categorySum = calculateCategorySum(allBudgets);
        List<BudgetRecommendResponse> responseList = createRecommendationList(
                categorySum, totalBudgetSum, totalAmount
        );

        return BudgetRecommendListResponse.from(responseList);
    }

    private User findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new BaseException(BaseExceptionStatus.NON_EXISTENT_USER));
    }

    private BudgetCategory findCategoryByName(String categoryName) {
        return categoryRepository.findByName(categoryName)
                .orElseThrow(() -> new BaseException(BaseExceptionStatus.NON_EXISTENT_CATEGORY));
    }

    private Budget findBudgetById(Long budgetId) {
        return budgetRepository.findById(budgetId)
                .orElseThrow(() -> new BaseException(BaseExceptionStatus.NON_EXISTENT_BUDGET));
    }

    private void validateDuplicateBudget(BudgetCategory category, LocalDate period, User user) {
        Budget existingBudget = budgetRepository.findByCategoryAndPeriodAndUser(category, period, user);
        if (existingBudget != null) {
            throw new BaseException(BaseExceptionStatus.DUPLICATE_BUDGET);
        }
    }

    private BigDecimal calculateTotalBudgetSum(List<Budget> budgets) {
        return budgets.stream()
                .map(Budget::getMoney)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Map<BudgetCategory, BigDecimal> calculateCategorySum(List<Budget> budgets) {
        return budgets.stream()
                .collect(Collectors.groupingBy(
                        Budget::getCategory,
                        Collectors.reducing(
                                BigDecimal.ZERO,
                                Budget::getMoney,
                                BigDecimal::add
                        )
                ));
    }

    private List<BudgetRecommendResponse> createRecommendationList(
            Map<BudgetCategory, BigDecimal> categorySum,
            BigDecimal totalBudgetSum,
            Long totalAmount
    ) {
        return categorySum.entrySet().stream()
                .map(entry -> createRecommendResponse(entry, totalBudgetSum, totalAmount))
                .sorted(Comparator.comparing(r -> r.getCategory().getId()))
                .toList();
    }

    private BudgetRecommendResponse createRecommendResponse(
            Map.Entry<BudgetCategory, BigDecimal> entry,
            BigDecimal totalBudgetSum,
            Long totalAmount
    ) {
        BudgetCategory category = entry.getKey();
        BigDecimal categoryMoney = entry.getValue();

        BigDecimal ratio = categoryMoney.divide(totalBudgetSum, 10, RoundingMode.HALF_UP);
        long recommendedAmount = BigDecimal.valueOf(totalAmount)
                .multiply(ratio)
                .setScale(0, RoundingMode.HALF_UP)
                .longValue();

        return BudgetRecommendResponse.builder()
                .category(BudgetCategoryResponse.from(category))
                .average(recommendedAmount)
                .build();
    }
}
