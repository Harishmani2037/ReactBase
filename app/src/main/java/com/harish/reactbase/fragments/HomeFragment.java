package com.harish.reactbase.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.harish.reactbase.AddPostActivity;
import com.harish.reactbase.LoginActivity;
import com.harish.reactbase.R;
import com.harish.reactbase.AboutActivity;
import com.harish.reactbase.adapters.AdapterPosts;
import com.harish.reactbase.feedPack;
import com.harish.reactbase.model.ModelPost;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {
    FirebaseAuth auth;
    RecyclerView recyclerView;
    List<ModelPost> postList;
    AdapterPosts adapterPosts;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view=inflater.inflate(R.layout.fragment_home, container, false);
        auth=FirebaseAuth.getInstance();
        recyclerView=view.findViewById(R.id.postsRecyclerView);
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(getActivity());

        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);

        recyclerView.setLayoutManager(linearLayoutManager);

        postList=new ArrayList<>();

        loadPosts();
        return view;
    }

    private void loadPosts() {
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Posts");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postList.clear();
                for (DataSnapshot ds :dataSnapshot.getChildren()){
                    ModelPost modelPost=ds.getValue(ModelPost.class);
                    postList.add(modelPost);

                    adapterPosts=new AdapterPosts(getActivity(),postList);

                    recyclerView.setAdapter(adapterPosts);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void searchPosts(final String searchQuery){

        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Posts");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postList.clear();
                for (DataSnapshot ds :dataSnapshot.getChildren()){
                    ModelPost modelPost=ds.getValue(ModelPost.class);

                    if (modelPost.getpTitle().toLowerCase().contains(searchQuery.toLowerCase())
                            ||modelPost.getpDescription().toLowerCase().contains(searchQuery.toLowerCase())){

                        postList.add(modelPost);

                    }
                    adapterPosts=new AdapterPosts(getActivity(),postList);

                    recyclerView.setAdapter(adapterPosts);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getActivity(), ""+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

  //how to add back button and onBackpressed() method in fragment android

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        MenuInflater menuInflater = inflater;
        menuInflater.inflate(R.menu.main_menu, menu);
        MenuItem item=menu.findItem(R.id.opt_search);
        menu.findItem(R.id.createGroup).setVisible(false);
        menu.findItem(R.id.info).setVisible(false);
        menu.findItem(R.id.action_add_Participant_group).setVisible(false);
        final androidx.appcompat.widget.SearchView searchView=(androidx.appcompat.widget.SearchView) MenuItemCompat.getActionView(item);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!TextUtils.isEmpty(query)){
                    searchPosts(query);
                }else{
                    loadPosts();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (TextUtils.isEmpty(newText)){
                    searchPosts(newText);
                }else{
                    loadPosts();
                }
                return false;
            }
        });


        super.onCreateOptionsMenu(menu,menuInflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_photo:
                startActivity(new Intent(getActivity(), AddPostActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void logout() {
        FirebaseUser firebaseUser=auth.getCurrentUser();
        if (firebaseUser!=null){

        }else{
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            getActivity().finish();
        }
    }

}

