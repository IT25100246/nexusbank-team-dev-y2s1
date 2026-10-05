package com.nexusbank.nexusbankdev.service;

import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

@Service
public class MailService {

    private static final Logger log = LoggerFactory.getLogger(MailService.class);

    private final JavaMailSender mailSender;

    @Value("${nexusbank.mail.development-mode:true}")
    private boolean devMode;

    @Value("${nexusbank.mail.from}")
    private String fromAddress;

    @Value("${nexusbank.mail.from-name}")
    private String fromName;

    public MailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendOtp(String to, String firstName, String otp, long expiryMinutes) {
        if (devMode) {
            log.info("[DEV MAIL] OTP for {} is {}", to, otp);
            return;
        }
        String html = """
                <div style="font-family:Arial,sans-serif;max-width:480px;margin:auto;padding:24px;border:1px solid #e5e7eb;border-radius:8px">
                  <h2 style="color:#1e3a8a;margin-top:0">NexusBank</h2>
                  <p>Hi %s,</p>
                  <p>Use this code to finish signing in:</p>
                  <p style="font-size:32px;letter-spacing:8px;font-weight:bold;margin:16px 0">%s</p>
                  <p>It expires in %d minutes. If you didn't try to log in, change your password and contact the bank.</p>
                  <p style="color:#6b7280;font-size:12px">Never share this code with anyone. NexusBank staff will never ask for it.</p>
                </div>
                """.formatted(HtmlUtils.htmlEscape(firstName == null ? "there" : firstName), otp, expiryMinutes);
        send(to, "Your NexusBank verification code", html);
    }

    /** Generic sender you can reuse for loan/KYC notifications. */
    public void send(String to, String subject, String html) {
        if (devMode) {
            log.info("[DEV MAIL] to={} subject={}", to, subject);
            return;
        }
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
            helper.setFrom(fromAddress, fromName);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(message);
        } catch (Exception e) {
            log.error("Failed to send email to {}", to, e);
            throw new RuntimeException("Could not send email right now. Please try again shortly.");
        }
    }
}