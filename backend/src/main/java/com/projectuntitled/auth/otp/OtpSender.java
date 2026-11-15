package com.projectuntitled.auth.otp;

/** Delivers sign-in codes. Pick the implementation with app.mail.delivery (log | smtp). */
public interface OtpSender {

    void send(OtpMessage message);
}
