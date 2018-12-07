package edu.gvsu.cis.jobquals;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
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

public class TestClass extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    SearchRecyclerAdapter adapter;
    private Boolean readSuccess = null;
    ArrayList<String> links = new ArrayList<String>();
    ArrayList<String> place_ids = new ArrayList<String>();
    private String data = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }
    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(43, -85.67);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        new Async().execute();
        while (data == null) {
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        try {
            JSONObject obj = new JSONObject(data);
            JSONArray arr = obj.getJSONArray("geometries").getJSONObject(0).getJSONArray("coordinates").getJSONArray(0).getJSONArray(0);
            PolygonOptions polyopts = new PolygonOptions();
            String temp = null;
            double lat, lng;
            for (int i = 0; i < arr.length(); i++) {
                temp = arr.getString(i);
                lng = Float.parseFloat(temp.substring(1, temp.indexOf(",")));
                lat = Float.parseFloat(temp.substring(temp.indexOf(",") + 1, temp.length() - 1));
                polyopts.add(new LatLng(lat, lng));
            }
//            polyopts.strokeColor(Color.RED)
//                    .fillColor(Color.BLUE);
            googleMap.addPolygon(polyopts);
        } catch (Exception e ) {
            e.printStackTrace();
        }
    }

    public class Async extends AsyncTask<String, Integer, Object> {

        @Override
        protected String doInBackground(String... params) {

            try {
                readSuccess = readUrl();
                return null;
//                return data;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        private boolean readUrl() throws Exception {
            Document doc;

            try {
                doc = Jsoup.connect("https://nominatim.openstreetmap.org/search.php?q=Grand+Rapids+michigan&polygon_geojson=1&viewbox=").get();
                Elements elements = doc.select("a[href]");
                ArrayList<String> links = new ArrayList<String>();
                ArrayList<String> place_ids = new ArrayList<String>();
                String detailsIdentifier = "details.php?place_id=";
                String osmIdentifier = "&osmid";

                for (Element link : elements)
                    if (link.attr("abs:href").contains(detailsIdentifier))
                        links.add(link.attr("abs:href"));

                for (String place : links) {
                    doc = Jsoup.connect(place).get();
                    elements = doc.select("a[href]");
                    for (Element link : elements)
                        if (link.attr("abs:href").contains(osmIdentifier))
                            place_ids.add(link.attr("abs:href"));
                }

                for (int i = 0; i < place_ids.size(); i++) {
                    String temp = place_ids.get(i);
                    temp = temp.substring(temp.indexOf(osmIdentifier) + osmIdentifier.length() + 1,
                            temp.indexOf("&", temp.indexOf(osmIdentifier) + osmIdentifier.length()));
                    place_ids.set(i, temp);
                }

                for (int i = 0; i < links.size(); i++) {
                    links.set(i, "http://polygons.openstreetmap.fr/get_geojson.py?id=" + place_ids.get(i) + "&params=0");
                }

//			for (String place : links)
//				place_ids.add(place.substring(place.indexOf(identifier) + identifier.length()));
                doc = Jsoup.connect(links.get(0)).get();
                data = doc.body().text();
                return true;
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return false;
            }
        }

    }

}

