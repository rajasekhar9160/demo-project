package com.registration.service;

public interface SendingMailService {
    boolean sendMail(String subject, String body);
}
