package com.harish.reactbase;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    Animation animationBottom;
    TextView textView1,textView2;
    private static final int splash=2000;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        animationBottom= AnimationUtils.loadAnimation(this,R.anim.bottom_animation);
        textView1=findViewById(R.id.textView1);
        textView2=findViewById(R.id.textView2);
        textView1.setAnimation(animationBottom);
        textView2.setAnimation(animationBottom);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent=new Intent(getApplicationContext(), ChoosingActivtiy.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        },splash);
    }
}
