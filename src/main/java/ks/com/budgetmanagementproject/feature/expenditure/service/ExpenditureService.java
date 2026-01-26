package ks.com.budgetmanagementproject.feature.expenditure.service;

import ks.com.budgetmanagementproject.feature.budget.entity.Budget;
import ks.com.budgetmanagementproject.feature.budget.entity.BudgetCategory;
import ks.com.budgetmanagementproject.feature.budget.repository.BudgetCategoryRepository;
import ks.com.budgetmanagementproject.feature.budget.repository.BudgetRepository;
import ks.com.budgetmanagementproject.feature.expenditure.dto.*;
import ks.com.budgetmanagementproject.feature.expenditure.entity.Expenditure;
import ks.com.budgetmanagementproject.feature.expenditure.repository.ExpenditureRepository;
import ks.com.budgetmanagementproject.feature.user.entity.User;
import ks.com.budgetmanagementproject.feature.user.repository.UserRepository;
import ks.com.budgetmanagementproject.global.common.logger.BaseException;
import ks.com.budgetmanagementproject.global.common.logger.BaseExceptionStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExpenditureService {

    private final UserRepository userRepository;
    private final ExpenditureRepository expenditureRepository;
    private final BudgetCategoryRepository categoryRepository;
    private final BudgetRepository budgetRepository;

    private static final BigDecimal DEFAULT_MAX_MONEY = new BigDecimal("999999999");
    private static final long MINIMUM_DAILY_BUDGET = 20000L;

    /**
     * 지출 생성
     * @param request money, memo, category, period
     * @param username 사용자명
     */
    @Transactional
    public void createExpenditure(ExpenditureCreateRequest request, String username) {
        User user = findUserByUsername(username);
        BudgetCategory category = findCategoryByName(request.getCategoryName());

        Expenditure expenditure = Expenditure.from(request, category, user);
        expenditureRepository.save(expenditure);

        deductBudget(request, category, user);
    }

    /**
     * 지출 수정
     * @param expenditureId 지출 아이디
     * @param request money, memo, category, period
     * @param username 사용자명
     */
    @Transactional
    public void updateExpenditure(Long expenditureId, ExpenditureUpdateRequest request, String username) {
        User user = findUserByUsername(username);
        Expenditure expenditure = findExpenditureById(expenditureId);
        BudgetCategory category = findCategoryByName(request.getCategoryName());

        validateExpenditureOwner(expenditure, user);
        expenditure.updateExpenditure(request, category);
    }


    /**
     * 지출 목록 조회
     * @param request 요청
     * @param username 사용자명
     * @return 지출 목록 응답
     */
    @Transactional(readOnly = true)
    public ExpenditureListResponse getExpenditureList(ExpenditureListRequest request, String username) {
        User user = findUserByUsername(username);
        BudgetCategory category = findCategoryIfExists(request.getCategoryName());

        BigDecimal minMoney = getOrDefault(request.getMinMoney(), BigDecimal.ZERO);
        BigDecimal maxMoney = getOrDefault(request.getMaxMoney(), DEFAULT_MAX_MONEY);

        List<ExpenditureList> expenditureList = expenditureRepository.findExpenditureList(
                request.getMinPeriod(),
                request.getMaxPeriod(),
                category,
                user,
                minMoney,
                maxMoney
        );

        long viewMoneyTotal = expenditureRepository.findViewMoneyTotal(
                request.getMinPeriod(),
                request.getMaxPeriod(),
                category,
                user,
                minMoney,
                maxMoney
        );

        long totalCategoryMoney = expenditureRepository.findTotalByCategory(category, user);

        return ExpenditureListResponse.of(expenditureList, viewMoneyTotal, totalCategoryMoney);
    }

    /**
     * 지출 상세 조회
     * @param expenditureId 지출 아이디
     * @param username 사용자명
     * @return 지출 상세 응답
     */
    @Transactional(readOnly = true)
    public ExpenditureDetailResponse getExpenditureDetail(Long expenditureId, String username) {
        User user = findUserByUsername(username);
        Expenditure expenditure = findExpenditureById(expenditureId);

        validateExpenditureOwner(expenditure, user);

        return ExpenditureDetailResponse.from(expenditure);
    }


    /**
     * 지출 Soft 삭제
     * @param expenditureId 지출 아이디
     * @param username 사용자명
     */
    @Transactional
    public void softDeleteExpenditure(Long expenditureId, String username) {
        User user = findUserByUsername(username);
        Expenditure e = expenditureRepository.findById(expenditureId)
                .orElseThrow(() -> new BaseException(BaseExceptionStatus.NON_EXISTENT_EXPENDITURE));
        if (!e.isOwnedBy(user)) {
            throw new BaseException(BaseExceptionStatus.FORBIDDEN_USER);
        }
        e.softDeleted();
        expenditureRepository.save(e);
    }

    /**
     * 지출 Hard 삭제
     * @param expenditureId 지출 아이디
     * @param username 사용자명
     */
    @Transactional
    public void hardDeleteExpenditure(Long expenditureId, String username) {
        User user = findUserByUsername(username);
        Expenditure expenditure = findExpenditureById(expenditureId);

        validateExpenditureOwner(expenditure, user);
        expenditureRepository.delete(expenditure);
    }

    /**
     * 지출 합계 제외 업데이트
     * @param expenditureId 지출 아이디
     * @param username 사용자명
     * @param request 요청
     */
    @Transactional
    public void updateExpenditureExclude(Long expenditureId, String username, ExpenditureExcludeRequest request) {
        User user = findUserByUsername(username);
        Expenditure expenditure = findExpenditureById(expenditureId);

        validateExpenditureOwner(expenditure, user);
        expenditure.excludingTotalUpdate(request.getExcludingTotal());
    }

    /**
     * 지출 추천
     * @param username 사용자명
     * @return 지출 추천 응답
     */
    @Transactional(readOnly = true)
    public ExpenditureRecommendResponse getExpenditureRecommendation(String username) {
        User user = findUserByUsername(username);

        LocalDate today = LocalDate.now();
        LocalDate monthStart = today.withDayOfMonth(1);
        LocalDate monthEnd = today.withDayOfMonth(today.lengthOfMonth());
        long remainingDays = ChronoUnit.DAYS.between(today, monthEnd);

        List<ExpenditureRecommend> recommendList = budgetRepository.findByExpenditureRecommend(
                user, monthStart, remainingDays
        );

        long todayTotalPossibleAmount = calculateTodayTotalAmount(recommendList);
        String message = generateRecommendationMessage(recommendList);

        return ExpenditureRecommendResponse.of(recommendList, todayTotalPossibleAmount, message);
    }

    /**
     * 오늘의 지출 안내 (카테고리별 적정 금액 대비 실제 지출)
     * @param username 사용자명
     * @return 지출 안내 응답 (ExpenditureGuideResponse)
     */
    @Transactional(readOnly = true)
    public ExpenditureGuideResponse getExpenditureGuide(String username) {
        User user = findUserByUsername(username);

        LocalDate today = LocalDate.now();
        LocalDate monthStart = today.withDayOfMonth(1);
        LocalDate monthEnd = today.withDayOfMonth(today.lengthOfMonth());
        long remainingDays = ChronoUnit.DAYS.between(today, monthEnd);

        List<ExpenditureGuide> guideList = expenditureRepository.findByExpenditureAmount(
                user, monthStart, today, remainingDays
        );

        BigDecimal totalAmount = calculateTotalAmountWithRisk(guideList);

        return ExpenditureGuideResponse.of(guideList, totalAmount);
    }

    private User findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new BaseException(BaseExceptionStatus.NON_EXISTENT_USER));
    }

    private BudgetCategory findCategoryByName(String categoryName) {
        return categoryRepository.findByName(categoryName)
                .orElseThrow(() -> new BaseException(BaseExceptionStatus.NON_EXISTENT_CATEGORY));
    }

    private BudgetCategory findCategoryIfExists(String categoryName) {
        return categoryName != null ? findCategoryByName(categoryName) : null;
    }

    private Expenditure findExpenditureById(Long expenditureId) {
        return expenditureRepository.findById(expenditureId)
                .orElseThrow(() -> new BaseException(BaseExceptionStatus.NON_EXISTENT_EXPENDITURE));
    }

    private void validateExpenditureOwner(Expenditure expenditure, User user) {
        if (!expenditure.isOwnedBy(user)) {
            throw new BaseException(BaseExceptionStatus.FORBIDDEN_USER);
        }
    }

    private <T> T getOrDefault(T value, T defaultValue) {
        return value != null ? value : defaultValue;
    }

    /**
     * 지출 생성 후 예산 차감
     */
    private void deductBudget(ExpenditureCreateRequest request, BudgetCategory category, User user) {
        LocalDate budgetPeriod = LocalDate.of(
                request.getPeriod().getYear(),
                request.getPeriod().getMonth(),
                1
        );

        Budget budget = budgetRepository.findByCategoryAndPeriodAndUser(category, budgetPeriod, user);
        if (budget == null) {
            throw new BaseException(BaseExceptionStatus.NON_EXISTENT_BUDGET);
        }

        budget.deduct(request.getMoney());
    }

    /**
     * 오늘 사용 가능한 총 금액 계산 (예산 초과 시 최소 금액으로 조정)
     */
    private long calculateTodayTotalAmount(List<ExpenditureRecommend> recommendList) {
        return recommendList.stream()
                .mapToLong(recommend -> {
                    if (recommend.getTodayExpenditurePossibleMoney() <= 0) {
                        recommend.setTodayExpenditurePossibleMoney(MINIMUM_DAILY_BUDGET);
                    }
                    return recommend.getTodayExpenditurePossibleMoney();
                })
                .sum();
    }

    /**
     * 지출 추천 메시지 생성
     */
    private String generateRecommendationMessage(List<ExpenditureRecommend> recommendList) {
        List<String> overBudgetCategories = recommendList.stream()
                .filter(r -> r.getTodayExpenditurePossibleMoney() == MINIMUM_DAILY_BUDGET)
                .map(r -> r.getCategory().getName())
                .toList();

        if (overBudgetCategories.isEmpty()) {
            return "절약을 잘 실천하고 계시네요! 앞으로 남은 날도 절약을 위해 화이팅!";
        }

        String categories = String.join(", ", overBudgetCategories);
        return String.format(
                "이번 달 %s에 예산을 초과하셨네요! 오늘은 최소 %,d원 이하의 금액만 사용하시는걸 권장해 드리고 " +
                        "앞으로 남은 날은 조금 아껴 쓰셔야 하겠어요!",
                categories,
                MINIMUM_DAILY_BUDGET
        );
    }

    /**
     * 총 지출 금액 계산 및 위험도 설정
     */
    private BigDecimal calculateTotalAmountWithRisk(List<ExpenditureGuide> guideList) {
        return guideList.stream()
                .peek(guide -> {
                    BigDecimal appropriateAmount = guide.getAppropriateAmount();
                    BigDecimal todaySpent = guide.getTodaySpent();

                    if (appropriateAmount == null || appropriateAmount.compareTo(BigDecimal.ZERO) <= 0) {
                        appropriateAmount = BigDecimal.valueOf(MINIMUM_DAILY_BUDGET);
                        guide.setAppropriateAmount(appropriateAmount);
                    }

                    int risk = todaySpent
                            .multiply(BigDecimal.valueOf(100))
                            .divide(appropriateAmount, 0, RoundingMode.HALF_UP)
                            .intValue();
                    guide.setRisk(risk);
                })
                .map(ExpenditureGuide::getTodaySpent)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}