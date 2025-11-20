package ks.com.budgetmanagementproject.feature.expenditure.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import ks.com.budgetmanagementproject.feature.budget.entity.BudgetCategory;
import ks.com.budgetmanagementproject.feature.expenditure.dto.ExpenditureUpdateRequest;
import ks.com.budgetmanagementproject.feature.user.entity.User;
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
public class Expenditure extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 60)
    private String memo;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDate period;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoryId")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private BudgetCategory category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @Column
    private boolean excludingTotal;

    @Column(nullable = false)
    private BigDecimal money;

    public void updateExpenditure(ExpenditureUpdateRequest request, BudgetCategory category) {
        this.money = request.getMoney();
        this.category = category;
        this.period = request.getPeriod();
        this.memo = request.getMemo();
    }

    public void excludingTotalUpdate(boolean excludingTotal) {
        this.excludingTotal = excludingTotal;
    }

    public boolean isOwnedBy(User user) {
        return this.user.getId().equals(user.getId());
    }
}