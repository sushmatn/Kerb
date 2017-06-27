package com.kerbless.kerb.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;

import com.kerbless.kerb.R;
import com.kerbless.kerb.models.Listing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by SushmaNayak on 10/15/2015.
 */
public class ListingUtils {

    public static final String BASE_URL = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?";
    public static final String DETAILS_BASE_URL = "https://maps.googleapis.com/maps/api/place/details/json?";
    public static final String PHOTO_URL = "https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photoreference=";
    public static final String API_KEY = "AIzaSyAhMYLbacq9Ut1TsX9YcUSjTGLSA06lo8E";
    public static Location currentLocation;
    public static String joinedRestaurantID;
    public static boolean customerOnWaitList;
    public static HashMap<String, String> FavRestaurants = new HashMap<>();
    final static String FAV_RESTAURANTSS = "Favorite_Restaurants";

    public interface RefreshListings {
        void RefreshList(boolean bLoadMore);

        void LoadData();
    }

    public static String fetchDetailsURL(String reference) {
        return DETAILS_BASE_URL + "&key=" + API_KEY + "&reference=" + reference;
    }

    public static String fetchURL(Location location, String nextPageToken) {
        if (nextPageToken == null)
            return BASE_URL + "location=" + location.getLatitude() + "," + location.getLongitude() + "&sensor=true&rankby=distance&key=" + API_KEY + "&types=zagat|restaurant";
        else
            return BASE_URL + "location=" + location.getLatitude() + "," + location.getLongitude() + "&sensor=true&rankby=distance&key=" + API_KEY + "&types=zagat|restaurant&pagetoken=" + nextPageToken;
    }

    public static String fetchURL(Location location, String nextPageToken, String name) {
        return fetchURL(location, nextPageToken) + "&keyword=" + name;
    }

    public static String getPhotoURL(String photoRef) {
        return PHOTO_URL + photoRef + "&key=" + API_KEY;
    }

    public static void LoadFavorites(Context context) {
        // Get the favorite movie Ids in SharedPreferences
        SharedPreferences prefs = context.getSharedPreferences(FAV_RESTAURANTSS, 0);
        for (Map.Entry entry : prefs.getAll().entrySet())
            FavRestaurants.put(entry.getKey().toString(), entry.getValue().toString());
    }

    public static void SaveFavorites(Context context) {
        SharedPreferences.Editor editor = context.getSharedPreferences(FAV_RESTAURANTSS, 0).edit();
        editor.clear();
        for (Map.Entry entry : FavRestaurants.entrySet())
            editor.putString(entry.getKey().toString(), entry.getValue().toString());
        editor.commit();
    }

    public static int getDrawable(int position) {
        int rem = position % 27;
        switch (rem) {
            case 1:
                return R.drawable.img1;
            case 2:
                return R.drawable.img2;
            case 3:
                return R.drawable.img3;
            case 4:
                return R.drawable.img4;
            case 5:
                return R.drawable.img5;
            case 6:
                return R.drawable.img6;
            case 7:
                return R.drawable.img7;
            case 8:
                return R.drawable.img8;
            case 9:
                return R.drawable.img9;
            case 10:
                return R.drawable.img10;
            case 11:
                return R.drawable.img11;
            case 12:
                return R.drawable.img12;
            case 13:
                return R.drawable.img13;
            case 14:
                return R.drawable.img14;
            case 15:
                return R.drawable.img15;
            case 16:
                return R.drawable.img16;
            case 17:
                return R.drawable.img17;
            case 18:
                return R.drawable.img18;
            case 19:
                return R.drawable.img19;
            case 20:
                return R.drawable.img20;
            case 21:
                return R.drawable.seafood1;
            case 22:
                return R.drawable.seafood2;
            case 23:
                return R.drawable.seafood3;
            case 24:
                return R.drawable.seafood4;
            case 25:
                return R.drawable.seafood5;
            case 26:
                return R.drawable.seafood6;
        }
        return R.drawable.img0;
    }
}
