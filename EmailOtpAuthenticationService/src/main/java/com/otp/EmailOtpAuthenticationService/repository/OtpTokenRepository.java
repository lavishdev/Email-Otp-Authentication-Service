package com.otp.EmailOtpAuthenticationService.repository;

import com.otp.EmailOtpAuthenticationService.entity.otpToken;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface OtpTokenRepository extends JpaRepository<otpToken, Long> {

    Optional<otpToken> findTopByEmailAndUsedFalseOrderByCreatedAtDesc(String email);
    Optional<otpToken> findTopByEmailOrderByCreatedAtDesc(String email);

    @Modifying
    @Transactional
    @Query("DELETE FROM otpToken o WHERE o.expiresAt < :now")
    void deleteExpiredTokens(LocalDateTime now);

    @Modifying
    @Transactional
    void deleteAllByEmail(String email);
}
