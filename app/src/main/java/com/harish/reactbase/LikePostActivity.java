package com.harish.reactbase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.harish.reactbase.adapters.AdapterUsers;
import com.harish.reactbase.model.ModelClass;

import java.util.ArrayList;
import java.util.List;

public class LikePostActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    String pId;
    FirebaseAuth firebaseAuth;
    private List<ModelClass> userList;
    private AdapterUsers adapterUsers;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_like_post);
        firebaseAuth=FirebaseAuth.getInstance();
        ActionBar actionBar=getSupportActionBar();
        actionBar.setTitle("Post Likes");
        actionBar.setSubtitle(firebaseAuth.getCurrentUser().getEmail());
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        userList=new ArrayList<>();
        recyclerView=findViewById(R.id.recyclerViewLikes);
        Intent intent=getIntent();
        pId=intent.getStringExtra("pId").toString().trim();

        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Likes");
        ref.child(pId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear();
                for (DataSnapshot ds:dataSnapshot.getChildren()) {
                    String hisUid=""+ds.getRef().getKey();

                    getUsers(hisUid);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getUsers(String hisUid) {
        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("uid").equalTo(hisUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds:dataSnapshot.getChildren()){
                    ModelClass modelUser=ds.getValue(ModelClass.class);
                    userList.add(modelUser);
                }
                adapterUsers=new AdapterUsers(userList,getApplicationContext());
                recyclerView.setAdapter(adapterUsers);
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
