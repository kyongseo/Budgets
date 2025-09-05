package ks.com.budgetmanagementproject.feature.token.repository;

import jakarta.transaction.Transactional;
import ks.com.budgetmanagementproject.feature.token.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshRepository extends JpaRepository<RefreshToken, Long> {

    Boolean existsByRefresh(String refresh);

    @Transactional
    void deleteByRefresh(String refresh);
}