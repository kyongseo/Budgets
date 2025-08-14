package ks.com.budgetmanagementproject.feature.budget.repository;

import ks.com.budgetmanagementproject.feature.budget.entity.BudgetCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BudgetCategoryRepository extends JpaRepository<BudgetCategory,Long> {

    Optional<BudgetCategory> findByName(String name);
}