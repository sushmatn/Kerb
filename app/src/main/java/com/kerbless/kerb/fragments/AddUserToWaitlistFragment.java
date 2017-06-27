package com.kerbless.kerb.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import com.kerbless.kerb.R;
import com.kerbless.kerb.utils.WaitlistUtils;

/**
 * Created by rmulla on 10/31/15.
 */
import android.support.v4.app.DialogFragment;
import android.widget.Toast;
// ...

public class AddUserToWaitlistFragment extends DialogFragment {

    private EditText etFirstName;
    private EditText etLastName;
    private EditText etPartySize;
    private EditText etPhone;
    private Button btnAdd;
    private Button btnCancel;
    String restID;

    public AddUserToWaitlistFragment() {
        // Empty constructor is required for DialogFragment
        // Make sure not to add arguments to the constructor
        // Use `newInstance` instead as shown below
    }

    public static AddUserToWaitlistFragment newInstance(String title, String restID) {
        AddUserToWaitlistFragment frag = new AddUserToWaitlistFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putString("restaurantID", restID);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.add_user_to_waitlist_fragment, container);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Get field from view
        etFirstName = (EditText)view.findViewById(R.id.etFirstName);
        etLastName = (EditText)view.findViewById(R.id.etLastName);
        etPartySize = (EditText)view.findViewById(R.id.etPartySize);
        etPhone = (EditText)view.findViewById(R.id.etPhone);
        btnAdd = (Button)view.findViewById(R.id.btnAdd);
        btnCancel= (Button)view.findViewById(R.id.btnCancel);


        // Fetch arguments from bundle and set title
        String title = getArguments().getString("title", "Enter Name");
        restID = getArguments().getString("restaurantID");
        getDialog().setTitle(title);
        getDialog().getWindow().setTitleColor(getResources().getColor(R.color.colorAccent));
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WaitlistUtils.joinList(restID,etFirstName.getText().toString()+etLastName.getText().toString() ,Integer.parseInt(etPartySize.getText().toString()), etFirstName.getText().toString(), etLastName.getText().toString()," ");
                dismiss();
            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

    }


}