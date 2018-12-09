package edu.gvsu.cis.jobquals;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
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
        qualificationsDisp.setMovementMethod(new ScrollingMovementMethod());
        qualificationsDisp.setText(Html.fromHtml(data));
        int secondaryColor = getResources().getColor(R.color.minimal_lavender);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(secondaryColor));
    }

    public class Async extends AsyncTask<String, Integer, Object> {

        @Override
        protected String doInBackground(String... params) {

            try {
                data = getQualifications(params[0]);

                return null;
//                return data;
            } catch (Exception e) {
                e.printStackTrace();
            }
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

                StringBuilder qualifications = new StringBuilder();
                for (int i = 0; i < linkBuilder.size(); i++) {
                    if ((requiredTags != null && requiredTags.length != 0) || (avoidTags != null && avoidTags.length != 0)) {
                        document = Jsoup.connect(linkBuilder.get(i)).get();
                        String body = document.body().text();
                        String title = document.title();

                        if (requiredTags != null)
                            for (String requiredTag : requiredTags) {
                                if (!body.toLowerCase().contains(requiredTag.toLowerCase()) && bodyCheckReq)
                                    continue;
                                if (!title.toLowerCase().contains(requiredTag.toLowerCase()) && titleCheckReq)
                                    continue;
                            }


                        if (avoidTags != null)
                            for (String avoidTag : avoidTags) {
                                if (!body.toLowerCase().contains(avoidTag.toLowerCase()) && bodyCheckIll)
                                    continue;
                                if (!title.toLowerCase().contains(avoidTag.toLowerCase()) && titleCheckIll)
                                    continue;
                            }
                    }
                    try {
                        document = Jsoup.connect(linkBuilder.get(i)).get();
                        String req = document.select("P:contains(require)").next().toString();
                        String quals = document.select("P:contains(qualification)").next().toString();

                        if (!req.isEmpty() && req.contains("<ul>"))
                            qualifications.append("<b>From " + document.title() + ":</b> " + req + "<br>");

                        if (!quals.isEmpty() && quals.contains("<ul>"))
                            qualifications.append("<b>From " + document.title() + ":</b> " + quals + "<br>");

                    } catch (Exception e1) { ; }
                }
                return qualifications.toString();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
    }

}
