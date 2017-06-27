package com.kerbless.kerb.fragments;


import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.kerbless.kerb.R;
import com.kerbless.kerb.activities.UserWaitListActivity;
import com.kerbless.kerb.models.Listing;
import com.kerbless.kerb.utils.ListingUtils;
import com.kerbless.kerb.utils.WaitlistUtils;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SendCallback;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class JoinWaitListFragment extends DialogFragment {

    private final static String TAG = JoinWaitListFragment.class.getName();
    private Listing listing;
    private static final String CURRENT_LISTING = "Kerb.CurrentListing";

    ImageView ivRestaurantImage;
    TextView tvName;
    TextView tvAddress;
    Button btnJoin;
    Toolbar mToolbar;
    ImageView ivClose;
    EditText etName;
    TextView etNumber;
    ImageView ivMinus;
    ImageView ivAdd;
    EditText etNotes;

    public interface OnJoinWaitListListener {
        public void onJoinWaitListClicked(String listingId, int partySize, String customerName, String notes);
    }

    public JoinWaitListFragment() {
        // Required empty public constructor
    }

    public static JoinWaitListFragment newInstance(Listing listing) {
        JoinWaitListFragment fragment = new JoinWaitListFragment();
        Bundle args = new Bundle();
        args.putParcelable(CURRENT_LISTING, listing);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Set the width and height of the dialog
     */
    @Override
    public void onResume() {
        ViewGroup.LayoutParams params = getDialog().getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        getDialog().getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);

        super.onResume();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow()
                .getAttributes().windowAnimations = R.style.DialogAnimation;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);

        // request a window without the title
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_join_waitlist, container, false);
        listing = getArguments().getParcelable(CURRENT_LISTING);

        mToolbar = (Toolbar) view.findViewById(R.id.toolbar);
        ivRestaurantImage = (ImageView) view.findViewById(R.id.restaurantImage);
        tvName = (TextView) view.findViewById(R.id.tvName);
        tvAddress = (TextView) view.findViewById(R.id.tvAddress);
        ivClose = (ImageView) view.findViewById(R.id.ivClose);
        btnJoin = (Button) view.findViewById(R.id.btnJoin);
        etName = (EditText) view.findViewById(R.id.etName);
        etNumber = (TextView) view.findViewById(R.id.etNumber);
        ivMinus = (ImageView) view.findViewById(R.id.ivMinus);
        ivAdd = (ImageView) view.findViewById(R.id.ivAdd);
        etNotes = (EditText) view.findViewById(R.id.etNotes);

        ivMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int partySize = Integer.parseInt(etNumber.getText().toString());
                if (partySize > 1) {
                    updateButtonEnabled(partySize, false);
                }
            }
        });

        ivAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int partySize = Integer.parseInt(etNumber.getText().toString());
                if (partySize < 10) {
                    updateButtonEnabled(partySize, true);
                }
            }
        });

        ivClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        if (listing.getRestaurantName().contains("Cebicheria"))
            Picasso.with(getActivity()).load(R.drawable.lobster).into(ivRestaurantImage);
        else
            Picasso.with(getActivity()).load(listing.getPhotoUrl()).into(ivRestaurantImage);
        tvName.setText(listing.getRestaurantName());
        tvAddress.setText(listing.getAddress());

        btnJoin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int partySize = 0;
                try {
                    partySize = Integer.parseInt(etNumber.getText().toString());
                } catch (Exception e) {
                    Toast.makeText(getActivity(), "Party size must be a number", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (partySize > 0) {
                    ((OnJoinWaitListListener) getActivity()).onJoinWaitListClicked(listing.getListingId(), partySize, etName.getText().toString(), etNotes.getText().toString());
                    dismiss();

                    /*if (WaitlistUtils.joinList(listing.getListingId(), ParseUser.getCurrentUser().getObjectId(), partySize, fname, lname, etNotes.getText().toString())) {
                        // Sends push message to restaurant
                        sendGCMNotification(listing.getListingId());

                        // Save the restaurantId and status
                        ListingUtils.joinedRestaurantID = listing.getListingId();
                        ListingUtils.customerOnWaitList = true;

                        Intent intent = new Intent(getContext(), UserWaitListActivity.class);
                        intent.putExtra("Kerb.Restaurant", listing.getRestaurantName());
                        intent.putExtra("Kerb.RestaurantID", listing.getListingId());
                        intent.putExtra("Kerb.PartySize", partySize);
                        intent.putExtra("Kerb.UserName", etName.getText().toString());
                        startActivity(intent);

                    } else
                        Toast.makeText(getActivity(), "Could not join the waitlist. Please see the hostess", Toast.LENGTH_SHORT).show();
                    dismiss();*/
                } else {
                    Toast.makeText(getActivity(), "Party size must be greater than 0", Toast.LENGTH_SHORT).show();
                }
            }
        });

        etName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 1) {
                    btnJoin.setEnabled(true);
                } else {
                    btnJoin.setEnabled(false);
                }
            }
        });
        return view;
    }

    private void updateButtonEnabled(int partySize, boolean bAdd) {
        partySize = bAdd ? partySize + 1 : partySize - 1;
        etNumber.setText(Integer.toString(partySize));
        if (partySize == 1) {
            ivMinus.setEnabled(false);
            ivMinus.setImageResource(R.drawable.ic_minus_gray);
        } else if (partySize == 10) {
            ivAdd.setEnabled(false);
            ivAdd.setImageResource(R.drawable.ic_add_gray);
        } else {
            ivAdd.setEnabled(true);
            ivMinus.setEnabled(true);
            ivAdd.setImageResource(R.drawable.ic_add_black);
            ivMinus.setImageResource(R.drawable.ic_minus_black);
        }
    }
}
