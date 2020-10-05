package com.harish.reactbase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.chaos.view.PinView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class OtpRegisteration extends AppCompatActivity {
    String phoneNumber;
    String codeBySystem;
    PinView otp;
    Button verify;
    FirebaseAuth auth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_registeration);
        phoneNumber=getIntent().getStringExtra("phoneNumber").toString();
        otp=findViewById(R.id.otpcode);
        verify=findViewById(R.id.verify);
        verifyCode(phoneNumber);
        auth=FirebaseAuth.getInstance();
        verify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProgressDialog pd=new ProgressDialog(OtpRegisteration.this);
                pd.setMessage("Verifying...");
                pd.show();
                String code=otp.getText().toString();
                if (!code.isEmpty()){
                    verifycod(code);
                }
            }
        });
    }

    private void verifyCode(String phoneNumber) {
        ProgressDialog pd=new ProgressDialog(this);
        pd.setMessage("Verifying...");
        pd.show();
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks);        // OnVerificationStateChangedCallbacks
    }
    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks=new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            codeBySystem=s;
        }

        @Override
        public void onCodeAutoRetrievalTimeOut(String s) {
            super.onCodeAutoRetrievalTimeOut(s);
        }

        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
            String code=phoneAuthCredential.getSmsCode();
            if (code!=null){
                otp.setText(code);
                verifycod(code);
            }
        }

        @Override
        public void onVerificationFailed(FirebaseException e) {
            Toast.makeText(OtpRegisteration.this, "Error "+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    };

    private void verifycod(String code) {
        PhoneAuthCredential authCredential=PhoneAuthProvider.getCredential(codeBySystem,code);
        signIn(authCredential);
    }

    private void signIn(PhoneAuthCredential authCredential) {
        auth.signInWithCredential(authCredential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    Toast.makeText(OtpRegisteration.this, "Success", Toast.LENGTH_SHORT).show();
                    Intent intent=new Intent(getApplicationContext(), RegisterActivity.class);
                    intent.putExtra("phoneNumber",phoneNumber);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(OtpRegisteration.this, "Error due to"+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}

