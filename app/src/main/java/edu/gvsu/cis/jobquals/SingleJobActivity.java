package edu.gvsu.cis.jobquals;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.widget.Button;
import android.widget.TextView;

public class SingleJobActivity extends AppCompatActivity {

    private Button doneBtn;
    private TextView jobView;
    @Override
    //only called once
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_job);

        doneBtn = findViewById(R.id.doneBtn);
        jobView = findViewById(R.id.singleJobText);

        doneBtn.setOnClickListener(v -> finish());
        jobView.setMovementMethod(new ScrollingMovementMethod());

        String jobData = getIntent().getStringExtra("Data");
        jobView.setText(Html.fromHtml(jobData));
    }
}
