package com.kerbless.kerb.utils;

import android.util.Log;
import android.widget.Toast;
import java.util.Random;

import com.kerbless.kerb.models.Restaurant;
import com.kerbless.kerb.models.WaitLists2;
import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by rmulla on 10/16/15.
 */
public class WaitlistUtils {

    private static String TAG = WaitlistUtils.class.getName();

    /*party_size=0 will return current number on overall waitlist.
      party_size=x will return your number on the waitlist for party_size=x
     */
    public static int getCurrentPositionOnWaitlist(String customerId, String restaurantId, int party_size) {


        int current_position = 0;

        ParseQuery<WaitLists2> query = ParseQuery.getQuery("WaitLists2");
        query.whereEqualTo("restaurantID", restaurantId);
        query.whereEqualTo("status", "waiting");
        if (party_size != 0) {
            query.whereEqualTo("partySize", party_size);
        }
        query.orderByAscending("createdAt");
        try {
            //list array
            List<WaitLists2> list = query.find();
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).getCustomerID().equals(customerId)) {
                    current_position = i + 1;
                    break;
                }
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return current_position;
    }


    /*
        same as func below , but without UTC cnversions, seems to work for me

     */

    public static int getCustomerAlreadyWaitedTime(String restaurantId, String customerId){
        long alreadyWaited = 0;
        ParseQuery<WaitLists2> query = ParseQuery.getQuery("WaitLists2");
        query.whereEqualTo("restaurantID", restaurantId);
        query.whereEqualTo("customerId", customerId);
        query.whereEqualTo("status","waiting");
        try {
            List<WaitLists2> list = query.find();
            for (int i = 0; i < list.size(); i++) {
                //calculated time Already waited
                alreadyWaited = (new Date().getTime() - list.get(i).getCreatedAt().getTime()) / (1000 * 60);
                break;
            }
        } catch (ParseException e) {
            e.printStackTrace();

        }
        return (int) alreadyWaited;
    }
    /*
        returns the waitTime user has already been waiting on the waitList
     */
    public static int getCustomerWaitTime(String restaurantId, String customerId) {
        long alreadyWaited = 0;
        ParseQuery<WaitLists2> query = ParseQuery.getQuery("WaitLists2");
        query.whereEqualTo("restaurantID", restaurantId);
        query.whereEqualTo("customerId", customerId);
        try {
            List<WaitLists2> list = query.find();
            for (int i = 0; i < list.size(); i++) {
                //calculated time Already waited
                String format = "yyyy/MM/dd HH:mm:ss";
                SimpleDateFormat sdf = new SimpleDateFormat(format);
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                Date gmtTime = new Date(sdf.format(list.get(i).getCreatedAt().getTime()));
                long ts = System.currentTimeMillis();
                Date localTime = new Date(ts);
                Date fromParseGmt = new Date(gmtTime.getTime() + TimeZone.getDefault().getOffset(localTime.getTime()));
                alreadyWaited = (localTime.getTime() - fromParseGmt.getTime());
                break;
            }
        } catch (ParseException e) {
            e.printStackTrace();

        }
        return (int) alreadyWaited;
    }


    /*
       returns the waitTime predicted for this customer when the customer joins. after another customer is seated this will get u the new estimate
    */
    public static int getPredictedWaitTime(String restaurantId, String customerId) {
        ParseQuery<WaitLists2> query = ParseQuery.getQuery("WaitLists2");
        query.whereEqualTo("restaurantID", restaurantId);
        query.whereEqualTo("customerId", customerId);
        int predictedWaitTime = 0;
        try {

            List<WaitLists2> list = query.find();
            for (int i = 0; i < list.size(); i++) {
                //calculated time Already waited
                predictedWaitTime = list.get(i).getEstimatedWaitTime();
                break;
            }
        } catch (ParseException e) {
            e.printStackTrace();

        }

        return predictedWaitTime;
    }

    /*
        if party_size = 0 return average wait_time for all party_sizes in the queue
        If no one is waiting but waitlist is open, this will return the restaurant default wait time.
        if restaurant waitlist is not open, return 0 .
        if restaurant is not in our parse table, return random number

     */
    public static int getWaitTime(String restaurantId, int party_size) {

        int calculatedWaitTime = 0;
        ParseQuery<WaitLists2> query = ParseQuery.getQuery("WaitLists2");
        query.whereEqualTo("restaurantID", restaurantId);
        if (party_size != 0) {
            query.whereEqualTo("partySize", party_size);
        }
        try {
            List<WaitLists2> list = query.find();
            for (int i = 0; i < list.size(); i++) {
                calculatedWaitTime += list.get(i).getEstimatedWaitTime();
            }
            if (list.size() > 0)
                calculatedWaitTime = calculatedWaitTime / list.size();
            else{
                //this means the query did not return any results, ==> restaurant NOT in waitlist table==>waitlist is not open
                //return 0.
                //for restaurants not in our parse table, return a random value
                Random randomGenerator = new Random();
                calculatedWaitTime = randomGenerator.nextInt(75) + 1;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return calculatedWaitTime;
    }

    /*returns waitlist for restaurant with restaurantID provided. returns overall waitlist in correct order
      returns an array of CustomerIDs. returns only customers with status=waiting
     */
    public static ArrayList<String> getMyCustomerArray(String restaurantId) {
        ArrayList<String> customerArray = new ArrayList<>();
        ParseQuery<WaitLists2> query = ParseQuery.getQuery("WaitLists2");
        query.whereEqualTo("restaurantID", restaurantId);
        query.whereEqualTo("status", "waiting");
        query.whereExists("customerId");

        query.orderByAscending("createdAt");
        try {
            List<WaitLists2> list = query.find();
            for (int i = 0; i < list.size(); i++) {
                customerArray.add(list.get(i).getCustomerID());
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return customerArray;
    }

    /*
        Returns size of current waitlist for the restaurant for the provided partySize
     */
    public static int getCurrentWaitListSize(String restaurantId, int partySize) {
        int waitlistSize = 0;
        ArrayList<String> customerArray = new ArrayList<>();
        ParseQuery<WaitLists2> query = ParseQuery.getQuery("WaitLists2");
        query.whereEqualTo("restaurantID", restaurantId);
        query.whereExists("customerId");
        query.whereEqualTo("status", "waiting");
        if(partySize > 0) {
            query.whereEqualTo("partySize", partySize);
        }

        try {
            List<WaitLists2> list = query.find();
            waitlistSize = list.size();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return waitlistSize;
    }

    /**
     * @param restaurantId
     * @return
     * @user alinc
     * Get list of customers for a restaurant - Used in RestaurantWaitList Adapter
     */
    public static List<WaitLists2> getMyCustomerList(String restaurantId) {
        ParseQuery<WaitLists2> query = ParseQuery.getQuery("WaitLists2");
        query.whereEqualTo("restaurantID", restaurantId);
        query.whereEqualTo("status", "waiting");
        query.orderByDescending("status");
        query.addAscendingOrder("createdAt");
        List<WaitLists2> list;

        try {
            list = query.find();
            return list;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @param customerId
     * @return
     * @user alinc
     * get waitlists2 object for a given customer - Used in RestaurantWaitListFragment when a new customer joins
     */
    public static List<WaitLists2> getCustomerFromList(String customerId) {
        ParseQuery<WaitLists2> query = ParseQuery.getQuery("WaitLists2");
        query.whereEqualTo("customerId", customerId);
        query.whereEqualTo("status", "waiting");
        List<WaitLists2> list;
        try {
            list = query.find();
            return list;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    /*returns waitlist of size party_size for restaurant with restaurantID provided. returns  waitlist in correct order
      returns an array of CustomerIDs
     */

    public static ArrayList<String> getMyCustomerArray(String restaurantId, int partySize) {
        ArrayList<String> customerArray = new ArrayList<>();
        ParseQuery<WaitLists2> query = ParseQuery.getQuery("WaitLists2");
        query.whereEqualTo("restaurantID", restaurantId);
        query.whereEqualTo("partySize", partySize);
        query.orderByAscending("createdAt");
        try {
            List<WaitLists2> list = query.find();
            for (int i = 0; i < list.size(); i++) {
                customerArray.add(list.get(i).getCustomerID());
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return customerArray;
    }


    /*
     Function returns all the information about a Customer. Queries User Table.
     Function also returns all waitlist related information for this customer for restauranID provided
     returns a Hashmap
     {
     "username":"abc",
     "email":"email",
     "firstName":"name",
     "lastName":"last",
     "phone":"100",
     "estimatedWaitTime":"30"
     "notified":"y",
     "partySize":"4",
     "status":"waiting"
     "createdAt":"Date.toString"
     "restaurantName":"myrestaurant"
     }
     */
    public static HashMap<String, String> getCustomerDetails(String customerId, String restaurantId) {
        HashMap<String, String> customerDetailsMap = new HashMap<>();
        //ParseQuery<ParseUser> query = ParseQuery.getQuery("_User");
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereEqualTo("objectId", customerId);
        try {
            ParseUser customer = query.getFirst();
            if(customer.getUsername()!=null){
            customerDetailsMap.put("username", customer.getUsername());
            }
            if(customer.get("firstName")!=null)
            customerDetailsMap.put("firstName", (String) customer.get("firstName"));

            if(customer.get("lastName")!=null)
            customerDetailsMap.put("lastName", (String) customer.get("lastName"));

            if(customer.get("phone")!=null)
            customerDetailsMap.put("phone", Integer.toString((int) customer.get("phone")));

        } catch (ParseException e) {
            e.printStackTrace();
        }

        ParseQuery<WaitLists2> queryWaitlist = ParseQuery.getQuery("WaitLists2");
        query.whereEqualTo("restaurantID", restaurantId);
        query.whereEqualTo("customerId", customerId);
        try {
            WaitLists2 wl = queryWaitlist.getFirst();
            customerDetailsMap.put("estimatedWaitTime", Integer.toString(wl.getEstimatedWaitTime()));
            customerDetailsMap.put("notified", wl.getNotified());
            customerDetailsMap.put("partySize", Integer.toString(wl.getPartySize()));
            customerDetailsMap.put("status", wl.getStatus());
            customerDetailsMap.put("createdAt", wl.getCreatedAt().toString());
            customerDetailsMap.put("specialRequest", wl.getSpecialRequest());
        } catch (ParseException e) {
            e.printStackTrace();
        }

        ParseQuery<Restaurant> rquery = ParseQuery.getQuery("Restaurants");
        rquery.whereEqualTo("restaurantID", restaurantId);
        try {
            Restaurant r = rquery.getFirst();
            if (r.getName()!=null){
                customerDetailsMap.put("restaurantName", r.getName().toString());
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return customerDetailsMap;
    }

    /*
        getCustomer returns a ParseUser object using the customer id.
     */
    public static ParseUser getCustomer(String customerId) {
        ParseUser customer = new ParseUser();
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereEqualTo("objectId", customerId);
        try {
            customer = query.getFirst();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return customer;
    }

    /*
        updateNotification will set 'notified' column in WaitLists2 table to Y
     */
    public static void updateNotification(String objectId) {

        if (objectId != null) {
            ParseObject point = ParseObject.createWithoutData("WaitLists2", objectId);
            point.put("notified", "Y");
            point.saveInBackground(new SaveCallback() {
                public void done(ParseException e) {
                    if (e == null) {
                        Log.d(TAG, "Notification update succeeded.");
                    } else {
                        Log.d(TAG, "Notification update failed.");
                        e.printStackTrace();
                    }
                }
            });
        } else {
            Log.d(TAG, "ERROR: received null objectId! ");
        }
    }


    /*
        Api opens the restaurant waitlist
        Creates a row in the Waitlist table with only the restaurantID populated. All other fields are not set.
        Also deletes all previous 'restaurantID' entries in the waitlist table in case restaurant forgot to closeWaitList
        TODO: entries that are closed should be move to archive!
     */
    public static Boolean openWaitList(String restaurantID, int defaultWaitTime){
        //closing previous waitList for this restaurant--deleting entires from waitlist table
        closeWaitList(restaurantID);
        WaitLists2 newWaitListOpenEntry = new WaitLists2();
        newWaitListOpenEntry.setRestaurantId(restaurantID);
        newWaitListOpenEntry.setStatus("open");
        newWaitListOpenEntry.setEstimatedWaitTime(defaultWaitTime);
        try {
            newWaitListOpenEntry.save();
            //updateDefaultWaitTime(restaurantID, defaultWaitTime);
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public static void updateDefaultWaitTime(String restaurantID, int defaultWaitTime) {

        if(restaurantID != null) {
            ParseObject point = ParseObject.createWithoutData("Restaurants", getRestaurantObjectId(restaurantID));
            point.put("waitTime", defaultWaitTime);
            point.saveInBackground(new SaveCallback() {
                public void done(ParseException e) {
                    if (e == null) {
                    } else {
                        Log.d(TAG, "Default wait time update failed.");
                        e.printStackTrace();
                    }
                }
            });
        }
        else { Log.d(TAG, "ERROR: received null objectId! "); }

    }


    /*
        Api closes the restaurant waitlist
        The row in the waitlist table that has only restaurantID populated will be deleted.
        Delete all rows that have this restaurantID
     */
    public static Boolean closeWaitList(String restaurantID) {

        ParseQuery<WaitLists2> query = ParseQuery.getQuery("WaitLists2");
        query.whereEqualTo("restaurantID", restaurantID);
        try {
            List<WaitLists2> list = query.find();
            for (int i = 0; i < list.size(); i++) {
                list.get(i).delete();
            }
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }


    /*
        Api sends push notification to the customer.
        TODO-sendpush --> this may not be needed. TO BE REMOVED. See updateNotification(String objectId) method above.
        Api also updates the waitlist table entry column 'notified' to 'Y'
     */
    public static Boolean notifyCustomer(String customerID, String restaurantID) {
        ParseQuery<WaitLists2> query = ParseQuery.getQuery("WaitLists2");
        query.whereEqualTo("restaurantID", restaurantID);
        query.whereEqualTo("customerId", customerID);
        query.whereEqualTo("status","waiting");
        try {
            List<WaitLists2> list = query.find();
            for (int i = 0; i < list.size(); i++) {
                //TODO-add logic to send push notification using parse
                list.get(i).setNotified("Y");
                list.get(i).save();
            }
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }

        return true;

    }

    /*
        Api updates the status for this customer in the waitlist table
        If the status == 'seated', calculate the total wait time = currentTime - createdAt &
        call function to update the new waitTime for all existing customers with that party size.

     */
    public static void updateCustomerStatus(String customerId, String restaurantID, String status) {
        ParseQuery<WaitLists2> query = ParseQuery.getQuery("WaitLists2");
        query.whereEqualTo("restaurantID", restaurantID);
        query.whereEqualTo("customerId", customerId);
        query.whereEqualTo("status","waiting");
        int partySize;
        String waitListid;
        long calculatedWaitTime;
        try {
            List<WaitLists2> list = query.find();
            for (int i = 0; i < list.size(); i++) {
                list.get(i).setStatus(status);
                list.get(i).save();
                if (status.equals("seated")) {
                    partySize = list.get(i).getPartySize();
                    waitListid = list.get(i).getObjectId();
                    Date createdAt = list.get(i).getCreatedAt();
                    Date seatedAt = list.get(i).getUpdatedAt();
                    long createdTime = seatedAt.getTime();
                    calculatedWaitTime = (seatedAt.getTime() - createdAt.getTime()) / (1000 * 60);
                    updateCustomerWaitTimes(restaurantID, partySize, (int) calculatedWaitTime);
                    ParseObject insert = new ParseObject("WaitListArchive");
                    insert.put("restaurantId", restaurantID);
                    insert.put("customerId", customerId);
                    insert.put("status", status);
                    insert.put("waitListId", waitListid);
                    insert.saveEventually();
                    //leaveWaitList(restaurantID, customerId);
                }
                //list.get(i).save();
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return;
    }


    /*
        updates the waitTimes for all existing customers of restaurant for this partySize in the waitlist table based on waitTime
        of last seated customer
        partySize 0 will update all customerWaitTimes regardless of partySize

        for customers in waitlist:
           get time already in waitlist for = timeWaited
           calculatedWaitTime = earlierEstimategiven + lastSeatedWaitTime / 2
           updatedeEstimate = calculatedWaitTime - timeWaited

        NOTE:called each time a customer gets seated. DO NOT invoke by itself

     */
    public static void updateCustomerWaitTimes(String restaurantId, int partySize, int lastSeatedWaitTime) {
        ParseQuery<WaitLists2> query = ParseQuery.getQuery("WaitLists2");
        query.whereEqualTo("restaurantID", restaurantId);
        //dont want to consider already seated party for updates
        query.whereNotEqualTo("status", "seated");
        query.whereEqualTo("notified","N");
        if (partySize != 0) {
            query.whereEqualTo("partySize", partySize);
        }
        try {
            List<WaitLists2> list = query.find();
            for (int i = 0; i < list.size(); i++) {
                //calculated time Already waited
                long oldEstimatedTime = list.get(i).getEstimatedWaitTime();
                long calculatedWaitTime = (int) (oldEstimatedTime + lastSeatedWaitTime) / 2;
                long alreadyWaited = (new Date().getTime() - list.get(i).getCreatedAt().getTime()) / (1000 * 60);
                //very arbitrary else clause
                int updatedEstimate = (int) ((calculatedWaitTime - alreadyWaited) > 0 ? calculatedWaitTime - alreadyWaited : alreadyWaited - calculatedWaitTime);
                //set new estimatedTime
                list.get(i).setEstimatedWaitTime(updatedEstimate);
                list.get(i).save();
            }
        } catch (ParseException e) {
            e.printStackTrace();

        }


        return;
    }


    /*
        Adds customer to the waitlist for retaurantId. sets isNotified ="N" since ur adding for first time
        sets  status="waiting" since u are adding to list for the first time.

        NOTE:when u join the waitList, the waitTime added for you will be :
        1. if u are the 1st person on waitlist(for that party_size), it will be pulled in from the Restaurants db
        2. if u are 2 or beyond 2nd on the waitlist, your waitlist will be calculated based on others ahead

        returns True if customer is added to waitlist.
        returns False : if waitlist is not open or an error.
     */
    public static boolean joinList(String restaurantId, String customerId, int partySize, String firstName, String lastName, String etNotes) {
        if (isWaitListOpen(restaurantId)) {

            //update User table with this firstName and lastName

            ParseQuery<ParseUser> cquery = ParseUser.getQuery();
            cquery.whereEqualTo("objectId", customerId);
            try {
                ParseUser customer = cquery.getFirst();
                customer.put("firstName",firstName);
                customer.put("lastName", lastName);
                customer.save();

            } catch (ParseException e) {
                e.printStackTrace();
            }


            WaitLists2 newEntry = new WaitLists2();
            int waitlistSize = WaitlistUtils.getCurrentWaitListSize(restaurantId, partySize);
            int estimatedWaitTime = 0;
            if (waitlistSize == 0) {
                //if this is first customer on waitlist, pull the waitlist from restaurant db
                ParseQuery<Restaurant> query = ParseQuery.getQuery("Restaurants");
                query.whereEqualTo("restaurantId", restaurantId);
                try {
                    List<Restaurant> list = query.find();
                    estimatedWaitTime = list.get(0).getWaitTime();
                } catch (ParseException e) {
                    e.printStackTrace();
                }

            } else {
                estimatedWaitTime = WaitlistUtils.getWaitTime(restaurantId, partySize);
            }

            newEntry.setWaitlist(customerId, restaurantId, partySize, estimatedWaitTime, "N", "waiting", etNotes);
            try {
                newEntry.save();
            } catch (ParseException e) {
                e.printStackTrace();
                return false;
            }

        } else {
            return false;
        }

        return true;
    }

    /**
     * DEMO data loader
     * @param restaurantId
     * @return
     */
    public static boolean loadDemoList(String restaurantId, String customerId, int partySize, String notes) {
        if (isWaitListOpen(restaurantId)) {
            WaitLists2 newEntry = new WaitLists2();
            int waitlistSize = WaitlistUtils.getCurrentWaitListSize(restaurantId, partySize);
            int estimatedWaitTime = 20;
            if (waitlistSize == 0) {
                //if this is first customer on waitlist, pull the waitlist from restaurant db
                ParseQuery<Restaurant> query = ParseQuery.getQuery("Restaurants");
                query.whereEqualTo("restaurantId", restaurantId);
                try {
                    List<Restaurant> list = query.find();
                    estimatedWaitTime = list.get(0).getWaitTime();
                } catch (ParseException e) {
                    e.printStackTrace();
                }

            } else {
                estimatedWaitTime = WaitlistUtils.getWaitTime(restaurantId, partySize);
            }

            newEntry.setWaitlist(customerId, restaurantId, partySize, estimatedWaitTime, "N", "waiting", notes);
            try {
                newEntry.save();
            } catch (ParseException e) {
                e.printStackTrace();
                return false;
            }

        } else {
            return false;
        }

        return true;
    }


    /*
        Api returns true if waitlist is open
        Api returns False if waitlist is not open
        Checks for entry in Waitlist table with this restauranId
     */

    public static boolean isWaitListOpen(String restaurantId) {
        ParseQuery<WaitLists2> query = ParseQuery.getQuery("WaitLists2");
        query.clearCachedResult();
        query.setCachePolicy(ParseQuery.CachePolicy.IGNORE_CACHE);
        query.whereEqualTo("restaurantID", restaurantId);
        query.whereEqualTo("status", "open");
        try {
            return query.getFirst().getObjectId().length() > 5;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static String getRestaurantName(String restaurantId) {
        ParseQuery<Restaurant> query = ParseQuery.getQuery("Restaurants");
        query.whereEqualTo("restaurantId", restaurantId);
        String name = "";
        try {
            name = query.getFirst().getName();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return name;
    }



    public static Number getRestaurantDefaultWaitTime(String restaurantId) {
        ParseQuery<Restaurant> query = ParseQuery.getQuery("Restaurants");
        query.whereEqualTo("restaurantId", restaurantId);
        Number waitTime = 25;
        try {
            waitTime = query.getFirst().getWaitTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return waitTime;
    }


    public static String getRestaurantLogoURL(String restaurantId) {
        ParseQuery<Restaurant> query = ParseQuery.getQuery("Restaurants");
        query.whereEqualTo("restaurantId", restaurantId);
        String url = "";
        try {
            url = query.getFirst().getPhotoURL();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return url;
    }

    /*
        Api used by UserWaitList Activity to find out the partySize this user is waiting for
        If the customer is not on the waitlist for this restaurant, returned partySize=0
     */
    public static int getPartySize(String restaurantId, String customerId) {
        ParseQuery<WaitLists2> query = ParseQuery.getQuery("WaitLists2");
        query.whereEqualTo("restaurantID", restaurantId);
        query.whereEqualTo("customerId", customerId);
        int partySize = 0;
        try {
            List<WaitLists2> list = query.find();
            if (list.size() > 0) {
                partySize = list.get(0).getPartySize();
            }

        } catch (ParseException e) {
            e.printStackTrace();

        }

        return partySize;

    }

    /*
        Remove the entry for this user from waitlist table
     */
    public static void leaveWaitList(String restaurantId, String customerId) {
        ParseQuery<WaitLists2> query = ParseQuery.getQuery("WaitLists2");
        query.whereEqualTo("restaurantID", restaurantId);
        query.whereEqualTo("customerId", customerId);
        try {
            List<WaitLists2> list = query.find();
            if (list.size() > 0) {
                list.get(0).delete();
            }

        } catch (ParseException e) {
            Log.d(TAG, "leaveWaitList Exception:");
            e.printStackTrace();

        }
    }

    /*
        Api used by Customer App to find out if he/she is already on the waitlist of ANY restaurant, also check for status='waiting'.
     */
    public static boolean isCustomerOnWaitList(String customerId){
        ParseQuery<WaitLists2> query = ParseQuery.getQuery("WaitLists2");
        query.whereEqualTo("customerId", customerId);
        query.whereEqualTo("status", "waiting");
        try {
            List<WaitLists2> list = query.find();
            if(list.size()>0){
                return true;
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static String getRestaurantObjectId (String restaurantId) {
        ParseQuery<Restaurant> query = ParseQuery.getQuery("Restaurants");
        query.whereEqualTo("restaurantId", restaurantId);
        String objectId = "";
        try {
            objectId = query.getFirst().getObjectId();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return objectId;
    }

    /*
        returns true if restaurant is found in parse DB.
     */
    public static boolean isRestaurantInDb (String restaurantId) {
        boolean restaurantInDb = false;
        ParseQuery<Restaurant> query = ParseQuery.getQuery("Restaurants");
        query.whereEqualTo("restaurantId", restaurantId);

        try {
            ParseObject restaurant = query.getFirst();
            if(restaurant == null)
                restaurantInDb = false;
            else
                restaurantInDb = true;
        } catch (ParseException e) {
            e.printStackTrace();
            return restaurantInDb;

        }

        return restaurantInDb;
    }

    public static boolean isCustomerNotified(String customerId) {
        ParseQuery<WaitLists2> query = ParseQuery.getQuery("WaitLists2");
        query.whereEqualTo("customerId", customerId);
        query.whereEqualTo("notified", "Y");
        try {
            if (query.count() > 0)
                return true;

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return false;
    }
    /*
        Api used by customer app to find out  customer status:
        valid status: waiting, seated
        customer not found in waitlist table: return 'notfound'
     */
    public static String getCustomerStatus(String customerId){
        String status;
        ParseQuery<WaitLists2> query = ParseQuery.getQuery("WaitLists2");
        query.whereEqualTo("customerId", customerId);
        try {
            List<WaitLists2> list = query.find();
            if(list.size()>0){
                status = list.get(0).getStatus();

            }
            else status="notfound";

        } catch (ParseException e) {
            e.printStackTrace();
            status="notfound";
        }
        return status;
    }

    /*
        Api used after using isCustomerOnWaitlist returns true, when u want to find out the restId customer is waiting on
        If customer is not on any waitlist, this will return "notfound"
        NOTE: this queries only customers with status='waiting'
     */
    public static String getRestaurantIdCustomerIsWaitlistedOn(String customerId){
        String restId;
        ParseQuery<WaitLists2> query = ParseQuery.getQuery("WaitLists2");
        query.whereEqualTo("customerId", customerId);
        query.whereEqualTo("status", "waiting");
        try {
            List<WaitLists2> list = query.find();
            if(list.size()>0){
                restId = list.get(0).getRestaurantID();

            }
            else restId="notfound";

        } catch (ParseException e) {
            e.printStackTrace();
            restId="notfound";
        }
        return restId;

    }

    /*
       getUserId, aka ownerId for a given restaturantId
    */
    public static ParseUser getOwnerId(String restaurantId) {
        ParseUser owner = new ParseUser();
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereEqualTo("restaurantId", restaurantId);
        try {
            owner = query.getFirst();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return owner;
    }

    /**
     * Get number of seated customers by restaurant ID. Used for RestaurantMainActivity summary data.
     */
    public static int getSeatedTotal(String restaurantId) {
        //ParseQuery<ParseObject> query = ParseQuery.getQuery("WaitListArchive");
        ParseQuery<WaitLists2> query = ParseQuery.getQuery("WaitLists2");
        query.whereEqualTo("restaurantID", restaurantId);
        query.whereEqualTo("status", "seated");
        query.setCachePolicy(ParseQuery.CachePolicy.IGNORE_CACHE);
        try {
            return query.count();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    //sample usage code to inject into parse
     /* this is how you create a new customer by making him signup
        //create a new customer instance
        final Customer c = new Customer();
        //set values of the columns and push to cloud
        c.setUsername("user4");
        c.setPassword("123");
        c.signUpInBackground(new SignUpCallback() {
            public void done(ParseException e) {
                if (e == null) {
                    // Show a simple Toast message upon successful registration
                    Toast.makeText(getApplicationContext(),
                            "Successfully Signed up, please log in.",
                            Toast.LENGTH_LONG).show();
                    c.setCustomer("1aa", "hillary", "jones", "test@test.com", 100);
                    try {
                        c.save();
                    } catch (ParseException e1) {
                        e1.printStackTrace();
                    }


                } else {
                    Toast.makeText(getApplicationContext(),
                            e.getMessage(), Toast.LENGTH_LONG)
                            .show();
                }
            }
        });*/
    //setCustomerId("1aa");
        /*c.setCustomer("1aa", "hillary", "jones", "test@test.com", 100);
        try {
            c.save();
        } catch (ParseException e) {
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }*/
        /*
        c.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e==null){
                    Toast.makeText(getApplicationContext(), "DONE SAVING", Toast.LENGTH_SHORT).show();
                }
                else{
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }

            }
        });*/

        /*
        //create a new restaurant object
        final Restaurant r = new Restaurant();
        //set key fields and create the row in the cloud
        r.setRestaurant("LIMON4", 37.7681446, -122.4, "www.limon4.com");

        //if i dont want async save
        try {
            r.save();
        } catch (ParseException e) {
            e.printStackTrace();
        }*/
    //async save-background
        /*
        r.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                //in order to access the created row, we need to wait for done to be called.
                if (e == null) {
                    Toast.makeText(getApplicationContext(), "saved successfully" + r.getObjectId(), Toast.LENGTH_SHORT).show();
                    //querying created object once its been created
                    ParseQuery<ParseObject> query = ParseQuery.getQuery("Restaurants");
                    query.getInBackground(r.getObjectId(), new GetCallback<ParseObject>() {

                        public void done(ParseObject object, ParseException e) {
                            if (e == null) {
                                Restaurant oldrow = (Restaurant) object;
                                Toast.makeText(getApplicationContext(), oldrow.getWebsite(), Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                } else {
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
        */

        /*
        WaitLists2 wl1 = new WaitLists2();
        wl1.setWaitlist(ParseUser.getCurrentUser().getObjectId(),"Dh5IU9VTQJ", 4, "N", "waiting");

        try {
            wl1.save();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        */
}
