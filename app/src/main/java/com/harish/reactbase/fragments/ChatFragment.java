package com.harish.reactbase.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

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
import com.harish.reactbase.R;
import com.harish.reactbase.AboutActivity;
import com.harish.reactbase.adapters.AdapterChatList;
import com.harish.reactbase.feedPack;
import com.harish.reactbase.model.ModelChatList;
import com.harish.reactbase.model.ModelClass;
import com.harish.reactbase.model.modelChat;

import java.util.ArrayList;
import java.util.List;

public class ChatFragment extends Fragment {

    FirebaseAuth auth;
    RecyclerView recyclerView;
    List<ModelChatList> chatlistList;
    List<ModelClass>userList;
    DatabaseReference reference;
    FirebaseUser currentUser;
    AdapterChatList adapterChatList;
    String hisUid;
    FirebaseUser user;
    public ChatFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_chat, container, false);
        auth=FirebaseAuth.getInstance();
        currentUser=FirebaseAuth.getInstance().getCurrentUser();
        recyclerView=view.findViewById(R.id.recyclerView);
        user=FirebaseAuth.getInstance().getCurrentUser();
        chatlistList=new ArrayList<>();
        hisUid=user.getUid();
        reference= FirebaseDatabase.getInstance().getReference("Chatlist").child(currentUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                chatlistList.clear();
                for (DataSnapshot ds:dataSnapshot.getChildren()){
                    ModelChatList chatList=ds.getValue(ModelChatList.class);
                    chatlistList.add(chatList);
                }
                loadChats();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        return view;
    }

    private void loadChats() {
        userList=new ArrayList<>();
        reference=FirebaseDatabase.getInstance().getReference("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear();
                for (DataSnapshot ds:dataSnapshot.getChildren()){
                    ModelClass user=ds.getValue(ModelClass.class);
                    for (ModelChatList chatList:chatlistList){
                        if (user.getUid()!=null && user.getUid().equals(chatList.getId())){
                            userList.add(user);
                            break;
                        }

                    }
                    adapterChatList=new AdapterChatList(getContext(),userList);

                    recyclerView.setAdapter(adapterChatList);

                    for (int i=0;i<userList.size();i++){
                        lastMessage(userList.get(i).getUid());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void lastMessage(final String userId) {
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String theLastMessage="default";
                for (DataSnapshot ds:dataSnapshot.getChildren()){
                    modelChat chat=ds.getValue(modelChat.class);
                    if (chat==null){
                        continue;
                    }
                    String sender=chat.getSender();
                    String reciver=chat.getReciver();
                    if (sender==null||reciver==null){
                        continue;
                    }
                    if (chat.getReciver().equals(currentUser.getUid())&& chat.getSender().equals(userId)
                            ||chat.getReciver().equals(userId)&& chat.getSender().equals(currentUser.getUid())){

                        if (chat.getType().equals("image")){
                            theLastMessage="Sent a Image";
                        }else{
                            theLastMessage=chat.getMessage();
                        }
                    }
                }
                adapterChatList.setLastMessageMap(userId,theLastMessage);
                adapterChatList.notifyDataSetChanged();
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
        menu.findItem(R.id.createGroup).setVisible(false);
        menu.findItem(R.id.info).setVisible(false);
        menu.findItem(R.id.action_add_Participant_group).setVisible(false);
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
