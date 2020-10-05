package com.harish.reactbase.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.harish.reactbase.ChatActivity;
import com.harish.reactbase.R;
import com.harish.reactbase.model.ModelClass;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;

public class AdapterChatList extends RecyclerView.Adapter<AdapterChatList.viewHolder> {

    FirebaseAuth firebaseAuth;
    Context context;
    List<ModelClass>userList;
    private HashMap<String,String> lastMessageMap;
    String myUid;

    public AdapterChatList(Context context, List<ModelClass> userList) {
        this.context = context;
        this.userList = userList;
        lastMessageMap = new HashMap<>();
        firebaseAuth = FirebaseAuth.getInstance();
        myUid = firebaseAuth.getUid();
    }


    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.row_chatlist,parent,false);
        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, final int position) {
        final String hisUid=userList.get(position).getUid();
        String userImage=userList.get(position).getImage();
        String userName=userList.get(position).getName();
        String lastmessage=lastMessageMap.get(hisUid);


        //
        holder.nameTv.setText(userName);
        if (lastmessage==null || lastmessage.equals("default")){
            holder.lastMessageTv.setVisibility(View.GONE);
        }else{
            holder.lastMessageTv.setVisibility(View.VISIBLE);
            holder.lastMessageTv.setText(lastmessage);
        }
        try{
            Picasso.get().load(userImage).placeholder(R.drawable.ic_face_black_24dp).into(holder.profileTv);
        }
        catch(Exception e){
            Picasso.get().load(R.drawable.ic_face_black_24dp).into(holder.profileTv);
        }

        if (userList.get(position).getOnlineStatus().equals("online")){
            holder.onlineStatusTv.setImageResource(R.drawable.circle_online);
        }else{
            holder.onlineStatusTv.setImageResource(R.drawable.circle_offline);
        }

        holder.blockTv.setImageResource(R.drawable.ic_unblock);

        checkisBlocked(hisUid, holder, position);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isBlockedorNot(hisUid);
            }
        });

        holder.blockTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (userList.get(position).isBlocked()) {
                    unBlockUser(hisUid);
                } else {
                    blockUser(hisUid);
                }
            }
        });

    }

    private void isBlockedorNot(final String hisUid) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(hisUid).child("BlockUsers").orderByChild("uid").equalTo(myUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds:dataSnapshot.getChildren()){
                    if (dataSnapshot.exists()) {
                        Toast.makeText(context, "You are blocked by the user cant't message", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                Intent intent=new Intent(context, ChatActivity.class);
                intent.putExtra("hisUid",hisUid);
                context.startActivity(intent);
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void checkisBlocked(String hisUID, final viewHolder holder, final int position) {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).child("BlockUsers").orderByChild("uid").equalTo(hisUID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds:dataSnapshot.getChildren()) {
                    if (dataSnapshot.exists()) {
                        holder.blockTv.setImageResource(R.drawable.ic_block);
                       userList.get(position).setBlocked(true);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void blockUser(String hisUID) {
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("uid", hisUID);
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(myUid).child("BlockUsers").child(hisUID).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(context, "Blocked", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, "Action Failed or Upgrade our App", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void unBlockUser(String hisUID) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(myUid).child("BlockUsers").orderByChild("uid").equalTo(hisUID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    if (ds.exists()) {
                        ds.getRef().removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(context, "UnBlocked", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(context, "Action Failed or Upgrade our App", Toast.LENGTH_SHORT).show();
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


    public void setLastMessageMap(String userId,String lastMessage){
        lastMessageMap.put(userId,lastMessage);
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public class viewHolder extends RecyclerView.ViewHolder {
        ImageView profileTv,onlineStatusTv,blockTv;
        TextView nameTv,lastMessageTv;
        public viewHolder(@NonNull View itemView) {
            super(itemView);
            profileTv=itemView.findViewById(R.id.profileTv);
            onlineStatusTv=itemView.findViewById(R.id.onlineStatusTv);
            nameTv=itemView.findViewById(R.id.nameTv);
            lastMessageTv=itemView.findViewById(R.id.lastMessageTv);
            blockTv=itemView.findViewById(R.id.blockTv);
        }
    }
}
