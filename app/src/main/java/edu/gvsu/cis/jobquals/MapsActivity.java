package edu.gvsu.cis.jobquals;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, SearchRecyclerAdapter.ItemClickListener {

    private GoogleMap mMap;
    SearchRecyclerAdapter adapter;
    private ArrayList<String> jobList;
    private ArrayList<String> displayData;
    private Button doneBtn;
    private String[] tags = null;
    private boolean requiredBool;
    String data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        doneBtn = findViewById(R.id.doneBtn);
        doneBtn.setOnClickListener(v -> finish());
        mapFragment.getMapAsync(this);
        jobList = new ArrayList<>();
        displayData = new ArrayList<>();
        setUpRecyclerView();

        Intent intent = getIntent();
        String url = intent.getStringExtra("URL");
        requiredBool = intent.getBooleanExtra("Required", false);
        Bundle b = intent.getExtras();
        for (String key : b.keySet())
            if (key.equals("Tags"))
                tags = b.getStringArray("Tags");
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
                for (int i = 0; i < json.length(); i++) {
                    boolean addVar = true;
                    String tempStr = "";
                    JSONObject e = json.getJSONObject(i);
                    //tempStr += "id: " + e.getString("id") + "<br>";
                    tempStr += "Type: " + e.getString("type") + "<br><br>";
                    tempStr += "URL: " + e.getString("url") + "<br><br>";
                    tempStr += "Created At: " + e.getString("created_at") + "<br><br>";
                    tempStr += "Company: " + e.getString("company") + "<br><br>";
                    tempStr += "Company URL: " + e.getString("company_url") + "<br><br>";
                    tempStr += "Title: " + e.getString("title") + "<br><br>";
                    tempStr += "Description: " + e.getString("description") + "<br><br>";
                    tempStr += "How to apply: " + e.getString("how_to_apply") + "<br><br>";
                   // tempStr += "company_logo: " + e.getString("company_logo") + "<br>";
                    if (tags != null)
                        for (String tag : tags)
                            if (e.getString("description").toLowerCase().contains(tag.toLowerCase()) && !requiredBool) {
                                addVar = false;
                            } else if (!e.getString("description").toLowerCase().contains(tag.toLowerCase()) && requiredBool)
                                addVar = false;

                    if (addVar) {
                        jobList.add(tempStr);
                        displayData.add(e.getString("title"));
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
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
    }

    private void setUpRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new SearchRecyclerAdapter(this, displayData);
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                layoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);
    }

    @Override
    public void onItemClick(View view, int position) {
        Intent i = new Intent(MapsActivity.this, SingleJobActivity.class);
        i.putExtra("Data", jobList.get(position));
        startActivity(i);
    }

    public class Async extends AsyncTask<String, Integer, Object> {

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
}
