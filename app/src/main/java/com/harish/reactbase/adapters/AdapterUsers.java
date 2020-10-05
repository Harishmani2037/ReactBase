package com.harish.reactbase.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import com.harish.reactbase.ThereprofileActivity;
import com.harish.reactbase.model.ModelClass;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;

public class AdapterUsers extends RecyclerView.Adapter<AdapterUsers.ViewHolder> {
    List<ModelClass> mUsers;
    Context context;
    FirebaseAuth firebaseAuth;
    String myUid;

    public AdapterUsers(List<ModelClass> mUsers, Context context) {
        this.mUsers = mUsers;
        this.context = context;
        firebaseAuth = FirebaseAuth.getInstance();
        myUid = firebaseAuth.getUid();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView avatarTv, blockTv;
        TextView nameTv, emailTv;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            avatarTv = itemView.findViewById(R.id.avatar);
            nameTv = itemView.findViewById(R.id.nameTv);
            emailTv = itemView.findViewById(R.id.emailTv);
            blockTv = itemView.findViewById(R.id.blockTv);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_users, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        final String hisUID = mUsers.get(position).getUid();
        String userImages = mUsers.get(position).getImage();
        final String userEmail = mUsers.get(position).getEmail();
        String userName = mUsers.get(position).getName();
        holder.nameTv.setText(userName);
        holder.emailTv.setText(userEmail);
        try {
            Picasso.get().load(userImages).placeholder(R.drawable.ic_face_black_24dp).into(holder.avatarTv);
        } catch (Exception e) {
            Picasso.get().load(R.drawable.ic_face_black_24dp).into(holder.avatarTv);
        }

        holder.blockTv.setImageResource(R.drawable.ic_unblock);

        checkisBlocked(hisUID,holder, position);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setItems(new String[]{"Profile", "Chat"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            Intent intent = new Intent(context, ThereprofileActivity.class);
                            intent.putExtra("uid", hisUID);
                            context.startActivity(intent);
                        }
                        if (which == 1) {
                            isBlockedorNot(hisUID);
                        }
                    }
                });
                builder.create().show();
            }
        });

        holder.blockTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mUsers.get(position).isBlocked()) {
                    unBlockUser(hisUID);
                } else {
                    blockUser(hisUID);
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
                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra("hisUid", hisUid);
                context.startActivity(intent);
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void checkisBlocked(String hisUID, final ViewHolder holder, final int position) {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).child("BlockUsers").orderByChild("uid").equalTo(hisUID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds:dataSnapshot.getChildren()) {
                    if (dataSnapshot.exists()) {
                        holder.blockTv.setImageResource(R.drawable.ic_block);
                        mUsers.get(position).setBlocked(true);
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

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

}
