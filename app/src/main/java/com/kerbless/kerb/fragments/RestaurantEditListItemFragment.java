package com.kerbless.kerb.fragments;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.kerbless.kerb.R;
import com.kerbless.kerb.utils.WaitlistUtils;

/**
 * Created by rmulla on 10/24/15.
 */
public class RestaurantEditListItemFragment extends DialogFragment {

    TextView tvUser;
    ImageButton btnRemoveFromWaitList;
    private actionListener actionListener;

    public RestaurantEditListItemFragment(){
        this.actionListener = null;
    }

    public static RestaurantEditListItemFragment newInstance(String name, String restaurantId, String customerId) {
        RestaurantEditListItemFragment frag = new RestaurantEditListItemFragment();
        Bundle args = new Bundle();
        args.putString("customerName", name);
        args.putString("restaurantId", restaurantId);
        args.putString("customerId", customerId);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        return inflater.inflate(R.layout.edit_waitlist_item_fragment, container);
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Get field from view
        tvUser = (TextView) view.findViewById(R.id.tvName);
        btnRemoveFromWaitList = (ImageButton) view.findViewById(R.id.btnRemoveFromWaitList);
        // Fetch arguments from bundle and set title
        final String user = getArguments().getString("customerName", "Enter Name");
        final String customerId = getArguments().getString("customerId");
        final String restaurantId = getArguments().getString("restaurantId");
        getDialog().setTitle(user);
        tvUser.setText(user);
        btnRemoveFromWaitList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (customerId != null && restaurantId != null) {
                    WaitlistUtils.leaveWaitList(restaurantId, customerId);
                    btnRemoveFromWaitList.setEnabled(false);
                    actionListener.onRemove();
                    getFragmentManager().beginTransaction().remove(RestaurantEditListItemFragment.this).commit();
                    //getFragmentManager().popBackStackImmediate();
                }
            }
        });

    }

    public interface actionListener {
        public void onRemove();
    }

    public void setActionListener(actionListener listener) {
        this.actionListener = listener;
    }



}

