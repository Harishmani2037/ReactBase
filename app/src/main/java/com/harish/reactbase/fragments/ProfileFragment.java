package com.harish.reactbase.fragments;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
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
import com.google.firebase.storage.UploadTask;
import com.harish.reactbase.AddPostActivity;
import com.harish.reactbase.LoginActivity;
import com.harish.reactbase.R;
import com.harish.reactbase.AboutActivity;
import com.harish.reactbase.adapters.AdapterPosts;
import com.harish.reactbase.feedPack;
import com.harish.reactbase.model.ModelPost;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.app.Activity.RESULT_OK;

public class ProfileFragment extends Fragment {
    FirebaseAuth auth;
    FirebaseUser user;
    FirebaseDatabase database;
    FirebaseStorage storage;
    int photoOrcover;  // 0- means profile,1-means cover
    DatabaseReference reference;
    ImageView profileImage, prof_coverPhoto;
    TextView prof_name, prof_email, prof_phone;
    FloatingActionButton prof_edit;
    ProgressDialog pd;
    RecyclerView postRecyclerView;

    List<ModelPost> postList;
    AdapterPosts adapterPosts;
    String uid;

    private StorageReference storageReference;
    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        reference = database.getReference("Users");

        reference.keepSynced(true);

        storage = FirebaseStorage.getInstance();
        storageReference=storage.getReference().child("images/");

        profileImage = view.findViewById(R.id.prof_img1);
        prof_name = view.findViewById(R.id.prof_name1);
        prof_email = view.findViewById(R.id.prof_email1);
        prof_phone = view.findViewById(R.id.prof_phone1);
        prof_coverPhoto = view.findViewById(R.id.prof_coverPhoto1);
        prof_edit = view.findViewById(R.id.prof_edit1);
        postRecyclerView=view.findViewById(R.id.recyclerviewPosts);

        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(getActivity());

        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);

        postRecyclerView.setLayoutManager(linearLayoutManager);

        Query query = reference.orderByChild("email").equalTo(user.getEmail());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    String name = "" + ds.child("name").getValue();
                    String email = "" + ds.child("email").getValue();
                    String phone = "" + ds.child("phoneNumber").getValue();
                    String image = "" + ds.child("image").getValue();
                    String cover = "" + ds.child("cover").getValue();
                    prof_name.setText(name);
                    prof_email.setText(email);
                    prof_phone.setText(phone);
                    try {
                        if (image == "") {
                            Picasso.get().load(R.drawable.ic_face_black_24dp).into(profileImage);
                        } else {
                            Picasso.get().load(image).into(profileImage);
                        }
                    } catch (Exception e) {
//                        Picasso.get().load(R.drawable.ic_face_black_24dp).into(profileImage);
                    }
                    try {
                        if (cover == "") {
                            Picasso.get().load(R.drawable.ic_photoimage).into(prof_coverPhoto);
                        } else {
                            Picasso.get().load(cover).into(prof_coverPhoto);
                        }
                    } catch (Exception e) {
                        Picasso.get().load(R.drawable.ic_photoimage).into(prof_coverPhoto);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
        prof_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditProfileDialog();
            }
        });

        postList=new ArrayList<>();

        logout();

        loadMyPosts();

        pd = new ProgressDialog(getActivity());
        return view;
    }

    private void loadMyPosts() {


        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Posts");

        Query query=ref.orderByChild("uid").equalTo(uid);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postList.clear();
                for (DataSnapshot ds:dataSnapshot.getChildren()){
                    ModelPost myposts=ds.getValue(ModelPost.class);

                    postList.add(myposts);

                    adapterPosts=new AdapterPosts(getActivity(),postList);

                    postRecyclerView.setAdapter(adapterPosts);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
//                Toast.makeText(getActivity(), ""+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void searchMyPosts(final String searchQuery) {
        LinearLayoutManager layoutManager=new LinearLayoutManager(getActivity());
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);

        postRecyclerView.setLayoutManager(layoutManager);

        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Posts");

        Query query=ref.orderByChild("uid").equalTo(uid);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postList.clear();
                for (DataSnapshot ds:dataSnapshot.getChildren()){
                    ModelPost myposts=ds.getValue(ModelPost.class);

                    if (myposts.getpTitle().toLowerCase().contains(searchQuery.toLowerCase())||myposts.getpDescription().toLowerCase()
                            .contains(searchQuery.toLowerCase())){

                        postList.add(myposts);

                    }


                    adapterPosts=new AdapterPosts(getActivity(),postList);

                    postRecyclerView.setAdapter(adapterPosts);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getActivity(), ""+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void showEditProfileDialog() {
        final ProgressDialog pd=new ProgressDialog(getActivity());
        pd.setTitle("Uploading...");
        String options[] = {"Edit profile pics", "Edit cover photo", "Edit Name", "Edit phone","Change Password"};
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Choose Actions");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    //propics
                    photoOrcover=0;
                    pd.setMessage("Updating profile picture...");
                    showImageDialog();
                } else if (which == 1) {
                    //coverPhoto
                    photoOrcover=1;

                    showImageDialog();
                } else if (which == 2) {
                    //Name
                    pd.setMessage("Updating your Name...");
                    showNamePhoneUpdateDialog("name");
                } else if (which == 3) {
                    //phone
                    pd.setMessage("Updating your Phone number...");
                    showNamePhoneUpdateDialog("phoneNumber");
                }
                else if (which==4){
                    pd.setMessage("Changing your Password...");
                    showChangePasswordDialog();
                }
            }
        });
        builder.create().show();
    }

    private void showChangePasswordDialog() {
        View view=LayoutInflater.from(getActivity()).inflate(R.layout.diaglog_update_password,null);

        AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
        builder.setView(view);
        final EditText passwordEd=view.findViewById(R.id.oldPassword);
        final EditText newpasswordEd=view.findViewById(R.id.newPassword);
        Button updatepasswordBtn=view.findViewById(R.id.changePassword);
        final AlertDialog dialog=builder.create();
        dialog.show();
        updatepasswordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String oldPassword=passwordEd.getText().toString();
                String NewPassword=newpasswordEd.getText().toString();
                if (TextUtils.isEmpty(oldPassword)){
                    Toast.makeText(getActivity(), "Enter the Current Password", Toast.LENGTH_SHORT).show();
                }
                else if (TextUtils.isEmpty(NewPassword)){
                    Toast.makeText(getActivity(), "Enter the New Password", Toast.LENGTH_SHORT).show();
                }else if (NewPassword.length()<6){
                    Toast.makeText(getActivity(), "Your New Password is Too short", Toast.LENGTH_SHORT).show();
                }else{
                    dialog.dismiss();
                    pd.setMessage("Changing your Password");
                    updatePassword(oldPassword,NewPassword);
                }
            }
        });
    }

    private void updatePassword(String oldPassword, final String newPassword) {
        pd.show();
        final FirebaseUser user=auth.getCurrentUser();

        AuthCredential authCredential= EmailAuthProvider.getCredential(user.getEmail(),oldPassword);
        user.reauthenticate(authCredential).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                user.updatePassword(newPassword).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        pd.dismiss();
                        Toast.makeText(getActivity(), "Password changed Successfully", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        pd.dismiss();
                        Toast.makeText(getActivity(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
                Toast.makeText(getActivity(), "Authentication failed\nyour current password was incorrect...", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showImageDialog() {
        Intent intent=new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,1);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode==1){
            if (resultCode==RESULT_OK){
                if (photoOrcover==0){
                    pd.setMessage("Uploading Profile Image");
                    pd.show();
                }else if (photoOrcover==1){
                    pd.setMessage("Uploading Cover Image");
                    pd.show();
                }
                Uri imageData=data.getData();
                Uri imageUri=data.getData();
                profileImage.setImageURI(imageUri);
                final StorageReference ImageName=storageReference.child("image"+imageData.getLastPathSegment());
                ImageName.putFile(imageData).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        ImageName.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                uploadImage(uri);
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getActivity(), "Failed", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }

    private void uploadImage(Uri uri) {
        if (photoOrcover==0) {
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("image", String.valueOf(uri));
            reference.child(user.getUid()).updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    pd.dismiss();
                    Toast.makeText(getActivity(), "Upload Successfully", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    pd.dismiss();
                    Toast.makeText(getActivity(), "Failed", Toast.LENGTH_SHORT).show();
                }
            });
        }else if (photoOrcover==1){
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("cover", String.valueOf(uri));
            reference.child(user.getUid()).updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void Void) {
                    pd.dismiss();
                    Toast.makeText(getActivity(), "Upload Successfully", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    pd.dismiss();
                    Toast.makeText(getActivity(), "Failed", Toast.LENGTH_SHORT).show();
                }
            });

        }
    }

    private void showNamePhoneUpdateDialog(final String key) {
        //custom dialog
        AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
        builder.setTitle("Update "+key);
        LinearLayout linearLayout=new LinearLayout(getActivity());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setPadding(10,10,10,10);
        final EditText editText=new EditText(getActivity());
        editText.setHint("Enter "+key);
        linearLayout.addView(editText);
        builder.setView(linearLayout);
        pd.setMessage("Uploading...");
        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final String value=editText.getText().toString().trim();
                if (!TextUtils.isEmpty(value)){
                    pd.show();
                    HashMap<String,Object>result=new HashMap<>();
                    result.put(key,value);
                    reference.child(user.getUid()).updateChildren(result).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            pd.dismiss();
                            Toast.makeText(getActivity(), "Updated...", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            pd.dismiss();
                            Toast.makeText(getActivity(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

                    if (key.equals("name")){
                        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Posts");

                        Query query=ref.orderByChild("uid").equalTo(uid);
                        query.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for (DataSnapshot ds:dataSnapshot.getChildren()){
                                    String child=ds.getKey();
                                    dataSnapshot.getRef().child(child).child("uName").setValue(value);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                        ref.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for (DataSnapshot ds:dataSnapshot.getChildren()){
                                    String child=ds.getKey();
                                    if (dataSnapshot.child(child).hasChild("Comments")){
                                        String child1=""+dataSnapshot.child(child).getKey();
                                        Query child2=FirebaseDatabase.getInstance().getReference("Posts").child(child1).child("Comments").orderByChild("uid").equalTo(uid);
                                        child2.addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                for (DataSnapshot ds:dataSnapshot.getChildren()){
                                                    String child=ds.getKey();
                                                    dataSnapshot.getRef().child(child).child("uName").setValue(value);
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

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

                }else{
                    editText.setError("Please Enter the "+key);
                    Toast.makeText(getActivity(), "Please Enter the "+key, Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        MenuInflater menuInflater = inflater;
        menuInflater.inflate(R.menu.main_menu, menu);
        MenuItem item=menu.findItem(R.id.opt_search);
        menu.findItem(R.id.info).setVisible(false);
        menu.findItem(R.id.createGroup).setVisible(false);
        menu.findItem(R.id.action_add_Participant_group).setVisible(false);
        androidx.appcompat.widget.SearchView searchView=(androidx.appcompat.widget.SearchView) MenuItemCompat.getActionView(item);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                if (!TextUtils.isEmpty(s)){
                    searchMyPosts(s);
                }else{
                    loadMyPosts();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {

                if (!TextUtils.isEmpty(s)){
                    searchMyPosts(s);
                }else{
                    loadMyPosts();
                }

                return false;
            }
        });
        super.onCreateOptionsMenu(menu,menuInflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout:
                auth.signOut();
                logout();
                break;
            case R.id.action_add_photo:
                startActivity(new Intent(getActivity(), AddPostActivity.class));
                break;
            case R.id.feedback:
                startActivity(new Intent(getActivity(), feedPack.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void logout() {
        FirebaseUser firebaseUser=auth.getCurrentUser();
        if (firebaseUser!=null){
            uid=firebaseUser.getUid();
        }else{
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            getActivity().finish();
        }
    }
}