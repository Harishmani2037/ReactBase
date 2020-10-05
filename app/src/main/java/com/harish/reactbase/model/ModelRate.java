package com.harish.reactbase.model;

public class ModelRate {
    String uid,email,rate;

    public ModelRate() {
    }

    public ModelRate(String uid, String email, String rate) {
        this.uid = uid;
        this.email = email;
        this.rate = rate;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRate() {
        return rate;
    }

    public void setRate(String rate) {
        this.rate = rate;
    }
}
