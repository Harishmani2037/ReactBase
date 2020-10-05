package com.harish.reactbase.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.Adapter;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.harish.reactbase.R;
import com.harish.reactbase.model.modelChat;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;


public class ChatAdapter extends Adapter<ChatAdapter.ViewHolder> {

    private  static  final  int MSG_TYPE_RIGHT=0;
    private  static  final  int MSG_TYPE_LEFT=1;

    Context context;
    List<modelChat> chatlist;
    String imageUri;
    FirebaseUser fUser;

    public ChatAdapter(Context context, List<modelChat> chatlist, String imageUri) {
        this.context = context;
        this.chatlist = chatlist;
        this.imageUri = imageUri;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType==MSG_TYPE_RIGHT){
            View view= LayoutInflater.from(context).inflate(R.layout.row_chat_right,parent,false);
            return new ViewHolder(view);
        }else{
            View view= LayoutInflater.from(context).inflate(R.layout.row_chat_left,parent,false);
            return new ViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        String message=chatlist.get(position).getMessage();
        String timestamp=chatlist.get(position).getTimestamp();
        String type=chatlist.get(position).getType();
        boolean isseen=chatlist.get(position).isSeen();
        Calendar calendar=Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(Long.parseLong(timestamp));
        String dateTime= DateFormat.format("dd/MM/yyyy hh:mm aa",calendar).toString();

        if (type.equals("text")){
            holder.messageTv.setVisibility(View.VISIBLE);
            holder.messageIv.setVisibility(View.GONE);
            holder.messageTv.setText(message);
        }else{
            holder.messageTv.setVisibility(View.GONE);
            holder.messageIv.setVisibility(View.VISIBLE);

            Picasso.get().load(message).placeholder(R.drawable.ic_photoimage).into(holder.messageIv);
        }
 holder.timeTv.setText(dateTime);
//        if (chatlist.get(position).isSeen()){
//            holder.isSeenTv.setVisibility(View.VISIBLE);
//            holder.isSeenTv.setText("seen");
//        }else{
//            holder.isSeenTv.setVisibility(View.VISIBLE);
//            holder.isSeenTv.setText("delivered");
//        }
        try{
            Picasso.get().load(imageUri).into(holder.profileTv);

        }catch (Exception e){

        }

        holder.messageLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder=new AlertDialog.Builder(context);
                builder.setTitle("Delete");
                builder.setMessage("Are you sure to delete this message ? ");
                builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                  deleteMessage(position);
                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.create().show();
            }
        });
            if (position==chatlist.size()-1){   //tot=3 last=2 curr=3-2
               if (chatlist.get(position).isSeen()){
                   holder.isSeenTv.setText("Seen");
               }else{
                   holder.isSeenTv.setVisibility(View.VISIBLE);
                   holder.isSeenTv.setText("Delivered");
               }
            }else {
                holder.isSeenTv.setVisibility(View.GONE);
            }


    }

    private void deleteMessage(int position) {

        final String myUID=FirebaseAuth.getInstance().getCurrentUser().getUid();

        String msgTimeStamp=chatlist.get(position).getTimestamp();
        DatabaseReference dbRef= FirebaseDatabase.getInstance().getReference("Chats");
        Query query=dbRef.orderByChild("timestamp").equalTo(msgTimeStamp);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds:dataSnapshot.getChildren()){

                    if (ds.child("sender").getValue().equals(myUID)) {
                        //ds.getRef().removeValue();
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("message", "This message was Deleted...");
                        ds.getRef().updateChildren(hashMap);
                        Toast.makeText(context, "message Deleted...", Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(context, "you can delete only your messages...", Toast.LENGTH_SHORT).show();
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
        return chatlist.size();
    }

    @Override
    public int getItemViewType(int position) {
        fUser= FirebaseAuth.getInstance().getCurrentUser();
        if (chatlist.get(position).getSender().equals(fUser.getUid())){    //sender=user,equals to fuser=user
            return MSG_TYPE_RIGHT;
        }else {
            return MSG_TYPE_LEFT;
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
       ImageView profileTv;
        ImageView messageIv;
        TextView messageTv,timeTv,isSeenTv;
        LinearLayout messageLayout;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            profileTv=itemView.findViewById(R.id.profileTv);
            messageTv=itemView.findViewById(R.id.messageTv);
            messageIv=itemView.findViewById(R.id.messageIv);
            timeTv=itemView.findViewById(R.id.timeTv);
            isSeenTv=itemView.findViewById(R.id.seenTv1);
            messageLayout=itemView.findViewById(R.id.messageLayout);
        }
    }

}