package com.harish.reactbase.adapters;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.harish.reactbase.R;
import com.harish.reactbase.model.ModelGroupChat;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AdapterGroupChat extends RecyclerView.Adapter<AdapterGroupChat.ViewHolder>{
    private  static  final  int MSG_TYPE_RIGHT=1;
    private  static  final  int MSG_TYPE_LEFT=0;

    Context context;
    List<ModelGroupChat> modelGroupchatlist;

    FirebaseAuth firebaseAuth;

    public AdapterGroupChat(Context context, List<ModelGroupChat> modelGroupchatlist) {
        this.context = context;
        this.modelGroupchatlist = modelGroupchatlist;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType==MSG_TYPE_RIGHT){
            View view= LayoutInflater.from(context).inflate(R.layout.row_groupchat_right,parent,false);
            return new ViewHolder(view);
        }else{
            View view= LayoutInflater.from(context).inflate(R.layout.row_groupchat_left,parent,false);
            return new ViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ModelGroupChat model=modelGroupchatlist.get(position);

        String message=model.getMessage();
        String senderUid=model.getSender();
        String timeStamp=model.getTimeStamp();
        String type=model.getType();
        Calendar calendar=Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(Long.parseLong(timeStamp));
        String dateTime= DateFormat.format("dd/MM/yyyy hh:mm aa",calendar).toString();

      if (type.equals("text")){
          holder.messageTv.setVisibility(View.VISIBLE);
          holder.messageIv.setVisibility(View.GONE);
          holder.messageTv.setText(message);
      }else{
          holder.messageTv.setVisibility(View.GONE);
          holder.messageIv.setVisibility(View.VISIBLE);
          try{
              Picasso.get().load(message).placeholder(R.drawable.ic_photoimage).into(holder.messageIv);
          }
          catch(Exception e){
              holder.messageIv.setImageResource(R.drawable.ic_photoimage);
          }
      }
        holder.timeTv.setText(dateTime);

        setUserName(model,holder);
    }


    private void setUserName(ModelGroupChat model, final ViewHolder holder) {
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("uid").equalTo(model.getSender()).addValueEventListener(new ValueEventListener() {
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

    @Override
    public int getItemCount() {
        return modelGroupchatlist.size();
    }

    @Override
    public int getItemViewType(int position) {
        firebaseAuth=FirebaseAuth.getInstance();
        FirebaseUser user=firebaseAuth.getCurrentUser();
        if (modelGroupchatlist.get(position).getSender().equals(user.getUid())){
            return MSG_TYPE_RIGHT;
        }else {
            return MSG_TYPE_LEFT;
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameTv,messageTv,timeTv;
        ImageView messageIv;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTv=itemView.findViewById(R.id.nameTv);
            messageTv=itemView.findViewById(R.id.messageTv);
            messageIv=itemView.findViewById(R.id.messageIv);
            timeTv=itemView.findViewById(R.id.timeTv);
        }
    }
}
