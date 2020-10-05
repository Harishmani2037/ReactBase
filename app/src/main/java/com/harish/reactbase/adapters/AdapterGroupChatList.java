package com.harish.reactbase.adapters;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.harish.reactbase.GroupChatActivity;
import com.harish.reactbase.R;
import com.harish.reactbase.model.ModelGroupChatList;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AdapterGroupChatList extends RecyclerView.Adapter<AdapterGroupChatList.ViewHolder>{
    private Context context;
    private List<ModelGroupChatList> groupChatList;

    public AdapterGroupChatList(Context context, List<ModelGroupChatList> groupChatList) {
        this.context = context;
        this.groupChatList = groupChatList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.row_groupchat_list,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ModelGroupChatList model=groupChatList.get(position);
        final String groupId=model.getGroupId();
        String groupIcon=model.getGroupIcon();
        String groupTitle=model.getGroupTitle();
        holder.nameTv.setText("");

        holder.messageTv.setText("");
        holder.groupTitileTv.setText(groupTitle);

        loadLastMessage(model,holder);

            try{
                Picasso.get().load(groupIcon).placeholder(R.drawable.ic_group_black_24dp).into(holder.groupIconTv);
            }
            catch(Exception e){
                Picasso.get().load(R.drawable.ic_group_black_24dp).into(holder.groupIconTv);
            }
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent  intent=new Intent(context, GroupChatActivity.class);
                    intent.putExtra("groupId",groupId);
                    context.startActivity(intent);
                }
            });
    }

    private void loadLastMessage(ModelGroupChatList model, final ViewHolder holder) {
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(model.getGroupId()).child("Messages").limitToLast(1).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds:dataSnapshot.getChildren()){
                    String message=""+ds.child("message").getValue();
                    String timeStamp=""+ds.child("timeStamp").getValue();
                    String sender=""+ds.child("sender").getValue();
                    String messageType=""+ds.child("type").getValue();

                    Calendar calendar=Calendar.getInstance(Locale.ENGLISH);
                    calendar.setTimeInMillis(Long.parseLong(timeStamp));
                    String dateTime= DateFormat.format("dd/MM/yyyy hh:mm aa",calendar).toString();


                    if (messageType.equals("image")){
                        holder.messageTv.setText("sent Photo");

                    }else {
                        holder.messageTv.setText(message);
                    }
                        holder.timetv.setText(dateTime);

                    DatabaseReference reference=FirebaseDatabase.getInstance().getReference("Users");
                    reference.orderByChild("uid").equalTo(sender).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot ds:dataSnapshot.getChildren()){
                                String name=""+ds.child("name").getValue();
                                holder.nameTv.setText(name);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return groupChatList.size();
    }

    public  class ViewHolder extends RecyclerView.ViewHolder {
        ImageView groupIconTv;
        TextView groupTitileTv,nameTv,messageTv,timetv;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            groupIconTv=itemView.findViewById(R.id.groupIconTv);
            groupTitileTv=itemView.findViewById(R.id.groupTitleTv);
            nameTv=itemView.findViewById(R.id.nameTv);
            messageTv=itemView.findViewById(R.id.messageTv);
            timetv=itemView.findViewById(R.id.timeTv1);
        }
    }
}
