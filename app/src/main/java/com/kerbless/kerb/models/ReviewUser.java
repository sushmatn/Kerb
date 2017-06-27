package com.kerbless.kerb.models;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by SushmaNayak on 10/29/2015.
 */
public class ReviewUser implements Parcelable {
    public String author;
    public int rating;
    public String review;

    public static ReviewUser fromJSON(JSONObject jsonObject) {
        ReviewUser user = new ReviewUser();
        try {
            user.author = jsonObject.getString("author_name");
            user.rating = jsonObject.getInt("rating");
            user.review = jsonObject.getString("text");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return user;
    }

    public static ArrayList<ReviewUser> fromJSONArray(JSONArray jsonArray) {
        ArrayList<ReviewUser> users = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                users.add(i, fromJSON(jsonArray.getJSONObject(i)));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return users;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.author);
        dest.writeInt(this.rating);
        dest.writeString(this.review);
    }

    public ReviewUser() {
    }

    private ReviewUser(Parcel in) {
        this.author = in.readString();
        this.rating = in.readInt();
        this.review = in.readString();
    }

    public static final Creator<ReviewUser> CREATOR = new Creator<ReviewUser>() {
        public ReviewUser createFromParcel(Parcel source) {
            return new ReviewUser(source);
        }

        public ReviewUser[] newArray(int size) {
            return new ReviewUser[size];
        }
    };
}
