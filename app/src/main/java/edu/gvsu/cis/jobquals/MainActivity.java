package edu.gvsu.cis.jobquals;

import android.app.Activity;
import android.content.Intent;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;
public class MainActivity extends AppCompatActivity {

//    private FirebaseAuth mAuth;
    private String userId;
    private DatabaseReference mDatabase;
    private EditText jobInput;
    private EditText locationInput;
    private EditText avoidInput;
    private Button searchBtn;
//    private Button signOutBtn;
    private RadioButton requiredRadio;
    private RadioButton avoidRadio;
//    private ImageButton userBtn;
//    private FirebaseUser currentUser;
    private String data;
//    private final int FIREBASE_REQUEST_CODE = 2;
    private final int SEARCHES_REQUEST_CODE = 3;

    @Override
    //only called once
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference("users");
//        currentUser = mAuth.getCurrentUser();

        jobInput = findViewById(R.id.jobInput);
        locationInput = findViewById(R.id.jobInput2);
        avoidInput = findViewById(R.id.jobInput3);
        searchBtn = findViewById(R.id.searchbtn);
//        userBtn = findViewById(R.id.userBtn);
//        signOutBtn = findViewById(R.id.signOutBtn);
        requiredRadio = findViewById(R.id.requiredRadio);
        avoidRadio = findViewById(R.id.avoidRadio);

        searchBtn.setOnClickListener(v -> jobSearch());
//        signOutBtn.setOnClickListener(v -> signOut());
//        userBtn.setOnClickListener(v -> changeUser());

//        if (currentUser == null)
//            changeUser();
//        else
//            updateUserInfo();
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
            Intent i = new Intent(this, SearchRecyclerActivity.class);
            startActivityForResult(i, SEARCHES_REQUEST_CODE);
        }
        return true;
    }


    private void jobSearch() {
        String lastSearch = jobInput.getText().toString();
        String lastLocation = locationInput.getText().toString().replaceAll(",", "%2C");
        String lastTags = avoidInput.getText().toString();

        if (!lastSearch.equals("") && !lastLocation .equals("") &&
                (avoidRadio.isChecked() || requiredRadio.isChecked())) {

            mDatabase.push().setValue(lastSearch);

            String[] searchSplit = lastSearch.split("\\s+");
            String[] locationSplit = lastLocation.split("\\s+");

            String urlToAdd = "";
            String locationToAdd = "";

            for (int i = 0; i < searchSplit.length; i++)
                urlToAdd += searchSplit[i] + "-";
            urlToAdd = urlToAdd.substring(0, urlToAdd.length() - 1);
            for (int i = 0; i < locationSplit.length; i++)
                locationToAdd += locationSplit[i] + "+";
            locationToAdd = locationToAdd.substring(0, locationToAdd.length() - 1);

            Intent i = new Intent(this, MapsActivity.class);
            String url = "https://www.indeed.com/jobs?q=" + urlToAdd + "&l=" + locationToAdd;
//            avoidInput.setText(url);
            boolean required = requiredRadio.isChecked();
            i.putExtra("URL", url);
            i.putExtra("Required", required);
            if (!lastTags.equals("")) {
                String[] tagsSplit = lastTags.split(",");
                for (int j = 0; j < tagsSplit.length; j++)
                    tagsSplit[j] = tagsSplit[j].trim();
                Bundle b = new Bundle();
                b.putStringArray("Tags", tagsSplit);
                i.putExtras(b);
            }
            startActivity(i);
        }
    }

//    private void changeUser() {
//        Intent i = new Intent(this, FirebaseUIActivity.class);
//        startActivityForResult(i, FIREBASE_REQUEST_CODE);
//    }

//    private void updateUserInfo() {
//        if (currentUser != null) {
//            ConstraintLayout layout = findViewById(R.id.layout);
//            Snackbar.make(layout, "Logged in as " + currentUser.getEmail(), Snackbar.LENGTH_SHORT).show();
//            userId = currentUser.getUid();
//            Picasso.get().load(currentUser.getPhotoUrl()).into(userBtn);
//        } else {
//            ConstraintLayout layout = findViewById(R.id.layout);
//            Snackbar.make(layout, "Not signed in.", Snackbar.LENGTH_SHORT).show();
//            userBtn.setImageDrawable(getResources().getDrawable(R.drawable.common_google_signin_btn_icon_dark));
//            userId = null;
//        }
//    }
//
//    public void signOut() {
//        mAuth.signOut();
//        updateUserInfo();
//    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if (requestCode == FIREBASE_REQUEST_CODE) {
//            if (resultCode == Activity.RESULT_OK) {
//                currentUser = mAuth.getCurrentUser();
//            }
//            updateUserInfo();
//        } else
            if (requestCode == SEARCHES_REQUEST_CODE) {
                if (resultCode == Activity.RESULT_OK) {
                    jobInput.setText(data.getStringExtra("Selected"));
            }
        }
    }
}
