package com.kerbless.kerb.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.kerbless.kerb.R;
import com.kerbless.kerb.activities.CustomerNotificationActivity;
import com.kerbless.kerb.activities.UserWaitListActivity;
import com.kerbless.kerb.models.Listing;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * Created by alinc on 10/22/15.
 */
public class KerbCustomerReceiver extends BroadcastReceiver {

    private static final String TAG = KerbCustomerReceiver.class.getName();

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            if (intent == null)
            {
                Log.d(TAG, "Receiver intent is null!!");
            }
            else
            {
                String action = intent.getAction();
                Log.d(TAG, "got receiver action " + action );
                if (action.equals("com.kerbless.kerb.UPDATE_STATUS"))
                {
                    String channel = intent.getExtras().getString("com.parse.Channel");
                    JSONObject json = new JSONObject(intent.getExtras().getString("com.parse.Data"));
                    Log.d(TAG, "got action " + action + " on channel " + channel + " with:");
                    String title = "title";
                    String body = "Notification Body";
                    String restaurantId = "null";
                    if (json.has("title")) {
                        title = json.getString("title");
                        body = json.getString("customdata");
                        restaurantId = json.getString("restaurantId");
                        generateNotification(context, title, body, restaurantId);
                    }

                }
            }
        } catch (JSONException e) {
            Log.d(TAG, "JSONException: " + e.getMessage());
        }
    }

    private void generateNotification(Context context, String title, String body, String restaurantId) {
        Intent pupInt = new Intent();
        //pupInt.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        pupInt.setClassName("com.kerbless.kerb", "com.kerbless.kerb.activities.UserWaitListActivity");
        pupInt.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        pupInt.putExtra("notification", R.string.table_ready);
        pupInt.putExtra("title", title);
        pupInt.putExtra("customdata", body);
        pupInt.putExtra("Kerb.RestaurantID", restaurantId);
        pupInt.putExtra("Kerb.Restaurant", title.replace("Kerb @", ""));
        context.getApplicationContext().startActivity(pupInt);
    }

}
