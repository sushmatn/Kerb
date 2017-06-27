package com.kerbless.kerb.models;

import com.parse.ParseClassName;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;

/**
 * Created by rmulla on 10/16/15.
 */
@ParseClassName("Restaurants")
public class Restaurant extends ParseObject {

    //def constructor required by Parse
    public Restaurant(){

    }

    public void setRestaurantId(String restaurantId){
        put("restaurantId", restaurantId);
    }
    public String getRestaurantId(){
        return getString("restaurantId");
    }

    public void setLatLng(Double lat, Double lng){
        ParseGeoPoint point = new ParseGeoPoint(lat, lng);
        put("latLng", point);
    }

    public void setName(String name){
        put("name", name);
    }
    public void setWebsite(String website){
        put("webSite", website);
    }
    public void setPhotoURL(String photoURL){
        put("photoURL", photoURL);
    }

    public String getName(){
        return getString("name");
    }
    public String getWebsite(){
        return getString("webSite");
    }
    public String getPhotoURL(){
        return getString("photoURL");
    }


    public ParseGeoPoint getLatLng(){
        return getParseGeoPoint("location");
    }

    public void setWaitTime(int waitTime){
        put("waitTime", waitTime);
    }
    public int getWaitTime(){
        return getInt("waitTime");
    }

    //wrapper to set all key columns in one call
    public void setRestaurant(String restaurantId, String name, Double lat, Double lng, int waitTime, String website){
        setRestaurantId(restaurantId);
        setName(name);
        setWaitTime(waitTime);
        setLatLng(lat, lng);
        setWebsite(website);
    }
}
