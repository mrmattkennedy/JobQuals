package edu.gvsu.cis.jobquals;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;

public class SingleJobActivity extends AppCompatActivity {

    private Button doneBtn;
    private WebView webView;
    @Override
    //only called once
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_job);

        doneBtn = findViewById(R.id.doneBtn);
        webView = findViewById(R.id.webView);

        doneBtn.setOnClickListener(v -> finish());

        String url = getIntent().getStringExtra("URL");
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return false;
            }
        });
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl(url);
    }
}
