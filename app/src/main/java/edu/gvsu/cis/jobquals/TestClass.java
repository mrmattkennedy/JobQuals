package edu.gvsu.cis.jobquals;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.net.ssl.HttpsURLConnection;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class TestClass extends AppCompatActivity {

//    private Button getBtn;
//    private TextView result;
    private WebView webView;
    private Boolean readSuccess = null;
    private List<String> nextPages;
    private List<String> jobs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
//        result = findViewById(R.id.result);
//        result.setMovementMethod(new ScrollingMovementMethod());
//        getBtn = findViewById(R.id.getBtn);
        webView = findViewById(R.id.webView1);
        String url = "https://www.indeed.com/q-computer-science-l-Grand-Rapids,-MI-jobs.html";
        new Async().execute(url);
        while (readSuccess == null) {
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
//        result.setText(Html.fromHtml(data));
        webView.loadUrl(jobs.get(0));
    }

    public class Async extends AsyncTask<String, Integer, Object> {

        @Override
        protected String doInBackground(String... params) {

            try {
                readUrl(params[0]);
                return null;
//                return data;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        private boolean readUrl(String urlString) throws Exception {
            try {
                Document doc = Jsoup.connect("https://www.indeed.com/jobs?q=computer+science&l=Grand+Rapids%2C+MI").get();
                Elements elements = doc.select("a[href]");
                nextPages = new ArrayList<String>();
                jobs = new ArrayList<String>();

                for (Element link : elements)
                    if (link.attr("href").contains("start="))
                        nextPages.add(link.attr("abs:href").toString());

                for (String nextPage : nextPages) {
                    for (Element link : elements) {
                        if (link.attr("href").contains("clk?")) {
                            jobs.add(link.attr("abs:href"));
                        }
                    }
                    doc = Jsoup.connect(nextPage).get();
                    elements = doc.select("a[href]");
                }
                Set<String> hs = new HashSet<>();
                hs.addAll(nextPages);
                nextPages.clear();
                nextPages.addAll(hs);


                return true;
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return false;
            }
        }

    }

}

