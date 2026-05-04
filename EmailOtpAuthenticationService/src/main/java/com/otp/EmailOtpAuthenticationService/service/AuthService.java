package com.otp.EmailOtpAuthenticationService.service;

import com.otp.EmailOtpAuthenticationService.dto.*;
import com.otp.EmailOtpAuthenticationService.entity.otpToken;
import com.otp.EmailOtpAuthenticationService.repository.OtpTokenRepository;
import com.otp.EmailOtpAuthenticationService.repository.UserRepository;
import com.otp.EmailOtpAuthenticationService.util.JwtUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.otp.EmailOtpAuthenticationService.entity.User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final OtpTokenRepository otpTokenRepository;
    private final OtpService otpService;
    private final JwtUtil jwtUtil;

    /**
     * Register a new user and send OTP for email verification.
     */
    @Transactional
    public ApiResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered. Please login instead.");
        }

        // Send OTP immediately after registration
        otpService.generateAndSendOtp(request.getEmail(), request.getName());

        return new ApiResponse(true,
                "Registration successful! OTP sent to " + request.getEmail() + ". Please verify to login.");
    }

    /**
     * Send OTP to existing user for login.
     */
    public ApiResponse sendLoginOtp(SendOtpRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException(
                        "No account found with email: " + request.getEmail() + ". Please register first."));

        otpService.generateAndSendOtp(request.getEmail(), user.getName());
        return new ApiResponse(true, "OTP sent to " + request.getEmail());
    }


    @Transactional
    public AuthResponse verifyOtpAndLogin(VerifyOtpRequest request) {
        // Step 1: verify OTP — throws if invalid
        otpService.verifyOtp(request.getEmail(), request.getOtp());

        // Step 2: check if user already exists (returning login user)
        // or create them now (first time registration)
        User user = userRepository.findByEmail(request.getEmail())
                .orElseGet(() -> {
                    // First time — fetch name from otp_tokens and save user NOW
                    String name = otpTokenRepository
                            .findTopByEmailOrderByCreatedAtDesc(request.getEmail())
                            .map(otpToken::getName)
                            .orElse("User");

                    return userRepository.save(User.builder()
                            .email(request.getEmail())
                            .name(name)
                            .verified(true)  // already verified
                            .build());
                });

        String token = jwtUtil.generateToken(user.getEmail());

        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .name(user.getName())
                .message("Login successful!")
                .build();
    }
}