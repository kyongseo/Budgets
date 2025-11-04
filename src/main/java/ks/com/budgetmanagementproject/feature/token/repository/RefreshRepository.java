package ks.com.budgetmanagementproject.feature.token.repository;

import jakarta.transaction.Transactional;
import ks.com.budgetmanagementproject.feature.token.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshRepository extends JpaRepository<RefreshToken, Long> {

    Boolean existsByRefresh(String refresh);

    Optional<RefreshToken> findByRefresh(String refresh);

    @Transactional
    void deleteByRefresh(String refresh);
}