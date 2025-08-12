package ks.com.budgetmanagementproject.feature.token;

import io.lettuce.core.dynamic.annotation.Param;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface RefreshRepository extends JpaRepository<RefreshToken, Long> {

    Boolean existsByRefresh(String refresh);

    @Transactional
    void deleteByRefresh(String refresh);

    @Transactional
    @Modifying
    @Query("INSERT INTO RefreshToken (refresh, expiration) VALUES (:refresh, :expiration)")
    void saveRefreshToken(@Param("refresh") String refresh, @Param("expiration") Long expiration);
}