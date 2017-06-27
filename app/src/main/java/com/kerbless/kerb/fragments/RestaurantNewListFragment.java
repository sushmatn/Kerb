package com.kerbless.kerb.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;

import com.kerbless.kerb.R;
import com.kerbless.kerb.utils.WaitlistUtils;

/**
 * Created by alinc on 10/23/15.
 */
public class RestaurantNewListFragment extends DialogFragment {

    private OnItemSelectedListener myListener;
    private int defaultTime = 10;
    private String TAG = RestaurantNewListFragment.class.getName();

    public interface OnItemSelectedListener {
        void onCreateWaitList();
    }

    public RestaurantNewListFragment() {
    }

    public static RestaurantNewListFragment newInstance(String title, String restaurantID) {
        RestaurantNewListFragment frag = new RestaurantNewListFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putString("restaurantID", restaurantID);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof OnItemSelectedListener) {
            myListener = (OnItemSelectedListener) activity;
        } else {
            throw new ClassCastException(activity.toString()
                    + " must implement RestaurantNewListFragment.OnItemSelectedListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.new_waitlist_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Get field from view
        final EditText etDefaultWaitTime = (EditText) view.findViewById(R.id.etDefaultWaitTime);
        // Fetch arguments from bundle and set title
        String title = getArguments().getString("title", "Create new Wait List");
        getDialog().setTitle(title);
        // Show soft keyboard automatically and request focus to field
        etDefaultWaitTime.requestFocus();
        getDialog().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        /*Button btnCreateList = (Button) view.findViewById(R.id.btnCreateList);
        btnCreateList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String min = etDefaultWaitTime.getText().toString();
                if(min != null && !min.isEmpty()) {
                    defaultTime = Integer.valueOf(min);
                }
                WaitlistUtils.openWaitList(getArguments().getString("restaurantID"), defaultTime);
                myListener.onCreateWaitList();
                dismiss();
            }
        });*/
    }

}
