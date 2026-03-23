package ks.com.budgetmanagementproject.feature.budget.service;

import ks.com.budgetmanagementproject.feature.budget.dto.BudgetRecommendListResponse;
import ks.com.budgetmanagementproject.feature.budget.dto.BudgetRecommendResponse;
import ks.com.budgetmanagementproject.feature.budget.dto.BudgetSettingRequest;
import ks.com.budgetmanagementproject.feature.budget.dto.BudgetUpdateRequest;
import ks.com.budgetmanagementproject.feature.budget.entity.Budget;
import ks.com.budgetmanagementproject.feature.budget.entity.BudgetCategory;
import ks.com.budgetmanagementproject.feature.budget.repository.BudgetCategoryRepository;
import ks.com.budgetmanagementproject.feature.budget.repository.BudgetRepository;
import ks.com.budgetmanagementproject.feature.expenditure.dto.ExpenditureCreateRequest;
import ks.com.budgetmanagementproject.feature.expenditure.dto.ExpenditureDetailResponse;
import ks.com.budgetmanagementproject.feature.expenditure.dto.ExpenditureExcludeRequest;
import ks.com.budgetmanagementproject.feature.expenditure.dto.ExpenditureUpdateRequest;
import ks.com.budgetmanagementproject.feature.expenditure.entity.Expenditure;
import ks.com.budgetmanagementproject.feature.expenditure.repository.ExpenditureRepository;
import ks.com.budgetmanagementproject.feature.expenditure.service.ExpenditureService;
import ks.com.budgetmanagementproject.feature.user.entity.User;
import ks.com.budgetmanagementproject.feature.user.repository.UserRepository;
import ks.com.budgetmanagementproject.global.common.logger.BaseException;
import ks.com.budgetmanagementproject.global.common.logger.BaseExceptionStatus;
import org.hibernate.mapping.Any;
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
import static org.awaitility.Awaitility.given;
import static org.mockito.ArgumentMatchers.*;
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
    ExpenditureService expenditureService;
    @Mock
    ExpenditureRepository expenditureRepository;

    final User user = User.builder().id(1L).username("test@test.com").build();
    final User other = User.builder().id(2L).username("other@test.com").build();
    private final BudgetCategory food = BudgetCategory.builder().id(10L).name("식비").build();
    private final BudgetCategory transport = BudgetCategory.builder().id(3L).name("교통비").build();
    private final LocalDate anyPeriod = LocalDate.of(2025, 9, 1);

    @Nested @DisplayName("지출 생성")
    class Create {

        @Test @DisplayName("성공: 카테고리/예산 존재, 예산 차감")
        void createSuccess() {
            // given
            ExpenditureCreateRequest req = ExpenditureCreateRequest.builder()
                    .money(BigDecimal.valueOf(30_000L))
                    .memo("점심")
                    .categoryName("식비")
                    .period(LocalDate.of(2025, 9, 10))
                    .build();
            given(userRepository.findByUsername(user.getUsername())).willReturn(Optional.of(user));
            given(categoryRepository.findByName("식비")).willReturn(Optional.of(food));
            LocalDate start = LocalDate.of(2025, 9, 1);
            Budget budget = Budget.builder()
                    .id(100L).category(food).user(user)
                    .money(BigDecimal.valueOf(100_000L)).period(start).build();
            given(budgetRepository.findByCategoryAndPeriodAndUser(food, start, user)).willReturn(budget);

            // when
            expenditureService.createExpenditure(req, user.getUsername());

            // then
            assertThat(budget.getMoney()).isEqualByComparingTo(BigDecimal.valueOf(70_000L));
            then(expenditureRepository).should().save(any(Expenditure.class));
        }


        @Test @DisplayName("실패: 카테고리 없음")
        void createFailNoCategory() {
            ExpenditureCreateRequest req = ExpenditureCreateRequest.builder()
                    .money(BigDecimal.valueOf(10_000L)).memo("커피")
                    .categoryName("없는카테고리").period(anyPeriod).build();
            given(userRepository.findByUsername(anyString())).willReturn(Optional.of(user));
            given(categoryRepository.findByName("없는카테고리")).willReturn(Optional.empty());

            assertThatThrownBy(() -> expenditureService.createExpenditure(req, user.getUsername()))
                    .isInstanceOf(BaseException.class)
                    .hasMessage("존재하지 않는 카테고리입니다.");
        }

        @Test @DisplayName("실패: 예산 없음")
        void createFailNoBudget() {
            ExpenditureCreateRequest req = ExpenditureCreateRequest.builder()
                    .money(BigDecimal.valueOf(10_000L)).memo("커피")
                    .categoryName("식비").period(anyPeriod).build();
            given(userRepository.findByUsername(user.getUsername())).willReturn(Optional.of(user));
            given(categoryRepository.findByName("식비")).willReturn(Optional.of(food));
            given(budgetRepository.findByCategoryAndPeriodAndUser(eq(food), any(LocalDate.class), eq(user))).willReturn(null);

            assertThatThrownBy(() -> expenditureService.createExpenditure(req, user.getUsername()))
                    .isInstanceOf(BaseException.class)
                    .hasMessage("존재하지 않는 예산입니다.");
        }
    }

    @Nested @DisplayName("지출 수정")
    class Update {

        @Test @DisplayName("성공: 본인 소유, 카테고리 존재")
        void updateSuccess() {
            ExpenditureUpdateRequest req = ExpenditureUpdateRequest.builder()
                    .money(BigDecimal.valueOf(50_000L)).memo("저녁")
                    .categoryName("식비").period(LocalDate.of(2025, 9, 12)).build();
            Expenditure entity = Expenditure.builder()
                    .id(1L).user(user).category(food)
                    .money(BigDecimal.valueOf(10_000L)).memo("old").period(anyPeriod).build();
            given(userRepository.findByUsername(user.getUsername())).willReturn(Optional.of(user));
            given(expenditureRepository.findById(1L)).willReturn(Optional.of(entity));
            given(categoryRepository.findByName("식비")).willReturn(Optional.of(food));

            expenditureService.updateExpenditure(1L, req, user.getUsername());

            assertThat(entity.getMoney()).isEqualByComparingTo(BigDecimal.valueOf(50_000L));
            assertThat(entity.getMemo()).isEqualTo("저녁");
            assertThat(entity.getCategory()).isSameAs(food);
            assertThat(entity.getPeriod()).isEqualTo(LocalDate.of(2025, 9, 12));
        }

        @Test @DisplayName("실패: 소유자 아님")
        void updateFailForbidden() {
            Expenditure entity = Expenditure.builder()
                    .id(1L).user(other).category(food)
                    .money(BigDecimal.valueOf(10_000L)).memo("old").period(anyPeriod).build();
            given(userRepository.findByUsername(user.getUsername())).willReturn(Optional.of(user));
            given(expenditureRepository.findById(1L)).willReturn(Optional.of(entity));
            given(categoryRepository.findByName(anyString())).willReturn(Optional.of(food));

            assertThatThrownBy(() -> expenditureService.updateExpenditure(1L,
                    ExpenditureUpdateRequest.builder().categoryName("식비").build(),
                    user.getUsername()))
                    .isInstanceOf(BaseException.class)
                    .hasMessage("권한이 없는 유저입니다.");
        }

        @Test @DisplayName("실패: 지출 없음")
        void updateFailNoExpenditure() {
            given(userRepository.findByUsername(user.getUsername())).willReturn(Optional.of(user));
            given(expenditureRepository.findById(999L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> expenditureService.updateExpenditure(999L,
                    ExpenditureUpdateRequest.builder().categoryName("식비").build(),
                    user.getUsername()))
                    .isInstanceOf(BaseException.class)
                    .hasMessage("존재하지 않는 지출입니다.");
        }

        @Test @DisplayName("실패: 카테고리 없음")
        void updateFailNoCategory() {
            Expenditure entity = Expenditure.builder()
                    .id(1L).user(user).category(food)
                    .money(BigDecimal.valueOf(10_000L)).memo("old").period(anyPeriod).build();
            given(userRepository.findByUsername(user.getUsername())).willReturn(Optional.of(user));
            given(expenditureRepository.findById(1L)).willReturn(Optional.of(entity));
            given(categoryRepository.findByName("식비")).willReturn(Optional.empty());

            assertThatThrownBy(() -> expenditureService.updateExpenditure(1L,
                    ExpenditureUpdateRequest.builder().categoryName("식비").build(),
                    user.getUsername()))
                    .isInstanceOf(BaseException.class)
                    .hasMessage("존재하지 않는 카테고리입니다.");
        }
    }
    @Nested @DisplayName("지출 상세조회")
    class Detail {

        @Test @DisplayName("성공: 본인 소유")
        void detailSuccess() {
            Expenditure e = Expenditure.builder()
                    .id(1L).user(user).memo("메모").period(anyPeriod)
                    .category(food).excludingTotal(false).money(BigDecimal.valueOf(12_345L)).build();
            given(userRepository.findByUsername(user.getUsername())).willReturn(Optional.of(user));
            given(expenditureRepository.findById(1L)).willReturn(Optional.of(e));

            ExpenditureDetailResponse resp = expenditureService.getExpenditureDetail(1L, user.getUsername());

            assertThat(resp.getMemo()).isEqualTo("메모");
            assertThat(resp.getCategoryName()).isEqualTo("식비");
            assertThat(resp.getMoney()).isEqualByComparingTo(BigDecimal.valueOf(12_345L));
        }

        @Test @DisplayName("실패: 소유자 아님")
        void detailFailForbidden() {
            Expenditure e = Expenditure.builder()
                    .id(1L).user(other).memo("메모").period(anyPeriod)
                    .category(food).excludingTotal(false).money(BigDecimal.valueOf(12_345L)).build();
            given(userRepository.findByUsername(user.getUsername())).willReturn(Optional.of(user));
            given(expenditureRepository.findById(1L)).willReturn(Optional.of(e));

            assertThatThrownBy(() -> expenditureService.getExpenditureDetail(1L, user.getUsername()))
                    .isInstanceOf(BaseException.class)
                    .hasMessage("권한이 없는 유저입니다.");
        }

        @Test @DisplayName("실패: 지출 없음")
        void detailFailNoExpenditure() {
            given(userRepository.findByUsername(user.getUsername())).willReturn(Optional.of(user));
            given(expenditureRepository.findById(999L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> expenditureService.getExpenditureDetail(999L, user.getUsername()))
                    .isInstanceOf(BaseException.class)
                    .hasMessage("존재하지 않는 지출입니다.");
        }
    }



    @Nested @DisplayName("지출 삭제")
    class Delete {

        @Test @DisplayName("성공: 본인 소유 하드 삭제")
        void hardDeleteSuccess() {
            Expenditure e = Expenditure.builder().id(1L).user(user).category(food).build();
            given(userRepository.findByUsername(user.getUsername())).willReturn(Optional.of(user));
            given(expenditureRepository.findById(1L)).willReturn(Optional.of(e));

            expenditureService.hardDeleteExpenditure(1L, user.getUsername());

            then(expenditureRepository).should().delete(e);
        }

        @Test @DisplayName("실패: 소유자 아님")
        void hardDeleteFailForbidden() {
            Expenditure e = Expenditure.builder().id(1L).user(other).category(food).build();
            given(userRepository.findByUsername(user.getUsername())).willReturn(Optional.of(user));
            given(expenditureRepository.findById(1L)).willReturn(Optional.of(e));

            assertThatThrownBy(() -> expenditureService.hardDeleteExpenditure(1L, user.getUsername()))
                    .isInstanceOf(BaseException.class)
                    .hasMessage("권한이 없는 유저입니다.");
        }

        @Test @DisplayName("성공: 본인 소유 소프트 삭제")
        void softDeleteSuccess() {
            Expenditure e = Expenditure.builder().id(1L).user(user).category(food).build();
            given(userRepository.findByUsername(user.getUsername())).willReturn(Optional.of(user));
            given(expenditureRepository.findById(1L)).willReturn(Optional.of(e));

            expenditureService.softDeleteExpenditure(1L, user.getUsername());

            assertThat(e.isDeleted()).isTrue();
            then(expenditureRepository).should().save(e);
        }
    }


    @Nested @DisplayName("합계 제외 업데이트")
    class ExcludeUpdate {

        @Test @DisplayName("성공: false → true")
        void excludeUpdateFalseToTrue() {
            Expenditure expenditure = Expenditure.builder()
                    .id(1L).user(user).category(food)
                    .money(new BigDecimal("10000")).period(LocalDate.of(2024, 1, 1))
                    .excludingTotal(false).build();
            ExpenditureExcludeRequest request = new ExpenditureExcludeRequest(true);
            given(userRepository.findByUsername(user.getUsername())).willReturn(Optional.of(user));
            given(expenditureRepository.findById(1L)).willReturn(Optional.of(expenditure));

            expenditureService.updateExpenditureExclude(1L, user.getUsername(), request);

            assertThat(expenditure.isExcludingTotal()).isTrue();
        }

        @Test @DisplayName("성공: true → false")
        void excludeUpdateTrueToFalse() {
            Expenditure expenditure = Expenditure.builder()
                    .id(1L).user(user).category(food)
                    .money(new BigDecimal("10000")).period(LocalDate.of(2024, 1, 1))
                    .excludingTotal(true).build();
            ExpenditureExcludeRequest request = new ExpenditureExcludeRequest(false);
            given(userRepository.findByUsername(user.getUsername())).willReturn(Optional.of(user));
            given(expenditureRepository.findById(1L)).willReturn(Optional.of(expenditure));

            expenditureService.updateExpenditureExclude(1L, user.getUsername(), request);

            assertThat(expenditure.isExcludingTotal()).isFalse();
        }

        @Test @DisplayName("실패: 지출 없음")
        void excludeUpdateFailNoExpenditure() {
            ExpenditureExcludeRequest request = new ExpenditureExcludeRequest(true);
            given(userRepository.findByUsername(user.getUsername())).willReturn(Optional.of(user));
            given(expenditureRepository.findById(999L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> expenditureService.updateExpenditureExclude(999L, user.getUsername(), request))
                    .isInstanceOf(BaseException.class)
                    .hasFieldOrPropertyWithValue("status", BaseExceptionStatus.NON_EXISTENT_EXPENDITURE);
        }

        @Test @DisplayName("실패: 권한 없음")
        void excludeUpdateFailForbidden() {
            Expenditure expenditure = Expenditure.builder()
                    .id(1L).user(other).category(food)
                    .money(new BigDecimal("10000")).period(LocalDate.of(2024, 1, 1))
                    .excludingTotal(false).build();
            ExpenditureExcludeRequest request = new ExpenditureExcludeRequest(true);
            given(userRepository.findByUsername(user.getUsername())).willReturn(Optional.of(user));
            given(expenditureRepository.findById(1L)).willReturn(Optional.of(expenditure));

            assertThatThrownBy(() -> expenditureService.updateExpenditureExclude(1L, user.getUsername(), request))
                    .isInstanceOf(BaseException.class)
                    .hasFieldOrPropertyWithValue("status", BaseExceptionStatus.FORBIDDEN_USER);
        }

        @Test @DisplayName("성공: 같은 값으로 변경")
        void excludeUpdateSameValue() {
            Expenditure expenditure = Expenditure.builder()
                    .id(1L).user(user).category(food)
                    .money(new BigDecimal("10000")).period(LocalDate.of(2024, 1, 1))
                    .excludingTotal(true).build();
            ExpenditureExcludeRequest request = new ExpenditureExcludeRequest(true);
            given(userRepository.findByUsername(user.getUsername())).willReturn(Optional.of(user));
            given(expenditureRepository.findById(1L)).willReturn(Optional.of(expenditure));

            expenditureService.updateExpenditureExclude(1L, user.getUsername(), request);

            assertThat(expenditure.isExcludingTotal()).isTrue();
        }
    }
}