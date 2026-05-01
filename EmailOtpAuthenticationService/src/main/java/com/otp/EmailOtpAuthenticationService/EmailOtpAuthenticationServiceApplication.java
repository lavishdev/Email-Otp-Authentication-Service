package com.otp.EmailOtpAuthenticationService;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class EmailOtpAuthenticationServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(EmailOtpAuthenticationServiceApplication.class, args);
	}

}
