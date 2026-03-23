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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StopWatch;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BudgetService {

    private final UserRepository userRepository;
    private final BudgetRepository budgetRepository;
    private final BudgetCategoryRepository categoryRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String BUDGET_RATIO_KEY = "budget:recommend:ratios";

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
     * 예산 추천 (Redis 캐싱 적용)
     * @return list
     */
    @Transactional(readOnly = true)
    public BudgetRecommendListResponse budgetRecommend(Long totalAmount) {
        StopWatch sw = new StopWatch();
        sw.start();

        // 1. Redis에서 캐시된 비율 데이터 조회
        Map<String, Double> cachedRatios = (Map<String, Double>) redisTemplate.opsForValue().get(BUDGET_RATIO_KEY);

        // 2. 캐시가 없으면 DB에서 계산 후 캐싱
        if (cachedRatios == null) {
            log.info(">>>> [BudgetRecommend] 캐시가 없어 실시간 통계 계산을 수행합니다.");
            cachedRatios = calculateAndCacheRatios();
        }

        final Map<String, Double> finalRatios = cachedRatios;

        // 3. 캐시된 비율로 추천 금액 산출
        List<BudgetRecommendResponse> responseList = categoryRepository.findAll().stream()
                .map(category -> {
                    Double ratio = finalRatios.getOrDefault(String.valueOf(category.getId()), 0.0);
                    long recommendedAmount = Math.round(totalAmount * ratio);
                    return BudgetRecommendResponse.builder()
                            .category(BudgetCategoryResponse.from(category))
                            .average(recommendedAmount)
                            .build();
                })
                .sorted(Comparator.comparing(r -> r.getCategory().getId()))
                .toList();

        sw.stop();
        log.info(">>>> [성능 측정] 추천 로직 전체 소요 시간: {} ms", sw.getTotalTimeMillis());

        return BudgetRecommendListResponse.from(responseList);
    }

    /**
     * 전체 예산 통계 계산 및 Redis 저장
     */
    @Transactional(readOnly = true)
    public Map<String, Double> calculateAndCacheRatios() {
        List<Budget> allBudgets = budgetRepository.findAllWithCategory();
        if (allBudgets.isEmpty()) return Collections.emptyMap();

        BigDecimal totalBudgetSum = allBudgets.stream()
                .map(Budget::getMoney)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, BigDecimal> categorySum = allBudgets.stream()
                .collect(Collectors.groupingBy(
                        b -> String.valueOf(b.getCategory().getId()),
                        Collectors.reducing(BigDecimal.ZERO, Budget::getMoney, BigDecimal::add)
                ));

        Map<String, Double> ratios = categorySum.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().divide(totalBudgetSum, 4, RoundingMode.HALF_UP).doubleValue()
                ));

        // Redis에 24시간 동안 저장
        redisTemplate.opsForValue().set(BUDGET_RATIO_KEY, ratios, 24, TimeUnit.HOURS);
        log.info(">>>> [BudgetRecommend] 통계 데이터 캐싱 완료 (Key: {})", BUDGET_RATIO_KEY);

        return ratios;
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
        return budgetId != null ? budgetRepository.findById(budgetId)
                .orElseThrow(() -> new BaseException(BaseExceptionStatus.NON_EXISTENT_BUDGET)) : null;
    }

    private void validateDuplicateBudget(BudgetCategory category, LocalDate period, User user) {
        Budget existingBudget = budgetRepository.findByCategoryAndPeriodAndUser(category, period, user);
        if (existingBudget != null) {
            throw new BaseException(BaseExceptionStatus.DUPLICATE_BUDGET);
        }
    }
}
