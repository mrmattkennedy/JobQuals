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
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class TestClass extends AppCompatActivity {

    private Button getBtn;
    private TextView result;
    private String data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        result = findViewById(R.id.result);
        result.setMovementMethod(new ScrollingMovementMethod());
        getBtn = findViewById(R.id.getBtn);
        String url = "https://jobs.github.com/positions.json?description=abc123&location=new+york";
        new Async().execute(url);
        while (data == null) {
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (!data.equals("fail")) {

            try {
                JSONArray json = new JSONArray(data);
                List<String> temp = new ArrayList<String>();
                String tempStr = "";
                for (int i = 0; i < json.length(); i++) {
                    JSONObject e = json.getJSONObject(i);
                    tempStr += "id: " + e.getString("id") + "<br>";
                    tempStr += "type: " + e.getString("type") + "<br>";
                    tempStr += "url: " + e.getString("url") + "<br>";
                    tempStr += "created_at: " + e.getString("created_at") + "<br>";
                    tempStr += "company: " + e.getString("company") + "<br>";
                    tempStr += "company_url: " + e.getString("company_url") + "<br>";
                    tempStr += "title: " + e.getString("title") + "<br>";
                    tempStr += "description: " + e.getString("description") + "<br>";
                    tempStr += "how_to_apply: " + e.getString("how_to_apply") + "<br>";
                    tempStr += "company_logo: " + e.getString("company_logo") + "<br>";
                    tempStr += "\n";
                }
                result.setText(Html.fromHtml(tempStr));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public class Async extends AsyncTask<String, Integer, Object>{

        @Override
        protected String doInBackground(String... params) {

            try {
                data = readUrl(params[0]);
                return null;
//                return data;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        private String readUrl(String urlString) throws Exception {
            BufferedReader reader = null;
            try {
                URL url = new URL(urlString);
                reader = new BufferedReader(new InputStreamReader(url.openStream()));
                StringBuffer buffer = new StringBuffer();
                int read;
                char[] chars = new char[1024];
                for (String line; (line = reader.readLine()) != null; ) {
                    buffer.append(line).append('\n');
                }
                return buffer.toString();
            } catch (Exception e) {
                data = "fail";
            } finally {
                if (reader != null)
                    reader.close();
            }
            return null;
        }

    }

   public class JobData {
       @SerializedName("id")
       private String id;

       @SerializedName("type")
       private String type;

       @SerializedName("url")
       private String url;

       @SerializedName("created_at")
       private String created_at;

       @SerializedName("company")
       private String company;

       @SerializedName("company_url")
       private String company_url;

       @SerializedName("title")
       private String title;

       @SerializedName("description")
       private String description;

       @SerializedName("how_to_apply")
       private String how_to_apply;

       @SerializedName("company_logo")
       private String company_logo;

       public String getId() {
           return id;
       }

       public String getType() {
           return type;
       }

       public String getUrl() {
           return url;
       }

       public String getCreated_at() {
           return created_at;
       }

       public String getCompany() {
           return company;
       }

       public String getCompany_url() {
           return company_url;
       }

       public String getTitle() {
           return title;
       }

       public String getDescription() {
           return description;
       }

       public String getHow_to_apply() {
           return how_to_apply;
       }

       public String getCompany_logo() {
           return company_logo;
       }

       public void setId(String id) {
           this.id = id;
       }

       public void setType(String type) {
           this.type = type;
       }

       public void setUrl(String url) {
           this.url = url;
       }

       public void setCreated_at(String created_at) {
           this.created_at = created_at;
       }

       public void setCompany(String company) {
           this.company = company;
       }

       public void setCompany_url(String company_url) {
           this.company_url = company_url;
       }

       public void setTitle(String title) {
           this.title = title;
       }

       public void setDescription(String description) {
           this.description = description;
       }

       public void setHow_to_apply(String how_to_apply) {
           this.how_to_apply = how_to_apply;
       }

       public void setCompany_logo(String company_logo) {
           this.company_logo = company_logo;
       }
   }
}

