package com.kerbless.kerb.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.os.Handler;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.kerbless.kerb.R;
import com.kerbless.kerb.models.Listing;
import com.kerbless.kerb.utils.ListingUtils;
import com.kerbless.kerb.utils.Utilities;
import com.kerbless.kerb.utils.WaitlistUtils;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SendCallback;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

public class UserWaitListActivity extends AppCompatActivity {

    private static final String TAG = UserWaitListActivity.class.getName();
    TextView tvName;
    TextView tvEstimatedTime;
    TextView tvPartySize;
    TextView tvPosition;
    TextView tvConfirmation;
    ImageView ivClock;
    ImageView ivMapView;
    ProgressBar progressBar;

    String restaurantId;
    String restaurantName;
    String customerId;
    Handler handler;
    int initialWaitTime;
    SharedPreferences pref;
    int savedWaitTime;
    int partiesAheadCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_wait_list);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Kerb");
        }

        Intent i = getIntent();
        restaurantId = i.getStringExtra("Kerb.RestaurantID");
        restaurantName = i.getStringExtra("Kerb.Restaurant");
        if(restaurantName == null || restaurantName.isEmpty()) {
            restaurantName = WaitlistUtils.getRestaurantName(restaurantId);
        }
        customerId = ParseUser.getCurrentUser().getObjectId();
        int partySize = WaitlistUtils.getPartySize(restaurantId, customerId);

        tvName = (TextView) findViewById(R.id.tvName);
        tvPartySize = (TextView) findViewById(R.id.tvPartySize);
        tvPosition = (TextView) findViewById(R.id.tvPosition);
        tvEstimatedTime = (TextView) findViewById(R.id.tvEstimatedTime);
        tvConfirmation = (TextView) findViewById(R.id.tvConfirmation);
        ivClock = (ImageView) findViewById(R.id.ivClock);
        Glide.with(this).load(R.drawable.stopwatch).asGif().into(ivClock);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        ivMapView = (ImageView) findViewById(R.id.ivMapView);
        if (ListingUtils.currentLocation != null) {
            String mapURL = "http://maps.google.com/maps/api/staticmap?center=" + ListingUtils.currentLocation.getLatitude() + "," + ListingUtils.currentLocation.getLongitude() + "&zoom=16&size=400x250&sensor=false&markers=color:blue%7Clabel:C%7C" + ListingUtils.currentLocation.getLatitude() + "," + ListingUtils.currentLocation.getLongitude();
            Picasso.with(this).load(mapURL).into(ivMapView);
        }

        pref = PreferenceManager.getDefaultSharedPreferences(this);
        savedWaitTime = pref.getInt("savedWait", -1);

        initialWaitTime = WaitlistUtils.getPredictedWaitTime(restaurantId, customerId);

        //Toast.makeText(getApplicationContext(), "First time, estimatedWait:" + Integer.toString(initialWaitTime), Toast.LENGTH_SHORT).show();

        tvName.setText(restaurantName);
        tvEstimatedTime.setText(Integer.toString(initialWaitTime));
        tvPartySize.setText(Integer.toString(partySize));
        updatePartiesAheadCount();
        handler = new Handler();
        final Runnable newRunnable = new Runnable() {
            @Override
            public void run() {

                if (WaitlistUtils.isCustomerNotified(customerId)) {
                    //if notified, stop the handler
                    handler.removeCallbacks(this);
                }
                //Toast.makeText(getApplicationContext(), "hi", Toast.LENGTH_SHORT).show();
                if (WaitlistUtils.getCustomerStatus(ParseUser.getCurrentUser().getObjectId()).equals("seated")) {

                    ListingUtils.joinedRestaurantID = null;
                    ListingUtils.customerOnWaitList = false;

                    //customer was seated, by restaurant, close this activity, remove tasks from queue
                    handler.removeCallbacks(this);
                    tvConfirmation.setText(" ");
                    tvConfirmation.setText("Thank You,Visit us again");
                    tvEstimatedTime.setText(" ");
                    tvPartySize.setText(" - ");
                    tvPosition.setText(" - ");
                    ivClock.setImageResource(R.drawable.delete);
                    WaitlistUtils.leaveWaitList(restaurantId, customerId);
                    //finish();
                    //Once user is seated, redirect to ListingsActivity
                    Intent intent = new Intent(UserWaitListActivity.this, ListingsActivity.class);
                    startActivity(intent);
                } else if (initialWaitTime == WaitlistUtils.getPredictedWaitTime(restaurantId, customerId)) {
                    //this means the db has not been updated since the last time we checked, OR  this page has been loaded before,
                    //in which case initialWaitTime represent lastsaved time
                    //get the actual time user has waited and subtract from initialWaitTime
                    //Toast.makeText(getApplicationContext(), "No customer got seated, WaitTime decremented by 1", Toast.LENGTH_SHORT).show();
                    if (initialWaitTime - WaitlistUtils.getCustomerAlreadyWaitedTime(restaurantId, customerId) < 0) {
                        //Toast.makeText(getApplicationContext(), "clipping to 3", Toast.LENGTH_SHORT).show();
                        tvEstimatedTime.setText("3");
                    } else {
                        tvEstimatedTime.setText(Integer.toString(initialWaitTime - WaitlistUtils.getCustomerAlreadyWaitedTime(restaurantId, customerId)));
                    }
                    handler.postDelayed(this, 60000);
                    if (initialWaitTime - WaitlistUtils.getCustomerAlreadyWaitedTime(restaurantId, customerId) < 3) {
                        //if estimate time is only 5mins, stop the timer.
                        //Toast.makeText(getApplicationContext(), "3min estimate, stop timer", Toast.LENGTH_SHORT).show();
                        handler.removeCallbacks(this);

                    }
                } else {
                    //this means another user was seated, so we will use this as the new initial estimate(it already accounts for time waited already)
                    //Toast.makeText(getApplicationContext(), "WaitTime adjusted with new data", Toast.LENGTH_SHORT).show();
                    initialWaitTime = WaitlistUtils.getPredictedWaitTime(restaurantId, customerId);
                    updatePartiesAheadCount();
                    if (initialWaitTime < 3) {
                        tvEstimatedTime.setText(Integer.toString(3));
                        handler.removeCallbacks(this);
                    } else {
                        tvEstimatedTime.setText(Integer.toString(initialWaitTime));
                        handler.postDelayed(this, 60000);
                    }

                }

            }

        };

        handler.post(newRunnable);
        if (WaitlistUtils.isCustomerNotified(customerId)) {
            tvConfirmation.setText(R.string.table_ready);
            tvName.setText(restaurantName);
            initialWaitTime = 0;
            tvEstimatedTime.setText("0");
        }

        if (i.getStringExtra("notification") != null) {
            if (!i.getStringExtra("notification").isEmpty()) {
                AlertDialog.Builder notificationAlert = new AlertDialog.Builder(this);
                notificationAlert.setTitle(i.getStringExtra("title"));
                notificationAlert.setMessage(i.getStringExtra("customdata"));
                notificationAlert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                tvConfirmation.setText(R.string.table_ready);
                initialWaitTime = 0;
                tvEstimatedTime.setText("0");
                tvPartySize.setText(WaitlistUtils.getPartySize(restaurantId, customerId));
                tvPosition.setText("You are next!");

                AlertDialog dialog = notificationAlert.create();
                dialog.show();
            }

        }

    }

    void updatePartiesAheadCount() {
        int totalWaiting = WaitlistUtils.getCurrentWaitListSize(restaurantId, 0);
        progressBar.setMax(totalWaiting);
        partiesAheadCount = WaitlistUtils.getCurrentPositionOnWaitlist(customerId, restaurantId, 0) - 1;
        //Toast.makeText(getApplicationContext(), "Ahead of you-" + Integer.toString(partiesAheadCount), Toast.LENGTH_SHORT).show();
        if (partiesAheadCount == 0) {
            tvEstimatedTime.setText("0");
            tvPosition.setText("You are next");
        } else if (partiesAheadCount == 1)
            tvPosition.setText("1 party ahead of you");
        else {
            tvPosition.setText(Integer.toString(partiesAheadCount) + " parties ahead of you");
        }
        progressBar.setProgress(totalWaiting - partiesAheadCount);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_user_wait_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_share) {
            ShareLocation();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void DropFromWaitlist(View view) {
        WaitlistUtils.leaveWaitList(restaurantId, customerId);
        sendGCMNotification(restaurantId);
        tvConfirmation.setText(" ");
        tvConfirmation.setText("You are Removed from the waitlist!");
        tvEstimatedTime.setText("");
        tvPartySize.setText(" - ");
        tvPosition.setText(" - ");
        ivClock.setImageResource(R.drawable.delete);
        ListingUtils.customerOnWaitList = false;
        ListingUtils.joinedRestaurantID = null;
        //finish();
        //after user leaves a waitlist, redirect to ListingsActivity
        Intent listingActivityIntent = new Intent(UserWaitListActivity.this, ListingsActivity.class);
        startActivity(listingActivityIntent);
        overridePendingTransition(R.anim.left_in, R.anim.right_out);
    }

    private void sendGCMNotification(final String restaurantId) {

        JSONObject obj;
        try {
            obj = new JSONObject();
            obj.put("action", "com.kerbless.kerb.REMOVE_FROM_WAITLIST");
            obj.put("customdata", ParseUser.getCurrentUser().getObjectId());

            ParsePush push = new ParsePush();
            ParseQuery query = ParseInstallation.getQuery();
            ParseUser owner = WaitlistUtils.getOwnerId(restaurantId);

            query.whereEqualTo("user", owner);
            push.setQuery(query);
            push.setData(obj);
            push.sendInBackground(new SendCallback() {
                @Override
                public void done(ParseException e) {
                    if (e == null) {
                        Log.d(TAG, "::Successfully sent notification to restaurant.");
                    } else {
                        Log.d(TAG, "Failed to push notification to restaurant : ");
                        e.printStackTrace();
                    }
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        overridePendingTransition(R.anim.left_in, R.anim.right_out);
    }

    private void ShareLocation() {

        Uri imageURI = Utilities.getLocalBitmapUri(this, ivMapView);
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_TEXT, "Currently on the wait list at " + restaurantName + " for a table for " + tvPartySize.getText().toString());
        shareIntent.putExtra(Intent.EXTRA_STREAM, imageURI);
        shareIntent.setType("image/*");

        startActivity(shareIntent);
    }

    /*@Override
    protected void onPause() {
        super.onPause();
        this.unregisterReceiver(mBroadcastReceiver);
        mBroadcastReceiver = null;
    }*/
}
