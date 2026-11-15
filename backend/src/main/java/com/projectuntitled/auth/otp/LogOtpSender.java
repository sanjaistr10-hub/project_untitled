package com.projectuntitled.auth.otp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/** Local development: prints the code to the backend console instead of emailing it. */
@Component
@ConditionalOnProperty(name = "app.mail.delivery", havingValue = "log", matchIfMissing = true)
public class LogOtpSender implements OtpSender {

    private static final Logger log = LoggerFactory.getLogger(LogOtpSender.class);

    @Override
    public void send(OtpMessage message) {
        log.info("\n\n    [DEV] {} sign-in code for {}: {}   (valid {} min)\n",
                message.brandName(), message.email(), message.code(), message.validFor().toMinutes());
    }
}
