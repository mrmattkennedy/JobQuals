package edu.gvsu.cis.jobquals;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, SearchRecyclerAdapter.ItemClickListener {

    private GoogleMap mMap;
    SearchRecyclerAdapter adapter;
    private Button doneBtn;
    private boolean requiredBool;
    private Boolean readSuccess = null;
    private List<String> nextPages;
    private List<String> jobs;
    private List<String> titles;
    private String data;
    private Map<String, String> states = new HashMap<String, String>();
    ArrayList<String> companies = new ArrayList<String>();
    ArrayList<String> addresses = new ArrayList<String>();

    private String[] requiredTags;
    private String[] avoidTags;
    private String salary;
    private int addressCount;
    private String jobType;
    private boolean bodyCheckReq;
    private boolean titleCheckReq;
    private boolean bodyCheckIll;
    private boolean titleCheckIll;

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
        nextPages = new ArrayList<>();
        jobs = new ArrayList<>();
        titles = new ArrayList<>();

        Intent intent = getIntent();
        String url = intent.getStringExtra("URL");
        String loc = intent.getStringExtra("Location");
        //"https://nominatim.openstreetmap.org/search.php?q=Grand+Rapids+michigan&polygon_geojson=1&viewbox="
        Iterator it = states.entrySet().iterator();

        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            String val = pair.getValue().toString();
            String key = pair.getKey().toString();

            if (loc.contains(val)) {
                loc = loc.replace(val, key);
                break;
            }
            it.remove(); // avoids a ConcurrentModificationException
        }
        String locUrl = "https://nominatim.openstreetmap.org/search.php?q=" + loc + "&polygon_geojson=1&viewbox=";
        requiredBool = intent.getBooleanExtra("Required", false);
        Bundle b = intent.getExtras();
        for (String key : b.keySet()) {
            if (key.equals("RequiredTags")) {
                requiredTags = b.getStringArray("RequiredTags");
            } else if (key.equals("AvoidTags")) {
                avoidTags = b.getStringArray("AvoidTags");
            } else if (key.equals("Salary")) {
                salary = b.getString("Salary");
            } else if (key.equals("JobType")) {
                jobType = b.getString("JobType");
            } else if (key.equals("AddressCount")) {
                addressCount = b.getInt("AddressCount");
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

        new Async().execute(url, locUrl);
        while (readSuccess == null || data == null) {
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (readSuccess)
            setUpRecyclerView();

//        int secondaryColor = getResources().getColor(R.color.minimal_lavender);
//        getActionBar().setBackgroundDrawable(new ColorDrawable(secondaryColor));
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
        while (readSuccess == null || data == null) {
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (!data.equals("fail")) {
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

                LatLng centerLatLng = null;
                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                List<LatLng> tempList = polyopts.getPoints();
                for (int i = 0; i < tempList.size(); i++) {
                    builder.include(tempList.get(i));
                }
                LatLngBounds bounds = builder.build();
                centerLatLng = bounds.getCenter();

                LatLng tempLoc = null;
                for (int i = 0; i < addresses.size(); i++)
                    try {
                        tempLoc = getLocationFromAddress(this, addresses.get(i));
                        mMap.addMarker(new MarkerOptions().position(tempLoc).title(companies.get(i)));
                    } catch (Exception e1) {
                        ;
                    }

                mMap.moveCamera(CameraUpdateFactory.newLatLng(centerLatLng));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(10.0f));

                googleMap.addPolygon(polyopts);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public LatLng getLocationFromAddress(Context context, String strAddress) throws Exception
    {
        Geocoder coder= new Geocoder(context);
        List<Address> address;
        LatLng p1 = null;

        address = coder.getFromLocationName(strAddress, 5);
        if(address==null)
            return null;

        Address location = address.get(0);
        p1 = new LatLng(location.getLatitude(), location.getLongitude());

        return p1;
    }

    private void setUpRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new SearchRecyclerAdapter(this, titles);
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                layoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);
    }

    @Override
    public void onItemClick(View view, int position) {
        Intent i = new Intent(MapsActivity.this, SingleJobActivity.class);
        i.putExtra("URL", jobs.get(position));
        startActivity(i);
    }

    public class Async extends AsyncTask<String, Integer, Object> {

        @Override
        protected String doInBackground(String... params) {

            try {
                readSuccess = readUrl(params[0]);
                if (readSuccess == false)
                    data = "fail";
                getCoords(params[1]);

                return null;
//                return data;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        private boolean readUrl(String urlString) {
            try {
                //Read the initial first page of jobs.
                Document doc = Jsoup.connect(urlString).get();
                Elements elements = doc.select("a[href]");
                nextPages = new ArrayList<String>();
                jobs = new ArrayList<String>();

                //Get next 5 pages.
                for (Element link : elements)
                    if (link.attr("href").contains("start="))
                        nextPages.add(link.attr("abs:href").toString());

                //Remove duplicates for nextPages
                Set<String> hs = new HashSet<>();
                hs.addAll(nextPages);
                nextPages.clear();
                nextPages.addAll(hs);

                //Loop through each page, get job url
                nextPages.add(urlString);
                for (String nextPage : nextPages) {
                    doc = Jsoup.connect(nextPage).get();
                    elements = doc.select("a[href]");
                    for (Element link : elements) {
                        if (link.attr("href").contains("clk?")) {
                            jobs.add(link.attr("abs:href"));
                            titles.add(link.attr("title"));
                        }
                    }
                }

                int count = 0;
                for (int i = 0; i < jobs.size(); i++) {
                    if ((requiredTags != null && requiredTags.length != 0) || (avoidTags != null && avoidTags.length != 0)) {
                        doc = Jsoup.connect(jobs.get(i)).get();
                        String body = doc.body().text();

                        if (requiredTags != null)
                            for (String requiredTag : requiredTags) {
                                if (!body.toLowerCase().contains(requiredTag.toLowerCase()) && bodyCheckReq)
                                    continue;
                                if (!titles.get(i).toLowerCase().contains(requiredTag.toLowerCase()) && titleCheckReq)
                                    continue;
                            }


                        if (avoidTags != null)
                            for (String avoidTag : avoidTags) {
                                if (!body.toLowerCase().contains(avoidTag.toLowerCase()) && bodyCheckIll)
                                    continue;
                                if (!titles.get(i).toLowerCase().contains(avoidTag.toLowerCase()) && titleCheckIll)
                                    continue;
                            }
                    }
                    if (count++ >= addressCount)
                        continue;

                    elements = doc.getElementsByClass("icl-u-lg-mr--sm icl-u-xs-mr--xs");
                    String company = elements.text().substring(0, elements.text().length() - 1).trim();
                    companies.add(company);

                    try {
                        String companyQuery = company.replaceAll("\\s", "+");
                        companyQuery = companyQuery.replaceAll(",", "%2C").substring(0, companyQuery.length() - 1);
                        doc = Jsoup.connect("https://www.google.com/search?q=" + companyQuery + "+address").get();

                        elements = doc.getElementsByClass("Z0LcW");
                        String address = elements.text();
                        if (address.isEmpty()) {
                            companies.remove(companies.size() - 1);
                            count--;
                            continue;
                        }
                        addresses.add(address);

                    } catch (Exception e1) {
                        companies.remove(companies.size() - 1);
                        count--;
                    }

			    }

			    for (int i = 0; i < titles.size(); i++) {
                    if (avoidTags != null) {
                        for (String avoidTag : avoidTags) {
                            if (titles.get(i).toLowerCase().contains(avoidTag.toLowerCase()) && titleCheckIll) {
                                jobs.remove(i);
                                titles.remove(i);
                            }
                        }
                    }
                }
			    for (int i = 0; i < addresses.size(); i++)
				    Log.d("SYSTEMTAG", companies.get(i) + " : " + addresses.get(i));
                return true;
            } catch (Exception e) {
                return false;
            }
        }

        private void getCoords(String url) {
            Document doc;
            try {
                doc = Jsoup.connect(url).get();
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

                doc = Jsoup.connect(links.get(0)).get();
                data = doc.body().text();
            } catch (IOException e) {
                e.printStackTrace();
                data = "fail";
            }
        }
    }

    {
        states.put("Alabama","AL");
        states.put("Alaska","AK");
        states.put("Alberta","AB");
        states.put("American Samoa","AS");
        states.put("Arizona","AZ");
        states.put("Arkansas","AR");
        states.put("Armed Forces (AE)","AE");
        states.put("Armed Forces Americas","AA");
        states.put("Armed Forces Pacific","AP");
        states.put("British Columbia","BC");
        states.put("California","CA");
        states.put("Colorado","CO");
        states.put("Connecticut","CT");
        states.put("Delaware","DE");
        states.put("District Of Columbia","DC");
        states.put("Florida","FL");
        states.put("Georgia","GA");
        states.put("Guam","GU");
        states.put("Hawaii","HI");
        states.put("Idaho","ID");
        states.put("Illinois","IL");
        states.put("Indiana","IN");
        states.put("Iowa","IA");
        states.put("Kansas","KS");
        states.put("Kentucky","KY");
        states.put("Louisiana","LA");
        states.put("Maine","ME");
        states.put("Manitoba","MB");
        states.put("Maryland","MD");
        states.put("Massachusetts","MA");
        states.put("Michigan","MI");
        states.put("Minnesota","MN");
        states.put("Mississippi","MS");
        states.put("Missouri","MO");
        states.put("Montana","MT");
        states.put("Nebraska","NE");
        states.put("Nevada","NV");
        states.put("New Brunswick","NB");
        states.put("New Hampshire","NH");
        states.put("New Jersey","NJ");
        states.put("New Mexico","NM");
        states.put("New York","NY");
        states.put("Newfoundland","NF");
        states.put("North Carolina","NC");
        states.put("North Dakota","ND");
        states.put("Northwest Territories","NT");
        states.put("Nova Scotia","NS");
        states.put("Nunavut","NU");
        states.put("Ohio","OH");
        states.put("Oklahoma","OK");
        states.put("Ontario","ON");
        states.put("Oregon","OR");
        states.put("Pennsylvania","PA");
        states.put("Prince Edward Island","PE");
        states.put("Puerto Rico","PR");
        states.put("Quebec","QC");
        states.put("Rhode Island","RI");
        states.put("Saskatchewan","SK");
        states.put("South Carolina","SC");
        states.put("South Dakota","SD");
        states.put("Tennessee","TN");
        states.put("Texas","TX");
        states.put("Utah","UT");
        states.put("Vermont","VT");
        states.put("Virgin Islands","VI");
        states.put("Virginia","VA");
        states.put("Washington","WA");
        states.put("West Virginia","WV");
        states.put("Wisconsin","WI");
        states.put("Wyoming","WY");
        states.put("Yukon Territory","YT");
    };
}
