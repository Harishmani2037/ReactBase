package com.harish.reactbase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {
    private EditText name,username,email,password;
    DatabaseReference reference;
    private FirebaseAuth mauth;
    ImageView profile;
    ProgressDialog progressDialog;
    private Button register;
    FirebaseStorage storage;
    StorageReference storageReference;
    String phoneNumber;
    private static final int CAMER_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 200;

    private static final int IMAGE_PICK_CAMERA_CODE = 300;
    private static final int IMAGE_PICK_GALLERY_CODE = 400;

    String[] cameraPermission;
    String[] storagePermission;
    ProgressDialog pd;
    Uri image_uri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        progressDialog=new ProgressDialog(this);
        name=findViewById(R.id.name);
        username=findViewById(R.id.userName);
        email=findViewById(R.id.email);
        password=findViewById(R.id.password);
        register=findViewById(R.id.register);
        storage = FirebaseStorage.getInstance();
        storageReference=storage.getReference().child("images/");
        cameraPermission = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
        //getting number and set into field
        phoneNumber=getIntent().getStringExtra("phoneNumber").toString();

        profile=findViewById(R.id.profileimg5);

        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImagePickDialog();
            }
        });

        mauth=FirebaseAuth.getInstance();
        reference= FirebaseDatabase.getInstance().getReference();
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              CreateUser();
            }
        });
    }

    private void CreateUser() {
        pd=new ProgressDialog(this);
        pd.setMessage("Creating User...");

        final String txtUsername = username.getText().toString();
        final String txtName = name.getText().toString();
        final String txtEmail = email.getText().toString();
        final String txtPassword = password.getText().toString();
        final String timeStamp=""+System.currentTimeMillis();

        if (TextUtils.isEmpty(txtUsername)){
            Toast.makeText(this, "Enter the User Name", Toast.LENGTH_SHORT).show();
            username.setError("Enter the User Name");
            return;
        }
        else if (TextUtils.isEmpty(txtName)){
            Toast.makeText(this, "Enter the Group Description", Toast.LENGTH_SHORT).show();
            name.setError("Enter the Name");
            return;
        }
        else if(TextUtils.isEmpty(txtEmail)){
            Toast.makeText(this, "Enter the Email", Toast.LENGTH_SHORT).show();
            name.setError("Enter the Email Address");
        }else if (txtPassword.length() < 6) {
            Toast.makeText(getApplicationContext(), "Password is too short", Toast.LENGTH_SHORT).show();
        }
        else{
            pd.show();
            if (image_uri==null){
                registerUser(txtUsername,txtName,txtEmail,txtPassword,phoneNumber,"");
            }else{
                String fileNameAndPath="User"+"images"+timeStamp;
                StorageReference ref = FirebaseStorage.getInstance().getReference().child(fileNameAndPath);
                ref.putFile(image_uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful()) ;
                        {
                            String downloadUri = uriTask.getResult().toString();
                            if (uriTask.isSuccessful()) {
                                Toast.makeText(RegisterActivity.this, ""+downloadUri, Toast.LENGTH_SHORT).show();
                                registerUser(txtUsername,txtName,txtEmail,txtPassword,phoneNumber,downloadUri);
                            }
                        }

                    }
                });

            }

        }
    }

    private void registerUser(final String username, final String name, final String email, final String password, final String phoneNumber, final String profileimg) {
        progressDialog.setMessage("Registering a new account");
        mauth.createUserWithEmailAndPassword(email,password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                HashMap<String,Object> hashMap=new HashMap<>();
                hashMap.put("name",name);
                hashMap.put("email",email);
                hashMap.put("userName",username);
                hashMap.put("phoneNumber",phoneNumber);
                hashMap.put("onlineStatus","online");
                hashMap.put("typingTo","noOne");
                hashMap.put("password",password);
                hashMap.put("image",profileimg);
                hashMap.put("cover","");
                hashMap.put("id",mauth.getCurrentUser().getUid());
                FirebaseUser user=mauth.getCurrentUser();
                String uid=user.getUid();
                hashMap.put("uid",uid);
                reference.child("Users").child(mauth.getCurrentUser().getUid()).setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            progressDialog.dismiss();
                            Toast.makeText(RegisterActivity.this, "Success", Toast.LENGTH_SHORT).show();
                            Intent intent=new  Intent(getApplicationContext(), DashBoardActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        }
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(RegisterActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showImagePickDialog() {
        String[] options = {"Camera", "Gallery"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose Image from ");

        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    if (!checkCameraPermission()) {
                        requestCameraPermission();
                    } else {
                        pickFromCamera();
                    }
                }
                if (which == 1) {
                    if (!checkStoragePermission()) {
                        requestStoragePermission();
                    } else {
                        pickFromGallery();
                    }
                }
            }
        });
        builder.create().show();
    }

    private void pickFromGallery() {

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, IMAGE_PICK_GALLERY_CODE);
    }

    private void pickFromCamera() {
        ContentValues cv = new ContentValues();
        cv.put(MediaStore.Images.Media.TITLE, "Temp pick");
        cv.put(MediaStore.Images.Media.DESCRIPTION, "Temp Descr");
        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(intent, IMAGE_PICK_CAMERA_CODE);
    }

    private boolean checkStoragePermission() {
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this, storagePermission, STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermission() {
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, cameraPermission, CAMER_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode){
            case CAMER_REQUEST_CODE:{
                if (grantResults.length>0){
                    boolean cameraAccepted=grantResults[0]==PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted=grantResults[1]==PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && storageAccepted){
                        pickFromCamera();
                    }else{
                        Toast.makeText(this, "Camera and Storage both permissions are neccessary...", Toast.LENGTH_SHORT).show();
                    }
                }else{
                }
            }
            break;
            case STORAGE_REQUEST_CODE:{
                if (grantResults.length>0){
                    boolean storageAccepted=grantResults[0]==PackageManager.PERMISSION_GRANTED;
                    if (storageAccepted){
                        pickFromGallery();
                    }else{
                        Toast.makeText(this, "Storage permisions are neccessary...", Toast.LENGTH_SHORT).show();
                    }
                }else{
                }
            }
            break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (resultCode==RESULT_OK){
            if (requestCode==IMAGE_PICK_GALLERY_CODE){
                image_uri=data.getData();

                profile.setImageURI(image_uri);

            } else if (requestCode==IMAGE_PICK_CAMERA_CODE){
                profile.setImageURI(image_uri);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

}