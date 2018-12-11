package edu.gvsu.cis.jobquals;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/*
 * Activity to display qualifications. Uses Async activity to get data.
 */
public class QualificationsActivity extends AppCompatActivity {

    private Button doneBtn;
    private TextView qualificationsDisp;
    private String data;

    private String[] requiredTags;
    private String[] avoidTags;
    private Boolean bodyCheckReq = null;
    private Boolean titleCheckReq = null;
    private Boolean bodyCheckIll = null;
    private Boolean titleCheckIll = null;

    private int primaryColor;
    private int secondaryColor;
    private int backgroundColor;
    private int editTextColor;

    @Override
    //only called once
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qualifications);

        doneBtn = findViewById(R.id.doneBtn);
        qualificationsDisp = findViewById(R.id.qualificationsDisp);

        doneBtn.setOnClickListener(v -> finish());

        Intent intent = getIntent();
        Bundle b = intent.getExtras();
        boolean dark = intent.getBooleanExtra("Dark", false);
        for (String key : b.keySet()) {
            if (key.equals("RequiredTags")) {
                requiredTags = b.getStringArray("RequiredTags");
            } else if (key.equals("AvoidTags")) {
                avoidTags = b.getStringArray("AvoidTags");
            } else if (key.equals("CheckBodyReq")) {
                bodyCheckReq = b.getBoolean("CheckBodyReq");
            } else if (key.equals("CheckTitleReq")) {
                titleCheckReq = b.getBoolean("CheckTitleReq");
            } else if (key.equals("CheckBodyIll")) {
                bodyCheckIll = b.getBoolean("CheckBodyIll");
            } else if (key.equals("CheckTitleIll")) {
                titleCheckIll = b.getBoolean("CheckTitleIll");
            }
        }


        String url = getIntent().getStringExtra("URL");
        new Async().execute(url);
        while (data == null) {
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (dark == true) {
            primaryColor = getResources().getColor(R.color.dark_text);
            secondaryColor = getResources().getColor(R.color.dark_actionbar);
            backgroundColor = getResources().getColor(R.color.dark_background);
            editTextColor = getResources().getColor(R.color.dark_text_border);
        } else {
            primaryColor = getResources().getColor(R.color.minimal_dusty);
            secondaryColor = getResources().getColor(R.color.minimal_lavender);
            backgroundColor = getResources().getColor(R.color.minimal_overcast);
            editTextColor = getResources().getColor(R.color.minimal_paper);
        }

        qualificationsDisp.setMovementMethod(new ScrollingMovementMethod());
        qualificationsDisp.setText(Html.fromHtml(data));
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(secondaryColor));
        ConstraintLayout layout = findViewById(R.id.layout);
        layout.setBackgroundColor(backgroundColor);
        qualificationsDisp.setBackgroundColor(editTextColor);
        qualificationsDisp.setTextColor(primaryColor);
        qualificationsDisp.setHintTextColor(primaryColor);
    }

    public class Async extends AsyncTask<String, Integer, Object> {

        @Override
        protected String doInBackground(String... params) {
            data = getQualifications(params[0]);
            return null;
        }

        private String getQualifications(String urlString) {
            Document document;
            List<String> linkBuilder = new ArrayList<String>();
            try {
                //Get Document object after parsing the html from given url.
                document = Jsoup.connect(urlString).get();
                Elements elements = document.select("a[href]");

                for (Element link : elements) {
                    if (link.attr("href").contains("clk?")) {
                        linkBuilder.add(link.attr("abs:href"));
                    }
                }

                //StringBuilder to display data in activity.
                StringBuilder qualifications = new StringBuilder();
                for (int i = linkBuilder.size() - 1; i >= 0; i--) {
                    //Filter data if there are tags applied.
                    if ((requiredTags != null && requiredTags.length != 0) || (avoidTags != null && avoidTags.length != 0)) {
                        document = Jsoup.connect(linkBuilder.get(i)).get();
                        String body = document.body().text();
                        String title = document.title();
                        boolean addJob = true;

                        if (requiredTags != null)
                            for (String requiredTag : requiredTags) {
                                if (!body.toLowerCase().contains(requiredTag.toLowerCase()) && bodyCheckReq)
                                    addJob = false;
                                if (!title.toLowerCase().contains(requiredTag.toLowerCase()) && titleCheckReq)
                                    addJob = false;
                            }


                        if (avoidTags != null)
                            for (String avoidTag : avoidTags) {
                                if (body.toLowerCase().contains(avoidTag.toLowerCase()) && bodyCheckIll)
                                    addJob = false;
                                if (title.toLowerCase().contains(avoidTag.toLowerCase()) && titleCheckIll)
                                    addJob = false;
                            }

                        if (!addJob)
                            continue;
                    }

                    //Connect to a link, get the requirements / qualifications is posted, then continue.
                    try {
                        document = Jsoup.connect(linkBuilder.get(i)).get();
                        String req = document.select("P:contains(require)").next().toString();
                        String quals = document.select("P:contains(qualification)").next().toString();

                        if (!req.isEmpty() && req.contains("<ul>"))
                            qualifications.append("<b>Requirements from " + document.title() + ":</b> " + req + "<br>");

                        if (!quals.isEmpty() && quals.contains("<ul>"))
                            qualifications.append("<b>Qualifications from " + document.title() + ":</b> " + quals + "<br>");

                    } catch (Exception e1) { continue; }
                }
                return qualifications.toString();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
    }

}
