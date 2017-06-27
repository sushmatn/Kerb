package com.kerbless.kerb.models;

import android.util.Log;

import com.parse.LogInCallback;
import com.parse.ParseAnonymousUtils;
import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;

/**
 * Created by rmulla on 10/15/15.
 */

//This model writes to the _User table in Parse
@ParseClassName("User")
public class Customer extends ParseUser {
    //member variables, we might not need these. All set apis save to cloud and get apis get from the cloud
    private String customerId;
    private String email;
    private String firstName;
    private String lastName;
    private Number phone;

    //default zero-arg constructor required by parse
    public Customer(){

    }

    public void setFirstName(String firstName){
        put("firstName", firstName);
    }

    public void setLastName(String lastName){
        put("lastName", lastName);
    }

    public void setPhone(Number phone){
        put("phone", phone);
    }

    public void setEmail(String email){
        put("email", email);
    }

    public void setCustomerId(String customerId){
       put("customerId",customerId);
    }

    public String getFirstName(){
        return getString("firstName");
    }
    public String getLastName(){
        return getString("lastName");
    }
    public String getEmail(){
        return getString("email");
    }
    public Number getPhone(){
        return getNumber("phone");
    }
    public String getCustomerId(){
        return getString("customerId");
    }

    //wrapper to set all fields for the Customer
    public void setCustomer(String customerId, String firstName, String lastName, String email, Number phone){
        setCustomerId(customerId);
        setFirstName(firstName);
        setLastName(lastName);
        setEmail(email);
        setPhone(phone);
    }
}
