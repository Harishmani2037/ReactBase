package com.harish.reactbase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
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

import java.util.HashMap;

public class LoginActivity extends AppCompatActivity {
    EditText email, password;
    TextView registerlink, forget;
    FirebaseAuth auth;
    DatabaseReference reference;
    Button login;
    ProgressDialog pd;
    @Override
    protected void onStart() {
        FirebaseUser firebaseUser = auth.getCurrentUser();
        if (firebaseUser != null) {
            Intent intent = new Intent(getApplicationContext(), DashBoardActivity.class);
            startActivity(intent);
        }
        super.onStart();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        email = findViewById(R.id.username);
        password = findViewById(R.id.password);
        registerlink = findViewById(R.id.registerLink);
        login = findViewById(R.id.login);

        forget = findViewById(R.id.forget);
        pd=new ProgressDialog(this);
        final ProgressDialog pd = new ProgressDialog(this);
        auth = FirebaseAuth.getInstance();
        reference = FirebaseDatabase.getInstance().getReference();
        reference.keepSynced(true);
        registerlink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), PhoneNumberAuthentication.class);
                startActivity(intent);
            }
        });
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });

        forget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                forget();
            }
        });

    }

    private void forget() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Recover Password");
        LinearLayout linearLayout = new LinearLayout(this);
        final EditText editText = new EditText(this);
        editText.setHint("Email");
        editText.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        editText.setMinEms(16);
        linearLayout.addView(editText);
        linearLayout.setPadding(10, 10, 10, 10);
        builder.setView(linearLayout);
        builder.setPositiveButton("Recover", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String email = editText.getText().toString().trim();
                Toast.makeText(LoginActivity.this, "Your email is " + email, Toast.LENGTH_SHORT).show();
                beingRecovered(email);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    private void beingRecovered(String email) {

        auth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful()) {
                    Toast.makeText(LoginActivity.this, "Email sent Successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(LoginActivity.this, "Error No-1", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                Toast.makeText(LoginActivity.this, "Error due  to " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void login() {
        if (!ValidateEmail() | !ValidatePassword()) {
            Toast.makeText(this, "Validation Failed", Toast.LENGTH_SHORT).show();
        } else {
            String Email = email.getText().toString();
            String Password = password.getText().toString();
            Login(Email, Password);
        }
    }

    private void Login(String email, String password) {
        pd.setMessage("Login...");
        pd.show();
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {

                    Toast.makeText(LoginActivity.this, "SuccessFully logined", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getApplicationContext(), DashBoardActivity.class);
                    startActivity(intent);
                } else {
                    pd.dismiss();
                    Toast.makeText(LoginActivity.this, "Failed in login", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
                Toast.makeText(LoginActivity.this, "Error due to " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public boolean ValidateEmail() {
        String Email = email.getText().toString();
        if (Email.isEmpty()) {
            email.setError("Email cannot be empty");
            return false;
        } else {
            return true;
        }
    }

    public boolean ValidatePassword() {
        String Password = password.getText().toString();
        if (Password.isEmpty()) {
            password.setError("Email cannot be empty");
            return false;
        } else {
            return true;
        }
    }


}


