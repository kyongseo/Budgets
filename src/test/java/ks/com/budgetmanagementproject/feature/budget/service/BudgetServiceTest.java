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
import ks.com.budgetmanagementproject.global.common.logger.BaseExceptionStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BudgetServiceTest {

    @Mock
    BudgetRepository budgetRepository;
    @Mock
    BudgetCategoryRepository categoryRepository;
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    BudgetService budgetService;

    private final User user = User.builder().id(1L).build();
    private final User other = User.builder().id(2L).build();
    private final BudgetCategory food = BudgetCategory.builder().id(10L).name("식비").build();
    private final BudgetCategory transport = BudgetCategory.builder().id(3L).name("교통비").build();


    @Nested
    @DisplayName("budgetSetting")
    class BudgetSetting {

        @Test
        @DisplayName("실패_카테고리_없음")
        void fail_when_category_not_found() {
            BudgetSettingRequest req = BudgetSettingRequest.builder()
                    .categoryName("없는카테고리")
                    .money(BigDecimal.valueOf(50_000L))
                    .period(YearMonth.from(LocalDate.of(2025, 9, 1)))
                    .build();
            given(categoryRepository.findByName("없는카테고리")).willReturn(Optional.empty());

            assertThatThrownBy(() -> budgetService.createBudget(req, String.valueOf(user)))
                    .isInstanceOf(BaseException.class);
        }

        @Test
        @DisplayName("실패_중복예산")
        void fail_when_budget_already_exists() {
            BudgetSettingRequest req = BudgetSettingRequest.builder()
                    .categoryName("식비")
                    .money(BigDecimal.valueOf(50_000L))
                    .period(YearMonth.from(LocalDate.of(2025, 9, 30)))
                    .build();
            given(categoryRepository.findByName("식비")).willReturn(Optional.of(food));
            LocalDate key = LocalDate.of(2025, 9, 1);
            Budget exists = Budget.builder().id(100L).category(food).user(user).money(BigDecimal.valueOf(10_000L)).period(key).build();
            given(budgetRepository.findByCategoryAndPeriodAndUser(food, key, user)).willReturn(exists);

            assertThatThrownBy(() -> budgetService.createBudget(req, String.valueOf(user)))
                    .isInstanceOf(BaseException.class)
                    .hasFieldOrPropertyWithValue("status", BaseExceptionStatus.DUPLICATE_BUDGET);
        }
    }

    @Nested
    @DisplayName("budgetUpdate")
    class BudgetUpdate {

        @Test
        @DisplayName("성공_본인소유_금액수정")
        void success_update_amount_by_owner() {
            // give
            Budget budget = Budget.builder()
                    .id(1L).user(user).category(food)
                    .money(BigDecimal.valueOf(100_000L))
                    .period(LocalDate.of(2025, 9, 1))
                    .build();
            given(budgetRepository.findById(1L)).willReturn(Optional.of(budget));

            BudgetUpdateRequest req = BudgetUpdateRequest.builder().money(BigDecimal.valueOf(150_000L)).build();

            // when
            budgetService.budgetUpdate(1L, req);

            // then
            assertThat(budget.getMoney()).isEqualByComparingTo(BigDecimal.valueOf(150_000L));
        }

        @Test
        @DisplayName("실패_예산없음")
        void fail_when_budget_not_found() {
            given(budgetRepository.findById(99L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> budgetService.budgetUpdate(99L,
                    BudgetUpdateRequest.builder().money(BigDecimal.valueOf(1L)).build()))
                    .isInstanceOf(BaseException.class);
        }
    }

    @Nested
    @DisplayName("budgetSoftDelete")
    class BudgetSoftDelete {

        @Test
        @DisplayName("성공_소프트삭제_save호출")
        void success_soft_delete_calls_save() {
            // give
            Budget b = Budget.builder()
                    .id(10L)
                    .user(user)
                    .category(food)
                    .money(BigDecimal.TEN)
                    .period(LocalDate.now())
                    .build();

            given(budgetRepository.findById(10L)).willReturn(Optional.of(b));

            // when
            budgetService.budgetSoftDelete(10L);

            //then
            then(budgetRepository).should().save(b);
            assertThat(b.getDeletedAt()).isNotNull();
        }

        @Test
        @DisplayName("실패_유저없음")
        void fail_when_budget_not_found() {
            given(budgetRepository.findById(10L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> budgetService.budgetSoftDelete(10L))
                    .isInstanceOf(BaseException.class);
        }
    }

    @Nested
    @DisplayName("budgetHardDelete")
    class BudgetHardDelete {

        @Test
        @DisplayName("성공_하드삭제_delete호출")
        void success_hard_delete_calls_delete() {
            // give
            Budget b = Budget.builder()
                    .id(11L)
                    .user(user)
                    .category(food)
                    .money(BigDecimal.TEN)
                    .period(LocalDate.now())
                    .build();

            given(budgetRepository.findById(11L)).willReturn(Optional.of(b));

            // when
            budgetService.budgetHardDelete(11L);

            // then
            then(budgetRepository).should().delete(b);
        }

        @Test
        @DisplayName("실패_유저없음")
        void fail_when_budget_not_found() {
            given(budgetRepository.findById(11L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> budgetService.budgetHardDelete(11L))
                    .isInstanceOf(BaseException.class);
        }
    }


    @Nested
    @DisplayName("budgetRecommend")
    class BudgetRecommend {

        @Test
        @DisplayName("성공_예산데이터기반_추천금액_계산")
        void success_calculate_recommended_amount_based_on_budget_data() {
            // give
            long totalAmount = 1_000_000L;

            List<Budget> budgets = List.of(
                    Budget.builder()
                            .id(1L)
                            .category(food)
                            .money(new BigDecimal("600000"))
                            .period(LocalDate.now())
                            .user(user)
                            .build(),
                    Budget.builder()
                            .id(2L)
                            .category(transport)
                            .money(new BigDecimal("400000"))
                            .period(LocalDate.now())
                            .user(user)
                            .build()
            );

            given(budgetRepository.findAllWithCategory()).willReturn(budgets);

            // when
            BudgetRecommendListResponse resp = budgetService.budgetRecommend(totalAmount);

            // then
            then(budgetRepository).should().findAllWithCategory();
            assertThat(resp.getResponseList()).hasSize(2);

            // 식비 검증 (ID=10)
            BudgetRecommendResponse foodRecommend = resp.getResponseList().stream()
                    .filter(r -> r.getCategory().getId().equals(10L))
                    .findFirst()
                    .orElseThrow();
            assertThat(foodRecommend.getCategory().getName()).isEqualTo("식비");
            assertThat(foodRecommend.getAverage()).isEqualTo(600_000L);

            // 교통비 검증 (ID=3)
            BudgetRecommendResponse transportRecommend = resp.getResponseList().stream()
                    .filter(r -> r.getCategory().getId().equals(3L))
                    .findFirst()
                    .orElseThrow();
            assertThat(transportRecommend.getCategory().getName()).isEqualTo("교통비");
            assertThat(transportRecommend.getAverage()).isEqualTo(400_000L);
        }

        @Test
        @DisplayName("성공_예산데이터_없을때_빈리스트_반환")
        void success_return_empty_list_when_no_budget_data() {
            // give
            long totalAmount = 1_000_000L;
            given(budgetRepository.findAllWithCategory()).willReturn(Collections.emptyList());

            // when
            BudgetRecommendListResponse resp = budgetService.budgetRecommend(totalAmount);

            // then
            then(budgetRepository).should().findAllWithCategory();
            assertThat(resp.getResponseList()).isEmpty();
        }
    }
}