package ks.com.budgetmanagementproject.feature.budget.entity;


import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import ks.com.budgetmanagementproject.feature.user.entity.User;
import ks.com.budgetmanagementproject.global.common.logger.BaseException;
import ks.com.budgetmanagementproject.global.common.logger.BaseExceptionStatus;
import ks.com.budgetmanagementproject.global.common.model.BaseTimeEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "budgets")
public class Budget extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_Id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_Id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private BudgetCategory category;

    @Column(precision = 14, scale = 0, nullable = false)
    private BigDecimal money;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDate period;

    public void updateBudget(BigDecimal money) {
        this.money = money;
    }

    public void deduct(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BaseException(BaseExceptionStatus.INVALID_AMOUNT);
        }

        this.money = this.money.subtract(amount);

        if (this.money.compareTo(BigDecimal.ZERO) < 0) {
            throw new BaseException(BaseExceptionStatus.INSUFFICIENT_BUDGET);
        }
    }
}