package com.otp.EmailOtpAuthenticationService.repository;

import com.otp.EmailOtpAuthenticationService.entity.user;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<user,Long> {


    Optional<user> findByEmail(String email);
}
