package com.mainpackage;

public class Message {

    public String from;
    public String message;
    public String date;

    public Message() {
    }

    public Message(String from, String message, String date) {
        this.from = from;
        this.message = message;
        this.date = date;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
