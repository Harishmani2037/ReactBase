package com.harish.reactbase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.harish.reactbase.adapters.AdapterParticipantsAdd;
import com.harish.reactbase.model.ModelClass;

import java.util.ArrayList;
import java.util.List;

public class GroupParticipantAddActivity extends AppCompatActivity {
    RecyclerView userRv;
    ActionBar actionBar;
    FirebaseAuth firebaseAuth;
    String groupId,myGroupRole;
    List<ModelClass> userList;
    AdapterParticipantsAdd adapterParticipantsAdd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_participant_add);
        actionBar=getSupportActionBar();
        actionBar.setTitle("Add Participants");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        firebaseAuth=FirebaseAuth.getInstance();
        userRv=findViewById(R.id.userRv);
        groupId=getIntent().getStringExtra("groupId").toString();
        loadGroupInfo();
    }

    private void getAllUsers() {
        userList=new ArrayList<>();
        final FirebaseUser firebaseUser=FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Users");

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear();
                for(DataSnapshot ds:dataSnapshot.getChildren()){
                    ModelClass modelUser=ds.getValue(ModelClass.class);

                    if (!firebaseUser.getUid().equals(modelUser.getUid())){
                        userList.add(modelUser);
                    }
                }
                adapterParticipantsAdd=new AdapterParticipantsAdd(GroupParticipantAddActivity.this,userList,""+groupId,myGroupRole);

                userRv.setAdapter(adapterParticipantsAdd);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void loadGroupInfo() {
        final DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Groups");
        ref.orderByChild("groupId").equalTo(groupId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds:dataSnapshot.getChildren()){
                    String groupId=""+ds.child("groupId").getValue();
                    final String groupTitle=""+ds.child("groupTitle").getValue();
                    String groupDescription=""+ds.child("groupDescription").getValue();
                    String groupIcon=""+ds.child("groupIcon").getValue();
                    String createdBy=""+ds.child("createdBy").getValue();
                    String timeStamp=""+ds.child("timesStamp").getValue();

                    ref.child(groupId).child("Participants").child(firebaseAuth.getUid()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()){
                                myGroupRole=""+dataSnapshot.child("role").getValue();
                                actionBar.setTitle(groupTitle+"("+myGroupRole+")");
                                getAllUsers();
                            }
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
