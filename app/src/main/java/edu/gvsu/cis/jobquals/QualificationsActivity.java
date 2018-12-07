package edu.gvsu.cis.jobquals;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
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

    @Override
    //only called once
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qualifications);

        doneBtn = findViewById(R.id.doneBtn);
        qualificationsDisp = findViewById(R.id.qualificationsDisp);

        doneBtn.setOnClickListener(v -> finish());

        Intent intent = getIntent();
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

        private String getQualifications(String urlString) throws Exception {
            Document document;
            StringBuilder builder = new StringBuilder();
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
                for (String job : linkBuilder) {
                    try {
                        document = Jsoup.connect(job).get();
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
