package ks.com.budgetmanagementproject.feature.expenditure.service;

import ks.com.budgetmanagementproject.feature.budget.entity.Budget;
import ks.com.budgetmanagementproject.feature.budget.entity.BudgetCategory;
import ks.com.budgetmanagementproject.feature.budget.repository.BudgetCategoryRepository;
import ks.com.budgetmanagementproject.feature.budget.repository.BudgetRepository;
import ks.com.budgetmanagementproject.feature.expenditure.dto.*;
import ks.com.budgetmanagementproject.feature.expenditure.entity.Expenditure;
import ks.com.budgetmanagementproject.feature.expenditure.repository.ExpenditureRepository;
import ks.com.budgetmanagementproject.feature.user.entity.User;
import ks.com.budgetmanagementproject.global.common.logger.BaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static ks.com.budgetmanagementproject.global.common.logger.BaseExceptionStatus.*;

@Service
@RequiredArgsConstructor
public class ExpenditureService {

    private final ExpenditureRepository expenditureRepository;
    private final BudgetCategoryRepository categoryRepository;
    private final BudgetRepository budgetRepository;

    /**
     * 지출 생성
     * @param request money, memo, category, period
     * @param user 사용자
     */
    @Transactional
    public void expenditureCreate(ExpenditureCreateRequest request, User user) {
        BudgetCategory category = categoryRepository.findByName(request.getCategoryName())
                .orElseThrow(() -> new BaseException(NON_EXISTENT_CATEGORY));
        Expenditure expenditure = Expenditure.builder()
                .money(request.getMoney())
                .memo(request.getMemo())
                .category(category)
                .period(request.getPeriod())
                .user(user)
                .build();

        expenditureRepository.save(expenditure);

        LocalDate date = LocalDate.of(request.getPeriod().getYear(), request.getPeriod().getMonth(), 1);
        Budget budget = budgetRepository.findByCategoryAndPeriodAndUser(category, date, user);

        if (budget == null) {
            throw new BaseException(NON_EXISTENT_BUDGET);
        }

        BigDecimal updatedMoney = budget.getMoney().subtract(request.getMoney());
        budget.updateBudget(updatedMoney);
    }

    /**
     * 지출 수정
     * @param expenditureId 지출 아이디
     * @param request money, memo, category, period
     * @param user 사용지
     */
    @Transactional
    public void expenditureUpdate(Long expenditureId, ExpenditureUpdateRequest request, User user) {
        Expenditure expenditure = expenditureRepository.findById(expenditureId)
                .orElseThrow(() -> new BaseException(NON_EXISTENT_EXPENDITURE));
        BudgetCategory category = categoryRepository.findByName(request.getCategoryName()).orElseThrow(() -> new BaseException(NON_EXISTENT_CATEGORY));
        if (!expenditure.getUser().getId().equals(user.getId())) {
            throw new BaseException(FORBIDDEN_USER);
        }
        expenditure.updateExpenditure(request, category);
    }

    /**
     * 지출 목록 조회
     * @param request 요청
     * @param user 사용자
     * @return 응답
     */
    @Transactional(readOnly = true)
    public ExpenditureListResponse getExpenditureList(ExpenditureListRequest request, User user) {

        BudgetCategory category = null;
        if (request.getCategoryName() != null) {
            category = categoryRepository.findByName(request.getCategoryName())
                    .orElseThrow(() -> new BaseException(NON_EXISTENT_CATEGORY));
        }

        BigDecimal minMoney = request.getMinMoney() != null ? request.getMinMoney() : BigDecimal.ZERO;
        BigDecimal maxMoney = request.getMaxMoney() != null ? request.getMaxMoney() : new BigDecimal("999999999");

        List<ExpenditureList> expenditures = expenditureRepository.findExpenditureList(
                request.getMinPeriod(),
                request.getMaxPeriod(),
                category,
                user,
                minMoney,
                maxMoney);

        long viewMoneyTotal = expenditureRepository.findViewMoneyTotal(
                request.getMinPeriod(),
                request.getMaxPeriod(),
                category,
                user,
                request.getMinMoney(),
                request.getMaxMoney());

        long totalCategoryMoneyTotal = expenditureRepository.findTotalByCategory(category, user);

        return ExpenditureListResponse.of(expenditures, viewMoneyTotal, totalCategoryMoneyTotal);
    }

    /**
     * 지출 상세 조회
     * @param expenditureId 지출 아이디
     * @param user 사용자
     * @return response
     */
    @Transactional(readOnly = true)
    public ExpenditureDetailResponse expenditureDetail(Long expenditureId, User user) {
        Expenditure expenditure = expenditureRepository.findById(expenditureId)
                .orElseThrow(() -> new BaseException(NON_EXISTENT_EXPENDITURE));

        if (!expenditure.getUser().getId().equals(user.getId())) {
            throw new BaseException(FORBIDDEN_USER);
        }

        return new ExpenditureDetailResponse(expenditure.getMemo(), expenditure.getPeriod(), expenditure.getCategory().getName(), expenditure.isExcludingTotal(), expenditure.getMoney());
    }

    /**
     * 지출 Soft 삭제
     * @param expenditureId 지출 아이디
     * @param user 사용자
     */
    public void expenditureSoftDelete(Long expenditureId, User user) {
        Expenditure expenditure = expenditureRepository.findById(expenditureId)
                .orElseThrow(() -> new BaseException(NON_EXISTENT_EXPENDITURE));

        if (!expenditure.getUser().getId().equals(user.getId())) {
            throw new BaseException(FORBIDDEN_USER);
        }

        expenditure.softDeleted();
        expenditureRepository.save(expenditure);
    }

    /**
     * 지출 Hard 삭제
     * @param expenditureId 지출 아이디
     * @param user 사용자
     */
    @Transactional
    public void expenditureHardDelete(Long expenditureId, User user) {
        Expenditure expenditure = expenditureRepository.findById(expenditureId)
                .orElseThrow(() -> new BaseException(NON_EXISTENT_EXPENDITURE));

        if (!expenditure.getUser().getId().equals(user.getId())) {
            throw new BaseException(FORBIDDEN_USER);
        }

        expenditureRepository.delete(expenditure);
    }

    /**
     * 지출 합계 제외 업데이트
     * @param expenditureId 아이디
     * @param user 사용자
     * @param request 요청
     */
    @Transactional
    public void updateExpenditureExclude(Long expenditureId, User user, ExpenditureExcludeRequest request) {

        Expenditure expenditure = expenditureRepository.findById(expenditureId)
                .orElseThrow(() -> new BaseException(NON_EXISTENT_EXPENDITURE));

        validateExpenditureOwner(expenditure, user);

        expenditure.excludingTotalUpdate(request.getExcludingTotal());
    }

    private void validateExpenditureOwner(Expenditure expenditure, User user) {
        if (!expenditure.isOwnedBy(user)) {
            throw new BaseException(FORBIDDEN_USER);
        }
    }

    /**
     * 지출 추천
     * @param user 사용자
     * @return response
     */
    @Transactional(readOnly = true)
    public ExpenditureRecommendResponse expenditureRecommend(User user) {
        LocalDate today = LocalDate.now();
        LocalDate start = today.withDayOfMonth(1);
        LocalDate end = today.withDayOfMonth(today.lengthOfMonth());
        long period = ChronoUnit.DAYS.between(today, end);

        List<ExpenditureRecommend> recommendList = budgetRepository.findByExpenditureRecommend(user, start, period);

        StringBuilder message = new StringBuilder("이번 달 ");
        long todayExpenditurePossibleTotal = 0;

        for (ExpenditureRecommend recommend : recommendList) {
            if (recommend.getTodayExpenditurePossibleMoney() <= 0) {
                recommend.setTodayExpenditurePossibleMoney(20000L);
                message.append(recommend.getCategory().getName()).append(",");
            }
            todayExpenditurePossibleTotal += recommend.getTodayExpenditurePossibleMoney();
        }

        if (message.toString().equals("이번 달 ")) {
            message = new StringBuilder("절약을 잘 실천하고 계시네요! 앞으로 남은 날도 절약을 위해 화이팅!");
        } else {
            message = new StringBuilder(message.substring(0, message.length() - 1));
            message.append("에 예산을 초과하셨네요! 오늘은 최소 20,000원 이하의 금액만 사용하시는걸 권장해 드리고 앞으로 남은 날은 조금 아껴 쓰셔야 하겠어요!");
        }

        return new ExpenditureRecommendResponse(recommendList, todayExpenditurePossibleTotal, message.toString());
    }

    /**
     * 지출 안내
     * @param user 사용자
     * @return response
     */
    @Transactional(readOnly = true)
    public ExpenditureGuideResponse expenditureGuide(User user) {
        LocalDate today = LocalDate.now();
        LocalDate start = today.withDayOfMonth(1);
        LocalDate end = today.withDayOfMonth(today.lengthOfMonth());
        long period = ChronoUnit.DAYS.between(today, end);

        List<ExpenditureGuide> list = expenditureRepository.findByExpenditureAmount(user, start, today, period);

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (ExpenditureGuide expenditureGuide : list) {
            if (expenditureGuide.getTodayAppropriateExpenditureAmount().compareTo(BigDecimal.ZERO) <= 0) {
                expenditureGuide.setTodayAppropriateExpenditureAmount(BigDecimal.valueOf(20000));
            }

            totalAmount = totalAmount.add(expenditureGuide.getTodayExpenditureAmount());

            BigDecimal risk = expenditureGuide.getTodayExpenditureAmount()
                    .multiply(BigDecimal.valueOf(100))
                    .divide(expenditureGuide.getTodayAppropriateExpenditureAmount(), 0, RoundingMode.HALF_UP);
            expenditureGuide.setRisk(risk + "%");
        }

        return new ExpenditureGuideResponse(list, totalAmount);
    }
}