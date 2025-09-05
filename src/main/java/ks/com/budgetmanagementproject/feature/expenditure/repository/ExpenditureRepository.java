package ks.com.budgetmanagementproject.feature.expenditure.repository;

import io.lettuce.core.dynamic.annotation.Param;
import ks.com.budgetmanagementproject.feature.budget.entity.BudgetCategory;
import ks.com.budgetmanagementproject.feature.expenditure.dto.ExpenditureGuide;
import ks.com.budgetmanagementproject.feature.expenditure.dto.ExpenditureList;
import ks.com.budgetmanagementproject.feature.expenditure.entity.Expenditure;
import ks.com.budgetmanagementproject.feature.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface ExpenditureRepository extends JpaRepository<Expenditure,Long> {

    // 목록 조회
    @Query("""
        select new ks.com.budgetmanagementproject.feature.expenditure.dto.ExpenditureList(
            e.memo, e.period, e.category, e.excludingTotal, e.money
        )
        from Expenditure e
        where e.category = :category
          and e.period between :minPeriod and :maxPeriod
          and e.user = :user
          and e.money between :minMoney and :maxMoney
        """)
    List<ExpenditureList> findExpenditureList(
            @Param("minPeriod") LocalDate minPeriod,
            @Param("maxPeriod") LocalDate maxPeriod,
            @Param("category") BudgetCategory category,
            @Param("user") User user,
            @Param("minMoney") long minMoney,
            @Param("maxMoney") long maxMoney
    );

    // 합계(뷰 범위) — SUM(long) ⇒ Long, COALESCE 0L
    @Query("""
        select COALESCE(sum(e.money), 0L)
        from Expenditure e
        where e.category = :category
          and e.period between :minPeriod and :maxPeriod
          and e.user = :user
          and e.money between :minMoney and :maxMoney
          and e.excludingTotal = false
        """)
    Long findViewMoneyTotal(
            @Param("minPeriod") LocalDate minPeriod,
            @Param("maxPeriod") LocalDate maxPeriod,
            @Param("category") BudgetCategory category,
            @Param("user") User user,
            @Param("minMoney") long minMoney,
            @Param("maxMoney") long maxMoney
    );

    // 3) 카테고리별 총합(전체): ifNull -> COALESCE, 별칭 추가
    @Query("""
        select COALESCE(sum(e.money), 0L)
        from Expenditure e
        where e.category = :category
          and e.user = :user
          and e.excludingTotal = false
        """)
    Long findTotalByCategory(
            @Param("category") BudgetCategory category,
            @Param("user") User user
    );

    // 4) 가이드: round/서브쿼리 이슈 -> FUNCTION 사용(Hibernate) 또는 네이티브/서비스단 분리
    @Query("""
        select new ks.com.budgetmanagementproject.feature.expenditure.dto.ExpenditureGuide(
            a.category,
            sum(a.money),
            COALESCE((
                select (sum(b.money) * 1.0) / :period
                from Budget b
                where b.period = :start
                  and b.user = :user
                  and b.category = a.category
            ), 0.0),
            '0%'
        )
        from Expenditure a
        where a.user = :user
          and a.period = :today
        group by a.category
        """)
    List<ExpenditureGuide> findByExpenditureAmount(
            @Param("user") User user,
            @Param("start") LocalDate start,
            @Param("today") LocalDate today,
            @Param("period") long period
    );
}
