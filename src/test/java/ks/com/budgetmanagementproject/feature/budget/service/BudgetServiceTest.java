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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class BudgetServiceTest {

    @Mock
    BudgetRepository budgetRepository;
    @Mock
    BudgetCategoryRepository categoryRepository;
    @Mock
    UserRepository userRepository;

    @InjectMocks
    BudgetService service;

    // fixtures
    private final User user = User.builder().id(1L).build();
    private final User other = User.builder().id(2L).build();
    private final BudgetCategory food = BudgetCategory.builder().id(10L).name("식비").build();

    @Nested
    @DisplayName("budgetSetting")
    class BudgetSetting {

        @Test
        void 성공_카테고리존재_중복없음_저장() {
            // given
            BudgetSettingRequest req = BudgetSettingRequest.builder()
                    .categoryName("식비")
                    .money(BigDecimal.valueOf(100_000L))
                    .period(YearMonth.from(LocalDate.of(2025, 9, 15)))
                    .build();
            given(categoryRepository.findByName("식비")).willReturn(Optional.of(food));
            LocalDate key = LocalDate.of(2025, 9, 1);
            given(budgetRepository.findByCategoryAndPeriodAndUser(food, key, user)).willReturn(null);

            // when
            service.budgetSetting(req, user);

            // then
            then(budgetRepository).should().save(argThat(b ->
                    false
            ));
        }

        @Test
        @DisplayName("없는 카테고리 뜨는지 테스트")
        void 실패_카테고리없음() {
            BudgetSettingRequest req = BudgetSettingRequest.builder()
                    .categoryName("없는카테고리")
                    .money(BigDecimal.valueOf(50_000L))
                    .period(YearMonth.from(LocalDate.of(2025, 9, 1)))
                    .build();
            given(categoryRepository.findByName("없는카테고리")).willReturn(Optional.empty());

            assertThatThrownBy(() -> service.budgetSetting(req, user))
                    .isInstanceOf(BaseException.class);
        }

        @Test
        void 실패_중복예산() {
            BudgetSettingRequest req = BudgetSettingRequest.builder()
                    .categoryName("식비")
                    .money(BigDecimal.valueOf(50_000L))
                    .period(YearMonth.from(LocalDate.of(2025, 9, 30)))
                    .build();
            given(categoryRepository.findByName("식비")).willReturn(Optional.of(food));
            LocalDate key = LocalDate.of(2025, 9, 1);
            Budget exists = Budget.builder().id(100L).category(food).user(user).money(BigDecimal.valueOf(10_000L)).period(key).build();
            given(budgetRepository.findByCategoryAndPeriodAndUser(food, key, user)).willReturn(exists);

            assertThatThrownBy(() -> service.budgetSetting(req, user))
                    .isInstanceOf(BaseException.class);
        }
    }

    @Nested
    @DisplayName("budgetUpdate")
    class BudgetUpdate {

        @Test
        void 성공_본인소유_금액수정() {
            Budget budget = Budget.builder()
                    .id(1L).user(user).category(food)
                    .money(BigDecimal.valueOf(100_000L))
                    .period(LocalDate.of(2025, 9, 1))
                    .build();
            given(budgetRepository.findById(1L)).willReturn(Optional.of(budget));

            BudgetUpdateRequest req = BudgetUpdateRequest.builder().money(BigDecimal.valueOf(150_000L)).build();

            service.budgetUpdate(1L, req, user);

            assertThat(budget.getMoney()).isEqualTo(150_000L);
        }

        @Test
        void 실패_예산없음() {
            given(budgetRepository.findById(99L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> service.budgetUpdate(99L,
                    BudgetUpdateRequest.builder().money(BigDecimal.valueOf(1L)).build(), user))
                    .isInstanceOf(BaseException.class);
        }

        @Test
        void 실패_소유자아님() {
            Budget budget = Budget.builder().id(1L).user(other).category(food).money(BigDecimal.valueOf(10L))
                    .period(LocalDate.of(2025, 9, 1)).build();
            given(budgetRepository.findById(1L)).willReturn(Optional.of(budget));

            assertThatThrownBy(() -> service.budgetUpdate(1L,
                    BudgetUpdateRequest.builder().money(BigDecimal.valueOf(1L)).build(), user))
                    .isInstanceOf(BaseException.class);
        }
    }

    @Nested
    @DisplayName("budgetSoftDelete")
    class BudgetSoftDelete {

        @Test
        void 성공_소프트삭제_save호출() {
            User u = User.builder().id(10L).build();
            given(userRepository.findById(10L)).willReturn(Optional.of(u));

            service.budgetSoftDelete(10L);

            then(userRepository).should().save(u);
        }

        @Test
        void 실패_유저없음() {
            given(userRepository.findById(10L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> service.budgetSoftDelete(10L))
                    .isInstanceOf(BaseException.class);
        }
    }

    @Nested
    @DisplayName("budgetHardDelete")
    class BudgetHardDelete {

        @Test
        void 성공_하드삭제_delete호출() {
            User u = User.builder().id(11L).build();
            given(userRepository.findById(11L)).willReturn(Optional.of(u));

            service.budgetHardDelete(11L);

            then(userRepository).should().delete(u);
        }

        @Test
        void 실패_유저없음() {
            given(userRepository.findById(11L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> service.budgetHardDelete(11L))
                    .isInstanceOf(BaseException.class);
        }
    }

    @Nested
    @DisplayName("budgetRecommend")
    class BudgetRecommend {

        @Test
        void 성공_리포지토리위임_결과반환() {
            long totalAmount = 1_000_000L;
            List<BudgetRecommendResponse> list = List.of(
                    new BudgetRecommendResponse(food, 300_000L)
            );
            given(budgetRepository.findByAverage(totalAmount)).willReturn(list);

            // when
            BudgetRecommendListResponse resp = service.budgetRecommend(totalAmount);

            // then
            then(budgetRepository).should().findByAverage(totalAmount);

            // 크기 검증
            assertThat(resp.getResponseList()).hasSize(1);
            assertThat(resp.getResponseList().get(0).getCategory().getName()).isEqualTo("식비");
            assertThat(resp.getResponseList().get(0).getAverage()).isEqualTo(300_000L);
        }
    }
}