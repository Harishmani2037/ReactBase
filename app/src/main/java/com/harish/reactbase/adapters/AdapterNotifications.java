package com.harish.reactbase.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.format.DateFormat;
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
import com.harish.reactbase.PostDetailsActivity;
import com.harish.reactbase.R;
import com.harish.reactbase.model.ModelNotifications;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AdapterNotifications extends RecyclerView.Adapter<AdapterNotifications.ViewHolder> {
    Context context;
    List<ModelNotifications>notificationsList;

    private FirebaseAuth firebaseAuth;

    public AdapterNotifications(Context context, List<ModelNotifications> notificationsList) {
        this.context = context;
        this.notificationsList = notificationsList;

        firebaseAuth=FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.row_notifications,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final ModelNotifications model=notificationsList.get(position);

        String name=model.getsName();
        String notification=model.getNotifications();
        String image=model.getsImage();
        final String timeStamp=model.getTimesStamp();
        String senderUid=model.getsUid();
        final String pId=model.getpId();


        Calendar calendar=Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(Long.parseLong(timeStamp));
        String pTime= DateFormat.format("dd/MM/yyyy hh:mm aa",calendar).toString();

        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("uid").equalTo(senderUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds:dataSnapshot.getChildren()){
                    String name=""+ds.child("name").getValue();
                    String image=""+ds.child("image").getValue();
                    String email=""+ds.child("email").getValue();

                    model.setsName(name);
                    model.setsEmail(email);
                    model.setsImage(image);

                    holder.nameTv.setText(name);

                    try{
                        Picasso.get().load(image).placeholder(R.drawable.ic_face_black_24dp).into(holder.avatarTv);
                    }
                    catch (Exception e){
                        holder.avatarTv.setImageResource(R.drawable.ic_face_black_24dp);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        try{
            Picasso.get().load(image).placeholder(R.drawable.ic_face_black_24dp).into(holder.avatarTv);
        }
        catch (Exception e) {
            holder.avatarTv.setImageResource(R.drawable.ic_face_black_24dp);
        }
        holder.nameTv.setText(name);
        holder.notificationTv.setText(notification);
        holder.timeTv.setText(pTime);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(context, PostDetailsActivity.class);
                intent.putExtra("pId",pId);
                context.startActivity(intent);
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                AlertDialog.Builder builder=new AlertDialog.Builder(context);
                builder.setTitle("Delete");
                builder.setMessage("Are you sure to delete this notification");
                builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Users");
                        ref.child(firebaseAuth.getUid()).child("Notifications").child(timeStamp).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(context, "Notification removed...", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.create().show();
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return notificationsList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView avatarTv;
        TextView nameTv,timeTv,notificationTv;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
           avatarTv=itemView.findViewById(R.id.avatarTv);
           nameTv=itemView.findViewById(R.id.nameTv);
           timeTv=itemView.findViewById(R.id.timeTv);
           notificationTv=itemView.findViewById(R.id.notificationTv);
        }
    }
}
