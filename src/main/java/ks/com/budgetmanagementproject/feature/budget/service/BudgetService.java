package ks.com.budgetmanagementproject.feature.budget.service;

import ks.com.budgetmanagementproject.feature.budget.dto.BudgetRecommendListResponse;
import ks.com.budgetmanagementproject.feature.budget.dto.BudgetRecommendResponse;
import ks.com.budgetmanagementproject.feature.budget.dto.BudgetSettingRequest;
import ks.com.budgetmanagementproject.feature.budget.dto.BudgetUpdateRequest;
import ks.com.budgetmanagementproject.feature.budget.entity.Budget;
import ks.com.budgetmanagementproject.feature.budget.entity.BudgetCategory;
import ks.com.budgetmanagementproject.feature.budget.repository.BudgetCategoryRepository;
import ks.com.budgetmanagementproject.feature.budget.repository.BudgetRepository;
import ks.com.budgetmanagementproject.feature.user.entity.User;
import ks.com.budgetmanagementproject.feature.user.repository.UserRepository;
import ks.com.budgetmanagementproject.global.common.logger.BaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static ks.com.budgetmanagementproject.global.common.logger.BaseExceptionStatus.*;

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
     * @param user 사용자
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

    /**
     * 이미 설정한 예산이라면 예외처리.
     * @param request period
     * @param user 사용자
     * @param category 카테고리
     */
    private void existsByBudget(BudgetSettingRequest request, User user, BudgetCategory category) {
        LocalDate date = LocalDate.of(request.getPeriod().getYear(), request.getPeriod().getMonth(), 1);
        Budget exists = budgetRepository.findByCategoryAndPeriodAndUser(category, date, user);
        if (exists != null) {
            throw new BaseException(DUPLICATE_BUDGET);
        }
    }


    /**
     * 예산 수정
     * budgetId, money, user를 받아서 예산을 수정한다.
     * 만약 없는 budgetId가 들어오면 예외 발생, 수정할 예산의 유저와 다를경우 예외 발생
     * @param budgetId 예산 아이디
     * @param request : money
     * @param user 사용자
     */
    @Transactional
    public void budgetUpdate(Long budgetId, BudgetUpdateRequest request, User user) {
        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new BaseException(NON_EXISTENT_BUDGET));
        if (!budget.getUser().getId().equals(user.getId())) {
            throw new BaseException(FORBIDDEN_USER);
        }
        budget.updateBudget(request.getMoney());
    }

    /**
     * 예산 Soft 삭제
     * @param userId 사용자 아이디
     */
    public void budgetSoftDelete(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(NON_EXISTENT_USER));
        user.softDeleted();
        userRepository.save(user);
    }

    /**
     * 예산 Hard 삭제
     * @param userId 사용자 아이디
     */
    public void budgetHardDelete(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(NON_EXISTENT_USER));
        userRepository.delete(user);
    }

    /**
     * 예산 추천
     * totalAmount를 기존 이용중인 유저들이 설정한 평균값으로 카테고리별로 적정 금액을 나눠서 반환한다.
     * @param totalAmount 평균값
     * @return list
     */
    @Transactional(readOnly = true)
    public BudgetRecommendListResponse budgetRecommend(long totalAmount) {
        List<BudgetRecommendResponse> responseList = budgetRepository.findByAverage(totalAmount);

        return new BudgetRecommendListResponse(responseList);
    }
}
