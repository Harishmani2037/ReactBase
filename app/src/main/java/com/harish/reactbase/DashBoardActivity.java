package com.harish.reactbase;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.harish.reactbase.fragments.ChatFragment;
import com.harish.reactbase.fragments.GroupFragment;
import com.harish.reactbase.fragments.HomeFragment;
import com.harish.reactbase.fragments.NotificationFragment;
import com.harish.reactbase.fragments.ProfileFragment;
import com.harish.reactbase.fragments.UsersFragment;
import com.squareup.picasso.Picasso;

public class DashBoardActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener  {
    int counter=0;
   Toolbar toolbar;
    BottomNavigationView bottomNavigationView;
    ActionBar actionBar;
    String mUID;
    TextView nameTv;
    ImageView profileTv;
    View hisView;
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    FirebaseAuth firebaseAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseUser user= FirebaseAuth.getInstance().getCurrentUser();
        mUID=user.getUid();
        setContentView(R.layout.activity_dash_board);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        firebaseAuth=FirebaseAuth.getInstance();
        drawerLayout=findViewById(R.id.drawerlayout);
        navigationView=findViewById(R.id.navigationview);
        actionBar = getSupportActionBar();
        bottomNavigationView = findViewById(R.id.bottom_nav);
        navigationView.bringToFront();
        ActionBarDrawerToggle toggle=new ActionBarDrawerToggle(this,drawerLayout,toolbar,R.string.open,R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        bottomNavigationView.setOnNavigationItemSelectedListener(navigationItemSelector);
        actionBar.setTitle("Home");
        getSupportFragmentManager().beginTransaction().replace(R.id.container,new HomeFragment()).commit();
        hisView=navigationView.getHeaderView(0);

        nameTv=hisView.findViewById(R.id.name2);
        profileTv=hisView.findViewById(R.id.profile2);

        updateNavigation();

        navigationView.setNavigationItemSelectedListener(this);


        checkUserStatus();

    }

    private void updateNavigation() {
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Users");
        Query query = ref.orderByChild("email").equalTo(firebaseAuth.getCurrentUser().getEmail());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    String name = "" + ds.child("name").getValue();
                    String image = "" + ds.child("image").getValue();
                    nameTv.setText(name);
                    try {
                        if (image == "") {
                            Picasso.get().load(R.drawable.ic_face_black_24dp).into(profileTv);
                        } else {
                            Picasso.get().load(image).into(profileTv);
                        }
                    } catch (Exception e) {
                        Picasso.get().load(R.drawable.ic_face_black_24dp).into(profileTv);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    @Override
    protected void onResume() {
        checkUserStatus();
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        if(drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        else {
            counter++;
            Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT).show();
            if (counter==2){
                super.onBackPressed();
            }
        }
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navigationItemSelector = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Fragment selectedFragments=null;
            switch (item.getItemId()) {
                case R.id.nav_home:
                    actionBar.setTitle("Home");
                   selectedFragments = new HomeFragment();
                    break;
                case R.id.nav_profile:
                    actionBar.setTitle("Profile");
                    selectedFragments = new ProfileFragment();
                    break;
                case R.id.nav_users:
                    actionBar.setTitle("Users");
                    selectedFragments = new UsersFragment();
                    break;
                case R.id.nav_chat:
                    actionBar.setTitle("Chat");

                    selectedFragments = new ChatFragment();
                    break;
                case R.id.nav_more:
                    showMoreOptions();
                    return true;
            }
            getSupportFragmentManager().beginTransaction().replace(R.id.container,selectedFragments).commit();

            return true;
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void showMoreOptions() {
        PopupMenu popupMenu=new PopupMenu(this,bottomNavigationView, Gravity.END);

        popupMenu.getMenu().add(Menu.NONE,0,0,"Notifications");
        popupMenu.getMenu().add(Menu.NONE,1,0,"Group");

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id=item.getItemId();

                if (id==0){
                    actionBar.setTitle("Notifications");
                    NotificationFragment notificationFragment=new NotificationFragment();
                    FragmentTransaction ft5=getSupportFragmentManager().beginTransaction();
                    ft5.replace(R.id.container,notificationFragment,"");
                    ft5.commit();
                }else if(id==1){
                    actionBar.setTitle("Group Chat");
                    GroupFragment groupFragment=new GroupFragment();
                    FragmentTransaction ft6=getSupportFragmentManager().beginTransaction();
                    ft6.replace(R.id.container,groupFragment,"");
                    ft6.commit();
                }
                return false;
            }
        });
        popupMenu.show();
    }

    private void checkUserStatus(){
        FirebaseUser user= FirebaseAuth.getInstance().getCurrentUser();
        if (user!=null){

        }else{
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.about:
               startActivity(new Intent(this,AboutActivity.class));
                break;
            case R.id.feedback:
            startActivity(new Intent(this,feedPack.class));
                break;
            case R.id.logout:
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
                break;
        }
        return false;
    }
}

