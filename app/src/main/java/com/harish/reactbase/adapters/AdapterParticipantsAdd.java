package com.harish.reactbase.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.harish.reactbase.R;
import com.harish.reactbase.model.ModelClass;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;

public class AdapterParticipantsAdd extends RecyclerView.Adapter<AdapterParticipantsAdd.ViewHolder> {
    Context context;
    List<ModelClass>userlist;

    String groupId,myGroupRole;

    public AdapterParticipantsAdd(Context context, List<ModelClass> userlist, String groupId, String myGroupRole) {
        this.context = context;
        this.userlist = userlist;
        this.groupId = groupId;
        this.myGroupRole = myGroupRole;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.row_participants_add,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final ModelClass modelUser=userlist.get(position);

        String name=modelUser.getName();
        String email=modelUser.getEmail();
        String image=modelUser.getImage();
        final String uid=modelUser.getUid();

        holder.nameTv.setText(name);
        holder.emailTv.setText(email);

        try{
            Picasso.get().load(image).placeholder(R.drawable.ic_face_black_24dp).into(holder.avatarTv);
        }
        catch(Exception e){
            Picasso.get().load(R.drawable.ic_face_black_24dp).into(holder.avatarTv);
        }
        checkIfAlreadyExist(modelUser,holder);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Groups");
                ref.child(groupId).child("Participants").child(uid).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            String hisPreviousRole=""+dataSnapshot.child("role").getValue();   //creatror
                            String[] options;

                            AlertDialog.Builder builder=new AlertDialog.Builder(context);
                            builder.setTitle("Choose Options");

                            if (myGroupRole.equals("creator")){
                                if (hisPreviousRole.equals("admin")){
                                    options=new String[]{"Remove Admin","Remove User"};
                                    builder.setItems(options, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (which==0){
                                                removeAdmin(modelUser);
                                            }else{
                                                removeParticipants(modelUser);
                                            }
                                        }
                                    }).show();
                                }
                                else if (hisPreviousRole.equals("participant")){
                                    options=new String[]{"Make Admin","Remove User"};
                                    builder.setItems(options, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (which==0){
                                                makeAdmin(modelUser);
                                            }else{
                                                removeParticipants(modelUser);
                                            }
                                        }
                                    }).show();
                                }
                            }
                            else if (myGroupRole.equals("admin")){
                                if (hisPreviousRole.equals("creator")){
                                    Toast.makeText(context, "Creator of Group...", Toast.LENGTH_SHORT).show();
                                }
                                else if (hisPreviousRole.equals("admin")){
                                    options=new String[]{"Remove Admin","Remove User"};
                                    builder.setItems(options, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (which==0){
                                                removeAdmin(modelUser);
                                            }else{
                                                removeParticipants(modelUser);   //////
                                            }
                                        }
                                    }).show();
                                }
                                else if (hisPreviousRole.equals("participant")){
                                    options=new String[]{"Make Admin","Remove User"};
                                    builder.setItems(options, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (which==0){
                                                makeAdmin(modelUser);
                                            }else{
                                                removeParticipants(modelUser);   ////////
                                            }
                                        }
                                    }).show();
                                }
                            }

                        }else{
                            AlertDialog.Builder builder=new AlertDialog.Builder(context);
                            builder.setTitle("Add Participants").setMessage("Add this user in this group")
                                    .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            addParticipant(modelUser);
                                        }
                                    }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            }).show();

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(context, ""+databaseError, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void addParticipant(ModelClass modelUser) {
        String timeStamp=""+System.currentTimeMillis();
        HashMap<String,Object>hashMap=new HashMap<>();
        hashMap.put("uid",modelUser.getUid());
        hashMap.put("role","participant");
        hashMap.put("timesStamp",""+timeStamp);


        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Participants").child(modelUser.getUid()).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(context, "Added Successfully...", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void makeAdmin(ModelClass modelUser) {
        HashMap<String,Object>hashMap=new HashMap<>();
        hashMap.put("role","admin");
        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Participants").child(modelUser.getUid()).updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(context, "The User is Now Admin", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void removeParticipants(ModelClass modelUser) {
        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Participants").child(modelUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(context, "Removed Successfully", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void removeAdmin(ModelClass modelUser) {
        HashMap<String,Object>hashMap=new HashMap<>();
        hashMap.put("role","participant");
        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Participants").child(modelUser.getUid()).updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(context, "The User is longer admin...", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkIfAlreadyExist(ModelClass modelUser, final ViewHolder holder) {
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Participants").child(modelUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    String hisRole=""+dataSnapshot.child("role").getValue();
                    holder.statusTv.setText(hisRole);
                }else {
                    holder.statusTv.setText("");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return userlist.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView avatarTv;
        TextView nameTv,emailTv,statusTv;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            avatarTv=itemView.findViewById(R.id.avatar);
            nameTv=itemView.findViewById(R.id.nameTv);
            emailTv=itemView.findViewById(R.id.emailTv);
            statusTv=itemView.findViewById(R.id.statusTv);
        }
    }
}