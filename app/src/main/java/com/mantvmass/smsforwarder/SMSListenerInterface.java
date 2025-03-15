package com.mantvmass.smsforwarder;

public interface SMSListenerInterface {

    void handleReceive(String from, String message);

}
