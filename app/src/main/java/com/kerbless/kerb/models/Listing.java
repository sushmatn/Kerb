package com.kerbless.kerb.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.kerbless.kerb.utils.ListingUtils;
import com.kerbless.kerb.utils.WaitlistUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by SushmaNayak on 10/15/2015.
 */
public class Listing implements Parcelable {

    String listingId;
    boolean openNow;
    String restaurantName;
    double latitude;
    double longitude;
    String photoUrl;
    double rating;
    String address;
    int waitTime;
    String reference;
    String phone;
    String hours;
    int priceLevel;
    int totalRatings;
    String website;
    ArrayList<String> photos;
    ArrayList<ReviewUser> reviews;
    boolean isFav = false;
    boolean include = true;
    String jSONString;
    int photoResId = 0;

    public String getListingId() {
        return listingId;
    }

    public boolean isOpenNow() {
        return openNow;
    }

    public String getjSONString() {
        return jSONString;
    }

    public boolean isFavorite() {
        return isFav;
    }

    public void setFavorite(boolean favorite) {
        isFav = favorite;
    }

    public String getPhone() {
        return phone;
    }

    public String getHours() {
        return hours;
    }

    public String getWebsite() {
        return website;
    }

    public int getTotalRatings() {
        return totalRatings;
    }

    public int getPriceLevel() {
        if (priceLevel == 0)
            return 1;
        else
            return priceLevel;
    }

    public ArrayList<ReviewUser> getReviews() {
        return reviews;
    }

    public ArrayList<String> getPhotos() {
        return photos;
    }

    public void setPhotos(ArrayList<String> photoArray) {
        photos.clear();
        photos.addAll(photoArray);
    }

    public String getRestaurantName() {
        return restaurantName;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public int getPhotoResId() {
        return photoResId;
    }

    public void setPhotoResId(int value) {
        photoResId = value;
    }

    public double getRating() {
        return rating;
    }

    public String getAddress() {
        return address;
    }

    public int getWaitTime() {
        return waitTime;
    }

    public String getReference() {
        return reference;
    }

    public static Listing fromJSON(JSONObject jsonObject) {
        final Listing listing = new Listing();
        try {
            listing.jSONString = jsonObject.toString();
            listing.restaurantName = jsonObject.getString("name");
            if (listing.restaurantName.contains("L'Olivier"))
                listing.include = false;
            listing.address = jsonObject.getString("vicinity");
            listing.listingId = jsonObject.getString("id");
            listing.waitTime = WaitlistUtils.getWaitTime(listing.getListingId(), 0);
            listing.latitude = jsonObject.getJSONObject("geometry").getJSONObject("location").getDouble("lat");
            listing.longitude = jsonObject.getJSONObject("geometry").getJSONObject("location").getDouble("lng");
            listing.isFav = ListingUtils.FavRestaurants.containsKey(listing.listingId);
            if (jsonObject.has("opening_hours"))
                listing.openNow = jsonObject.getJSONObject("opening_hours").getBoolean("open_now");
            else
                listing.include = false;
            if (jsonObject.has("rating"))
                listing.rating = jsonObject.getDouble("rating");
            if (jsonObject.has("photos"))
                listing.photoUrl = ListingUtils.getPhotoURL(jsonObject.getJSONArray("photos").getJSONObject(0).getString("photo_reference"));
            if (jsonObject.has("reference"))
                listing.reference = jsonObject.getString("reference");
            if (jsonObject.has("price_level"))
                listing.priceLevel = jsonObject.getInt("price_level");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return listing;
    }

    public static Listing fillDetails(Listing listing, JSONObject jsonObject) {
        try {
            if (jsonObject.has("formatted_phone_number"))
                listing.phone = jsonObject.getString("formatted_phone_number");
            if (jsonObject.has("opening_hours"))
                listing.hours = jsonObject.getJSONObject("opening_hours").getString("weekday_text");
            if (jsonObject.has("reviews"))
                listing.reviews = ReviewUser.fromJSONArray(jsonObject.getJSONArray("reviews"));
            if (jsonObject.has("user_ratings_total"))
                listing.totalRatings = jsonObject.getInt("user_ratings_total");
            else
                listing.totalRatings = 73;
            if (jsonObject.has("photos")) {
                JSONArray photoArray = jsonObject.getJSONArray("photos");
                listing.photos = new ArrayList<>();
                for (int i = 0; i < photoArray.length(); i++) {
                    listing.photos.add(ListingUtils.getPhotoURL(photoArray.getJSONObject(i).getString("photo_reference")));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return listing;
    }

    public static ArrayList<Listing> fromJSON(JSONArray jsonArray) {
        ArrayList<Listing> listings = new ArrayList<>();
        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                Listing listing = fromJSON(jsonArray.getJSONObject(i));
                if (listing.rating != 0 && listing.photoUrl != null && listing.include == true) {
                    listing.photoResId = ListingUtils.getDrawable(listings.size());
                    listings.add(listing);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return listings;
    }

    public Listing() {
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.listingId);
        dest.writeByte(openNow ? (byte) 1 : (byte) 0);
        dest.writeString(this.restaurantName);
        dest.writeDouble(this.latitude);
        dest.writeDouble(this.longitude);
        dest.writeString(this.photoUrl);
        dest.writeDouble(this.rating);
        dest.writeString(this.address);
        dest.writeInt(this.waitTime);
        dest.writeString(this.reference);
        dest.writeInt(this.priceLevel);
        dest.writeByte(isFav ? (byte) 1 : (byte) 0);
        dest.writeInt(this.photoResId);
    }

    private Listing(Parcel in) {
        this.listingId = in.readString();
        this.openNow = in.readByte() != 0;
        this.restaurantName = in.readString();
        this.latitude = in.readDouble();
        this.longitude = in.readDouble();
        this.photoUrl = in.readString();
        this.rating = in.readDouble();
        this.address = in.readString();
        this.waitTime = in.readInt();
        this.reference = in.readString();
        this.priceLevel = in.readInt();
        this.isFav = in.readByte() != 0;
        this.photoResId = in.readInt();
    }

    public static final Creator<Listing> CREATOR = new Creator<Listing>() {
        public Listing createFromParcel(Parcel source) {
            return new Listing(source);
        }

        public Listing[] newArray(int size) {
            return new Listing[size];
        }
    };
}
