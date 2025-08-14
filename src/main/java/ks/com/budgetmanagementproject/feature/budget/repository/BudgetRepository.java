package ks.com.budgetmanagementproject.feature.budget.repository;

import ks.com.budgetmanagementproject.feature.budget.entity.Budget;
import ks.com.budgetmanagementproject.feature.budget.entity.BudgetCategory;
import ks.com.budgetmanagementproject.feature.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface BudgetRepository extends JpaRepository<Budget,Long> {
    Budget findByCategoryAndPeriodAndUser(BudgetCategory category, LocalDate period, User user);
}