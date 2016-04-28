package com.app.places.placesapp;

import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

/**
 * Credit to http://www.android4devs.com/2015/12/tab-layout-material-design-support.html for a great
 * tutorial that helped us get tabs to switch between fragments
 */
public class MainActivity extends AppCompatActivity
        implements OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks,
        com.google.android.gms.location.LocationListener {

    //Declaring All The Variables Needed

    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private ViewPagerAdapter viewPagerAdapter;

    private GoogleApiClient mGoogleApiClient; // Connects to Google's API
    private Location currentLocation; // Stores the most recent location data
    private String TAG = "MainActivity"; // Used to identify error messages

    // Lists of information about nearby places and the categories to request from Google Places
    private ArrayList<Place> food;
    private String foodType = "restaurant|bakery|bar|cafe|meal_delivery|meal_takeaway";
    private ArrayList<Place> fun;
    private String funType = "amusement_park|aquarium|art_gallery|beauty_salon|bowling_alley|" +
            "campground|casino|movie_rental|movie_theater|museum|night_club|park|spa|stadium|" +
            "zoo|gym";
    private ArrayList<Place> shopping;
    private String shopType = "bicycle_store|book_store|jewelry_store|liquor_store|pet_store|" +
            "shoe_store|shopping_mall|store|clothing_store|convenience_store|department_store|" +
            "electronics_store|florist|furniture_store|grocery_or_supermarket|hardware_store|" +
            "home_goods_store";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*
        Assigning view variables to their respective view in xml
        by findViewByID method
         */

        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        viewPager = (ViewPager) findViewById(R.id.viewpager);

        /*
        Creating Adapter and setting that adapter to the viewPager
        setSupportActionBar method takes the toolbar and sets it as
        the default action bar thus making the toolbar work like a normal
        action bar.
         */
        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(viewPagerAdapter);
        setSupportActionBar(toolbar);

        /*
        TabLayout.newTab() method creates a tab view, Now a Tab view is not the view
        which is below the tabs, its the tab itself.
         */

        final TabLayout.Tab food = tabLayout.newTab();
        final TabLayout.Tab fun = tabLayout.newTab();
        final TabLayout.Tab stores = tabLayout.newTab();

        /*
        Setting Title text for our tabs respectively
         */

        food.setText("Food");
        fun.setText("Fun");
        stores.setText("Stores");

        /*
        Adding the tab view to our tablayout at appropriate positions
        As I want home at first position I am passing home and 0 as argument to
        the tablayout and like wise for other tabs as well
         */
        tabLayout.addTab(food, 0);
        tabLayout.addTab(fun, 1);
        tabLayout.addTab(stores, 2);

        /*
        TabTextColor sets the color for the title of the tabs, passing a ColorStateList here makes
        tab change colors in different situations such as selected, active, inactive etc

        TabIndicatorColor sets the color for the indiactor below the tabs
         */

        tabLayout.setTabTextColors(ContextCompat.getColorStateList(this, R.color.tab_selector));
        tabLayout.setSelectedTabIndicatorColor(ContextCompat.getColor(this, R.color.indicator));

        /*
        Adding a onPageChangeListener to the viewPager
        1st we add the PageChangeListener and pass a TabLayoutPageChangeListener so that Tabs Selection
        changes when a viewpager page changes.
         */

        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        /*
        Changes the fragment based on the selected tab
         */

        tabLayout.setOnTabSelectedListener(new
           TabLayout.OnTabSelectedListener() {
               @Override
               public void onTabSelected(TabLayout.Tab tab) {
                   viewPager.setCurrentItem(tab.getPosition());
               }

               @Override
               public void onTabUnselected(TabLayout.Tab tab) {

               }

               @Override
               public void onTabReselected(TabLayout.Tab tab) {

               }
           });

        // Initialize the API client
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

//    private boolean resumeHasRun = false;
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        if (!resumeHasRun) {
//            resumeHasRun = true;
//            return;
//        }
//        // Normal case behavior follows
//
//        float currLat = (float) currentLocation.getLatitude();
//        float currLon = (float) currentLocation.getLongitude();
//
//        makePlacesRequest(currLat, currLon, foodType);
//        makePlacesRequest(currLat, currLon, funType);
//        makePlacesRequest(currLat, currLon, shopType);
//    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    // Called when the Client is initialized
    protected void onStart() {
        Log.i(TAG, "Client Connecting");
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    // Called when Client is finished
    protected void onStop() {
        Log.i(TAG, "Client Disconnecting");
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        // As soon as the Client connects, get the last location
        currentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        // Get places data
        float currLat = (float) currentLocation.getLatitude();
        float currLon = (float) currentLocation.getLongitude();

        makePlacesRequest(currLat, currLon, foodType);
        makePlacesRequest(currLat, currLon, funType);
        makePlacesRequest(currLat, currLon, shopType);

        // Create a location request for the automatically updating data
        LocationRequest request = LocationRequest.create();
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        request.setInterval(30000); // Poll every thirty seconds
        request.setFastestInterval(30000);

        // Create a special instance of the location provider
        FusedLocationProviderApi fusedLocationProviderApi = LocationServices.FusedLocationApi;

        // Request regular updates from the special location provider using the request object
        fusedLocationProviderApi.requestLocationUpdates(mGoogleApiClient, request, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Connection Suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(TAG, "Connection failed. Error: " + connectionResult.getErrorCode());
    }

    @Override
    // Called every time the location provider gets new data
    public void onLocationChanged(Location location) {

        // If it's not null, get the latitude and longitude
        if (location != null) {
            float currLat = (float) currentLocation.getLatitude();
            float currLon = (float) currentLocation.getLongitude();
            float newLat = (float) location.getLatitude();
            float newLon = (float) location.getLongitude();

            // Update places if the latitude and longitude are different enough from the current
            // location
            if (distFrom(currLat, currLon, newLat, newLon) > 500) {
                // Update the current location with the new data
                currentLocation = location;

                // Get places data
                makePlacesRequest(newLat, newLon, foodType);
                makePlacesRequest(newLat, newLon, funType);
                makePlacesRequest(newLat, newLon, shopType);
            }
        }
    }

    // Calculates the distance between two sets of coordinates
    // Credit to http://stackoverflow.com/questions/837872/calculate-distance-in-meters-when-you-
    // know-longitude-and-latitude-in-java
    public float distFrom(float lat1, float lng1, float lat2, float lng2) {
        double earthRadius = 6371000; //meters
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        float dist = (float) (earthRadius * c);

        return dist;
    }

    // Sends a request to Google Places API
    private void makePlacesRequest(float lat, float lon, final String type) {

        // List of places to return
        ArrayList<Place> places = new ArrayList<Place>();

        // URL components
        String placesKey = "AIzaSyCl-8E28wKWpmbIEstGpjWoPrMPsBvYGXk";               // API key
        int radius = 5000;                                                          // Search Radius
        String location = lat + "," + lon;                                          // Location
        String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?"; // Base url

        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();

        // Add parameters to request
        params.put("location", location);
        params.put("radius", radius);
        params.put("type", type);
        params.put("key", placesKey);
        client.get(url, params, new TextHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, String res) {
                        // called when response HTTP status is "200 OK"
                        // Depending on which request was made, update a different fragment
                        if (type == foodType) {
                            food = parseResponse(res);

                            // Find the food fragment and send it the data
                            List<Fragment> fragments = getSupportFragmentManager().getFragments();
                            FoodTabFrag foodFrag;
                            for (int i = 0; i < fragments.size(); i ++) {
                                Class<? extends Fragment> classType = fragments.get(i).getClass();
                                if (classType.isInstance(new FoodTabFrag())) {
                                    foodFrag = (FoodTabFrag) fragments.get(i);
                                    foodFrag.updateView(food);
                                }
                            }
                        }
                        else if (type == funType) {
                            fun = parseResponse(res);

                            // Find the fun fragment and send it the data
                            List<Fragment> fragments = getSupportFragmentManager().getFragments();
                            FunTabFrag funFrag;
                            for (int i = 0; i < fragments.size(); i ++) {
                                Class<? extends Fragment> classType = fragments.get(i).getClass();
                                if (classType.isInstance(new FunTabFrag())) {
                                    funFrag = (FunTabFrag) fragments.get(i);
                                    funFrag.updateView(fun);
                                }
                            }
                        }
                        else if (type == shopType) {
                            shopping = parseResponse(res);

                            // Find the store fragment and send it the data
                            List<Fragment> fragments = getSupportFragmentManager().getFragments();
                            StoresTabFrag storeFrag;
                            for (int i = 0; i < fragments.size(); i ++) {
                                Class<? extends Fragment> classType = fragments.get(i).getClass();
                                if (classType.isInstance(new StoresTabFrag())) {
                                    storeFrag = (StoresTabFrag) fragments.get(i);
                                    storeFrag.updateView(shopping);
                                }
                            }
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String res, Throwable t) {
                        // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                    }
                }
        );
    }

    // Parse the JSON data that is returned
    private ArrayList<Place> parseResponse(String responseString) {
        try {
            JSONObject inJson = new JSONObject(responseString); // Initial Json response
            JSONArray results = (JSONArray) inJson.get("results");

            ArrayList<Place> places = new ArrayList<Place>(); // List of Place objects to return

            // Populate array of Places from places in response
            for (int i = 0; i < results.length(); i ++) {
                JSONObject placeObj = (JSONObject) results.get(i);
                Place place = new Place();
                if (placeObj.has("name")) {
                    place.setName(placeObj.getString("name"));
                }
                if (placeObj.has("rating")) {
                    place.setRating(placeObj.getDouble("rating"));
                }
                if (placeObj.has("vicinity")) {
                    place.setAddress(placeObj.getString("vicinity"));
                }
                places.add(place);
            }

            return places;
        } catch (Exception e) {
            Log.d("parseResponse()", "Unable to parse response");
        }
        return null;
    }
}