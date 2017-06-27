package com.kerbless.kerb.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.kerbless.kerb.utils.WaitlistUtils;
import com.parse.ParseClassName;
import com.parse.ParseObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.Map;

/**
 * Created by rmulla on 10/16/15.
 */
@ParseClassName("WaitLists2")
public class WaitLists2 extends ParseObject implements Parcelable {

    String restaurantID;
    String customerID;
    String objectID;
    int partySize;
    int timer;
    String status;
    int estimatedWaitTime;
    String notified;
    String firstName;
    String lastName;
    String specialRequest;

    public void setEstimatedWaitTime(int waitTime) {
        put("estimatedWaitTime", waitTime);
    }

    //column in parse is type String.
    public void setNotified(String isNotified) {
        put("notified", isNotified);
    }

    public void setPartySize(int partySize) {
        put("partySize", partySize);
    }

    public void setRestaurantId(String restaurantId) {
        put("restaurantID", restaurantId);
    }

    public void setCustomerId(String customerId) {
        put("customerId", customerId);
    }

    public void setStatus(String status) {
        put("status", status);
    }

    public Date getAddedtoWaitlistTime() {
        return getDate("createdAt");
    }

    public Date getUpdatedWaitlistTime() {
        return getDate("updatedAt");
    }

    public String getCustomerID() {
        return customerID = getString("customerId");
    }

    public String getRestaurantID() {
        return restaurantID = getString("restaurantID");
    }

    public String getNotified() {
        return notified = getString("notified");
    }

    public String getStatus() {
        return status = getString("status");
    }

    public String getFirstName() {
        return firstName = WaitlistUtils.getCustomer(getCustomerID()).getString("firstName");
    }

    public String getLastName() {
        return lastName = WaitlistUtils.getCustomer(getCustomerID()).getString("lastName");
    }

    public int getPartySize() {
        int partySize = 2;
        try {
            partySize = getNumber("partySize").intValue();
        } catch (Exception e) {

        }
        return partySize;
    }

    public int getTimer() {
        return timer = 0; //TO DO: now - getDate("createdAt")
    }

    public int getEstimatedWaitTime() {
        return estimatedWaitTime = getInt("estimatedWaitTime");
    }

    public String getObjectID() {
        return objectID = getString("objectId");
    }

    public void setWaitlist(String customer, String restaurant, int partySize, int estimatedWaitTime, String isNotified, String status, String notes) {
        setCustomerId(customer);
        setRestaurantId(restaurant);
        setPartySize(partySize);
        setEstimatedWaitTime(estimatedWaitTime);
        setNotified(isNotified);
        setStatus(status);
        setSpecialRequest(notes);
    }


    public WaitLists2() {
    }


    public void setSpecialRequest(String specialRequest) {
        put("specialRequest", specialRequest);
    }

    public String getSpecialRequest() {
        return specialRequest = getString("specialRequest");
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.restaurantID);
        dest.writeString(this.customerID);
        dest.writeString(this.objectID);
        dest.writeInt(this.partySize);
        dest.writeInt(this.timer);
        dest.writeString(this.status);
        dest.writeInt(this.estimatedWaitTime);
        dest.writeString(this.notified);
        dest.writeString(this.firstName);
        dest.writeString(this.lastName);
        dest.writeString(this.specialRequest);
    }

    private WaitLists2(Parcel in) {
        this.restaurantID = in.readString();
        this.customerID = in.readString();
        this.objectID = in.readString();
        this.partySize = in.readInt();
        this.timer = in.readInt();
        this.status = in.readString();
        this.estimatedWaitTime = in.readInt();
        this.notified = in.readString();
        this.firstName = in.readString();
        this.lastName = in.readString();
        this.specialRequest = in.readString();
    }

    public static final Creator<WaitLists2> CREATOR = new Creator<WaitLists2>() {
        public WaitLists2 createFromParcel(Parcel source) {
            return new WaitLists2(source);
        }

        public WaitLists2[] newArray(int size) {
            return new WaitLists2[size];
        }
    };
}
