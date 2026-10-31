package com.hieu.identity_service.service;

public interface EmailService {

    void sendPasswordResetEmail(String toEmail, String rawToken);
}