package com.projectuntitled.support;

import com.projectuntitled.auth.otp.OtpMessage;
import com.projectuntitled.auth.otp.OtpSender;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/** Replaces email delivery in tests so a test can read the code it was "sent". */
public class CapturingOtpSender implements OtpSender {

    private final List<OtpMessage> sent = new ArrayList<>();

    @Override
    public synchronized void send(OtpMessage message) {
        sent.add(message);
    }

    public synchronized OtpMessage lastFor(String email) {
        return lastMessage(email).orElseThrow(() -> new AssertionError("No code was sent to " + email));
    }

    public synchronized Optional<OtpMessage> lastMessage(String email) {
        for (int i = sent.size() - 1; i >= 0; i--) {
            if (sent.get(i).email().equals(email)) {
                return Optional.of(sent.get(i));
            }
        }
        return Optional.empty();
    }

    public synchronized void clear() {
        sent.clear();
    }

    @TestConfiguration
    public static class Config {

        @Bean
        @Primary
        public CapturingOtpSender capturingOtpSender() {
            return new CapturingOtpSender();
        }
    }
}
