package com.kerbless.kerb.activities;

import android.app.Application;
import android.util.Log;
import android.widget.Toast;

import com.kerbless.kerb.models.Customer;
import com.kerbless.kerb.models.Restaurant;
import com.kerbless.kerb.models.WaitLists2;
import com.parse.LogInCallback;
import com.parse.Parse;
import com.parse.ParseAnonymousUtils;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseUser;
import com.parse.SaveCallback;

public class InitializeParse extends Application {

    private static final String TAG =InitializeParse.class.getName();
    public static final String KERB_APPLICATION_ID = "Sv3p7Y2SlLD4vy2jHfEYtN2wWTiNBwd5M0qhpiGc";
    public static final String KERB_CLIENT_KEY = "2rn1eh4gbqLrWID3T2my2zwtjswBM1RGa6edbJzD";

    @Override
    public void onCreate() {
        super.onCreate();

        ParseObject.registerSubclass(WaitLists2.class);
        ParseObject.registerSubclass(Customer.class);
        ParseObject.registerSubclass(Restaurant.class);

        Parse.setLogLevel(Parse.LOG_LEVEL_VERBOSE);
        Parse.initialize(this, KERB_APPLICATION_ID, KERB_CLIENT_KEY);
        ParseInstallation installation = ParseInstallation.getCurrentInstallation();

        if (ParseUser.getCurrentUser() == null) {
            ParseUser.enableAutomaticUser();
            //ParseUser.getCurrentUser().increment("RunCount");
            ParseUser.getCurrentUser().saveInBackground();
            //String userid = ParseUser.getCurrentUser().getObjectId();

            /*ParseAnonymousUtils.logIn(new LogInCallback() {
                @Override
                public void done(ParseUser user, ParseException e) {
                    if (e != null) {
                        Log.d("MyApp", "Anonymous login failed.");
                    } else {
                        Toast.makeText(getApplicationContext(), "Created anonymous user"+ParseUser.getCurrentUser().getObjectId(), Toast.LENGTH_SHORT).show();
                        Log.d("MyApp", "Anonymous user logged in.");
                    }
                }
            });*/
        } else {
            installation.put("user", ParseUser.getCurrentUser());
            //Toast.makeText(getApplicationContext(), "Existing user:" + ParseUser.getCurrentUser().getObjectId(), Toast.LENGTH_SHORT).show();
        }
        installation.saveInBackground();

        ParsePush.subscribeInBackground(KERB_APPLICATION_ID, new SaveCallback() {
            @Override
            public void done(ParseException e) {
                Log.e(TAG, "Successfully subscribed to Parse!", e);
            }
        });
        ParseUser u1 = ParseUser.getCurrentUser();
    }
}
