package com.kerbless.kerb.activities;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.kerbless.kerb.fragments.PhotoViewerFragment;
import com.kerbless.kerb.R;
import com.kerbless.kerb.fragments.JoinWaitListFragment;
import com.kerbless.kerb.models.ReviewUser;
import com.kerbless.kerb.utils.ListingUtils;
import com.kerbless.kerb.utils.Utilities;
import com.kerbless.kerb.utils.WaitlistUtils;
import com.kerbless.kerb.models.Listing;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SendCallback;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Random;

import cz.msebera.android.httpclient.Header;

public class ListingDetailActivity extends AppCompatActivity implements JoinWaitListFragment.OnJoinWaitListListener {

    Listing listing;
    ImageView ivRestaurantImage;
    ImageView ivMapView;
    RatingBar ratingBar;
    TextView tvName;
    TextView tvAddress;
    TextView tvWaiting;
    TextView tvWaitTime;
    Button btnJoin;
    Button btnView;
    Toolbar mToolbar;
    CollapsingToolbarLayout mCollapsingToolbarLayout;
    TextView tvTable2;
    TextView tvTable4;
    TextView tvTable6;
    TextView tvPhone;
    TextView tvWebSite;
    TextView tvStatus;
    LinearLayout reviewContainer;
    ImageView ivUber;
    TextView tvUber;
    TextView tvPriceLevel;
    LinearLayout photoContainer;
    ImageView ivMaps;
    TextView tvDirections;
    boolean isWaitListOpen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listing_detail);

        //String status = WaitlistUtils.getCustomerStatus(ParseUser.getCurrentUser().getObjectId());
        //Toast.makeText(getApplicationContext(), "customer status:" + status, Toast.LENGTH_SHORT).show();
        //Toast.makeText(getApplicationContext(), "Waitlisted on:" + WaitlistUtils.getRestaurantIdCustomerIsWaitlistedOn(ParseUser.getCurrentUser().getObjectId()), Toast.LENGTH_SHORT).show();

        listing = getIntent().getParcelableExtra("listing");
        fetchListingDetails();
        tvTable2 = (TextView) findViewById(R.id.tvTable2);
        tvTable4 = (TextView) findViewById(R.id.tvTable4);
        tvTable6 = (TextView) findViewById(R.id.tvTable6);
        tvWaiting = (TextView) findViewById(R.id.tvWaiting);
        isWaitListOpen = WaitlistUtils.isWaitListOpen(listing.getListingId());
        new FetchDetailsAsync().execute();

        ivRestaurantImage = (ImageView) findViewById(R.id.restaurantImage);
        ratingBar = (RatingBar) findViewById(R.id.userRatingBar);
        tvName = (TextView) findViewById(R.id.tvName);
        tvAddress = (TextView) findViewById(R.id.tvAddress);
        btnJoin = (Button) findViewById(R.id.btnJoin);
        btnView = (Button) findViewById(R.id.btnView);
        tvPhone = (TextView) findViewById(R.id.tvPhone);
        tvWebSite = (TextView) findViewById(R.id.tvWebSite);
        ivMapView = (ImageView) findViewById(R.id.ivMapView);
        ivUber = (ImageView) findViewById(R.id.ivUber);
        tvUber = (TextView) findViewById(R.id.tvUber);
        tvStatus = (TextView) findViewById(R.id.tvStatus);
        tvPriceLevel = (TextView) findViewById(R.id.tvPriceLevel);
        ivMaps = (ImageView) findViewById(R.id.ivMaps);
        tvDirections = (TextView) findViewById(R.id.tvDirections);
        tvWaitTime = (TextView) findViewById(R.id.tvWaitTime);
        reviewContainer = (LinearLayout) findViewById(R.id.reviewContainer);
        photoContainer = (LinearLayout) findViewById(R.id.photoContainer);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mCollapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar_layout);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mCollapsingToolbarLayout.setTitle(listing.getRestaurantName());
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryTransparent));
        }

        Location lcn = new Location(ListingUtils.currentLocation);
        lcn.setLatitude(listing.getLatitude());
        lcn.setLongitude(listing.getLongitude());
        double distance = ListingUtils.currentLocation.distanceTo(lcn);


        if (WaitlistUtils.isRestaurantInDb(listing.getListingId()) != true) {
            btnJoin.setText(getResources().getString(R.string.reportwait));
            btnJoin.setEnabled(true);
            tvStatus.setText(getResources().getString(R.string.reportWaitStatus));
        } else if (distance < 165) {
            if (isWaitListOpen) {
                tvStatus.setText(getResources().getString(R.string.waitlistOpen));
                btnJoin.setEnabled(true);
            } else {
                tvStatus.setText(getResources().getString(R.string.waitlistClosed));
                btnJoin.setEnabled(false);
            }
        } else {
            tvStatus.setText(getResources().getString(R.string.notInVicinity));
            btnJoin.setEnabled(false);
        }

        int partiesWaiting = WaitlistUtils.getCurrentWaitListSize(listing.getListingId(), 0);
        tvWaiting.setText(partiesWaiting == 0 ? "No parties waiting" : partiesWaiting + " parties waiting");

        ivUber.setOnClickListener(uberClickListener);
        tvUber.setOnClickListener(uberClickListener);
        ivMaps.setOnClickListener(directionsListener);
        tvDirections.setOnClickListener(directionsListener);

        String sWaitTime = Integer.toString(listing.getWaitTime());
        SpannableStringBuilder ssb = new SpannableStringBuilder(sWaitTime + "\nmins");
        ssb.setSpan(new AbsoluteSizeSpan(12, true), sWaitTime.length(), ssb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        tvWaitTime.setText(ssb);

        //String mapURL = "http://dev.virtualearth.net/REST/V1/Imagery/Map/Road/" + listing.getLatitude() + "%2C" + listing.getLongitude() + "/16?mapSize=400,250&format=png&pushpin=" + listing.getLatitude() + "," + listing.getLongitude() + ";66;&key=Ako7RSutgKf7dtQXqULu9p1vhM62XX4KsFTd26nE5Zp0JmOPxDkTiFgDXwYkQ4pW";
        //String mapURL  = "http://dev.virtualearth.net/REST/V1/Imagery/Map/Road/"+ listing.getLatitude() +"%2C"+ listing.getLongitude() +"/16?mapSize=400,250&format=png&pushpin="+ listing.getLatitude() + "," + listing.getLongitude() + ";47;X&key=Ako7RSutgKf7dtQXqULu9p1vhM62XX4KsFTd26nE5Zp0JmOPxDkTiFgDXwYkQ4pW";
        String mapURL = "http://maps.google.com/maps/api/staticmap?center=" + listing.getLatitude() + "," + listing.getLongitude() + "&zoom=16&size=400x250&sensor=false&markers=color:blue%7Clabel:C%7C" + listing.getLatitude() + "," + listing.getLongitude();
        Picasso.with(this).load(mapURL).into(ivMapView);

        Picasso.with(this).load(listing.getPhotoResId()).into(ivRestaurantImage);
        ratingBar.setRating((float) listing.getRating());
        tvName.setText(listing.getRestaurantName());
        tvAddress.setText(listing.getAddress());

        tvPriceLevel.setText(Utilities.getPriceLevelString(listing.getPriceLevel()));
        Log.d("LDA:::", "" + listing.getListingId() + " - " + listing.getRestaurantName() + " - " + listing.getAddress() + " - " + listing.getPhotoUrl());

        btnJoin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!WaitlistUtils.isCustomerOnWaitList(ParseUser.getCurrentUser().getObjectId())) {
                    JoinWaitListFragment taskDetails = JoinWaitListFragment.newInstance(listing);
                    taskDetails.show(getSupportFragmentManager(), "WaitListDialog");
                } else {
                    Toast.makeText(ListingDetailActivity.this, "You cannot join more than one waitlist", Toast.LENGTH_SHORT).show();
                }
            }
        });
        btnView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ListingDetailActivity.this, UserWaitListActivity.class);
                intent.putExtra("Kerb.Restaurant", listing.getRestaurantName());
                intent.putExtra("Kerb.RestaurantID", listing.getListingId());
                startActivity(intent);
                overridePendingTransition(R.anim.right_in, R.anim.left_out);
            }
        });
    }

    View.OnClickListener directionsListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String sUri = "http://maps.google.com/maps?saddr=" + ListingUtils.currentLocation.getLatitude() + "," + ListingUtils.currentLocation.getLongitude()
                    + "&daddr=" + listing.getLatitude() + "," + listing.getLongitude();
            try {
                PackageManager pm = getPackageManager();
                pm.getApplicationInfo("com.google.android.apps.maps", 0);
                Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                        Uri.parse(sUri));
                startActivity(intent);
            } catch (PackageManager.NameNotFoundException e) {
                // No Maps app! Open mobile website.
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(sUri));
                startActivity(i);
            }
        }
    };

    View.OnClickListener uberClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            try {
                PackageManager pm = getPackageManager();
                pm.getPackageInfo("com.ubercab", PackageManager.GET_ACTIVITIES);
                String uri =
                        "uber://?action=setPickup&pickup=" + ListingUtils.currentLocation.getLatitude() + "," + ListingUtils.currentLocation.getLongitude() + "&client_id=cI21Exfn1cUhevX6AqkirG4tZCV7OmHz";
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(uri));
                startActivity(intent);
            } catch (PackageManager.NameNotFoundException e) {
                // No Uber app! Open mobile website.
                String url = "https://m.uber.com/sign-up?client_id=cI21Exfn1cUhevX6AqkirG4tZCV7OmHz";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        }
    };

    @Override
    protected void onResume() {
        if (ListingUtils.customerOnWaitList &&
                ListingUtils.joinedRestaurantID.equals(listing.getListingId())) {
            btnJoin.setVisibility(View.GONE);
            btnView.setVisibility(View.VISIBLE);
        } else {
            btnJoin.setVisibility(View.VISIBLE);
            btnView.setVisibility(View.GONE);
        }
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_listing_detail, menu);
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
            ParseUser.getCurrentUser().logOutInBackground();
            return true;
        } else if (item.getItemId() == android.R.id.home) {
            supportFinishAfterTransition();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    void updateUI() {
        if (listing.getPhone() == null)
            tvPhone.setVisibility(View.GONE);
        else
            tvPhone.setText(listing.getPhone());
        if (listing.getWebsite() == null)
            tvWebSite.setVisibility(View.GONE);
        else
            tvWebSite.setText(listing.getWebsite());

        for (ReviewUser user : listing.getReviews()) {
            View view = LayoutInflater.from(this).inflate(R.layout.item_review, reviewContainer, false);
            TextView tvAuthor = (TextView) view.findViewById(R.id.tvAuthor);
            TextView tvReview = (TextView) view.findViewById(R.id.tvReview);
            RatingBar userRatingBar = (RatingBar) view.findViewById(R.id.userRatingBar);
            tvAuthor.setText(user.author);
            tvReview.setText(user.review);
            userRatingBar.setRating(user.rating);
            reviewContainer.addView(view);
        }

        if (listing.getRestaurantName().contains("Kokkari")) {
            ArrayList<String> photos = new ArrayList<>();
            photos.add(Uri.parse("android.resource://com.kerbless.kerb/" + R.drawable.seafood1).toString());
            photos.add(Uri.parse("android.resource://com.kerbless.kerb/" + R.drawable.seafood2).toString());
            photos.add(Uri.parse("android.resource://com.kerbless.kerb/" + R.drawable.seafood3).toString());
            photos.add(Uri.parse("android.resource://com.kerbless.kerb/" + R.drawable.seafood4).toString());
            photos.add(Uri.parse("android.resource://com.kerbless.kerb/" + R.drawable.seafood5).toString());
            photos.add(Uri.parse("android.resource://com.kerbless.kerb/" + R.drawable.seafood6).toString());
            listing.setPhotos(photos);
        }
        for (String photo : listing.getPhotos()) {
            ImageView ivPhoto = new ImageView(this);
            ivPhoto.setPadding(2, 2, 2, 2);
            ivPhoto.setScaleType(ImageView.ScaleType.CENTER_CROP);
            Picasso.with(this).load(photo).centerCrop().resize(500, 500).into(ivPhoto);
            ivPhoto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PhotoViewerFragment fragment = PhotoViewerFragment.newInstance(listing.getPhotos(), listing.getRestaurantName());
                    fragment.show(getSupportFragmentManager(), "ViewPhotos");
                }
            });
            photoContainer.addView(ivPhoto);
        }
    }

    void fetchListingDetails() {
        if (!Utilities.checkNetwork(this)) {
            Toast.makeText(this, "Network not available", Toast.LENGTH_SHORT).show();
            return;
        }

        AsyncHttpClient client = new AsyncHttpClient();
        client.get(ListingUtils.fetchDetailsURL(listing.getReference()), null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    String status = response.getString("status");
                    if (status.equals("OK")) {
                        listing = Listing.fillDetails(listing, response.getJSONObject("result"));
                        updateUI();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject object) {
                Toast.makeText(ListingDetailActivity.this, "Could not fetch listing details", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public Intent getParentActivityIntent() {
        return super.getParentActivityIntent().addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Display display = getWindowManager().getDefaultDisplay();
        Utilities.screenWidth = display.getWidth();
        Utilities.screenHeight = display.getHeight();
    }

    @Override
    public void onJoinWaitListClicked(String listingId, int partySize, String customerName, String notes) {
        new JoinWaitListAsync(this, listingId, customerName, partySize, notes).execute();
    }

    private void sendGCMNotification(final String restaurantId) {

        JSONObject obj;
        try {
            obj = new JSONObject();
            obj.put("action", "com.kerbless.kerb.UPDATE_WAITLIST");
            obj.put("customdata", ParseUser.getCurrentUser().getObjectId());

            ParsePush push = new ParsePush();
            ParseQuery query = ParseInstallation.getQuery();
            // Get owner(user) id from _User table using restaurantId
            ParseUser owner = WaitlistUtils.getOwnerId(restaurantId);

            query.whereEqualTo("user", owner);
            push.setQuery(query);
            push.setData(obj);
            push.sendInBackground(new SendCallback() {
                @Override
                public void done(ParseException e) {
                    if (e == null) {
                        Log.d(ListingDetailActivity.class.getName(), "::Successfully sent notification to restaurant.");
                    } else {
                        Log.d(ListingDetailActivity.class.getName(), "Failed to push notification to restaurant : ");
                        e.printStackTrace();
                    }
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    class JoinWaitListAsync extends AsyncTask<Void, Void, Boolean> {

        String listingID;
        String customerName;
        int partySize;
        String notes;
        MaterialDialog progressDialog;
        Context mContext;

        public JoinWaitListAsync(Context context, String listId, String custName, int number, String instructions) {
            listingID = listId;
            customerName = custName;
            partySize = number;
            notes = instructions;
            mContext = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new MaterialDialog.Builder(mContext)
                    .title(R.string.progress_dialog)
                    .content(R.string.please_wait)
                    .progress(true, 0)
                    .show();

        }

        @Override
        protected Boolean doInBackground(Void... params) {

            String[] name = customerName.split(" ");
            String fname = " ";
            String lname = " ";
            fname = name[0];
            if (name.length > 1) {
                lname = name[1];
            }

            if (WaitlistUtils.joinList(listing.getListingId(), ParseUser.getCurrentUser().getObjectId(), partySize, fname, lname, notes)) {
                // Sends push message to restaurant
                sendGCMNotification(listing.getListingId());

                // Save the restaurantId and status
                ListingUtils.joinedRestaurantID = listing.getListingId();
                ListingUtils.customerOnWaitList = true;
                return true;
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);

            progressDialog.dismiss();
            if (success) {
                Intent intent = new Intent(mContext, UserWaitListActivity.class);
                intent.putExtra("Kerb.Restaurant", listing.getRestaurantName());
                intent.putExtra("Kerb.RestaurantID", listing.getListingId());
                intent.putExtra("Kerb.PartySize", partySize);
                intent.putExtra("Kerb.UserName", customerName);
                startActivity(intent);
                overridePendingTransition(R.anim.right_in, R.anim.left_out);
            } else
                Toast.makeText(mContext, "Could not join the waitlist. Please see the hostess", Toast.LENGTH_SHORT).show();
        }
    }

    class FetchDetailsAsync extends AsyncTask<Void, Void, Void> {

        int partiesWaitingTable2 = 0, waitTimeTable2 = 0;
        int partiesWaitingTable4 = 0, waitTimeTable4 = 0;
        int partiesWaitingTable6 = 0, waitTimeTable6 = 0;

        @Override
        protected Void doInBackground(Void... params) {
            Random randomGenerator = new Random();
            if (isWaitListOpen) {
                waitTimeTable2 = WaitlistUtils.getWaitTime(listing.getListingId(), 2);
                waitTimeTable4 = WaitlistUtils.getWaitTime(listing.getListingId(), 4);
                waitTimeTable6 = WaitlistUtils.getWaitTime(listing.getListingId(), 6);
                partiesWaitingTable2 = WaitlistUtils.getCurrentWaitListSize(listing.getListingId(), 2);
                partiesWaitingTable4 = WaitlistUtils.getCurrentWaitListSize(listing.getListingId(), 4);
                partiesWaitingTable6 = WaitlistUtils.getCurrentWaitListSize(listing.getListingId(), 6);
            } else {
                waitTimeTable2 = (int) (listing.getWaitTime() - (float) listing.getWaitTime() * 0.08);
                waitTimeTable4 = (int) (listing.getWaitTime() + (float) listing.getWaitTime() * 0.04);
                waitTimeTable6 = (int) (listing.getWaitTime() + (float) listing.getWaitTime() * 0.12);
                partiesWaitingTable2 = randomGenerator.nextInt(5) + 1;
                partiesWaitingTable4 = randomGenerator.nextInt(5) + 1;
                partiesWaitingTable6 = randomGenerator.nextInt(5) + 1;
            }
            return null;
        }

        String getFormattedWaitTime(int waitTime, int partiesWaiting) {
            return (waitTime == 0 ? "No wait" : waitTime + " mins"
                    + "\n" + (partiesWaiting == 0 ? "No" : partiesWaiting) + " parties waiting");
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if (!isWaitListOpen)
                tvWaiting.setText((partiesWaitingTable2 + partiesWaitingTable4 + partiesWaitingTable6) + " parties waiting");
            tvTable2.setText(getFormattedWaitTime(waitTimeTable2, partiesWaitingTable2));
            tvTable4.setText(getFormattedWaitTime(waitTimeTable4, partiesWaitingTable4));
            tvTable6.setText(getFormattedWaitTime(waitTimeTable6, partiesWaitingTable6));

        }
    }
}
