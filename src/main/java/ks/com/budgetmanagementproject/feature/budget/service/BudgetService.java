package ks.com.budgetmanagementproject.feature.budget.service;

import jakarta.persistence.EntityNotFoundException;
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

    /**
     * 이미 설정한 예산이라면 예외처리.
     * @param request
     * @param user
     * @param category
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
     * @param budgetId
     * @param request : money
     * @param user
     */
    @Transactional
    public void budgetUpdate(Long budgetId, BudgetUpdateRequest request, User user) {
        Budget budget = budgetRepository.findById(budgetId).orElseThrow(() -> new BaseException(NON_EXISTENT_BUDGET));
        if (!budget.getUser().getId().equals(user.getId())) {
            throw new BaseException(FORBIDDEN_USER);
        }
        budget.updateBudget(request.getMoney());
    }

    /**
     * 예산 Soft 삭제
     * @param userId
     */
    public void budgetSoftDelete(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));
        user.softDeleted();
    }
}
