package ks.com.budgetmanagementproject.feature.budget.repository;

import io.lettuce.core.dynamic.annotation.Param;
import ks.com.budgetmanagementproject.feature.budget.entity.Budget;
import ks.com.budgetmanagementproject.feature.budget.entity.BudgetCategory;
import ks.com.budgetmanagementproject.feature.expenditure.dto.ExpenditureRecommend;
import ks.com.budgetmanagementproject.feature.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface BudgetRepository extends JpaRepository<Budget,Long> {
    Budget findByCategoryAndPeriodAndUser(BudgetCategory category, LocalDate period, User user);

    @Query("select new ks.com.budgetmanagementproject.feature.expenditure.dto.ExpenditureRecommend(" +
            "category, round(sum(money) / :period, -3) as todayExpenditurePossibleMoney) " +
            "from Budget " +
            "where user = :user AND period = :start " +
            "group by category")
    List<ExpenditureRecommend> findByExpenditureRecommend(@Param("user") User user, @Param("start") LocalDate start, @Param("period") long period);

}