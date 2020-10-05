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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.harish.reactbase.LoginActivity;
import com.harish.reactbase.CreateGroup;
import com.harish.reactbase.R;
import com.harish.reactbase.AboutActivity;
import com.harish.reactbase.adapters.AdapterUsers;
import com.harish.reactbase.feedPack;
import com.harish.reactbase.model.ModelClass;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class UsersFragment extends Fragment {
    RecyclerView recyclerView;
    AdapterUsers adapterUsers;
    List<ModelClass> userList;
    FirebaseAuth auth;
    public UsersFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_users, container, false);
        auth=FirebaseAuth.getInstance();
        recyclerView=view.findViewById(R.id.users);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        userList=new ArrayList<>();
        getAllUsers();
        return view;
    }
    private void getAllUsers(){
        final FirebaseUser firebaseUser=FirebaseAuth.getInstance().getCurrentUser();

        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Users");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear();
                for (DataSnapshot ds:dataSnapshot.getChildren()){
                    ModelClass modelUser=ds.getValue(ModelClass.class);

                    if (!firebaseUser.getUid().equals(modelUser.getUid())){
                        userList.add(modelUser);
                    }

                    adapterUsers=new AdapterUsers(userList, getActivity());
                    recyclerView.setAdapter(adapterUsers);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void searchUsers(final String query) {
        final FirebaseUser firebaseUser=FirebaseAuth.getInstance().getCurrentUser();

        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Users");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear();
                for (DataSnapshot ds:dataSnapshot.getChildren()){
                    ModelClass modelUser=ds.getValue(ModelClass.class);

                    if (!firebaseUser.getUid().equals(modelUser.getUid())){

                        if (modelUser.getName().toLowerCase().contains(query.toLowerCase()) || modelUser.getName().toLowerCase().contains(query.toLowerCase())){
                            userList.add(modelUser);
                        }
                    }
                    adapterUsers=new AdapterUsers(userList, getActivity());
                    adapterUsers.notifyDataSetChanged();
                    recyclerView.setAdapter(adapterUsers);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        MenuInflater menuInflater = inflater;
        menuInflater.inflate(R.menu.main_menu, menu);
        menu.findItem(R.id.action_add_photo).setVisible(false);
        menu.findItem(R.id.action_add_Participant_group).setVisible(false);
        menu.findItem(R.id.info).setVisible(false);
        menu.findItem(R.id.createGroup).setVisible(false);
        MenuItem item=menu.findItem(R.id.opt_search);
        final SearchView searchView=(SearchView) MenuItemCompat.getActionView(item);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!TextUtils.isEmpty(query)){
                    searchUsers(query);
                }else{
                    getAllUsers();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (TextUtils.isEmpty(newText)){
                    searchUsers(newText);
                }else{
                    getAllUsers();
                }
                return false;
            }
        });

        super.onCreateOptionsMenu(menu,menuInflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout:
                auth.signOut();
                logout();
                break;
            case R.id.feedback:
                startActivity(new Intent(getActivity(), feedPack.class));
                break;
            case R.id.createGroup:
                startActivity(new Intent(getActivity(), CreateGroup.class));
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

