package com.harish.reactbase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rilixtech.widget.countrycodepicker.CountryCodePicker;

import java.util.HashMap;

public class PhoneNumberAuthentication extends AppCompatActivity {
    EditText editText;
    CountryCodePicker countryCodePicker;
    Button button;
    FirebaseAuth auth;
    DatabaseReference reference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phonenumberauthentication);

        reference = FirebaseDatabase.getInstance().getReference();

        auth = FirebaseAuth.getInstance();

        editText=findViewById(R.id.editText1);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        countryCodePicker=findViewById(R.id.code);
        button=findViewById(R.id.button1);
    }
    public void button1(View view){
        String number=editText.getText().toString();
        String code=countryCodePicker.getSelectedCountryCode().toString();
        if (number.isEmpty()){
            editText.setError("Phone cannot be empty");
        }else if (number.length()==10){
            String phoneNumber="+"+code+number;
            Intent intent=new Intent(getApplicationContext(), OtpRegisteration.class);
            intent.putExtra("phoneNumber",phoneNumber);
            startActivity(intent);
        }else{
            Toast.makeText(this, "Invalid Number", Toast.LENGTH_SHORT).show();
        }
    }
}

