package com.ecommerce.app.EcommerceApp.repositories;

import com.ecommerce.app.EcommerceApp.entities.ExpiredToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface ExpiredTokenRepository extends JpaRepository<ExpiredToken,Long> {

    Optional<ExpiredToken> findByToken(String token);
    @Modifying
    @Query("DELETE FROM ExpiredToken e WHERE e.logoutTime <= :expirationTime")
    void deleteByExpirationTime(@Param("expirationTime") LocalDateTime expirationTime);
}
