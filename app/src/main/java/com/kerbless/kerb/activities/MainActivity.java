package com.kerbless.kerb.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.util.Util;
import com.kerbless.kerb.R;
import com.kerbless.kerb.utils.Utilities;
import com.kerbless.kerb.utils.WaitlistUtils;
import com.parse.LogInCallback;
import com.parse.ParseAnonymousUtils;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private String restaurantID = "";
    EditText etUsername;
    EditText etPassword;
    ArrayList<String> listOfRestaurantOwnerUsers;
    String TAG = this.getClass().getName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Log.d(":::", "ObjectID:" + ParseUser.getCurrentUser().getObjectId());

        Display display = getWindowManager().getDefaultDisplay();
        Utilities.screenWidth = display.getWidth();
        Utilities.screenHeight = display.getHeight();

        etUsername = (EditText) findViewById(R.id.etUsername);
        etPassword = (EditText) findViewById(R.id.etPassword);

        listOfRestaurantOwnerUsers = new ArrayList<>();
        listOfRestaurantOwnerUsers.add("dejavu");
        listOfRestaurantOwnerUsers.add("alink");
        listOfRestaurantOwnerUsers.add("plantcafe");
        listOfRestaurantOwnerUsers.add("sushResto");
        listOfRestaurantOwnerUsers.add("rehan");

        // Only set to true once per restaurant open list. Set to false immediately after run initiated to avoid dups.
        boolean loadData = false;
        //restaurantID = "a7b30587f181ff9aae539bc3d5a5f1f3f22356a3";

        /* Alin gAPI android:value="AIzaSyAVRKV5GOfZETYFpEDpC5GzeQyC3CdOSEI"*/
        

        /*if(!Utilities.checkNetwork(this)) {
            Toast.makeText(this, "No active network found", Toast.LENGTH_SHORT).show();
        } else if(restaurantID != null && restaurantID.length() > 0) {
            Intent intent = new Intent(this, RestaurantMainActivity.class);
            intent.putExtra("restaurantID", restaurantID);
            startActivity(intent);
            finish();
        } else {
            Intent intent = new Intent(this, ListingsActivity.class);
            startActivity(intent);
        }*/

        if (ParseUser.getCurrentUser() != null) {
            //Toast.makeText(MainActivity.this, "ObjectID:" + ParseUser.getCurrentUser().getObjectId(), Toast.LENGTH_SHORT).show();

            //if the user is already waiting on a waitlist, take him to the userWaitlistActivity
            String rstId = WaitlistUtils.getRestaurantIdCustomerIsWaitlistedOn(ParseUser.getCurrentUser().getObjectId());
            if (!rstId.equals("notfound")) {
                //Toast.makeText(MainActivity.this, "Redirecting to userwaitlist since already on waitlist", Toast.LENGTH_SHORT).show();
                HashMap<String, String> map = WaitlistUtils.getCustomerDetails(ParseUser.getCurrentUser().getObjectId(), rstId);
                Intent intent = new Intent(MainActivity.this, UserWaitListActivity.class);
                intent.putExtra("Kerb.Restaurant", map.get("restaurantName"));
                intent.putExtra("Kerb.RestaurantID", rstId);
                intent.putExtra("Kerb.PartySize", map.get("partySize"));
                intent.putExtra("Kerb.UserName", map.get("username"));
                Utilities.appFirstOpened = false;
                startActivity(intent);
                overridePendingTransition(R.anim.left_in, R.anim.right_out);
                finish();

            } else {
                if (ParseUser.getCurrentUser().getUsername() != null) {
                    //if user has username and is a registered restaurant owner open restaurant app
                    if (listOfRestaurantOwnerUsers.contains(ParseUser.getCurrentUser().getUsername())) {

                        //if you need to refresh the currentUser restaurantID enable the try/catch below.
                        try {
                            ParseUser.getCurrentUser().fetch();
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                        String restId = ParseUser.getCurrentUser().get("restaurantId").toString();
                        Intent intent = new Intent(getApplicationContext(), RestaurantMainActivity.class);
                        if (loadData) {
                            try {
                                WaitlistUtils.loadDemoList(restId, "YJ0yl05jpo", 6, "burrito bowl");
                                Thread.sleep(60000);
                                WaitlistUtils.loadDemoList(restId, "vP0mA3uMis", 4, "Outdoor please");
                                Thread.sleep(120000);
                                WaitlistUtils.loadDemoList(restId, "OqLBQn63wA", 8, "Soft Music");
                                Thread.sleep(40000);
                                WaitlistUtils.loadDemoList(restId, "rfjtprDOan", 5, "Bar seating ");
                                Thread.sleep(60000);
                                /*WaitlistUtils.loadDemoList(restId, "M9cF4Wjsii", 6, "vegetarian");
                                Thread.sleep(80000);
                                WaitlistUtils.loadDemoList(restId, "FyEzKEoT3K", 4, " ");
                                Thread.sleep(160000);
                                WaitlistUtils.loadDemoList(restId, "mH76M5bxkb", 4, " ");
                                Thread.sleep(30000);
                                WaitlistUtils.loadDemoList(restId, "0oVBNP0A1d", 3, "virtual meal");*/
                                //Thread.sleep(600000);
                                //WaitlistUtils.loadDemoList(restId, "HqG04CvtgS", 2, " ");
                                //WaitlistUtils.loadDemoList(restId, "8U1gv3mQyj", 2);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                        }
                        intent.putExtra("restaurantID", restId);
                        startActivity(intent);
                        finish();
                    } else {//if username is not in restaurantowners list, open listings activity
                        Intent intent;
                        if (Utilities.appFirstOpened) {
                            Utilities.appFirstOpened = false;
                            intent = new Intent(getApplicationContext(), SplashActivity.class);
                        }
                        else
                            intent = new Intent(getApplicationContext(), ListingsActivity.class);

                        startActivity(intent);
                        overridePendingTransition(R.anim.left_in, R.anim.right_out);
                        finish();
                    }
                }
            }
        } else {
            //current parse user is NULL, NO action =>login page and create an anonymous user login
            ParseAnonymousUtils.logIn(new LogInCallback() {
                @Override
                public void done(ParseUser user, ParseException e) {
                    if (e != null) {
                        Log.d("MyApp", "Anonymous login failed.");
                        //Toast.makeText(getApplicationContext(), "anonymous log in failed", Toast.LENGTH_SHORT).show();

                    } else {
                        //Toast.makeText(getApplicationContext(), "Created anonymous user" + ParseUser.getCurrentUser().getObjectId(), Toast.LENGTH_SHORT).show();
                        Log.d("MyApp", "Anonymous user logged in.");
                    }
                }
            });
        }
    }


    public void onLogin(View view) {
        final MaterialDialog progressDialog;
        progressDialog = new MaterialDialog.Builder(this)
                .title(R.string.progress_dialog)
                .content(R.string.login)
                .progress(true, 0)
                .show();

        String user = etUsername.getText().toString().toLowerCase();
        String password = etPassword.getText().toString();
        ParseUser.logInInBackground(user, password, new LogInCallback() {
            public void done(ParseUser user, ParseException e) {
                progressDialog.dismiss();
                final String invalidCredError = "invalid login credentials";
                if (user != null) {
                    ParseInstallation installation =
                            ParseInstallation.getCurrentInstallation();
                    installation.put("user", user);
                    installation.saveInBackground();
                    if (listOfRestaurantOwnerUsers.contains(user.getUsername())) {
                        String restId = user.get("restaurantId").toString();
                        Intent intent = new Intent(getApplicationContext(), RestaurantMainActivity.class);
                        intent.putExtra("restaurantID", restId);
                        startActivity(intent);
                        overridePendingTransition(R.anim.right_in, R.anim.left_out);
                        finish();
                    } else {
                        ParseUser newuser = ParseUser.getCurrentUser();
                        Intent intent = new Intent(getApplicationContext(), ListingsActivity.class);
                        startActivity(intent);
                        overridePendingTransition(R.anim.right_in, R.anim.left_out);
                    }
                } else {
                    // Signin failed. Look at the ParseException to see what happened.
                    Log.e(TAG, "Error: " + e.getMessage());
                    if (e.getMessage().equals(invalidCredError)) {
                        Toast.makeText(MainActivity.this, R.string.invalid_cred_msg, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    public void continueAnonymous(View view) {
        ParseUser newuser = ParseUser.getCurrentUser();
        if (ParseUser.getCurrentUser().getObjectId() != null) {
            //Toast.makeText(getApplicationContext(), "Created anonymous user" + ParseUser.getCurrentUser().getObjectId(), Toast.LENGTH_SHORT).show();
            ParseInstallation installation = ParseInstallation.getCurrentInstallation();
            installation.put("user", newuser);
            installation.saveInBackground();
            Intent intent = new Intent(getApplicationContext(), ListingsActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.right_in, R.anim.left_out);
        } else {
            //Toast.makeText(this, "Cannot start- Parseuser has null ojectID", Toast.LENGTH_SHORT).show();
        }
    }
}
