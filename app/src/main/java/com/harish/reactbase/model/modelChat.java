package com.harish.reactbase.model;

import com.google.firebase.database.PropertyName;

public class modelChat {
    String message,reciver,sender,timestamp,type;
    boolean isSeen;

    public modelChat() {
    }

    public modelChat(String message, String reciver, String sender, String timestamp, String type, boolean isSeen) {
        this.message = message;
        this.reciver = reciver;
        this.sender = sender;
        this.timestamp = timestamp;
        this.type = type;
        this.isSeen = isSeen;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getReciver() {
        return reciver;
    }

    public void setReciver(String reciver) {
        this.reciver = reciver;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isSeen() {
        return isSeen;
    }

    public void setSeen(boolean seen) {
        isSeen = seen;
    }
}
