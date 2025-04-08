package com.mantvmass.smsforwarder.model;

public class SMSMessage {
    private String id;
    private String from;
    private String message;
    private String timestamp;

    public SMSMessage(String id, String from, String message, String timestamp) {
        this.id = id;
        this.from = from;
        this.message = message;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public String getFrom() {
        return from;
    }

    public String getMessage() {
        return message;
    }

    public String getTimestamp() {
        return timestamp;
    }
}
