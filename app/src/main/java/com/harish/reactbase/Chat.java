package com.harish.reactbase;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

public class Chat extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}
