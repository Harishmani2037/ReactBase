package com.harish.reactbase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.harish.reactbase.adapters.AdapterPosts;
import com.harish.reactbase.model.ModelPost;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class ThereprofileActivity extends AppCompatActivity {
    RecyclerView postRecyclerView;
    ImageView profileImage, prof_coverPhoto;
    TextView prof_name, prof_email, prof_phone;

    List<ModelPost> postList;
    AdapterPosts adapterPosts;
    String uid;

    FirebaseAuth firebaseAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thereprofile);
        ActionBar actionBar=getSupportActionBar();
        actionBar.setTitle("Profile");
        actionBar.setDisplayHomeAsUpEnabled(true);

        profileImage = findViewById(R.id.prof_img);
        prof_name = findViewById(R.id.prof_name);
        prof_email = findViewById(R.id.prof_email);
        prof_phone = findViewById(R.id.prof_phone);
        prof_coverPhoto = findViewById(R.id.prof_coverPhoto);
        postRecyclerView=findViewById(R.id.recyclerviewPosts);

        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(getApplicationContext());

        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);

        postRecyclerView.setLayoutManager(linearLayoutManager);

        Intent intent=getIntent();
        uid=intent.getStringExtra("uid").toString();
        firebaseAuth=FirebaseAuth.getInstance();

        Query query = FirebaseDatabase.getInstance().getReference("Users").orderByChild("uid").equalTo(uid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    String name = "" + ds.child("name").getValue();
                    String email = "" + ds.child("email").getValue();
                    String phone = "" + ds.child("phoneNumber").getValue();
                    String image = "" + ds.child("image").getValue();
                    String cover = "" + ds.child("cover").getValue();
                    prof_name.setText(name);
                    prof_email.setText(email);
                    prof_phone.setText(phone);
                    try {
                        if (image == "") {
                            Picasso.get().load(R.drawable.ic_add_a_photo_black_24dp).into(profileImage);
                        } else {
                            Picasso.get().load(image).into(profileImage);
                        }
                    } catch (Exception e) {
                        Picasso.get().load(R.drawable.ic_add_a_photo_black_24dp).into(profileImage);
                    }
                    try {
                        if (cover == "") {
                            Picasso.get().load(R.drawable.ic_photoimage).into(prof_coverPhoto);
                        } else {
                            Picasso.get().load(cover).into(prof_coverPhoto);
                        }
                    } catch (Exception e) {
                        Picasso.get().load(R.drawable.ic_photoimage).into(prof_coverPhoto);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });



        postList=new ArrayList<>();
        logout();
        loadHisPosts();
    }

    private void loadHisPosts() {

        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Posts");

        Query query=ref.orderByChild("uid").equalTo(uid);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postList.clear();
                for (DataSnapshot ds:dataSnapshot.getChildren()){
                    ModelPost myposts=ds.getValue(ModelPost.class);

                    postList.add(myposts);

                    adapterPosts=new AdapterPosts(getApplicationContext(),postList);

                    postRecyclerView.setAdapter(adapterPosts);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), ""+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void searchHisPosts(final String searchQuery){
        LinearLayoutManager layoutManager=new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);

        postRecyclerView.setLayoutManager(layoutManager);

        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Posts");

        Query query=ref.orderByChild("uid").equalTo(uid);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postList.clear();
                for (DataSnapshot ds:dataSnapshot.getChildren()){
                    ModelPost myposts=ds.getValue(ModelPost.class);

                    if (myposts.getpTitle().toLowerCase().contains(searchQuery.toLowerCase())||myposts.getpDescription().toLowerCase()
                            .contains(searchQuery.toLowerCase())){

                        postList.add(myposts);

                    }
                    adapterPosts=new AdapterPosts(getApplicationContext(),postList);

                    postRecyclerView.setAdapter(adapterPosts);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), ""+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void logout() {
        FirebaseUser firebaseUser=firebaseAuth.getCurrentUser();
        if (firebaseUser!=null){

        }else{
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu,menu);
        menu.findItem(R.id.action_add_photo).setVisible(false);
        menu.findItem(R.id.info).setVisible(false);
        MenuItem item=menu.findItem(R.id.opt_search);
        androidx.appcompat.widget.SearchView searchView=(androidx.appcompat.widget.SearchView) MenuItemCompat.getActionView(item);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                if (!TextUtils.isEmpty(s)){
                    searchHisPosts(s);
                }else{
                    loadHisPosts();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {

                if (!TextUtils.isEmpty(s)){
                    searchHisPosts(s);
                }else{
                    loadHisPosts();
                }

                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout:
                firebaseAuth.signOut();
                logout();
                break;

        }
        return super.onOptionsItemSelected(item);
    }

}

