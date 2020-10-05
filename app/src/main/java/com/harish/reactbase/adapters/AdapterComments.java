package com.harish.reactbase.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.harish.reactbase.R;
import com.harish.reactbase.model.modelComment;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AdapterComments extends RecyclerView.Adapter<AdapterComments.ViewHolder> {

    Context context;
    List<modelComment>commentList;
    String myUid,postId;

    public AdapterComments(Context context, List<modelComment> commentList, String myUid, String postId) {
        this.context = context;
        this.commentList = commentList;
        this.myUid = myUid;
        this.postId = postId;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.row_comments,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final String uid=commentList.get(position).getUid();
        String name=commentList.get(position).getuName();
        String email=commentList.get(position).getuEmail();
        String image=commentList.get(position).getuDp();
        final String cid=commentList.get(position).getcId();
        String comment=commentList.get(position).getComment();
        String timeStamp=commentList.get(position).getTimestamp();

        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(Long.parseLong(timeStamp));
        String pTime = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();

        holder.nameTv.setText(name);
        holder.commentTv.setText(comment);
        holder.timeTv.setText(pTime);

        try {
            Picasso.get().load(image).placeholder(R.drawable.ic_face_black_24dp).into(holder.avatarTv);
        }
        catch(Exception e){
            Picasso.get().load(R.drawable.ic_face_black_24dp).into(holder.avatarTv);
        }

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (myUid.equals(uid)){
                    AlertDialog.Builder builder=new AlertDialog.Builder(v.getRootView().getContext());
                    builder.setTitle("Delete");
                    builder.setMessage("Are you sure to delete this comments?");
                    builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            deleteComment(cid);
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    builder.create().show();
                }else{
                    Toast.makeText(context, "Can't Delete other's comment", Toast.LENGTH_SHORT).show();
                }

                return false;
            }
        });
    }

    private void deleteComment(String cid) {
        final DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Posts").child(postId);
        ref.child("Comments").child(cid).removeValue();

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String comments=""+dataSnapshot.child("pComments").getValue();
                int newcommentVal=Integer.parseInt(comments)-1;
                ref.child("pComments").setValue(""+newcommentVal);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView avatarTv;
        TextView nameTv,commentTv,timeTv;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            avatarTv=itemView.findViewById(R.id.avatarTv);
            nameTv=itemView.findViewById(R.id.nameTv);
            commentTv=itemView.findViewById(R.id.commentTv);
            timeTv=itemView.findViewById(R.id.timeTv);
        }
    }
}
