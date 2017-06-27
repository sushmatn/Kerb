package com.kerbless.kerb.activities;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.BounceInterpolator;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.astuetz.PagerSlidingTabStrip;
import com.github.jlmd.animatedcircleloadingview.AnimatedCircleLoadingView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.kerbless.kerb.R;
import com.kerbless.kerb.fragments.FilterFragment;
import com.kerbless.kerb.fragments.ListingsFragment;
import com.kerbless.kerb.fragments.MapFragment;
import com.kerbless.kerb.utils.ListingUtils;
import com.kerbless.kerb.models.Listing;
import com.kerbless.kerb.utils.Utilities;
import com.kerbless.kerb.utils.WaitlistUtils;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.parse.ParseUser;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cz.msebera.android.httpclient.Header;

public class ListingsActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        ListingUtils.RefreshListings,
        LocationListener {

    public static final double INITIAL_LATITUDE = 37.7970832;
    public static final double INITIAL_LONGITUDE = -122.3969807;

    ListingsPagerAdapter adapter;
    ViewPager viewPager;
    PagerSlidingTabStrip tabStrip;
    ArrayList<Listing> listings;
    public String nextPageToken;
    public String searchName;
    MenuItem miActionProgressItem;
    AnimatedCircleLoadingView loadingView;
    RelativeLayout frameContainer;

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;

    private long UPDATE_INTERVAL = 60000;  /* 60 secs */
    private long FASTEST_INTERVAL = 5000; /* 5 secs */

    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private final static String LISTINGS = "Kerb.Listings";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listings);

        ListingUtils.customerOnWaitList = WaitlistUtils.isCustomerOnWaitList(ParseUser.getCurrentUser().getObjectId());
        if (ListingUtils.customerOnWaitList)
            ListingUtils.joinedRestaurantID = WaitlistUtils.getRestaurantIdCustomerIsWaitlistedOn(ParseUser.getCurrentUser().getObjectId());

        ListingUtils.LoadFavorites(this);

        //Toast.makeText(this, "User logged in" + ParseUser.getCurrentUser().getUsername(), Toast.LENGTH_SHORT).show();

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        adapter = new ListingsPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);
        tabStrip = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        tabStrip.setViewPager(viewPager);
        loadingView = (AnimatedCircleLoadingView) findViewById(R.id.loadingView);
        frameContainer = (RelativeLayout) findViewById(R.id.frameContainer);

        // Display the logo
        //getSupportActionBar().setLogo(getResources().getDrawable(R.mipmap.ic_launcher));
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle("Kerb");
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayUseLogoEnabled(true);

        buildGoogleApiClient();
    }

    private void fetchData(boolean bLoadMore) {
        if (bLoadMore && nextPageToken == null)
            return;

        if (searchName == null)
            fetchData(ListingUtils.fetchURL(ListingUtils.currentLocation, nextPageToken), bLoadMore);
        else
            fetchData(ListingUtils.fetchURL(ListingUtils.currentLocation, nextPageToken, searchName), bLoadMore);
    }

    private void fetchData(String url, final boolean bLoadMore) {

        if (!Utilities.checkNetwork(this)) {
            Toast.makeText(this, "Network not available", Toast.LENGTH_SHORT).show();
            return;
        }

        AsyncHttpClient client = new AsyncHttpClient();
        if (bLoadMore)
            showProgressBar();

        client.get(url, null, new JsonHttpResponseHandler() {

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        try {
                            String status = response.getString("status");
                            if (status.equals("OK")) {
                                new FetchAsyncData(response, bLoadMore).execute();
                                if (response.has("next_page_token"))
                                    nextPageToken = response.getString("next_page_token");
                            } else {
                                setFragmentData(status);
                            }


                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject object) {
                        Toast.makeText(ListingsActivity.this, "Could not fetch listings data", Toast.LENGTH_SHORT).show();
                        hideProgressBar();
                    }
                }
        );
    }

    void setFragmentData(String status) {
        ListingsFragment listingsFragment = (ListingsFragment) adapter.instantiateItem(viewPager, 0);
        MapFragment mapFragment = (MapFragment) adapter.instantiateItem(viewPager, 1);
        listingsFragment.SetListings(status);
        mapFragment.SetListings(status);
    }

    void setFragmentData(ArrayList<Listing> listings, boolean bUpdateExisting) {
        ListingsFragment listingsFragment = (ListingsFragment) adapter.instantiateItem(viewPager, 0);
        MapFragment mapFragment = (MapFragment) adapter.instantiateItem(viewPager, 1);
        listingsFragment.SetListings(listings, bUpdateExisting);
        mapFragment.SetListings(listings, bUpdateExisting);
    }

    @Override
    public void RefreshList(boolean bLoadMore) {
        if (!bLoadMore) {
            nextPageToken = null;
            searchName = null;
            Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (location != null)
                ListingUtils.currentLocation = location;
        }
        fetchData(bLoadMore);
    }

    @Override
    public void LoadData() {
        setFragmentData(listings, false);
    }


    public void showProgressBar() {
        // Show progress item
        miActionProgressItem.setVisible(true);
    }

    public void hideProgressBar() {
        // Hide progress item
        miActionProgressItem.setVisible(false);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // Store instance of the menu item containing progress
        miActionProgressItem = menu.findItem(R.id.miActionProgress);
        // Extract the action-view from the menu item
        ProgressBar v = (ProgressBar) MenuItemCompat.getActionView(miActionProgressItem);
        // Return to finish
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_listings, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                nextPageToken = null;
                fetchData(ListingUtils.fetchURL(ListingUtils.currentLocation, nextPageToken, query), false);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_fav) {
            ShowFavorites();
            return true;
        }
        if (id == R.id.action_settings){
            FragmentManager fm = getSupportFragmentManager();
            FilterFragment filterFragment = FilterFragment.newInstance("Filter Settings");
            filterFragment.show(fm, "fragment_edit_name");

        }

        return super.onOptionsItemSelected(item);
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    protected void connectClient() {
        // Connect the client.
        if (Utilities.isGooglePlayServicesAvailable(this) && mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    /*
 * Called when the Activity becomes visible.
*/
    @Override
    protected void onStart() {
        super.onStart();
        connectClient();
    }

    /*
     * Called when the Activity is no longer visible.
	 */
    @Override
    protected void onStop() {
        // Disconnecting the client invalidates it.
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
        ListingUtils.SaveFavorites(this);
        super.onStop();
    }

    /*
     * Handle results returned to the FragmentActivity by Google Play services
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Decide what to do based on the original request code
        switch (requestCode) {

            case CONNECTION_FAILURE_RESOLUTION_REQUEST:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        mGoogleApiClient.connect();
                        break;
                }
        }
    }

    /*
     * Called by Location Services when the request to connect the client
     * finishes successfully. At this point, you can request the current
     * location or start periodic updates
     */
    @Override
    public void onConnected(Bundle dataBundle) {
        // Display the connection status
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (ListingUtils.currentLocation == null) {
            ListingUtils.currentLocation = new Location("");
            ListingUtils.currentLocation.setLatitude(INITIAL_LATITUDE);
            ListingUtils.currentLocation.setLongitude(INITIAL_LONGITUDE);
        }
        if (location != null)
            ListingUtils.currentLocation = location;

        if (listings == null)
            fetchData(ListingUtils.fetchURL(ListingUtils.currentLocation, nextPageToken), false);

        startLocationUpdates();
    }

    protected void startLocationUpdates() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                mLocationRequest, this);
    }

    public void onLocationChanged(Location location) {
        /*MapFragment mapFragment = (MapFragment) adapter.instantiateItem(viewPager, 1);
        mapFragment.updateLocation(location);*/
        ListingUtils.currentLocation = location;
    }

    /*
     * Called by Location Services if the connection to the location client
     * drops because of an error.
     */
    @Override
    public void onConnectionSuspended(int i) {
        if (i == CAUSE_SERVICE_DISCONNECTED) {
            Toast.makeText(this, "Disconnected. Please re-connect.", Toast.LENGTH_SHORT).show();
        } else if (i == CAUSE_NETWORK_LOST) {
            Toast.makeText(this, "Network lost. Please re-connect.", Toast.LENGTH_SHORT).show();
        }
    }

    /*
     * Called by Location Services if the attempt to Location Services fails.
     */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        /*
         * Google Play services can resolve some errors it detects. If the error
		 * has a resolution, try sending an Intent to start a Google Play
		 * services activity that can resolve error.
		 */
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this,
                        CONNECTION_FAILURE_RESOLUTION_REQUEST);
                /*
                 * Thrown if Google Play services canceled the original
				 * PendingIntent
				 */
            } catch (IntentSender.SendIntentException e) {
                // Log the error
                e.printStackTrace();
            }
        } else {
            Toast.makeText(getApplicationContext(),
                    "Sorry. Location services not available to you", Toast.LENGTH_LONG).show();
        }
    }

    void ShowFavorites() {
        nextPageToken = null;
        ArrayList<Listing> listings = new ArrayList<>();
        for (Map.Entry entry : ListingUtils.FavRestaurants.entrySet()) {
            try {
                Listing listing = Listing.fromJSON(new JSONObject(entry.getValue().toString()));
                listings.add(listing);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        setFragmentData(listings, false);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(LISTINGS, listings);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            listings = savedInstanceState.getParcelableArrayList(LISTINGS);
        }
    }

    public class ListingsPagerAdapter extends FragmentPagerAdapter implements PagerSlidingTabStrip.IconTabProvider {
        private String tabTitles[] = {"Listings", "Map"};
        final int[] tabIcons = new int[]{
                R.drawable.ic_restaurant,
                R.drawable.ic_mapview
        };

        public ListingsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getPageIconResId(int position) {
            return tabIcons[position];
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0)
                return ListingsFragment.newInstance();
            else if (position == 1)
                return MapFragment.newInstance();
            else
                return null;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return tabTitles[position];
        }

        @Override
        public int getCount() {
            return tabTitles.length;
        }
    }

    class FetchAsyncData extends AsyncTask<Void, Void, ArrayList<Listing>> {

        JSONObject response;
        boolean loadMore;

        public FetchAsyncData(JSONObject jResponse, boolean bLoadMore) {
            response = jResponse;
            loadMore = bLoadMore;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (!loadMore) {
                setFragmentData(null, false);
                loadingView.resetLoading();
                loadingView.setAlpha(1);
                loadingView.startIndeterminate();
            }
        }

        @Override
        protected ArrayList<Listing> doInBackground(Void... params) {
            try {
                listings = Listing.fromJSON(response.getJSONArray("results"));
                return listings;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<Listing> listings) {
            super.onPostExecute(listings);

            hideProgressBar();

            if (listings != null) {
                setFragmentData(listings, loadMore);
                loadingView.stopOk();
            } else
                loadingView.stopFailure();

            if (!loadMore) {
                ObjectAnimator fadeOut = ObjectAnimator.ofFloat(loadingView, View.ALPHA, 0f);
                fadeOut.setDuration(2500);
                fadeOut.start();
            }
        }
    }
}
