package com.otp.EmailOtpAuthenticationService.service;

import com.otp.EmailOtpAuthenticationService.entity.otpToken;
import com.otp.EmailOtpAuthenticationService.repository.OtpTokenRepository;
import com.otp.EmailOtpAuthenticationService.util.OtpGenerator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpService {


    private final OtpTokenRepository otpTokenRepository;

    private final OtpGenerator otpGenerator;

    private final EmailService emailService;


    @Value("${otp.expiry.minutes:5}")
    private int otpExpiryMinutes;

    private static final int MAX_ATTEMPTS = 3;

    @Transactional
    public void generateAndSendOtp(String email, String name) {
        otpTokenRepository.deleteAllByEmail(email);
        String otp = otpGenerator.generate();

        otpToken token = otpToken.builder()
                .email(email)
                .name(name)
                .otp(otp)
                .expiresAt(LocalDateTime.now().plusMinutes(otpExpiryMinutes))
                .used(false)
                .attemptCount(0)
                .build();
        otpTokenRepository.save(token);
        emailService.SendOtpEmail(email,otp,name);
        log.info("OTP generated and send to: {}" , email);
    }


    public boolean verifyOtp(String email, String inputOtp) {
        otpToken otpToken = otpTokenRepository
                .findTopByEmailAndUsedFalseOrderByCreatedAtDesc(email)
                .orElseThrow(() -> new RuntimeException("OTP token not found"));

        if(otpToken.isExpired()) {
            otpTokenRepository.delete(otpToken);
            throw new RuntimeException("OTP token expired");
        }

        if(otpToken.getAttemptCount() >= MAX_ATTEMPTS){
            otpTokenRepository.delete(otpToken);
            throw new RuntimeException("Maximum number of attempts reached");
        }

        if(!otpToken.getOtp().equals(inputOtp)){
            otpToken.setAttemptCount(otpToken.getAttemptCount()+1);
            otpTokenRepository.save(otpToken);
            int remainingAttempts = MAX_ATTEMPTS - otpToken.getAttemptCount();
            throw  new RuntimeException("Invalid OTP" + remainingAttempts + " attempts left");
        }

        otpToken.setUsed(true);
        otpTokenRepository.save(otpToken);

        log.info("OTP verified for email: {}" , email);
        return true;

    }

    @Scheduled(fixedRate = 600_000)
    public void CleanExpiredOtps(String email) {
        otpTokenRepository.deleteExpiredTokens(LocalDateTime.now());
        log.debug("Expired otps cleaned up");
    }

}
