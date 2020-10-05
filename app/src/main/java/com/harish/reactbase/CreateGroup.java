package com.harish.reactbase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
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
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

public class CreateGroup extends AppCompatActivity {


    private static final int CAMER_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 200;

    private static final int IMAGE_PICK_CAMERA_CODE = 300;
    private static final int IMAGE_PICK_GALLERY_CODE = 400;

    String[] cameraPermission;
    String[] storagePermission;

    Uri image_uri = null;

    ProgressDialog pd;


    FirebaseAuth firebaseAuth;
    ImageView groupIconTv;
    EditText groupTitleTv,groupDescriptionTv;
    Button createGroupBtn;
    ActionBar actionBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);
        actionBar=getSupportActionBar();
        actionBar.setTitle("Create Group");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        firebaseAuth=FirebaseAuth.getInstance();
        groupIconTv=findViewById(R.id.groupAvatarTv);
        groupTitleTv=findViewById(R.id.groupNameTv);
        groupDescriptionTv=findViewById(R.id.groupDescriptionTv);
        createGroupBtn=findViewById(R.id.CreategroupBtn);
        checkUserStatus();

        cameraPermission = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        groupIconTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImagePickDialog();
            }
        });

        createGroupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startCreatingGroup();
            }
        });
    }

    private void startCreatingGroup() {
        pd=new ProgressDialog(this);
        pd.setMessage("Creating Group...");

        final String groupTitle=groupTitleTv.getText().toString();
        final String groupDescription=groupDescriptionTv.getText().toString();
        final String timeStamp=""+System.currentTimeMillis();

        if (TextUtils.isEmpty(groupTitle)){
            Toast.makeText(this, "Enter the Group Ttile", Toast.LENGTH_SHORT).show();
            return;
        }
        else if (TextUtils.isEmpty(groupDescription)){
            Toast.makeText(this, "Enter the Group Description", Toast.LENGTH_SHORT).show();
            return;
        }else{
            pd.show();
            if (image_uri==null){

                createGroup(""+timeStamp,""+groupTitle,""+groupDescription,"");
                groupTitleTv.setText("");
                groupDescriptionTv.setText("");
                groupIconTv.setImageResource(R.drawable.ic_group_black_24dp);
                image_uri = null;
            }else{
                String fileNameAndPath="group_image"+"images"+timeStamp;


                StorageReference ref = FirebaseStorage.getInstance().getReference().child(fileNameAndPath);
                ref.putFile(image_uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful()) ;
                        {
                            String downloadUri = uriTask.getResult().toString();
                            if (uriTask.isSuccessful()) {
                                createGroup(""+timeStamp,""+groupTitle,""+groupDescription,downloadUri);
                            }
                        }
                        groupTitleTv.setText("");
                        groupDescriptionTv.setText("");
                        groupIconTv.setImageURI(null);
                        image_uri = null;
                    }
                });

            }
        }
    }

    private void createGroup(final String g_timeStamp, String groupTitle, String groupDescrition, String groupIcon) {
        HashMap hashMap=new HashMap();
        hashMap.put("groupId",g_timeStamp);
        hashMap.put("groupTitle",groupTitle);
        hashMap.put("groupDescription",groupDescrition);
        hashMap.put("groupIcon",groupIcon);
        hashMap.put("timesStamp",g_timeStamp);
        hashMap.put("createdBy",firebaseAuth.getUid());

        final DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(g_timeStamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                HashMap hashMap1=new HashMap();
                hashMap1.put("uid",firebaseAuth.getUid());
                hashMap1.put("role","creator");
                hashMap1.put("timesStamp",g_timeStamp);

                DatabaseReference reference=FirebaseDatabase.getInstance().getReference("Groups");
                ref.child(g_timeStamp).child("Participants").child(firebaseAuth.getUid()).setValue(hashMap1).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        pd.dismiss();
                        Toast.makeText(CreateGroup.this, "Group Created Successfully", Toast.LENGTH_SHORT).show();

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        pd.dismiss();
                        Toast.makeText(CreateGroup.this, "Failed due to "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(CreateGroup.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkUserStatus() {
        FirebaseUser firebaseUser=firebaseAuth.getCurrentUser();
        if (firebaseUser!=null){
            actionBar.setSubtitle(firebaseUser.getEmail());
        }else{

        }
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

                groupIconTv.setImageURI(image_uri);

            } else if (requestCode==IMAGE_PICK_CAMERA_CODE){
                groupIconTv.setImageURI(image_uri);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

}