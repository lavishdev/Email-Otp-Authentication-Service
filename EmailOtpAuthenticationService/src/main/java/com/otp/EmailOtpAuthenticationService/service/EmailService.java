package com.otp.EmailOtpAuthenticationService.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final  JavaMailSender javaMailSender;



    public void SendOtpEmail(String toEmail, String otp, String name) {
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("YOUR OTP CODE - LOGIN VERIFICATION");
            helper.setText(buildEmailBody(name, otp), true); // true = HTML

            javaMailSender.send(mimeMessage);

            log.info("OTP email sent successfully to: {}", toEmail);

        } catch (MessagingException ex) {
            log.error("Failed to send OTP email to: {}", toEmail, ex);
            throw new RuntimeException("Failed to send OTP email. Please try again");
        }
    }

    private String buildEmailBody(String name, String otp) {
        return String.format("""
        <!DOCTYPE html>
        <html>
        <head>
            <style>
                body {
                    font-family: Arial, sans-serif;
                    background-color: #f4f4f4;
                    margin: 0;
                    padding: 0;
                }
                .container {
                    max-width: 600px;
                    margin: 20px auto;
                    background: #ffffff;
                    padding: 20px;
                    border-radius: 8px;
                    box-shadow: 0 0 10px rgba(0,0,0,0.1);
                }
                .header {
                    text-align: center;
                    padding-bottom: 10px;
                }
                .otp-box {
                    text-align: center;
                    margin: 20px 0;
                }
                .otp-code {
                    font-size: 24px;
                    font-weight: bold;
                    color: #2c3e50;
                    background: #ecf0f1;
                    padding: 10px;
                    display: inline-block;
                    border-radius: 5px;
                    letter-spacing: 3px;
                }
                .note {
                    font-size: 14px;
                    color: #555;
                }
                .warning {
                    color: red;
                    font-weight: bold;
                    font-size: 14px;
                }
                .footer {
                    margin-top: 20px;
                    font-size: 12px;
                    color: #888;
                    text-align: center;
                }
            </style>
        </head>
        <body>
            <div class="container">
                <div class="header">
                    <h2>LOGIN VERIFICATION</h2>
                </div>

                <p>Hi <strong>%s</strong>,</p>

                <p>
                    Use this OTP code below to complete your login. 
                    This code is valid for <strong>5 minutes</strong>.
                </p>

                <div class="otp-box">
                    <div class="otp-code">%s</div>
                </div>

                <p class="note">
                    Enter this code on the verification page to sign in to your account.
                </p>

                <p class="warning">
                    DO NOT SHARE THIS CODE WITH ANYONE. WE WILL NEVER ASK FOR YOUR OTP.
                </p>

                <div class="footer">
                    <p>If you didn't request this, please ignore this email.</p>
                </div>
            </div>
        </body>
        </html>
        """, name, otp);
    }
}
