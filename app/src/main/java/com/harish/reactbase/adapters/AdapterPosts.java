package com.harish.reactbase.adapters;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.harish.reactbase.LikePostActivity;
import com.harish.reactbase.PostDetailsActivity;
import com.harish.reactbase.R;
import com.harish.reactbase.ThereprofileActivity;
import com.harish.reactbase.model.ModelPost;
import com.squareup.picasso.Picasso;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;


public class AdapterPosts extends RecyclerView.Adapter<AdapterPosts.ViewHolder>{
    Context context;
    List<ModelPost>postList;

    String myuid;

    private DatabaseReference likeRef;
    private  DatabaseReference postsRef;

    boolean mProcessLikes=false;

    public AdapterPosts(Context context, List<ModelPost> postList) {
        this.context = context;
        this.postList = postList;
        myuid= FirebaseAuth.getInstance().getCurrentUser().getUid();
        likeRef=FirebaseDatabase.getInstance().getReference().child("Likes");
        postsRef=FirebaseDatabase.getInstance().getReference().child("Posts");
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.row_posts,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        final String uid=postList.get(position).getUid();
        String uEmail=postList.get(position).getuEmail();
        final String uName=postList.get(position).getuName();
        String uDp=postList.get(position).getuDP();
        final String pId=postList.get(position).getpId();
        final String pTitle=postList.get(position).getpTitle();
        final String pDescription=postList.get(position).getpDescription();
        final String pImage=postList.get(position).getpImage();
        String pTimeStamp=postList.get(position).getpTime();
        final String pLike=postList.get(position).getpLikes();
        final String pComments=postList.get(position).getpComments();

        Calendar calendar=Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(Long.parseLong(pTimeStamp));
        String pTime= DateFormat.format("dd/MM/yyyy hh:mm aa",calendar).toString();

        holder.uNameTv.setText(uName);
        holder.pTimeTv.setText(pTime);
        holder.pTitleTv.setText(pTitle);
        holder.pDescriptionTv.setText(pDescription);
        holder.pLikesTv.setText(pLike+" Likes");
        holder.pCommentsTv.setText(pComments+" Comments");

        setLikes(holder,pId);
        try{
            Picasso.get().load(uDp).placeholder(R.drawable.ic_face_black_24dp).into(holder.uPictureTv);
        }
        catch (Exception e){
            Picasso.get().load(R.drawable.ic_face_black_24dp).into(holder.uPictureTv);
        }
        if (pImage.equals("noImage")){
           holder.pImageTv.setVisibility(View.GONE);
        }else{
            holder.pImageTv.setVisibility(View.VISIBLE);
            try{
                Picasso.get().load(pImage).into(holder.pImageTv);
            }
            catch(Exception e){
                Toast.makeText(context, "Image not found", Toast.LENGTH_SHORT).show();
            }
        }

        holder.moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
             showMoreOptions(holder.moreBtn,uid,myuid,pId,pImage);
            }
        });

        holder.likeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final int pLikes=Integer.parseInt(postList.get(position).getpLikes());

                mProcessLikes=true;
                final String postIde=postList.get(position).getpId();

                likeRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (mProcessLikes){
                            if (dataSnapshot.child(postIde).hasChild(myuid)){
                                postsRef.child(postIde).child("pLikes").setValue(""+(pLikes-1));
                                likeRef.child(postIde).child(myuid).removeValue();
                                mProcessLikes=false;
                            }
                            else {
                                postsRef.child(postIde).child("pLikes").setValue(""+(pLikes+1));
                                likeRef.child(postIde).child(myuid).setValue("Liked");
                                mProcessLikes=false;
                                addTextNotification(""+uid,""+pId,"Liked your Post");
                            }
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }
        });

        holder.commentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(context, PostDetailsActivity.class);
                intent.putExtra("pId",pId);
                context.startActivity(intent);
            }
        });

        holder.shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BitmapDrawable bitmapDrawable=(BitmapDrawable)holder.pImageTv.getDrawable();
                if (bitmapDrawable==null){
                    shareTextOnly(pTitle,pDescription);
                }else{

                    Bitmap bitmap=bitmapDrawable.getBitmap();
                    shareImageAndText(bitmap,pTitle,pDescription);
                }
            }
        });

        holder.profileLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent=new Intent(context, ThereprofileActivity.class);
                intent.putExtra("uid",uid);
                context.startActivity(intent);
            }
        });

        holder.pLikesTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(context, LikePostActivity.class);
                intent.putExtra("pId",pId);
                context.startActivity(intent);
            }
        });
    }

    private void addTextNotification(String hisUid,String pId,String notification){
        String timeStamp = ""+System.currentTimeMillis();

        HashMap<String ,Object>hashmap=new HashMap<>();
        //pId,timesStamp,pUid,notifications,sUid,sName,sEmail,sImage;
       hashmap.put("pId",pId);
       hashmap.put("timesStamp",timeStamp);
       hashmap.put("pUid",hisUid);
       hashmap.put("notifications",notification);
       hashmap.put("sUid",myuid);

       DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Users");
       ref.child(hisUid).child("Notifications").child(timeStamp).setValue(hashmap).addOnSuccessListener(new OnSuccessListener<Void>() {
           @Override
           public void onSuccess(Void aVoid) {

           }
       }).addOnFailureListener(new OnFailureListener() {
           @Override
           public void onFailure(@NonNull Exception e) {

           }
       });
    }

    private void shareTextOnly(String pTitle, String pDescription) {

        String shareBody=pTitle+"\n"+pDescription;

        //
        Intent sIntent=new Intent(Intent.ACTION_SEND);
        sIntent.setType("text/plain");
        sIntent.putExtra(Intent.EXTRA_SUBJECT,"Subject Here");
        sIntent.putExtra(Intent.EXTRA_TEXT,shareBody);
        context.startActivity(Intent.createChooser(sIntent,"Shar Via"));
    }

    private void shareImageAndText(Bitmap bitmap, String pTitle, String pDescription) {
        String shareBody=pTitle+"\n"+pDescription;

        Uri uri=saveImageToShare(bitmap);

        Intent sIntent=new Intent(Intent.ACTION_SEND);
        sIntent.putExtra(Intent.EXTRA_STREAM,uri);
        sIntent.putExtra(Intent.EXTRA_TEXT,shareBody);
        sIntent.putExtra(Intent.EXTRA_SUBJECT,"Subject Here");
        sIntent.setType("image/png");
        context.startActivity(Intent.createChooser(sIntent,"Share Via"));
    }

    private Uri saveImageToShare(Bitmap bitmap) {
        File imageFolder=new File(context.getCacheDir(),"images");
        Uri uri=null;

        try{
            imageFolder.mkdir();
            File file=new File(imageFolder,"shared_image.png");

            FileOutputStream stream=new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG,90,stream);
            stream.flush();
            stream.close();
            uri= FileProvider.getUriForFile(context,"com.harish.chat.fileprovider",file);
        }
        catch(Exception e){
            Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return uri;
    }

    private void setLikes(final ViewHolder holder, final String postKey) {
        likeRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(postKey).hasChild(myuid)){

                    holder.likeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_tlike,0,0,0);
                    holder.likeBtn.setText("Liked");

                }else{

                    holder.likeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_like,0,0,0);
                    holder.likeBtn.setText("Like");

                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void showMoreOptions(ImageButton moreBtn, String uid, String myuid, final String pId, final String pImage) {
        PopupMenu popupMenu= null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            popupMenu = new PopupMenu(context,moreBtn, Gravity.END);
        }

        if (uid.equals(myuid)){
            popupMenu.getMenu().add(Menu.NONE,0,0,"Delete");
        }
        popupMenu.getMenu().add(Menu.NONE,1,0,"View Details");
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id=item.getItemId();
               if (id==0){
                   beginDelete(pId,pImage);
               }else if (id==1){
                   Intent intent=new Intent(context, PostDetailsActivity.class);
                   intent.putExtra("pId",pId);
                   context.startActivity(intent);
               }
                return false;
            }
        });

        popupMenu.show();
    }

    private void beginDelete(String pId, String pImage) {
        if (pImage.equals("noImage")){
            deleteWithoutimage(pId);
        }else{
            deleteWithimage(pId,pImage);
        }
    }

    private void deleteWithimage(final String pId, String pImage) {
        final ProgressDialog pd=new ProgressDialog(context);
        pd.setMessage("Deleting...");

        StorageReference picRef= FirebaseStorage.getInstance().getReferenceFromUrl(pImage);

        picRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Query fquery= FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(pId);
                fquery.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds:  dataSnapshot.getChildren()){
                            ds.getRef().removeValue();
                        }
                        Toast.makeText(context, "Deleted Successfully", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
                Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteWithoutimage(String pId) {
        final ProgressDialog pd=new ProgressDialog(context);
        pd.setMessage("Deleting...");

        Query fquery= FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(pId);
        fquery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds:  dataSnapshot.getChildren()){
                    ds.getRef().removeValue();
                }

                Toast.makeText(context, "Deleted Successfully", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView pImageTv;
       ImageView uPictureTv;
        TextView uNameTv,pTimeTv,pTitleTv,pDescriptionTv,pLikesTv,pCommentsTv;
        ImageButton moreBtn;
        Button likeBtn,commentBtn,shareBtn;
        LinearLayout profileLayout;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            uPictureTv=itemView.findViewById(R.id.uPictureTv);
            pImageTv=itemView.findViewById(R.id.imageO);
            uNameTv=itemView.findViewById(R.id.uNameTv);
            pTimeTv=itemView.findViewById(R.id.pTimeTv);
            pTitleTv=itemView.findViewById(R.id.pTitleTv);
            pDescriptionTv=itemView.findViewById(R.id.pDescriptionTv);
            pLikesTv=itemView.findViewById(R.id.pLikes);
            moreBtn=itemView.findViewById(R.id.moreBtn);
            likeBtn=itemView.findViewById(R.id.likeBtn);
            commentBtn=itemView.findViewById(R.id.commentBtn);
            shareBtn=itemView.findViewById(R.id.shareBtn);
            profileLayout= itemView.findViewById(R.id.profileLayout);
            pCommentsTv= itemView.findViewById(R.id.pCommentsTv);
        }
    }
}
