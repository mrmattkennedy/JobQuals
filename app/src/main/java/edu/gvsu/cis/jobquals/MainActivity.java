package edu.gvsu.cis.jobquals;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;
public class MainActivity extends AppCompatActivity {

    private String userId;
    private DatabaseReference mDatabase;
    private EditText jobInput;
    private EditText locationInput;
    private Button searchBtn;
    private String data;
    private Button qualsBtn;
    private Button clearBtn;
    private Switch colorSwitch;
    private TextView leftLabel;
    private TextView rightLabel;

    private String requiredTags;
    private String avoidTags;
    private String salary = "";
    private int addressCount;
    private String jobType = "";
    private boolean bodyCheckReq = false;
    private boolean titleCheckReq = false;
    private boolean bodyCheckIll = false;
    private boolean titleCheckIll = false;
    private final int SETTINGS_REQUEST_CODE = 2;
    private final int SEARCHES_REQUEST_CODE = 3;
    private int primaryColor;
    private int secondaryColor;
    private int backgroundColor;
    private int editTextColor;
    private ColorStateList oldColors;
    private Tracker mTracker;

    private SharedPreferences prefs;

    @Override
    //only called once
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mDatabase = FirebaseDatabase.getInstance().getReference("users");

        jobInput = findViewById(R.id.jobInput);
        locationInput = findViewById(R.id.jobInput2);
        searchBtn = findViewById(R.id.searchbtn);
        qualsBtn = findViewById(R.id.qualsBtn);
        clearBtn = findViewById(R.id.clearBtn);
        colorSwitch = findViewById(R.id.colorSwitch);
        leftLabel = findViewById(R.id.leftLabel);
        rightLabel = findViewById(R.id.rightLabel);

        searchBtn.setOnClickListener(v -> jobSearch());
        qualsBtn.setOnClickListener(v -> displayQualifications());
        clearBtn.setOnClickListener(v -> clearFields());
        colorSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> changeColor());

        primaryColor = getResources().getColor(R.color.minimal_dusty);
        secondaryColor = getResources().getColor(R.color.minimal_lavender);
        backgroundColor = getResources().getColor(R.color.minimal_overcast);
        editTextColor = getResources().getColor(R.color.minimal_paper);

        ConstraintLayout layout = findViewById(R.id.layout);
        layout.setBackgroundColor(backgroundColor);

        jobInput.setBackgroundColor(editTextColor);
        jobInput.setTextColor(primaryColor);
        jobInput.setHintTextColor(primaryColor);

        locationInput.setBackgroundColor(editTextColor);
        locationInput.setTextColor(primaryColor);
        locationInput.setHintTextColor(primaryColor);

        oldColors = rightLabel.getTextColors();

        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(secondaryColor));

        // Obtain the shared Tracker instance.
        getTracker().setScreenName("JobQuals");
        getTracker().send(new HitBuilders.ScreenViewBuilder().build()); // send screen name

        prefs = getSharedPreferences("edu.gvsu.cis.jobquals", Context.MODE_PRIVATE);
        String temp = prefs.getString("job", "");
        if (!temp.equals(""))
            jobInput.setText(temp);
        temp = prefs.getString("loc", "");
        if (!temp.equals(""))
            locationInput.setText(temp);

    }

    @Override
    public void onStart() { super.onStart(); }

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
            Intent i = new Intent(this, SettingsActivity.class);
            i.putExtra("Dark", colorSwitch.isChecked());
            startActivityForResult(i, SETTINGS_REQUEST_CODE);
        } else if (item.getItemId() == R.id.recentSearches) {
            Intent i = new Intent(this, SearchRecyclerActivity.class);
            i.putExtra("Dark", colorSwitch.isChecked());
            startActivityForResult(i, SEARCHES_REQUEST_CODE);
        }
        return true;
    }

    public Tracker getTracker() {
        return GoogleAnalytics.getInstance(this).newTracker(R.xml.global_tracker);
    }

    private void clearFields() {
        jobInput.setText("");
        locationInput.setText("");
        prefs.edit().remove("job").commit();
        prefs.edit().remove("loc").commit();
    }

    private void jobSearch() {
        String lastSearch = jobInput.getText().toString();
        String lastLocation = locationInput.getText().toString().replaceAll(",", "%2C");

        if (!lastSearch.equals("") && !lastLocation .equals("")) {
            getTracker().send(new HitBuilders.EventBuilder()
                    .setCategory("Action")
                    .setAction("Job Search")
                    .build());

            mDatabase.push().setValue(lastSearch + " - " + locationInput.getText().toString());
            prefs.edit().putString("job", lastSearch).commit();
            prefs.edit().putString("loc", locationInput.getText().toString()).commit();

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

            Bundle b = new Bundle();

            if (requiredTags != null && !requiredTags.isEmpty()) {
                String[] tagsSplit = requiredTags.split(",");
                for (int j = 0; j < tagsSplit.length; j++)
                    tagsSplit[j] = tagsSplit[j].trim();
                b.putStringArray("RequiredTags", tagsSplit);
            }

            if (avoidTags != null && !avoidTags.isEmpty()) {
                String[] tagsSplit = avoidTags.split(",");
                for (int j = 0; j < tagsSplit.length; j++)
                    tagsSplit[j] = tagsSplit[j].trim();
                b.putStringArray("AvoidTags", tagsSplit);
            }

            b.putString("Salary", salary);
            b.putInt("AddressCount", addressCount);
            b.putString("JobType", jobType);

            b.putBoolean("CheckBodyReq", bodyCheckReq);
            b.putBoolean("CheckTitleReq", titleCheckReq);
            b.putBoolean("CheckBodyIll", bodyCheckIll);
            b.putBoolean("CheckTitleIll", titleCheckIll);

            Intent i = new Intent(this, MapsActivity.class);
            String url = "https://www.indeed.com/jobs?q=" + urlToAdd + "+" + salary +"&l=" + locationToAdd + jobType;
            i.putExtra("URL", url);
            i.putExtra("Location", locationToAdd);
            i.putExtras(b);
            startActivity(i);
        }
    }

    private void changeColor() {
        if (colorSwitch.isChecked()) {
            primaryColor = getResources().getColor(R.color.dark_text);
            secondaryColor = getResources().getColor(R.color.dark_actionbar);
            backgroundColor = getResources().getColor(R.color.dark_background);
            editTextColor = getResources().getColor(R.color.dark_text_border);

            leftLabel.setTextColor(primaryColor);
            rightLabel.setTextColor(primaryColor);
        } else {
            primaryColor = getResources().getColor(R.color.minimal_dusty);
            secondaryColor = getResources().getColor(R.color.minimal_lavender);
            backgroundColor = getResources().getColor(R.color.minimal_overcast);
            editTextColor = getResources().getColor(R.color.minimal_paper);

            leftLabel.setTextColor(oldColors);
            rightLabel.setTextColor(oldColors);
        }

        ConstraintLayout layout = findViewById(R.id.layout);
        layout.setBackgroundColor(backgroundColor);

        jobInput.setBackgroundColor(editTextColor);
        jobInput.setTextColor(primaryColor);
        jobInput.setHintTextColor(primaryColor);

        locationInput.setBackgroundColor(editTextColor);
        locationInput.setTextColor(primaryColor);
        locationInput.setHintTextColor(primaryColor);

        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(secondaryColor));
    }

    private void displayQualifications() {
        String lastSearch = jobInput.getText().toString();
        String lastLocation = locationInput.getText().toString().replaceAll(",", "%2C");

        if (!lastSearch.equals("") && !lastLocation .equals("")) {
            getTracker().send(new HitBuilders.EventBuilder()
                    .setCategory("Action")
                    .setAction("Qualifications Search")
                    .build());

            mDatabase.push().setValue(lastSearch + " - " + locationInput.getText().toString());
            String[] searchSplit = lastSearch.split("\\s+");
            String[] locationSplit = lastLocation.split("\\s+");

            String urlToAdd = "";
            String locationToAdd = "";

            for (int i = 0; i < searchSplit.length; i++)
                urlToAdd += searchSplit[i] + "+";
            urlToAdd = urlToAdd.substring(0, urlToAdd.length() - 1);
            for (int i = 0; i < locationSplit.length; i++)
                locationToAdd += locationSplit[i] + "+";
            locationToAdd = locationToAdd.substring(0, locationToAdd.length() - 1);
            Bundle b = new Bundle();

            if (requiredTags != null && !requiredTags.isEmpty()) {
                String[] tagsSplit = requiredTags.split(",");
                for (int j = 0; j < tagsSplit.length; j++)
                    tagsSplit[j] = tagsSplit[j].trim();
                b.putStringArray("RequiredTags", tagsSplit);
            }

            if (avoidTags != null && !avoidTags.isEmpty()) {
                String[] tagsSplit = avoidTags.split(",");
                for (int j = 0; j < tagsSplit.length; j++)
                    tagsSplit[j] = tagsSplit[j].trim();
                b.putStringArray("AvoidTags", tagsSplit);
            }

            b.putBoolean("CheckBodyReq", bodyCheckReq);
            b.putBoolean("CheckTitleReq", titleCheckReq);
            b.putBoolean("CheckBodyIll", bodyCheckIll);
            b.putBoolean("CheckTitleIll", titleCheckIll);

            Intent i = new Intent(this, QualificationsActivity.class);
            String url = "https://www.indeed.com/jobs?q=" + urlToAdd + "+" + salary +"&l=" + locationToAdd + jobType;
            i.putExtra("Dark", colorSwitch.isChecked());
            i.putExtra("URL", url);
            i.putExtras(b);

            startActivity(i);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SEARCHES_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                String[] temp = data.getStringExtra("Selected").split("-");
                jobInput.setText(temp[0].trim());
                locationInput.setText(temp[1].trim());
            }
        } else if (requestCode == SETTINGS_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                requiredTags = data.getStringExtra("RequiredTags");
                avoidTags = data.getStringExtra("AvoidTags");
                salary = data.getStringExtra("Salary");
                addressCount = Integer.parseInt(data.getStringExtra("AddressCount"));
                jobType = data.getStringExtra("JobType");
                bodyCheckReq = data.getBooleanExtra("CheckBodyReq", false);
                titleCheckReq = data.getBooleanExtra("CheckTitleReq", false);
                bodyCheckIll = data.getBooleanExtra("CheckBodyIll", false);
                titleCheckIll = data.getBooleanExtra("CheckTitleIll", false);

                if (!salary.isEmpty())
                    salary = "$" + salary;

                if (!jobType.equals("All"))
                    jobType = "&jt=" + jobType.replaceAll("\\s+", "");
                else
                    jobType = "";
            }
        }
    }
}
