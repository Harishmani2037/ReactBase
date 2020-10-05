package com.harish.reactbase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuInflater;
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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.harish.reactbase.adapters.ChatAdapter;
import com.harish.reactbase.model.modelChat;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import hani.momanii.supernova_emoji_library.Actions.EmojIconActions;
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;

public class ChatActivity extends AppCompatActivity {
    Toolbar toolbar;
    RecyclerView recyclerView;
    ImageView profileTv;
    ImageView blockTv;//isSeen hisUid message myUid time
    TextView nameTv,userStatusTv;
    EmojiconEditText messageEd;
    ImageButton sendBtn,attachBtn,emoji,camera;
    FirebaseAuth auth;
    //for if user see the message or not
    ValueEventListener seenListner;
    DatabaseReference userRefForSeen;
    List<modelChat> chatList;
    ChatAdapter adapterChat;
    FirebaseDatabase firebaseDatabase;
    String dateTime;
    DatabaseReference userDbreference;
    String hisUid;
    String myUid;
    String hisImage;
    FirebaseUser firebaseUser;
    boolean isBlocked=false;

    View view;
    EmojIconActions emojIconActions;

    //
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
        setContentView(R.layout.activity_chat);
        toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("");
        recyclerView=findViewById(R.id.recyle);
        profileTv=findViewById(R.id.pro_imgTv);
        nameTv=findViewById(R.id.pro_names);
        camera=findViewById(R.id.camera);
        userStatusTv=findViewById(R.id.pro_status);


        messageEd=findViewById(R.id.message);
        view=findViewById(R.id.rootview);
        emoji=findViewById(R.id.emoji);

        emojIconActions=new EmojIconActions(this,view,messageEd,emoji);
        emojIconActions.ShowEmojIcon();

        sendBtn=findViewById(R.id.sent);
        attachBtn=findViewById(R.id.attach);
        blockTv=findViewById(R.id.blockTv);
        auth=FirebaseAuth.getInstance();
        firebaseUser=auth.getCurrentUser();

        cameraPermission = new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);



        auth=FirebaseAuth.getInstance();
        Intent intent=getIntent();
        hisUid=intent.getStringExtra("hisUid").toString();
        firebaseDatabase=FirebaseDatabase.getInstance();
        userDbreference=firebaseDatabase.getReference("Users");

        Query Userquery=userDbreference.orderByChild("uid").equalTo(hisUid);

        Userquery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds:dataSnapshot.getChildren()) {
                    String name = "" + ds.child("name").getValue();
                    hisImage = "" + ds.child("image").getValue();
                    String typingStatus = "" + ds.child("typingTo").getValue();
                    if (typingStatus.equals(myUid)) {
                        userStatusTv.setText("typing...");
                    } else {
                        String onlineStatus = "" + ds.child("onlineStatus").getValue();
                        nameTv.setText(name);
                        if (onlineStatus.equals("online")) {
                            userStatusTv.setText(onlineStatus);
                        } else {
                            Calendar calendar = Calendar.getInstance(Locale.ENGLISH);

                            calendar.setTimeInMillis(Long.parseLong(onlineStatus));

                            dateTime = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();

                            userStatusTv.setText("Last seen at :" + dateTime);
                        }
                        try {
                            Picasso.get().load(hisImage).placeholder(R.drawable.ic_account_circle_black_24dp).into(profileTv);
                        } catch (Exception e) {
                            Picasso.get().load(R.drawable.ic_account_circle_black_24dp).into(profileTv);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message=messageEd.getText().toString().trim();
                if (TextUtils.isEmpty(message)){
                    messageEd.setError("Enter the Message");
                }else{
                    sentMessage(message);
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

        messageEd.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().length()==0){
                    checkTypingStatus("noOne");
                }else {
                    checkTypingStatus(hisUid);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        blockTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isBlocked){
                    unBlockUser();
                }else{
                    blockUser();
                }
            }
        });

        readMessage();

        checkisBlocked();

        seenMessage();
    }


    private void checkisBlocked() {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseUser.getUid()).child("BlockUsers").orderByChild("uid").equalTo(hisUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds:dataSnapshot.getChildren()) {
                    if (ds.exists()) {
                        Toast.makeText(getApplicationContext(), "You are blocked by the user cant't message", Toast.LENGTH_SHORT).show();
                        isBlocked=true;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void blockUser() {
        HashMap<String,Object> hashMap=new HashMap<>();
        hashMap.put("hisUid",hisUid);
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Users");
        ref.child(myUid).child("BlockUsers").child(hisUid).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(getApplicationContext(), "Blocked", Toast.LENGTH_SHORT).show();
                blockTv.setImageResource(R.drawable.ic_block);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), "Action Failed or Upgrade our App", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void unBlockUser() {
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Users");
        ref.child(myUid).child("BlockUsers").orderByChild("uid").equalTo(hisUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds:dataSnapshot.getChildren()){
                    if (ds.exists()){
                        ds.getRef().removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                blockTv.setImageResource(R.drawable.ic_unblock);
                                Toast.makeText(getApplicationContext(), "UnBlocked", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getApplicationContext(), "Action Failed or Upgrade our App", Toast.LENGTH_SHORT).show();
                            }
                        });
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
        boolean result= ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)==(PackageManager.PERMISSION_GRANTED);
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

    private void readMessage() {
        chatList=new ArrayList<>();
        DatabaseReference dbRef=FirebaseDatabase.getInstance().getReference("Chats");
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                chatList.clear();
                for (DataSnapshot ds:dataSnapshot.getChildren()){
                    modelChat chat=ds.getValue(modelChat.class);
                    if (chat.getReciver().equals(myUid)&&chat.getSender().equals(hisUid)||chat.getReciver().equals(hisUid)&&chat.getSender().equals(myUid)){
                        chatList.add(chat);
                    }
                    adapterChat=new ChatAdapter(ChatActivity.this,chatList,hisImage);
                    adapterChat.notifyDataSetChanged();
                    recyclerView.setAdapter(adapterChat);
                    recyclerView.smoothScrollToPosition(recyclerView.getAdapter().getItemCount());
                    seenMessage();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void seenMessage() {
        userRefForSeen=FirebaseDatabase.getInstance().getReference("Chats");
        seenListner=userRefForSeen.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds:dataSnapshot.getChildren()){
                    modelChat chat=ds.getValue(modelChat.class);
                    if (chat.getReciver().equals(myUid)&&chat.getSender().equals(hisUid)){
                        HashMap<String,Object>hasSeenHashMap=new HashMap<>();
                        hasSeenHashMap.put("isSeen",true);
                        ds.getRef().updateChildren(hasSeenHashMap);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void sentMessage(final String message) {
        DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference();

        String timestamp=""+System.currentTimeMillis();

        HashMap<String ,Object>hashMap=new HashMap<>();
        hashMap.put("sender",myUid);
        hashMap.put("reciver",hisUid);
        hashMap.put("message",message);
        hashMap.put("timestamp",timestamp);
        hashMap.put("isSeen",false);   //isSeen
        hashMap.put("type","text");
        databaseReference.child("Chats").push().setValue(hashMap);
        final DatabaseReference chatref1=FirebaseDatabase.getInstance().getReference("Chatlist").child(myUid).child(hisUid);

        chatref1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()){
                    chatref1.child("id").setValue(hisUid);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        final DatabaseReference chatref2=FirebaseDatabase.getInstance().getReference("Chatlist").child(hisUid).child(myUid);

        chatref2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()){
                    chatref2.child("id").setValue(myUid);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void sendImageMessage(Uri image_uri) throws IOException {
        final ProgressDialog progressDialog=new ProgressDialog(this);
        progressDialog.setMessage("Sending Image...");
        progressDialog.show();

        final String timeStamp=""+System.currentTimeMillis();
        String fileNameAndPath="ChatImages/"+"post_"+timeStamp;

        Bitmap bitmap=MediaStore.Images.Media.getBitmap(this.getContentResolver(),image_uri);
        ByteArrayOutputStream baos=new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,100,baos);
        byte[] data=baos.toByteArray();
        StorageReference ref= FirebaseStorage.getInstance().getReference().child(fileNameAndPath);
        ref.putBytes(data).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                progressDialog.dismiss();

                Task<Uri> uriTask=taskSnapshot.getStorage().getDownloadUrl();
                while (!uriTask.isSuccessful());
                String downloadUri=uriTask.getResult().toString();

                if(uriTask.isSuccessful()){
                    //
                    DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference();
                    HashMap<String,Object>hashMap=new HashMap<>();
                    hashMap.put("sender",myUid);
                    hashMap.put("reciver",hisUid);
                    hashMap.put("message",downloadUri);
                    hashMap.put("timestamp",timeStamp);
                    hashMap.put("type","image");
                    hashMap.put("isSeen",false);

                    databaseReference.child("Chats").push().setValue(hashMap);
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
            }
        });

        final DatabaseReference chatref1=FirebaseDatabase.getInstance().getReference("Chatlist").child(myUid).child(hisUid);

        chatref1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()){
                    chatref1.child("id").setValue(hisUid);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        final DatabaseReference chatref2=FirebaseDatabase.getInstance().getReference("Chatlist").child(hisUid).child(myUid);

        chatref2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()){
                    chatref2.child("id").setValue(myUid);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void logout() {
        FirebaseUser firebaseUser=auth.getCurrentUser();
        if (firebaseUser!=null){
            myUid=firebaseUser.getUid();
        }else{
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }
    }

    private void checkOnlineStatus(String status){
        DatabaseReference dbRef=FirebaseDatabase.getInstance().getReference("Users").child(myUid);
        HashMap<String,Object>hashMap=new HashMap<>();
        hashMap.put("onlineStatus",status);

        dbRef.updateChildren(hashMap);
    }

    private void checkTypingStatus(String typing){
        DatabaseReference dbRef=FirebaseDatabase.getInstance().getReference("Users").child(myUid);
        HashMap<String,Object>hashMap=new HashMap<>();
        hashMap.put("typingTo...",typing);

        dbRef.updateChildren(hashMap);
    }

    @Override
    protected void onStart() {
        logout();

        //set Online
        checkOnlineStatus("online");
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
        String timeStamp=String.valueOf(System.currentTimeMillis());
        checkOnlineStatus(timeStamp);
        checkTypingStatus("noOne");
        userRefForSeen.removeEventListener(seenListner);
    }

    @Override
    protected void onDestroy() {
        userStatusTv.setText(dateTime);
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        userStatusTv.setText(dateTime);
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_menu, menu);
        menu.findItem(R.id.opt_search).setVisible(false);
        menu.findItem(R.id.action_add_photo).setVisible(false);
        menu.findItem(R.id.info).setVisible(false);
        menu.findItem(R.id.action_add_Participant_group).setVisible(false);
        menu.findItem(R.id.createGroup).setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout:
                auth.signOut();
                logout();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}