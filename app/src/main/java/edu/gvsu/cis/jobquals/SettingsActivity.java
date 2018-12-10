package edu.gvsu.cis.jobquals;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.Tracker;

import java.util.ArrayList;
import java.util.List;

public class SettingsActivity extends AppCompatActivity  {

    private EditText requiredTags;
    private TextView requiredLabel;
    private CheckBox bodyChkReq;
    private CheckBox titleChkReq;
    private EditText avoidTags;
    private TextView avoidLabel;
    private CheckBox bodyChkIll;
    private CheckBox titleChkIll;
    private TextView salaryLabel;
    private SeekBar salarySeek;
    private EditText salaryText;
    private SeekBar addressSeek;
    private TextView addressesText;
    private Spinner jobTypeSpinner;
    private Button doneBtn;
    private static final int MAX_SALARY = 150000;
    private int primaryColor;
    private int secondaryColor;
    private int backgroundColor;
    private int editTextColor;
    private boolean dark;
    private final String JOB_LABEL = "Job type: ";

    private ColorStateList oldColors;

    @Override
    //only called once
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        requiredTags = findViewById(R.id.requiredTags);
        requiredLabel = findViewById(R.id.requiredLabel);
        bodyChkReq = findViewById(R.id.bodyChkReq);
        titleChkReq = findViewById(R.id.titleChkReq);
        avoidTags = findViewById(R.id.avoidTags);
        avoidLabel = findViewById(R.id.avoidLabel);
        bodyChkIll = findViewById(R.id.bodyChkIll);
        titleChkIll = findViewById(R.id.titleChkIll);
        salaryLabel = findViewById(R.id.salaryLabel);
        salarySeek = findViewById(R.id.salarySeek);
        salaryText = findViewById(R.id.salaryText);
        addressSeek = findViewById(R.id.addressSeek);
        addressesText = findViewById(R.id.addressesText);
        jobTypeSpinner = findViewById(R.id.jobTypeSpinner);
        doneBtn = findViewById(R.id.doneBtn);

        Intent intent = getIntent();
        dark = intent.getBooleanExtra("Dark", false);

        if (dark == true) {
            primaryColor = getResources().getColor(R.color.dark_text);
            secondaryColor = getResources().getColor(R.color.dark_actionbar);
            backgroundColor = getResources().getColor(R.color.dark_background);
            editTextColor = getResources().getColor(R.color.dark_text_border);

            requiredTags.setTextColor(primaryColor);
            requiredTags.setHintTextColor(primaryColor);

            requiredLabel.setTextColor(primaryColor);

            bodyChkReq.setTextColor(primaryColor);

            titleChkReq.setTextColor(primaryColor);

            avoidTags.setTextColor(primaryColor);
            avoidTags.setHintTextColor(primaryColor);

            avoidLabel.setTextColor(primaryColor);

            bodyChkIll.setTextColor(primaryColor);

            titleChkIll.setTextColor(primaryColor);

            salaryLabel.setTextColor(primaryColor);

            salaryText.setTextColor(primaryColor);
            salaryText.setHintTextColor(primaryColor);

            addressesText.setTextColor(primaryColor);
        } else {
            primaryColor = getResources().getColor(R.color.minimal_dusty);
            secondaryColor = getResources().getColor(R.color.minimal_lavender);
            backgroundColor = getResources().getColor(R.color.minimal_overcast);
            editTextColor = getResources().getColor(R.color.minimal_paper);

            requiredTags.setBackgroundColor(editTextColor);
            requiredTags.setTextColor(primaryColor);
            requiredTags.setHintTextColor(primaryColor);

            avoidTags.setBackgroundColor(editTextColor);
            avoidTags.setTextColor(primaryColor);
            avoidTags.setHintTextColor(primaryColor);
        }

        ConstraintLayout layout = findViewById(R.id.layout);
        layout.setBackgroundColor(backgroundColor);

        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(secondaryColor));

        oldColors = bodyChkIll.getTextColors();

        addressSeek.setOnSeekBarChangeListener(new addressesListener());
        salarySeek.setOnSeekBarChangeListener(new salaryListener());
        salaryText.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
                if (salaryText.getText().toString().isEmpty())
                    return;
                int progress = Integer.parseInt(salaryText.getText().toString());
                if (progress > MAX_SALARY ) {
                    salarySeek.setProgress(MAX_SALARY);
                    salaryText.setSelection(salaryText.getText().length());
                    return;
                } else if (progress < 0) {
                    salarySeek.setProgress(0);
                    salaryText.setSelection(salaryText.getText().length());
                    return;
                }

                salarySeek.setProgress(progress);
                salaryText.setSelection(salaryText.getText().length());
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { ; }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { ; }
        });
        List jobTypes = new ArrayList<String>();
        jobTypes.add(JOB_LABEL + "All");
        jobTypes.add(JOB_LABEL + "Full time");
        jobTypes.add(JOB_LABEL + "Contract");
        jobTypes.add(JOB_LABEL + "Part time");
        jobTypes.add(JOB_LABEL + "Internship");
        jobTypes.add(JOB_LABEL + "Temporary");
        jobTypes.add(JOB_LABEL + "Commission");
        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, jobTypes);

        jobTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (dark)
                    ((TextView) parent.getChildAt(0)).setTextColor(primaryColor); /* if you want your item to be white */
                else
                    ((TextView) parent.getChildAt(0)).setTextColor(oldColors);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        jobTypeSpinner.setAdapter(dataAdapter);
        doneBtn.setOnClickListener(v -> finishSettings());
    }

    private void finishSettings() {
        String required = requiredTags.getText().toString();
        String avoid = avoidTags.getText().toString();
        String salary = salaryText.getText().toString();
        String addresses = addressesText.getText().toString();
        addresses = addresses.substring("Number of address to display:".length() + 1);
        String jobType = jobTypeSpinner.getSelectedItem().toString();
        jobType = jobType.substring(jobType.indexOf(JOB_LABEL) + JOB_LABEL.length()).trim();

        if (!required.isEmpty() && (!bodyChkReq.isChecked() && !titleChkReq.isChecked())) {
            Toast.makeText(SettingsActivity.this, "Please selected where to check for required tags.", Toast.LENGTH_LONG).show();
            return;
        } else if (!avoid.isEmpty() && (!bodyChkIll.isChecked() && !titleChkIll.isChecked())) {
            Toast.makeText(SettingsActivity.this, "Please selected where to check for avoid tags.", Toast.LENGTH_LONG).show();
            return;
        }

        Intent resultIntent = new Intent();
        resultIntent.putExtra("RequiredTags", required);
        resultIntent.putExtra("AvoidTags", avoid);
        resultIntent.putExtra("Salary", salary);
        resultIntent.putExtra("AddressCount", addresses);
        resultIntent.putExtra("JobType", jobType);
        resultIntent.putExtra("CheckBodyReq", bodyChkReq.isChecked());
        resultIntent.putExtra("CheckTitleReq", titleChkReq.isChecked());
        resultIntent.putExtra("CheckBodyIll", bodyChkIll.isChecked());
        resultIntent.putExtra("CheckTitleIll", titleChkIll.isChecked());

        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }

    private class salaryListener implements SeekBar.OnSeekBarChangeListener {

        public void onProgressChanged(SeekBar seekBar, int progress,
                                      boolean fromUser) {
            salaryText.setText(String.valueOf(progress));
        }

        public void onStartTrackingTouch(SeekBar seekBar) {}

        public void onStopTrackingTouch(SeekBar seekBar) {}

    }

    private class addressesListener implements SeekBar.OnSeekBarChangeListener {

        public void onProgressChanged(SeekBar seekBar, int progress,
                                      boolean fromUser) {
            addressesText.setText("Number of address to display: " + String.valueOf(progress));
        }

        public void onStartTrackingTouch(SeekBar seekBar) {}

        public void onStopTrackingTouch(SeekBar seekBar) {}

    }
}
