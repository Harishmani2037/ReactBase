package com.harish.reactbase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.harish.reactbase.adapters.AdapterComments;
import com.harish.reactbase.model.modelComment;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class PostDetailsActivity extends AppCompatActivity {

    String myUid,myEmail,myName, hisUid ,myDp
            ,postId,pLikes,hisDp,pImage,hisName;
    boolean mProcessComment=false;
    boolean mProcessLikes=false;


    ProgressDialog pd;

    ImageView uPictureTv,pImageTv;
    TextView uNameTv,pTimeTv,pTitleTv,pDescriptioTv,pLikesTv,pCommentsTv;
    ImageButton moreBtn;
    Button likeBtn,shareBtn;
    LinearLayout profileLayout;
    RecyclerView recyclerView;

    List<modelComment> commentList;
    AdapterComments adapterComments;

    EditText commentEt;
    ImageButton sendBtn;
    ImageView cAvatarTv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_details);
        ActionBar actionBar=getSupportActionBar();
        actionBar.setTitle("Post Details");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        Intent intent=getIntent();
        postId=intent.getStringExtra("pId").toString();

        recyclerView=findViewById(R.id.recyclerView);
        uPictureTv=findViewById(R.id.uPictureTv);
        commentEt=findViewById(R.id.commentEt);
        pImageTv=findViewById(R.id.imageO);
        uNameTv=findViewById(R.id.uNameTv);
        pTimeTv=findViewById(R.id.pTimeTv);
        pTitleTv=findViewById(R.id.pTitleTv);
        pDescriptioTv=findViewById(R.id.pDescriptionTv);
        pLikesTv=findViewById(R.id.pLikes);
        moreBtn=findViewById(R.id.moreBtn);
        likeBtn=findViewById(R.id.likeBtn);
        shareBtn=findViewById(R.id.shareBtn);
        pCommentsTv=findViewById(R.id.pCommentsTv);
        profileLayout=findViewById(R.id.profileLayout);
        cAvatarTv=findViewById(R.id.cAvatarTv);
        sendBtn=findViewById(R.id.sendBtn);

        loadPostInfo();    //success

        checkUserStatus(); //sucess

        loadUserInfo();   //success

        setLikes();

        loadComments();

        actionBar.setSubtitle("SignedIn as :"+myEmail);

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postComment();
            }
        });

        likeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                likepost();
            }
        });

        moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMoreOptions();
            }
        });

        shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pTitle=pTitleTv.getText().toString().trim();
                String pDescription=pDescriptioTv.getText().toString().trim();
                BitmapDrawable bitmapDrawable=(BitmapDrawable)pImageTv.getDrawable();
                if (bitmapDrawable==null){
                    shareTextOnly(pTitle,pDescription);
                }else{

                    Bitmap bitmap=bitmapDrawable.getBitmap();
                    shareImageAndText(bitmap,pTitle,pDescription);
                }
            }
        });

        pLikesTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), LikePostActivity.class);
                intent.putExtra("pId", postId);
                startActivity(intent);
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
        startActivity(Intent.createChooser(sIntent,"Shar Via"));
    }

    private void shareImageAndText(Bitmap bitmap, String pTitle, String pDescription) {
        String shareBody=pTitle+"\n"+pDescription;

        Uri uri=saveImageToShare(bitmap);

        Intent sIntent=new Intent(Intent.ACTION_SEND);
        sIntent.putExtra(Intent.EXTRA_STREAM,uri);
        sIntent.putExtra(Intent.EXTRA_TEXT,shareBody);
        sIntent.putExtra(Intent.EXTRA_SUBJECT,"Subject Here");
        sIntent.setType("image/png");
        startActivity(Intent.createChooser(sIntent,"Share Via"));
    }

    private Uri saveImageToShare(Bitmap bitmap) {
        File imageFolder=new File(getApplicationContext().getCacheDir(),"images");
        Uri uri=null;

        try{
            imageFolder.mkdir();
            File file=new File(imageFolder,"shared_image.png");

            FileOutputStream stream=new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG,90,stream);
            stream.flush();
            stream.close();
            uri= FileProvider.getUriForFile(getApplicationContext(),"com.harish.chat.fileprovider",file);
        }
        catch(Exception e){
            Toast.makeText(getApplicationContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return uri;
    }

    private void loadComments() {
        LinearLayoutManager layoutManager=new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        commentList=new ArrayList<>();

        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Posts").child(postId).child("Comments");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                commentList.clear();
                for (DataSnapshot ds:dataSnapshot.getChildren()){
                    modelComment modelComment=ds.getValue(com.harish.reactbase.model.modelComment.class);

                    commentList.add(modelComment);

                    adapterComments =new AdapterComments(getApplicationContext(),commentList,myUid,postId);
                    recyclerView.setAdapter(adapterComments);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void addTextNotification(String hisUid,String pId,String notification){
        String timeStamp = ""+System.currentTimeMillis();

        HashMap<String ,Object> hashmap=new HashMap<>();
        //pId,timesStamp,pUid,notifications,sUid,sName,sEmail,sImage;
        hashmap.put("pId",pId);
        hashmap.put("timesStamp",timeStamp);
        hashmap.put("pUid",hisUid);
        hashmap.put("notifications",notification);
        hashmap.put("sUid",myUid);

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

    private void showMoreOptions() {
        PopupMenu popupMenu= null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            popupMenu = new PopupMenu(this,moreBtn, Gravity.END);
        }

        if (myUid.equals(hisUid)){
            popupMenu.getMenu().add(Menu.NONE,0,0,"Delete");
        }
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if (id == 0) {
                    beginDelete();
                    Toast.makeText(PostDetailsActivity.this, ""+pImage, Toast.LENGTH_SHORT).show();
                }
                return false;
            }
        });

        popupMenu.show();

    }

    private void beginDelete() {
        if (pImage.equals("noImage")){
            deleteWithoutimage();
        }else{
            deleteWithimage();
        }
    }

    private void deleteWithimage() {

        final ProgressDialog pd=new ProgressDialog(this);
        pd.setMessage("Deleting...");

        StorageReference picRef= FirebaseStorage.getInstance().getReferenceFromUrl(pImage);

        picRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Query fquery= FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(postId);
                fquery.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds:  dataSnapshot.getChildren()){
                            ds.getRef().removeValue();
                        }
                        Toast.makeText(PostDetailsActivity.this, "Deleted Successfully", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(PostDetailsActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void deleteWithoutimage() {

        final ProgressDialog pd=new ProgressDialog(this);
        pd.setMessage("Deleting...");

        Query fquery= FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(postId);
        fquery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds:  dataSnapshot.getChildren()){
                    ds.getRef().removeValue();
                }

                Toast.makeText(PostDetailsActivity.this, "Deleted Successfully", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setLikes() {
        final DatabaseReference likeRef=FirebaseDatabase.getInstance().getReference().child("Likes");

        likeRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(postId).hasChild(myUid)){

                    likeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_tlike,0,0,0);
                    likeBtn.setText("Liked");

                }else{

                    likeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_like,0,0,0);
                    likeBtn.setText("Like");

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void likepost() {
        mProcessLikes=true;
        final DatabaseReference likeRef=FirebaseDatabase.getInstance().getReference().child("Likes");
        final DatabaseReference postsRef=FirebaseDatabase.getInstance().getReference().child("Posts");
        likeRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (mProcessLikes){
                    if (dataSnapshot.child(postId).hasChild(myUid)){
                        postsRef.child(postId).child("pLikes").setValue(""+(Integer.parseInt(pLikes)-1));
                        likeRef.child(postId).child(myUid).removeValue();
                        mProcessLikes=false;
                    }
                    else {
                        postsRef.child(postId).child("pLikes").setValue(""+(Integer.parseInt(pLikes)+1));
                        likeRef.child(postId).child(myUid).setValue("Liked");
                        mProcessLikes=false;

                        addTextNotification(""+hisUid,""+postId,"Liked your Post");
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void postComment() {
        pd=new ProgressDialog(this);
        pd.setMessage("Adding Comment...");

        String comment=commentEt.getText().toString().trim();

        if (TextUtils.isEmpty(comment)){
            Toast.makeText(this, "Comment not Empty", Toast.LENGTH_SHORT).show();
            return;
        }
        String timeStamp=String.valueOf(System.currentTimeMillis());

        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Posts").child(postId).child("Comments");

        HashMap hashMap=new HashMap();
        hashMap.put("cId",timeStamp);
        hashMap.put("comment",comment);
        hashMap.put("timestamp",timeStamp);
        hashMap.put("uid",myUid);
        hashMap.put("uEmail",myEmail);
        hashMap.put("uDp",myDp);
        hashMap.put("uName",myName);


        ref.child(timeStamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                pd.dismiss();
                Toast.makeText(PostDetailsActivity.this, "Comment Added", Toast.LENGTH_SHORT).show();
                commentEt.setText("");
                updateCommentCount();

                addTextNotification(""+hisUid,""+postId,"Commented your Post");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
                Toast.makeText(PostDetailsActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateCommentCount() {
        mProcessComment=true;

        final DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Posts").child(postId);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (mProcessComment){
                    String comments=""+dataSnapshot.child("pComments").getValue();
                    int newcommentVal=Integer.parseInt(comments)+1;
                    ref.child("pComments").setValue(""+newcommentVal);
                    mProcessComment=false;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void loadUserInfo() {

        Query myRef=FirebaseDatabase.getInstance().getReference("Users");
        myRef.orderByChild("uid").equalTo(myUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds:dataSnapshot.getChildren()){
                    myName=""+ds.child("name").getValue();
                    myDp=""+ds.child("image").getValue();

                    try {
                        Picasso.get().load(myDp).placeholder(R.drawable.ic_face_black_24dp).into(cAvatarTv);
                    }
                    catch(Exception e){
                        Picasso.get().load(R.drawable.ic_face_black_24dp).into(cAvatarTv);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void loadPostInfo() {

        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Posts");

        Query query=ref.orderByChild("pId").equalTo(postId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    String pTitle = "" + ds.child("pTitle").getValue();
                    String pDescr = "" + ds.child("pDescription").getValue();
                    pLikes = "" + ds.child("pLikes").getValue();
                    String pTimeStamp = "" + ds.child("pTime").getValue();
                    pImage = "" + ds.child("pImage").getValue();
                    hisDp = "" + ds.child("uDP").getValue();
                    hisUid = "" + ds.child("uid").getValue();
                    String uEmail = "" + ds.child("uEmail").getValue();
                    hisName = "" + ds.child("uName").getValue();
                    String commentcount="" + ds.child("pComments").getValue();

                    Calendar calendar = Calendar.getInstance(Locale.getDefault());
                    calendar.setTimeInMillis(Long.parseLong(pTimeStamp));
                    String pTime = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();

                    pTitleTv.setText(pTitle);
                    pDescriptioTv.setText(pDescr);
                    pLikesTv.setText(pLikes + " Likes");
                    pTimeTv.setText(pTime);
                    uNameTv.setText(hisName);
                    pCommentsTv.setText(commentcount+" comments");


                    if (pImage.equals("noImage")){
                        pImageTv.setVisibility(View.GONE);
                    }else{
                        //pImageTv.setVisibility(View.VISIBLE);
                        try{
                            Picasso.get().load(pImage).into(pImageTv);
                        }
                        catch(Exception e){
                            Toast.makeText(getApplicationContext(), "Image not found", Toast.LENGTH_SHORT).show();
                        }
                    }



                    try {
                        Picasso.get().load(hisDp).placeholder(R.drawable.ic_face_black_24dp).into(uPictureTv);
                    }
                    catch(Exception e){
                        Picasso.get().load(R.drawable.ic_face_black_24dp).into(uPictureTv);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void checkUserStatus(){
        FirebaseUser user= FirebaseAuth.getInstance().getCurrentUser();
        if (user!=null){
            myEmail=user.getEmail();
            myUid=user.getUid();

        }else{
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu,menu);
        menu.findItem(R.id.action_add_photo).setVisible(false);
        menu.findItem(R.id.info).setVisible(false);
        menu.findItem(R.id.createGroup).setVisible(false);
        menu.findItem(R.id.action_add_Participant_group).setVisible(false);
        menu.findItem(R.id.opt_search).setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.logout:
                FirebaseAuth.getInstance().signOut();
                checkUserStatus();
                break;

        }
        return super.onOptionsItemSelected(item);
    }
}
