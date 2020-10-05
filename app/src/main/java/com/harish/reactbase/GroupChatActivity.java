package com.harish.reactbase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.harish.reactbase.adapters.AdapterGroupChat;
import com.harish.reactbase.model.ModelGroupChat;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import hani.momanii.supernova_emoji_library.Actions.EmojIconActions;
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;

public class GroupChatActivity extends AppCompatActivity {
    String groupId,myGroupRole="";
    Toolbar toolbar;
    ImageView groupIconTv;
    ImageButton attachBtn,sentBtn,emoji,camera;;
    TextView groupTitleTv;
    EmojiconEditText messageEd;
    RecyclerView chatRv;

    List<ModelGroupChat> groupChatList;
    AdapterGroupChat adapterGroupChat;
    FirebaseAuth firebaseAuth;

    View view;
    EmojIconActions emojIconActions;

    private static  final  int CAMER_REQUEST_CODE=100;
    private static  final  int STORAGE_REQUEST_CODE=200;

    private static  final  int IMAGE_PICK_CAMERA_CODE=300;
    private static  final  int IMAGE_PICK_GALLERY_CODE=400;


    String[]cameraPermission;
    String[]storagePermission;
    Uri image_uri=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);
        toolbar=findViewById(R.id.toolbar1);
        setSupportActionBar(toolbar);
        groupIconTv=findViewById(R.id.groupIconTv);
        attachBtn=findViewById(R.id.attach);
        sentBtn=findViewById(R.id.sent1);
        groupTitleTv=findViewById(R.id.groupTitle);
        messageEd=findViewById(R.id.message);
        camera=findViewById(R.id.camera);
        chatRv=findViewById(R.id.chatRv);
        view=findViewById(R.id.chatLayout);
        emoji=findViewById(R.id.emoji);

        emojIconActions=new EmojIconActions(this,view,messageEd,emoji);
        emojIconActions.ShowEmojIcon();

        Intent intent=getIntent();
        groupId=intent.getStringExtra("groupId").toString();

        firebaseAuth=FirebaseAuth.getInstance();
        loadGroupInfo();
        loadGroupMessage();
        loadMyGroupRole();
        sentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message=messageEd.getText().toString().trim();

                if (TextUtils.isEmpty(message)){
                    messageEd.setError("Message cannot Empty");
                    Toast.makeText(GroupChatActivity.this, "Enter the message", Toast.LENGTH_SHORT).show();
                }
                else{
                    sendMessage(message);
                }
                messageEd.setText("");
            }
        });

        attachBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!checkStoragePermission()){
                    requestStoragePermission();
                }else{
                    pickFromGallery();
                }
            }
        });

        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!checkCameraPermission()){
                    requestCameraPermission();
                }else{
                    pickFromCamera();
                }
            }
        });

    }

    private void loadMyGroupRole() {
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Participants").orderByChild("uid").equalTo(firebaseAuth.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds:dataSnapshot.getChildren()) {
                    myGroupRole=""+ds.child("role").getValue();

                    invalidateOptionsMenu();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void loadGroupMessage() {
        groupChatList=new ArrayList<>();
        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Messages").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                groupChatList.clear();
                for (DataSnapshot ds:dataSnapshot.getChildren()){
                    ModelGroupChat model=ds.getValue(ModelGroupChat.class);
                    groupChatList.add(model);
                }
                adapterGroupChat=new AdapterGroupChat(getApplicationContext(),groupChatList);
                chatRv.setAdapter(adapterGroupChat);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendMessage(String message) {
        String timeStamp=""+System.currentTimeMillis();

        HashMap<String,Object> hashMap=new HashMap<>();;

        hashMap.put("sender",""+firebaseAuth.getUid());
        hashMap.put("message",message);
        hashMap.put("timeStamp",timeStamp);
        hashMap.put("type","text");

        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Messages").child(timeStamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                messageEd.setText("");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(GroupChatActivity.this, "Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadGroupInfo() {
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Groups");
        ref.orderByChild("groupId").equalTo(groupId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds:dataSnapshot.getChildren()){
                    String groupTitle=""+ds.child("groupTitle").getValue();
                    //  String groupDescription=""+ds.child("groupDescription").getValue();
                    String groupIcon=""+ds.child("groupIcon").getValue();
                    //String timeStamp=""+ds.child("timesStamp").getValue();
                    // String createdBy=""+ds.child("createdBy").getValue();

                    groupTitleTv.setText(groupTitle);

                    try{
                        Picasso.get().load(groupIcon).placeholder(R.drawable.ic_group_black_24dp).into(groupIconTv);
                    }
                    catch(Exception e){
                        Picasso.get().load(R.drawable.ic_group_black_24dp).into(groupIconTv);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    
    private void pickFromGallery() {

        Intent intent=new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,IMAGE_PICK_GALLERY_CODE);
    }

    private void pickFromCamera() {
        ContentValues cv=new ContentValues();
        cv.put(MediaStore.Images.Media.TITLE,"Temp pick");
        cv.put(MediaStore.Images.Media.DESCRIPTION,"Temp Descr");
        image_uri=getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,cv);

        Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT,image_uri);
        startActivityForResult(intent,IMAGE_PICK_CAMERA_CODE);
    }

    private boolean checkStoragePermission(){
        boolean result= ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)==(PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void requestStoragePermission(){
        ActivityCompat.requestPermissions(this,storagePermission,STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermission(){
        boolean result= ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA)==(PackageManager.PERMISSION_GRANTED);
        boolean result1= ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)==(PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }

    private void requestCameraPermission(){
        ActivityCompat.requestPermissions(this,cameraPermission,CAMER_REQUEST_CODE);
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

                try {
                    sendImageMessage(image_uri);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } else if (requestCode==IMAGE_PICK_CAMERA_CODE){
                try {
                    sendImageMessage(image_uri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void sendImageMessage(Uri image_uri) throws IOException {
        final ProgressDialog progressDialog=new ProgressDialog(this);
        progressDialog.setMessage("Sending Image...");
        progressDialog.show();

        final String timeStamp=""+System.currentTimeMillis();
        String fileNameAndPath="ChatImages/"+""+timeStamp;

        StorageReference ref= FirebaseStorage.getInstance().getReference().child(fileNameAndPath);
        ref.putFile(image_uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Task<Uri> uriTask=taskSnapshot.getStorage().getDownloadUrl();
                while (!uriTask.isSuccessful());
                String downloadUri=uriTask.getResult().toString();

                if(uriTask.isSuccessful()){
                    //
                    HashMap<String,Object>hashMap=new HashMap<>();
                    hashMap.put("sender",""+firebaseAuth.getUid());
                    hashMap.put("message",downloadUri);
                    hashMap.put("timeStamp",timeStamp);
                    hashMap.put("type","image");

                    DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Groups");
                    ref.child(groupId).child("Messages").child(timeStamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            progressDialog.dismiss();
                            Toast.makeText(GroupChatActivity.this, "Sent Successfully", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(GroupChatActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(GroupChatActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu,menu);
        menu.findItem(R.id.info).setVisible(true);
        menu.findItem(R.id.createGroup).setVisible(false);
        menu.findItem(R.id.action_add_photo).setVisible(false);
        menu.findItem(R.id.opt_search).setVisible(false);

        if (myGroupRole.equals("creator")||myGroupRole.equals("admin")){
            menu.findItem(R.id.action_add_Participant_group).setVisible(true);
        }else{
            menu.findItem(R.id.action_add_Participant_group).setVisible(false);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_add_Participant_group:
                Intent intent=new Intent(getApplicationContext(),GroupParticipantAddActivity.class);
                intent.putExtra("groupId",groupId);
                startActivity(intent);
                break;
            case R.id.info:
                Intent intent1=new Intent(getApplicationContext(),GroupInfoActivity.class);
                intent1.putExtra("groupId",groupId);
                startActivity(intent1);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

}
