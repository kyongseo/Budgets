package ks.com.budgetmanagementproject.feature.expenditure.service;

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
import ks.com.budgetmanagementproject.feature.user.entity.User;
import ks.com.budgetmanagementproject.global.common.logger.BaseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static ks.com.budgetmanagementproject.global.common.logger.BaseExceptionStatus.FORBIDDEN_USER;
import static ks.com.budgetmanagementproject.global.common.logger.BaseExceptionStatus.NON_EXISTENT_EXPENDITURE;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ExpenditureServiceTest {

    @Mock
    ExpenditureRepository expenditureRepository;

    @Mock
    BudgetCategoryRepository categoryRepository;

    @Mock
    BudgetRepository budgetRepository;

    @InjectMocks
    ExpenditureService expenditureService;

    // ---- Test fixtures
    private final User user = User.builder().id(1L).build();
    private final User other = User.builder().id(2L).build();
    private final BudgetCategory food = BudgetCategory.builder().id(10L).name("식비").build();
    private final LocalDate anyPeriod = LocalDate.of(2025, 9, 1);

    @Nested
    @DisplayName("expenditureCreate")
    class ExpenditureCreate {

        @Test
        void 성공_카테고리존재_해당월예산존재_예산차감() {

            // given
            ExpenditureCreateRequest req = ExpenditureCreateRequest.builder()
                    .money(BigDecimal.valueOf(30_000L)).memo("점심").categoryName("식비")
                    .period(LocalDate.of(2025, 9, 10)).build();

            given(categoryRepository.findByName("식비")).willReturn(Optional.of(food));

            LocalDate start = LocalDate.of(2025, 9, 1);
            Budget budget = Budget.builder()
                    .id(100L)
                    .category(food)
                    .user(user)
                    .money(BigDecimal.valueOf(100_000L))
                    .period(start)
                    .build();

            given(budgetRepository.findByCategoryAndPeriodAndUser(eq(food), eq(start), eq(user)))
                    .willReturn(budget);

            // when
            expenditureService.expenditureCreate(req, user);

            // then
            assertThat(budget.getMoney()).isEqualByComparingTo(BigDecimal.valueOf(70_000L));
            then(expenditureRepository).should().save(Mockito.any(Expenditure.class));
        }

        @Test
        void 실패_카테고리없음() {
            // given
            ExpenditureCreateRequest req = ExpenditureCreateRequest.builder()
                    .money(BigDecimal.valueOf(10_000L)).memo("커피").categoryName("없는카테고리")
                    .period(LocalDate.of(2025, 9, 2)).build();

            given(categoryRepository.findByName("없는카테고리")).willReturn(Optional.empty());

            // expect
            assertThatThrownBy(() -> expenditureService.expenditureCreate(req, user))
                    .isInstanceOf(BaseException.class)
                    .hasMessage("존재하지 않는 카테고리입니다.");
        }

        @Test
        void 실패_예산없음() {
            // given
            ExpenditureCreateRequest req = ExpenditureCreateRequest.builder()
                    .money(BigDecimal.valueOf(10_000L)).memo("커피").categoryName("식비")
                    .period(LocalDate.of(2025, 9, 2)).build();

            given(categoryRepository.findByName("식비")).willReturn(Optional.of(food));
            LocalDate start = LocalDate.of(2025, 9, 1);
            given(budgetRepository.findByCategoryAndPeriodAndUser(eq(food), eq(start), eq(user)))
                    .willReturn(null);

            // expect
            assertThatThrownBy(() -> expenditureService.expenditureCreate(req, user))
                    .isInstanceOf(BaseException.class)
                    .hasMessage("존재하지 않는 예산입니다.");
        }
    }

    @Nested
    @DisplayName("expenditureUpdate")
    class ExpenditureUpdate {

        @Test
        void 성공_본인소유_카테고리존재() {
            // given
            ExpenditureUpdateRequest req = ExpenditureUpdateRequest.builder()
                    .money(BigDecimal.valueOf(50_000L)).memo("저녁").categoryName("식비")
                    .period(LocalDate.of(2025, 9, 12)).build();

            Expenditure entity = Expenditure.builder().id(1L).user(user)
                    .category(food).money(BigDecimal.valueOf(10_000L)).memo("old").period(anyPeriod).build();

            given(expenditureRepository.findById(1L)).willReturn(Optional.of(entity));
            given(categoryRepository.findByName("식비")).willReturn(Optional.of(food));

            // when
            expenditureService.expenditureUpdate(1L, req, user);

            // then
            assertThat(entity.getMoney()).isEqualByComparingTo(BigDecimal.valueOf(50_000L));
            assertThat(entity.getMemo()).isEqualTo("저녁");
            assertThat(entity.getCategory()).isSameAs(food);
            assertThat(entity.getPeriod()).isEqualTo(LocalDate.of(2025, 9, 12));
        }

        @Test
        void 실패_소유자아님() {
            Expenditure entity = Expenditure.builder().id(1L).user(other)
                    .category(food).money(BigDecimal.valueOf(10_000L)).memo("old").period(anyPeriod).build();
            given(expenditureRepository.findById(1L)).willReturn(Optional.of(entity));
            given(categoryRepository.findByName(anyString())).willReturn(Optional.of(food));

            assertThatThrownBy(() -> expenditureService.expenditureUpdate(1L,
                    ExpenditureUpdateRequest.builder().categoryName("식비").build(), user))
                    .isInstanceOf(BaseException.class)
                    .hasMessage("권한이 없는 유저입니다.");
        }

        @Test
        void 실패_지출없음() {
            given(expenditureRepository.findById(999L)).willReturn(Optional.empty());
            assertThatThrownBy(() -> expenditureService.expenditureUpdate(999L,
                    ExpenditureUpdateRequest.builder().categoryName("식비").build(), user))
                    .isInstanceOf(BaseException.class)
                    .hasMessage("존재하지 않는 지출입니다.");
        }

        @Test
        void 실패_카테고리없음() {
            Expenditure entity = Expenditure.builder().id(1L).user(user)
                    .category(food).money(BigDecimal.valueOf(10_000L)).memo("old").period(anyPeriod).build();
            given(expenditureRepository.findById(1L)).willReturn(Optional.of(entity));
            given(categoryRepository.findByName("식비")).willReturn(Optional.empty());

            assertThatThrownBy(() -> expenditureService.expenditureUpdate(1L,
                    ExpenditureUpdateRequest.builder().categoryName("식비").build(), user))
                    .isInstanceOf(BaseException.class)
                    .hasMessage("존재하지 않는 카테고리입니다.");
        }
    }

    @Nested
    @DisplayName("expenditureDetail")
    class ExpenditureDetail {

        @Test
        void 성공_본인소유() {
            Expenditure e = Expenditure.builder().id(1L).user(user)
                    .memo("메모").period(anyPeriod).category(food).excludingTotal(false)
                    .money(BigDecimal.valueOf(12_345L)).build();
            given(expenditureRepository.findById(1L)).willReturn(Optional.of(e));

            ExpenditureDetailResponse resp = expenditureService.expenditureDetail(1L, user);

            assertThat(resp.getMemo()).isEqualTo("메모");
            assertThat(resp.getCategoryName()).isEqualTo("식비");
            assertThat(resp.getMoney()).isEqualByComparingTo(BigDecimal.valueOf(12_345L));
        }

        @Test
        void 실패_소유자아님() {
            Expenditure e = Expenditure.builder().id(1L).user(other)
                    .memo("메모").period(anyPeriod).category(food).excludingTotal(false).money(BigDecimal.valueOf(12_345L)).build();
            given(expenditureRepository.findById(1L)).willReturn(Optional.of(e));

            assertThatThrownBy(() -> expenditureService.expenditureDetail(1L, user))
                    .isInstanceOf(BaseException.class)
                    .hasMessage("권한이 없는 유저입니다.");
        }

        @Test
        void 실패_지출없음() {
            given(expenditureRepository.findById(999L)).willReturn(Optional.empty());
            assertThatThrownBy(() -> expenditureService.expenditureDetail(999L, user))
                    .isInstanceOf(BaseException.class)
                    .hasMessage("존재하지 않는 지출입니다.");
        }
    }

    @Nested
    @DisplayName("expenditureDelete")
    class ExpenditureDelete {

        @Test
        void 성공_본인소유_삭제() {
            Expenditure e = Expenditure.builder().id(1L).user(user).category(food).build();
            given(expenditureRepository.findById(1L)).willReturn(Optional.of(e));

            expenditureService.expenditureHardDelete(1L, user);

            then(expenditureRepository).should().delete(e);
        }

        @Test
        void 실패_소유자아님() {
            Expenditure e = Expenditure.builder().id(1L).user(other).category(food).build();
            given(expenditureRepository.findById(1L)).willReturn(Optional.of(e));

            assertThatThrownBy(() -> expenditureService.expenditureHardDelete(1L, user))
                    .isInstanceOf(BaseException.class)
                    .hasMessage("권한이 없는 유저입니다.");
        }
    }

    @Nested
    @DisplayName("updateExpenditureExclude")
    class UpdateExpenditureExcludeTest {

        private User user;
        private User otherUser;
        private BudgetCategory category;

        @BeforeEach
        void setUp() {
            user = User.builder()
                    .id(1L)
                    .username("test@test.com")
                    .build();

            otherUser = User.builder()
                    .id(2L)
                    .username("other@test.com")
                    .build();

            category = BudgetCategory.builder()
                    .id(1L)
                    .name("식비")
                    .build();
        }

        @Test
        @DisplayName("합계 제외 업데이트 성공 - false에서 true로 변경")
        void updateExcludingTotalSuccess_FalseToTrue() {
            // given
            Expenditure expenditure = Expenditure.builder()
                    .id(1L)
                    .user(user)
                    .category(category)
                    .money(new BigDecimal("10000"))
                    .period(LocalDate.of(2024, 1, 1))
                    .excludingTotal(false)
                    .build();

            ExpenditureExcludeRequest request = new ExpenditureExcludeRequest(true);

            given(expenditureRepository.findById(1L))
                    .willReturn(Optional.of(expenditure));

            // when
            expenditureService.updateExpenditureExclude(1L, user, request);

            // then
            assertThat(expenditure.isExcludingTotal()).isTrue();
            verify(expenditureRepository).findById(1L);
        }

        @Test
        @DisplayName("합계 제외 업데이트 성공 - true에서 false로 변경")
        void updateExcludingTotalSuccess_TrueToFalse() {
            // given
            Expenditure expenditure = Expenditure.builder()
                    .id(1L)
                    .user(user)
                    .category(category)
                    .money(new BigDecimal("10000"))
                    .period(LocalDate.of(2024, 1, 1))
                    .excludingTotal(true)
                    .build();

            ExpenditureExcludeRequest request = new ExpenditureExcludeRequest(false);

            given(expenditureRepository.findById(1L))
                    .willReturn(Optional.of(expenditure));

            // when
            expenditureService.updateExpenditureExclude(1L, user, request);

            // then
            assertThat(expenditure.isExcludingTotal()).isFalse();
            verify(expenditureRepository).findById(1L);
        }

        @Test
        @DisplayName("합계 제외 업데이트 실패 - 지출 내역이 존재하지 않음")
        void updateExcludingTotalFail_ExpenditureNotFound() {
            // given
            ExpenditureExcludeRequest request = new ExpenditureExcludeRequest(true);

            given(expenditureRepository.findById(999L))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() ->
                    expenditureService.updateExpenditureExclude(999L, user, request))
                    .isInstanceOf(BaseException.class)
                    .hasFieldOrPropertyWithValue("status", NON_EXISTENT_EXPENDITURE);
            verify(expenditureRepository).findById(999L);
        }

        @Test
        @DisplayName("합계 제외 업데이트 실패 - 권한 없는 사용자")
        void updateExcludingTotalFail_Forbidden() {
            // given
            ExpenditureExcludeRequest request = new ExpenditureExcludeRequest(true);

            Expenditure expenditure = Expenditure.builder()
                    .id(1L)
                    .user(otherUser)
                    .category(category)
                    .money(new BigDecimal("10000"))
                    .period(LocalDate.of(2024, 1, 1))
                    .excludingTotal(false)
                    .build();

            given(expenditureRepository.findById(1L))
                    .willReturn(Optional.of(expenditure));

            // when & then
            assertThatThrownBy(() ->
                    expenditureService.updateExpenditureExclude(1L, user, request))
                    .isInstanceOf(BaseException.class)
                    .hasFieldOrPropertyWithValue("status", FORBIDDEN_USER);

            assertThat(expenditure.isExcludingTotal()).isFalse(); // 변경되지 않았는지 확인
            verify(expenditureRepository).findById(1L);
        }

        @Test
        @DisplayName("합계 제외 업데이트 성공 - 같은 값으로 변경해도 성공")
        void updateExcludingTotalSuccess_SameValue() {
            // given
            Expenditure expenditure = Expenditure.builder()
                    .id(1L)
                    .user(user)
                    .category(category)
                    .money(new BigDecimal("10000"))
                    .period(LocalDate.of(2024, 1, 1))
                    .excludingTotal(true)
                    .build();

            ExpenditureExcludeRequest request = new ExpenditureExcludeRequest(true);

            given(expenditureRepository.findById(1L))
                    .willReturn(Optional.of(expenditure));

            // when
            expenditureService.updateExpenditureExclude(1L, user, request);

            // then
            assertThat(expenditure.isExcludingTotal()).isTrue();
            verify(expenditureRepository).findById(1L);
        }
    }
}