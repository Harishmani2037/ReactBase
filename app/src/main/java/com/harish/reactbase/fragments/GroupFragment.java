package com.harish.reactbase.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
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
import com.harish.reactbase.adapters.AdapterGroupChatList;
import com.harish.reactbase.model.ModelGroupChatList;

import java.util.ArrayList;
import java.util.List;

public class GroupFragment extends Fragment {
    FirebaseAuth firebaseAuth;

    private RecyclerView groupRv;

    List<ModelGroupChatList> groupChatList;

    AdapterGroupChatList adapterGroupChatList;

    public GroupFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_group, container, false);
        groupRv=view.findViewById(R.id.groupsRv);
        firebaseAuth=FirebaseAuth.getInstance();
        loadGroupChatList();
        return view;
    }

    private void loadGroupChatList() {
        groupChatList=new ArrayList<>();

        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Groups");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                groupChatList.clear();
                for (DataSnapshot ds:dataSnapshot.getChildren()){
                    if (ds.child("Participants").child(firebaseAuth.getUid()).exists()){
                        ModelGroupChatList model=ds.getValue(ModelGroupChatList.class);
                        groupChatList.add(model);
                    }
                }
                adapterGroupChatList=new AdapterGroupChatList(getActivity(),groupChatList);
                groupRv.setAdapter(adapterGroupChatList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void searchGroupChatList(final String query) {
        groupChatList=new ArrayList<>();

        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Groups");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                groupChatList.clear();
                for (DataSnapshot ds:dataSnapshot.getChildren()){
                    if (ds.child("Participants").child(firebaseAuth.getUid()).exists()){
                        if (ds.child("groupTitle").toString().toLowerCase().contains(query.toLowerCase())){
                            ModelGroupChatList model=ds.getValue(ModelGroupChatList.class);
                            groupChatList.add(model);
                        }
                    }
                }
                adapterGroupChatList=new AdapterGroupChatList(getActivity(),groupChatList);
                groupRv.setAdapter(adapterGroupChatList);
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

        MenuItem item=menu.findItem(R.id.opt_search);
        final SearchView searchView=(SearchView) MenuItemCompat.getActionView(item);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!TextUtils.isEmpty(query)){
                    searchGroupChatList(query);
                }else{
                    loadGroupChatList();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (TextUtils.isEmpty(newText)){
                    searchGroupChatList(newText);
                }else{
                    loadGroupChatList();
                }
                return false;
            }
        });

        super.onCreateOptionsMenu(menu,menuInflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.createGroup:
                startActivity(new Intent(getActivity(), CreateGroup.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void logout() {
        FirebaseUser firebaseUser=firebaseAuth.getCurrentUser();
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
