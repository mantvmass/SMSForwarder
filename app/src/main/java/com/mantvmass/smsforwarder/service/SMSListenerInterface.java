package com.mantvmass.smsforwarder.service;

public interface SMSListenerInterface {
    void handleReceive(String from, String message);
}