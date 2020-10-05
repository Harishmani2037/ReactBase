package com.harish.reactbase.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.harish.reactbase.R;
import com.harish.reactbase.adapters.AdapterNotifications;
import com.harish.reactbase.model.ModelNotifications;

import java.util.ArrayList;
import java.util.List;

public class NotificationFragment extends Fragment {

    RecyclerView notificationsRv;

    FirebaseAuth firebaseAuth;

    private List<ModelNotifications> notificationsList;

    AdapterNotifications adapterNotifications;

    public NotificationFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view=inflater.inflate(R.layout.fragment_notification, container, false);
        notificationsRv=view.findViewById(R.id.notificationTv);
        firebaseAuth=FirebaseAuth.getInstance();
        getAllNotifications();
        return view;
    }

    private void getAllNotifications() {
        notificationsList=new ArrayList<>();

        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).child("Notifications").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                notificationsList.clear();
                for (DataSnapshot ds :dataSnapshot.getChildren()){
                    ModelNotifications model=ds.getValue(ModelNotifications.class);
                    notificationsList.add(model);
                }
                adapterNotifications=new AdapterNotifications(getActivity(),notificationsList);

                notificationsRv.setAdapter(adapterNotifications);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}