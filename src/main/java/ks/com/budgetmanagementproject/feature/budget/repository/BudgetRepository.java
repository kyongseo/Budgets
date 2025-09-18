package ks.com.budgetmanagementproject.feature.budget.repository;

import io.lettuce.core.dynamic.annotation.Param;
import ks.com.budgetmanagementproject.feature.budget.dto.BudgetRecommendResponse;
import ks.com.budgetmanagementproject.feature.budget.entity.Budget;
import ks.com.budgetmanagementproject.feature.budget.entity.BudgetCategory;
import ks.com.budgetmanagementproject.feature.expenditure.dto.ExpenditureRecommend;
import ks.com.budgetmanagementproject.feature.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BudgetRepository extends JpaRepository<Budget,Long> {
    Budget findByCategoryAndPeriodAndUser(BudgetCategory category, LocalDate period, User user);

    @Query("select new ks.com.budgetmanagementproject.feature.budget.dto.BudgetRecommendResponse( " +
            "  b.category, " +
            "  cast( FUNCTION('round', :totalAmount * ( sum(b.money) * 1.0 / (select sum(b2.money) from Budget b2) ) ) as long ) " +
            ") " +
            "from Budget b " +
            "group by b.category")
    List<BudgetRecommendResponse> findByAverage(@Param("totalAmount") long totalAmount);


    @Query("select new ks.com.budgetmanagementproject.feature.expenditure.dto.ExpenditureRecommend(" +
            "category, round(sum(money) / :period, -3) as todayExpenditurePossibleMoney) " +
            "from Budget " +
            "where user = :user AND period = :start " +
            "group by category")
    List<ExpenditureRecommend> findByExpenditureRecommend(@Param("user") User user, @Param("start") LocalDate start, @Param("period") long period);

}