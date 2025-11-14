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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final BudgetCategoryRepository categoryRepository;
    private final UserRepository userRepository;

    /**
     * 예산 설정
     * request에서 받은 categoryName으로 카테고리를 조회 후 존재하지 않은 카테고리면 예외 처리
     * @param request money, categoryName, period
     * @param user    사용자
     */
    @Transactional
    public void createBudget(BudgetSettingRequest request, User user) {

        BudgetCategory budgets = categoryRepository.findByName(request.getCategoryName())
                .orElseThrow(() -> new BaseException(BaseExceptionStatus.NON_EXISTENT_CATEGORY));

        LocalDate date = LocalDate.of(
                request.getPeriod().getYear(),
                request.getPeriod().getMonth(),
                1
        );

        Budget exists = budgetRepository.findByCategoryAndPeriodAndUser(budgets, date, user);
        if (exists != null) {
            throw new BaseException(BaseExceptionStatus.DUPLICATE_BUDGET);
        }

        Budget budget = Budget.builder()
                .category(budgets)
                .money(request.getMoney())
                .period(date)
                .user(user)
                .build();
        Budget savedBudget = budgetRepository.save(budget);

        BudgetSettingResponse.from(savedBudget);
    }

    /**
     * 예산 수정
     * budgetId, money, user를 받아서 예산을 수정한다.
     * 만약 없는 budgetId가 들어오면 예외 발생, 수정할 예산의 유저와 다를경우 예외 발생
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
     * totalAmount를 기존 이용중인 유저들이 설정한 평균값으로 카테고리별로 적정 금액을 나눠서 반환한다.
     * @param totalAmount 평균값
     * @return list
     */
    @Transactional(readOnly = true)
    public BudgetRecommendListResponse budgetRecommend(Long totalAmount) {

        List<BudgetRecommendResponse> responseList = budgetRepository.findByAverage(totalAmount);

        return BudgetRecommendListResponse.from(responseList);
    }
}
