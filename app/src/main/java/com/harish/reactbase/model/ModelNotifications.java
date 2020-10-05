package com.harish.reactbase.model;

public class ModelNotifications {
    String pId,timesStamp,pUid,notifications,sUid,sName,sEmail,sImage;

    public ModelNotifications() {
    }

    public ModelNotifications(String pId, String timesStamp, String pUid, String notifications, String sUid, String sName, String sEmail, String sImage) {
        this.pId = pId;
        this.timesStamp = timesStamp;
        this.pUid = pUid;
        this.notifications = notifications;
        this.sUid = sUid;
        this.sName = sName;
        this.sEmail = sEmail;
        this.sImage = sImage;
    }

    public String getpId() {
        return pId;
    }

    public void setpId(String pId) {
        this.pId = pId;
    }

    public String getTimesStamp() {
        return timesStamp;
    }

    public void setTimesStamp(String timesStamp) {
        this.timesStamp = timesStamp;
    }

    public String getpUid() {
        return pUid;
    }

    public void setpUid(String pUid) {
        this.pUid = pUid;
    }

    public String getNotifications() {
        return notifications;
    }

    public void setNotifications(String notifications) {
        this.notifications = notifications;
    }

    public String getsUid() {
        return sUid;
    }

    public void setsUid(String sUid) {
        this.sUid = sUid;
    }

    public String getsName() {
        return sName;
    }

    public void setsName(String sName) {
        this.sName = sName;
    }

    public String getsEmail() {
        return sEmail;
    }

    public void setsEmail(String sEmail) {
        this.sEmail = sEmail;
    }

    public String getsImage() {
        return sImage;
    }

    public void setsImage(String sImage) {
        this.sImage = sImage;
    }
}
