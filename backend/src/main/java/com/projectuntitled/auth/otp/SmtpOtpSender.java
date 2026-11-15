package com.projectuntitled.auth.otp;

import com.projectuntitled.config.AppProperties;
import jakarta.mail.internet.InternetAddress;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

/** Sends codes over SMTP (spring.mail.*). In dev, point it at Mailpit from docker-compose. */
@Component
@ConditionalOnProperty(name = "app.mail.delivery", havingValue = "smtp")
public class SmtpOtpSender implements OtpSender {

    private final JavaMailSender mailSender;
    private final AppProperties properties;

    public SmtpOtpSender(JavaMailSender mailSender, AppProperties properties) {
        this.mailSender = mailSender;
        this.properties = properties;
    }

    @Override
    public void send(OtpMessage message) {
        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setFrom(fromHeader(message.brandName()));
        mail.setTo(message.email());
        mail.setSubject(message.code() + " is your " + message.brandName() + " sign-in code");
        mail.setText("""
                Your sign-in code for %s is:

                    %s

                It expires in %d minutes. If you didn't ask for this, you can ignore this email.
                """.formatted(message.brandName(), message.code(), message.validFor().toMinutes()));
        mailSender.send(mail);
    }

    private String fromHeader(String brandName) {
        try {
            return new InternetAddress(properties.mail().fromAddress(), brandName, StandardCharsets.UTF_8.name()).toString();
        } catch (UnsupportedEncodingException e) {
            return properties.mail().fromAddress();
        }
    }
}
