package com.harish.reactbase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.harish.reactbase.adapters.AdapterParticipantsAdd;
import com.harish.reactbase.model.ModelClass;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class GroupInfoActivity extends AppCompatActivity {
    ActionBar actioBar;
    String myGroupRole="";
    FirebaseAuth firebaseAuth;
    String groupId;
    ImageView groupIconTv;
    TextView descriptionTv,createdByTv,editGroupTv,addParticipantTv,leaveGroupTv,particpantsTv;
    RecyclerView participantsRv;
    private ArrayList<ModelClass> userList;
    AdapterParticipantsAdd adapterParticipantsAdd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_info);
        actioBar=getSupportActionBar();
        actioBar.setDisplayHomeAsUpEnabled(true);
        actioBar.setDisplayShowHomeEnabled(true);
        firebaseAuth=FirebaseAuth.getInstance();
        groupId=getIntent().getStringExtra("groupId").toString();
        groupIconTv=findViewById(R.id.groupIcon);
        descriptionTv=findViewById(R.id.descriptionTv);
        createdByTv=findViewById(R.id.createdBy);
        editGroupTv=findViewById(R.id.editGroupTv);
        addParticipantTv=findViewById(R.id.addParticipantTv);
        leaveGroupTv=findViewById(R.id.leaveGroupTv);
        particpantsTv=findViewById(R.id.participantsTv);
        participantsRv=findViewById(R.id.participantsRv);

        loadGroupInfo();
        loadMyGroupRole();

        addParticipantTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(GroupInfoActivity.this,GroupParticipantAddActivity.class);
                intent.putExtra("groupId",groupId);
                startActivity(intent);
            }
        });
        editGroupTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(GroupInfoActivity.this,GroupEditActivity.class);
                intent.putExtra("groupId",groupId);
                startActivity(intent);
            }
        });
        leaveGroupTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String dialogTitle;
                String dialogDescription;
                String postiveButton;
                if (myGroupRole.equals("creator")){
                    dialogTitle="Delete Group";
                    dialogDescription="Are you sure to delete the group permenantly";
                    postiveButton="Delete";
                }else{
                    dialogTitle="Leave Group";
                    dialogDescription="Are you sure to Leave the group";
                    postiveButton="Leave";
                }
                AlertDialog.Builder builder=new AlertDialog.Builder(GroupInfoActivity.this);
                builder.setTitle(dialogTitle).setTitle(dialogDescription).setPositiveButton(postiveButton, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (myGroupRole.equals("creator")){
                            deleteGroup();
                        }else{
                            leaveGroup();
                        }
                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.show();
            }
        });
    }

    private void leaveGroup() {
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Participants").child(firebaseAuth.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(GroupInfoActivity.this, "Group left Successfully", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(GroupInfoActivity.this, DashBoardActivity.class));
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(GroupInfoActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteGroup() {
        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(GroupInfoActivity.this, "Group Deleted Successfully", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(GroupInfoActivity.this, DashBoardActivity.class));
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(GroupInfoActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
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
                    String groupDescription=""+ds.child("groupDescription").getValue();
                    String groupIcon=""+ds.child("groupIcon").getValue();
                    String timeStamp=""+ds.child("timesStamp").getValue();
                    String createdBy=""+ds.child("createdBy").getValue();

                    Calendar calendar=Calendar.getInstance(Locale.ENGLISH);
                    calendar.setTimeInMillis(Long.parseLong(timeStamp));
                    String dateTime= DateFormat.format("dd/MM/yyyy hh:mm aa",calendar).toString();

                    loadCreatorInfo(dateTime,createdBy);

                    actioBar.setTitle(groupTitle);
                    descriptionTv.setText(groupDescription);

                    try{
                        Picasso.get().load(groupIcon).placeholder(R.drawable.ic_group_black_24dp).into(groupIconTv);
                    }
                    catch(Exception e){
                        groupIconTv.setImageResource(R.drawable.ic_group_black_24dp);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void loadCreatorInfo(final String dateTime, String createdBy) {
        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("uid").equalTo(createdBy).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds:dataSnapshot.getChildren()){
                    String name=""+ds.child("name").getValue();
                    createdByTv.setText("Created by "+name+" on"+dateTime);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

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
                    actioBar.setSubtitle(firebaseAuth.getCurrentUser().getEmail()+"("+myGroupRole+")");

                    if (myGroupRole.equals("participant")){
                        editGroupTv.setVisibility(View.GONE);
                        addParticipantTv.setVisibility(View.GONE);
                        leaveGroupTv.setText("Leave Group");

                    }else if (myGroupRole.equals("admin")){
                        editGroupTv.setVisibility(View.GONE);
                        addParticipantTv.setVisibility(View.VISIBLE);
                        leaveGroupTv.setText("Leave Group");
                    }else if (myGroupRole.equals("creator")){
                        editGroupTv.setVisibility(View.VISIBLE);
                        addParticipantTv.setVisibility(View.VISIBLE);
                        leaveGroupTv.setText("Delete Group");
                    }
                    loadparticipants();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void loadparticipants() {
        userList=new ArrayList<>();

        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Participants").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear();
                for (DataSnapshot ds:dataSnapshot.getChildren()){
                    String uid=""+ds.child("uid").getValue();
                    DatabaseReference ref1=FirebaseDatabase.getInstance().getReference("Users");
                    ref1.orderByChild("uid").equalTo(uid).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot ds:dataSnapshot.getChildren()){
                                ModelClass modelUser=ds.getValue(ModelClass.class);

                                userList.add(modelUser);
                            }
                            adapterParticipantsAdd=new AdapterParticipantsAdd(GroupInfoActivity.this,userList,groupId,myGroupRole);
                            participantsRv.setAdapter(adapterParticipantsAdd);
                            particpantsTv.setText("Participants ("+userList.size()+")");
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}