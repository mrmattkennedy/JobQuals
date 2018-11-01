package edu.gvsu.cis.jobquals;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private String userId;
    private DatabaseReference  mDatabase;
    private EditText jobInput;
    private Button searchBtn;
    private Button signOutBtn;
    private ImageButton userBtn;
    private FirebaseUser currentUser;

    @Override
    //only called once
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference("users");
        currentUser = mAuth.getCurrentUser();
        jobInput = findViewById(R.id.jobInput);
        searchBtn = findViewById(R.id.searchbtn);
        userBtn = findViewById(R.id.userBtn);
        signOutBtn = findViewById(R.id.signOutBtn);

        searchBtn.setOnClickListener(v -> jobSearch());
        signOutBtn.setOnClickListener(v -> signOut());
        userBtn.setOnClickListener(v -> changeUser());

        if (currentUser == null)
            changeUser();
        else
            updateUserInfo();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    /*
     * Creates the main menu and puts the settings menu resource file in.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.settings, menu);
        return true;
    }
    /*
     * This will get called when an item is selected from the menu.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.settings) {
            //do stuff
        } else if (item.getItemId() == R.id.recentSearches) {
            Intent i = new Intent(this, RecyclerViewActivity.class);
            startActivityForResult(i, 2);
        }
        return true;
    }


    private void jobSearch() {
        String lastSearch = jobInput.getText().toString();
        if (lastSearch != "" && lastSearch != null)
            mDatabase.child(userId).push().setValue(lastSearch);
    }

    private void changeUser() {
        Intent i = new Intent(this, FirebaseUIActivity.class);
        startActivityForResult(i, 2);
    }

    private void updateUserInfo() {
        if (currentUser != null) {
            ConstraintLayout layout = findViewById(R.id.layout);
            Snackbar.make(layout, "Logged in as " + currentUser.getEmail(), Snackbar.LENGTH_SHORT).show();
            userId = currentUser.getUid();
            Picasso.get().load(currentUser.getPhotoUrl()).into(userBtn);
        } else {
            ConstraintLayout layout = findViewById(R.id.layout);
            Snackbar.make(layout, "Not signed in.", Snackbar.LENGTH_SHORT).show();
            userBtn.setImageDrawable(getResources().getDrawable(R.drawable.common_google_signin_btn_icon_dark));
            userId = null;
        }
    }

    public void signOut() {
        mAuth.signOut();
        updateUserInfo();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 2)
            if(resultCode == Activity.RESULT_OK) {
                currentUser = mAuth.getCurrentUser();
//                userId = getIntent().getStringExtra("UserId");
//                PicassogetIntent().getStringExtra("UserPic");
            }
        updateUserInfo();
    }
}
