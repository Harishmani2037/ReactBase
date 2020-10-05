package com.harish.reactbase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.harish.reactbase.model.ModelRate;
import com.hsalf.smilerating.SmileRating;
import com.hsalf.smileyrating.SmileyRating;

import java.util.HashMap;

public class feedPack extends AppCompatActivity {
    SmileRating smileRating;
    FirebaseAuth firebaseAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed_pack);

        smileRating=findViewById(R.id.smile_rating);
        firebaseAuth=FirebaseAuth.getInstance();
        smileRating.setOnSmileySelectionListener(new SmileRating.OnSmileySelectionListener() {
            @Override
            public void onSmileySelected(int smiley, boolean reselected) {
                switch (smiley){
                    case SmileRating.BAD:
                        Toast.makeText(feedPack.this, "Bad", Toast.LENGTH_SHORT).show();
                        break;
                    case SmileRating.GOOD:
                        Toast.makeText(feedPack.this, "GOOD", Toast.LENGTH_SHORT).show();

                        break;
                    case SmileRating.GREAT:
                        Toast.makeText(feedPack.this, "GREAT", Toast.LENGTH_SHORT).show();

                        break;
                    case  SmileRating.OKAY:
                        Toast.makeText(feedPack.this, "OKAY", Toast.LENGTH_SHORT).show();

                        break;
                    case  SmileRating.TERRIBLE:
                        Toast.makeText(feedPack.this, "TERRIBLE", Toast.LENGTH_SHORT).show();

                        break;
                }
            }
        });
        smileRating.setOnRatingSelectedListener(new SmileRating.OnRatingSelectedListener() {
            @Override
            public void onRatingSelected(int level, boolean reselected) {
                FirebaseUser user=firebaseAuth.getCurrentUser();

             rateIt(level);
            }
        });
}

    private void rateIt(int rate) {
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference();
        String uid=firebaseAuth.getCurrentUser().getUid();
        String email=firebaseAuth.getCurrentUser().getEmail();
        HashMap<String,Object>hashMap=new HashMap<>();
        hashMap.put("uid",uid);
        hashMap.put("email",email);
        hashMap.put("rate",rate);
        ref.child("Rating").child(uid).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(feedPack.this, "Rate it SuccessFully", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(feedPack.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}
