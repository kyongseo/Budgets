package ks.com.budgetmanagementproject.feature.expenditure.service;

import ks.com.budgetmanagementproject.feature.budget.entity.Budget;
import ks.com.budgetmanagementproject.feature.budget.entity.BudgetCategory;
import ks.com.budgetmanagementproject.feature.budget.repository.BudgetCategoryRepository;
import ks.com.budgetmanagementproject.feature.budget.repository.BudgetRepository;
import ks.com.budgetmanagementproject.feature.expenditure.repository.ExpenditureRepository;
import ks.com.budgetmanagementproject.feature.expenditure.dto.ExpenditureUpdateRequest;
import ks.com.budgetmanagementproject.feature.expenditure.dto.*;
import ks.com.budgetmanagementproject.feature.expenditure.entity.Expenditure;
import ks.com.budgetmanagementproject.feature.user.entity.User;
import ks.com.budgetmanagementproject.global.common.logger.BaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
     * request에서 받은 categoryName으로 카테고리를 조회 후 존재하지 않은 카테고리면 예외처리하고,
     * request에서 받은 값들을 저장하고,
     * 지정된 카테고리 예산에서 마이너스 해준다.
     * @param request money, memo, category, period
     * @param user 사용자
     */
    @Transactional
    public void expenditureCreate(ExpenditureCreateRequest request, User user) {
        BudgetCategory category = categoryRepository.findByName(request.getCategoryName()).orElseThrow(() -> new BaseException(NON_EXISTENT_CATEGORY));
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

        budget.updateBudget(budget.getMoney() - request.getMoney());
    }

    /**
     * 지출 수정
     * money, memo, category, period을 수정한다.
     * request에서 받은 categoryName으로 카테고리를 조회 후 존재하지 않은 카테고리면 예외 발생
     * 존재하지 않는 expenditureId가 들어오면 예외 발생,
     * 수정할 지출의 유저와 다를경우 예외 발생
     * @param expenditureId 지출 아이디
     * @param request money, memo, category, period
     * @param user 사용지
     */
    @Transactional
    public void expenditureUpdate(Long expenditureId, ExpenditureUpdateRequest request, User user) {
        Expenditure expenditure = expenditureRepository.findById(expenditureId).orElseThrow(() -> new BaseException(NON_EXISTENT_EXPENDITURE));
        BudgetCategory category = categoryRepository.findByName(request.getCategoryName()).orElseThrow(() -> new BaseException(NON_EXISTENT_CATEGORY));
        if (!expenditure.getUser().getId().equals(user.getId())) {
            throw new BaseException(FORBIDDEN_USER);
        }
        expenditure.updateExpenditure(request, category);
    }

    /**
     * 지출 목록 조회
     * 기간, 카테고리, 최소, 최대 금액으로 지출 목록을 조회한다.
     * 조회된 모든 내용의 지출 합계, 카테고리별 지출 합계를 같이 반환합니다.
     * request에서 받은 categoryName으로 카테고리를 조회 후 존재하지 않은 카테고리면 예외 발생
     * @param minPeriod 최소 기간
     * @param maxPeriod 최대 기간
     * @param categoryName 카테고리 이름
     * @param minMoney 최소 금액
     * @param maxMoney 최대 금액
     * @param user 시용자
     * @return response
     */
    @Transactional(readOnly = true)
    public ExpenditureListResponse expenditureList(LocalDate minPeriod, LocalDate maxPeriod,
                                                   String categoryName, long minMoney, long maxMoney, User user) {
        BudgetCategory category = categoryRepository.findByName(categoryName).orElseThrow(() -> new BaseException(NON_EXISTENT_CATEGORY));
        List<ExpenditureList> expenditureList = expenditureRepository.findExpenditureList(minPeriod, maxPeriod, category, user, minMoney, maxMoney);
        long viewMoneyTotal = expenditureRepository.findViewMoneyTotal(minPeriod, maxPeriod, category, user, minMoney, maxMoney);
        long totalCategoryMoneyTotal = expenditureRepository.findTotalByCategory(category, user);

        return new ExpenditureListResponse(expenditureList, viewMoneyTotal, totalCategoryMoneyTotal);
    }

    /**
     * 지출 상세 조회
     * expenditureId로 지출 상세 조회한다.
     * 존재하지 않는 expenditureId가 들어오면 예외 발생,
     * 조회할 지출의 유저와 다를경우 예외 발생
     * @param expenditureId 지출 아이디
     * @param user 사용자
     * @return response
     */
    @Transactional(readOnly = true)
    public ExpenditureDetailResponse expenditureDetail(Long expenditureId, User user) {
        Expenditure expenditure = expenditureRepository.findById(expenditureId).orElseThrow(() -> new BaseException(NON_EXISTENT_EXPENDITURE));

        if (!expenditure.getUser().getId().equals(user.getId())) {
            throw new BaseException(FORBIDDEN_USER);
        }

        return new ExpenditureDetailResponse(expenditure.getMemo(), expenditure.getPeriod(), expenditure.getCategory().getName(), expenditure.isExcludingTotal(), expenditure.getMoney());
    }

    /**
     * 지출 삭제
     * expenditureId로 지출을 삭제한다.
     * 존재하지 않는 expenditureId가 들어오면 예외 발생,
     * 삭제할 지출의 유저와 다를경우 예외 발생
     * @param expenditureId 지출 아이디
     * @param user 사용자
     */
    @Transactional
    public void expenditureDelete(Long expenditureId, User user) {
        Expenditure expenditure = expenditureRepository.findById(expenditureId).orElseThrow(() -> new BaseException(NON_EXISTENT_EXPENDITURE));

        if (!expenditure.getUser().getId().equals(user.getId())) {
            throw new BaseException(FORBIDDEN_USER);
        }

        expenditureRepository.delete(expenditure);
    }

    /**
     * 지출 합계 제외 업데이트
     * expenditureId, excludingTotal로 지출 합계 제외를 업데이트한다.
     * 존재하지 않는 expenditureId가 들어오면 예외 발생,
     * 업데이트할 지출의 유저와 다를경우 예외 발생
     * @param expenditureId 지출 아이디
     * @param user 사용자
     * @param excludingTotal : false = 합계 제외 안함, true = 합계 제외 함.
     */
    @Transactional
    public void expenditureExceptUpdate(Long expenditureId, User user, boolean excludingTotal) {
        Expenditure expenditure = expenditureRepository.findById(expenditureId).orElseThrow(() -> new BaseException(NON_EXISTENT_EXPENDITURE));

        if (!expenditure.getUser().getId().equals(user.getId())) {
            throw new BaseException(FORBIDDEN_USER);
        }

        expenditure.excludingTotalUpdate(excludingTotal);
    }

    /**
     * 지출 추천
     * 오늘 날짜, 이번 달 마지막 날짜로 이번 달 남은 날의 기간을 구해 오늘 지출 금액을 추천한다.
     * 예산이 초과된 카테고리의 최소 금액은 20,000원으로 설정.
     * @param user 사용자
     * @return response
     */
    @Transactional(readOnly = true)
    public ExpenditureRecommendResponse expenditureRecommend(User user) {
        LocalDate today = LocalDate.now();
        /* start는 예산을 처음 저장할 때 YearMonth로 저장했기 때문에 DB에서 찾을 때 사용하기 위함. */
        LocalDate start = today.withDayOfMonth(1);
        LocalDate end = today.withDayOfMonth(today.lengthOfMonth());
        long period = ChronoUnit.DAYS.between(today, end);

        List<ExpenditureRecommend> recommendList = budgetRepository.findByExpenditureRecommend(user, start, period);

        StringBuilder message = new StringBuilder("이번 달 ");
        long todayExpenditurePossibleTotal = 0;

        for (int i = 0; i < recommendList.size(); i++) {
            ExpenditureRecommend recommend = recommendList.get(i);
            if (recommend.getTodayExpenditurePossibleMoney() <= 0) {
                /* 예산 초과시 최소 지출 가능 금액 20,000으로 설정. */
                recommend.setTodayExpenditurePossibleMoney(20000L);
                /* 예산을 초과한 카테고리 이름을 message에 저장 */
                message.append(recommend.getCategory().getName()).append(",");
            }
            todayExpenditurePossibleTotal += recommend.getTodayExpenditurePossibleMoney();
        }

        /*
         * 이번 달에 예산을 초과한 카테고리가 하나도 없다면 첫 번째 message 출력하고
         * 이번 달에 예산을 초과한 카테고리가 하나라도 있으면 위 예산을 초과한 카테고리 이름 + 두 번째 message를 출력한다.
         */
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
     * 오늘 날짜로 오늘 사용한 카테고리별 지출 금액, 총 지출 금액을 구하고
     * 오늘 날짜, 이번 달 마지막 날짜로 이번 달 남은 날의 기간을 구해 오늘 사용했으면 적절한 금액을 구한다.
     * 예산이 초과된 카테고리의 최소 금액은 20,000원으로 설정.
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

        long totalAmount = 0L;

        for (int i = 0; i < list.size(); i++) {
            ExpenditureGuide expenditureGuide = list.get(i);
            if (expenditureGuide.getTodayAppropriateExpenditureAmount() <= 0) {
                expenditureGuide.setTodayAppropriateExpenditureAmount(20000L);
            }
            totalAmount += expenditureGuide.getTodayExpenditureAmount();
            expenditureGuide.setRisk(expenditureGuide.getTodayExpenditureAmount() * 100 / expenditureGuide.getTodayAppropriateExpenditureAmount() + "%");
        }

        return new ExpenditureGuideResponse(list, totalAmount);
    }
}