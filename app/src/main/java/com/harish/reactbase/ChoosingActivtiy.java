package com.harish.reactbase;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChoosingActivtiy extends AppCompatActivity {
    Button login,signup;
    Animation animationBottom;
    FirebaseAuth auth;
    @Override
    protected void onStart() {
        FirebaseUser firebaseUser = auth.getCurrentUser();
        if (firebaseUser != null) {
            Intent intent = new Intent(getApplicationContext(), DashBoardActivity.class);
            startActivity(intent);
            finish();
        }
        super.onStart();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choosing_activtiy);
        auth = FirebaseAuth.getInstance();
        animationBottom= AnimationUtils.loadAnimation(this,R.anim.bottom_animation);
        login=findViewById(R.id.loginc);
        signup=findViewById(R.id.signUpc);
        login.setAnimation(animationBottom);
        signup.setAnimation(animationBottom);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ChoosingActivtiy.this,LoginActivity.class));
            }
        });

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ChoosingActivtiy.this,PhoneNumberAuthentication.class));
            }
        });
    }
}