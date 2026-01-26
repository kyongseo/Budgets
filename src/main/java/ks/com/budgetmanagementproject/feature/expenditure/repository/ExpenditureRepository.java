package ks.com.budgetmanagementproject.feature.expenditure.repository;

import io.lettuce.core.dynamic.annotation.Param;
import ks.com.budgetmanagementproject.feature.budget.entity.BudgetCategory;
import ks.com.budgetmanagementproject.feature.expenditure.dto.ExpenditureGuide;
import ks.com.budgetmanagementproject.feature.expenditure.dto.ExpenditureList;
import ks.com.budgetmanagementproject.feature.expenditure.entity.Expenditure;
import ks.com.budgetmanagementproject.feature.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
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
            @Param("minMoney") BigDecimal minMoney,
            @Param("maxMoney") BigDecimal  maxMoney
    );

    // 합계(뷰 범위)
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
            @Param("minMoney") BigDecimal minMoney,
            @Param("maxMoney") BigDecimal maxMoney
    );

    // 카테고리별 총합(전체)
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

    // 가이드
    @Query("""
    select new ks.com.budgetmanagementproject.feature.expenditure.dto.ExpenditureGuide(
        a.category.name, sum(a.money),COALESCE((
            select sum(b.money) / :period
            from Budget b
            where b.period = :start
              and b.user = :user
              and b.category = a.category
        ), 0), 0)
    from Expenditure a
    where a.user = :user
      and a.period = :today
    group by a.category.id, a.category.name
    """)
    List<ExpenditureGuide> findByExpenditureAmount(
            @Param("user") User user,
            @Param("start") LocalDate start,
            @Param("today") LocalDate today,
            @Param("period") long period
    );
}
