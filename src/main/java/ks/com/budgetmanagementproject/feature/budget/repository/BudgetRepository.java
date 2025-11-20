package ks.com.budgetmanagementproject.feature.budget.repository;

import ks.com.budgetmanagementproject.feature.budget.entity.Budget;
import ks.com.budgetmanagementproject.feature.budget.entity.BudgetCategory;
import ks.com.budgetmanagementproject.feature.expenditure.dto.ExpenditureRecommend;
import ks.com.budgetmanagementproject.feature.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BudgetRepository extends JpaRepository<Budget,Long> {

    Budget findByCategoryAndPeriodAndUser(BudgetCategory category, LocalDate period, User user);

    List<Budget> findAll();

    @Query("SELECT b FROM Budget b JOIN FETCH b.category")
    List<Budget> findAllWithCategory();

    @Query("select new ks.com.budgetmanagementproject.feature.expenditure.dto.ExpenditureRecommend(" +
            "b.category, " +
            "cast(round(sum(b.money) / :period, -3) as long)" +
            ") " +
            "from Budget b " +
            "where b.user = :user AND b.period = :start " +
            "group by b.category")
    List<ExpenditureRecommend> findByExpenditureRecommend(@Param("user") User user, @Param("start") LocalDate start, @Param("period") long period);

}