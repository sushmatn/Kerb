package com.kerbless.kerb.fragments;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.transition.Transition;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.kerbless.kerb.R;
import com.kerbless.kerb.adapters.RestaurantWaitListAdapter;
import com.kerbless.kerb.utils.FlipAnimation;
import com.kerbless.kerb.utils.WaitlistUtils;
import com.kerbless.kerb.models.WaitLists2;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.recyclerview.animators.FadeInAnimator;
import jp.wasabeef.recyclerview.animators.SlideInLeftAnimator;
import jp.wasabeef.recyclerview.animators.SlideInRightAnimator;


/**
 * Created by alinc on 10/17/15.
 */
public class RestaurantWaitListFragment extends Fragment {
    private static String TAG = RestaurantWaitListFragment.class.getName();
    private RestaurantWaitListAdapter adapter = null;
    private RestaurantEditListItemFragment editNameDialog;
    private BroadcastReceiver mBroadcastReceiver;
    private FragmentActivity myContext;
    private List<WaitLists2> myCustomerList;
    //private LinearLayout llHeader;
    private LinearLayout llSummary;
    private TextView tvEmptyWaitList;
    private TextView tvWaitingTotal;
    private TextView tvAverageWaitTime;
    private TextView tvSeatedTotal;
    private FloatingActionButton fab;

    private Transition.TransitionListener mEnterTransitionListener;
    private OnAddNewUserToWaitlistListener listener;

    RecyclerView rvWaitList;
    private RecyclerView.LayoutManager mLayoutManager;
    String restaurantID;

    public RestaurantWaitListFragment() {
    }

    public interface OnAddNewUserToWaitlistListener {
        // This can be any number of events to be sent to the activity
        public void onAddNewUser();
    }

    public void LoadFragmentData() {
        new FetchCustomerData().execute();
    }

    @Override
    public void onAttach(Activity activity) {
        myContext = (FragmentActivity) activity;
        super.onAttach(activity);
        if (activity instanceof OnAddNewUserToWaitlistListener) {
            listener = (OnAddNewUserToWaitlistListener) activity;
        } else {
            throw new ClassCastException(activity.toString()
                    + " must implement RestaurantWaitListFragment.OnAddNewUserToWaitlistListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (fab != null) {
            fab.setVisibility(View.INVISIBLE);
        }

    }

    public static RestaurantWaitListFragment newInstance() {
        return new RestaurantWaitListFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment


        restaurantID = getArguments().getString("restaurantID");
        View view = inflater.inflate(R.layout.fragment_waitlist, container, false);

        fab = (FloatingActionButton) view.findViewById(R.id.fab);
        tvEmptyWaitList = (TextView) view.findViewById(R.id.tvEmptyWaitList);
        rvWaitList = (RecyclerView) view.findViewById(R.id.rvWaitList);
        rvWaitList.setHasFixedSize(false);
        llSummary = (LinearLayout) view.findViewById(R.id.llSummary);
        tvWaitingTotal = (TextView) view.findViewById(R.id.tvWaitingTotal);
        tvAverageWaitTime = (TextView) view.findViewById(R.id.tvAverageWaitTime);
        tvSeatedTotal = (TextView) view.findViewById(R.id.tvSeatedTotal);

        myCustomerList = new ArrayList<>();
        adapter = new RestaurantWaitListAdapter(myCustomerList, getContext());
        rvWaitList.setAdapter(adapter);
        mLayoutManager = new LinearLayoutManager(getContext());
        rvWaitList.setLayoutManager(mLayoutManager);

        adapter.setEventListener(new RestaurantWaitListAdapter.eventListener() {
            @Override
            public void onSeated() {
                setSummaryDataViews();
                if (myCustomerList.size() == 0) {
                    setEmptyWaitList();
                }
            }

            @Override
            public void onWaiting() {
                setWaitingTotal();
            }

            @Override
            public void onRemove() {
                rvWaitList.setItemAnimator(new SlideInRightAnimator());
                rvWaitList.getItemAnimator().setRemoveDuration(700);
            }

            @Override
            public void onUpdate() {
                if (myCustomerList.size() == 0) {
                    setEmptyWaitList();
                }
                setWaitingTotal();
                setAverageWaitTime();
            }
        });

        //LoadFragmentData();
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onAddNewUser();

            }
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent == null) {
                    Log.d(TAG, "Receiver intent is null, nothing to do.");
                } else {
                    String action = intent.getAction();
                    Bundle extras = intent.getExtras();
                    String jsonData = extras.getString("com.parse.Data");
                    String customerId = "";
                    JSONObject jsonObject;
                    try {
                        jsonObject = new JSONObject(jsonData);
                        customerId = jsonObject.getString("customdata");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    if (action.equals("com.kerbless.kerb.UPDATE_WAITLIST")) {
                        if (tvEmptyWaitList.isEnabled()) {
                            //llHeader.setVisibility(View.VISIBLE);
                            llSummary.setVisibility(View.VISIBLE);
                            rvWaitList.setVisibility(View.VISIBLE);
                            tvEmptyWaitList.setVisibility(View.GONE);
                        }
                        rvWaitList.setItemAnimator(new SlideInLeftAnimator());
                        rvWaitList.getItemAnimator().setAddDuration(700);
                        int position = adapter.addAll(WaitlistUtils.getCustomerFromList(customerId).get(0));
                        adapter.notifyItemInserted(position);
                        setSummaryDataViews();
                    }
                    if (action.equals("com.kerbless.kerb.REMOVE_FROM_WAITLIST")) {
                        List<WaitLists2> newList = WaitlistUtils.getMyCustomerList(restaurantID);
                        rvWaitList.setItemAnimator(new SlideInRightAnimator());
                        rvWaitList.getItemAnimator().setRemoveDuration(500);
                        int position = adapter.remove(customerId);
                        adapter.notifyItemRemoved(position);
                        if (position != -1) {
                            setSummaryDataViews();
                        }
                        if (newList.size() == 0) {
                            rvWaitList.setVisibility(View.GONE);
                            tvEmptyWaitList.setVisibility(View.VISIBLE);
                            llSummary.setVisibility(View.VISIBLE);
                        }
                    }
                }
            }
        };
        IntentFilter filter = new IntentFilter("com.kerbless.kerb.UPDATE_WAITLIST");
        getActivity().registerReceiver(mBroadcastReceiver, filter);

        IntentFilter remove_filter = new IntentFilter("com.kerbless.kerb.REMOVE_FROM_WAITLIST");
        getActivity().registerReceiver(mBroadcastReceiver, remove_filter);
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(mBroadcastReceiver);
        mBroadcastReceiver = null;
    }

    public void refreshWaitListAdapter() {
        adapter.notifyDataSetChanged();
    }

    public void setSummaryDataViews() {
        setSeatedTotal();
        setAverageWaitTime();
        setWaitingTotal();
    }

    public void setSeatedTotal() {
        tvSeatedTotal.setText(WaitlistUtils.getSeatedTotal(restaurantID) + " " + getResources().getString(R.string.seated));
    }

    public void setAverageWaitTime() {
        tvAverageWaitTime.setText(WaitlistUtils.getWaitTime(restaurantID, 0) + " " + getResources().getString(R.string.minutes) + " " + getResources().getString(R.string.waittime));
    }

    public void setWaitingTotal() {
        tvWaitingTotal.setText(WaitlistUtils.getCurrentWaitListSize(restaurantID, 0) + " " + getResources().getString(R.string.waiting));
    }

    public void setEmptyWaitList() {
        rvWaitList.setVisibility(View.GONE);
        tvEmptyWaitList.setVisibility(View.VISIBLE);
        tvEmptyWaitList.setText(R.string.emptyWaitList);
        llSummary.setVisibility(View.VISIBLE);
        setSummaryDataViews();
    }

    class FetchCustomerData extends AsyncTask<Void, Void, Void> {
        boolean isWaitListOpen = false;
        int seatedTotal;
        int avgWaitTime;
        int totalWaiting;

        @Override
        protected Void doInBackground(Void... params) {
            isWaitListOpen = WaitlistUtils.isWaitListOpen(restaurantID);
            myCustomerList.addAll(WaitlistUtils.getMyCustomerList(restaurantID));
            seatedTotal = WaitlistUtils.getSeatedTotal(restaurantID);
            avgWaitTime = WaitlistUtils.getWaitTime(restaurantID, 0);
            totalWaiting = WaitlistUtils.getCurrentWaitListSize(restaurantID, 0);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            adapter.notifyDataSetChanged();
            if (!isWaitListOpen) {
                rvWaitList.setVisibility(View.GONE);
                llSummary.setVisibility(View.GONE);
                fab.setVisibility(View.INVISIBLE);
                tvEmptyWaitList.setVisibility(View.VISIBLE);
                tvEmptyWaitList.setText(R.string.notOpen);
            } else if (myCustomerList.size() == 0) {
                rvWaitList.setVisibility(View.GONE);
                tvEmptyWaitList.setVisibility(View.VISIBLE);
                tvEmptyWaitList.setText(R.string.emptyWaitList);
                llSummary.setVisibility(View.VISIBLE);
                setSummary();
            } else {
                rvWaitList.setVisibility(View.VISIBLE);
                llSummary.setVisibility(View.VISIBLE);
                tvEmptyWaitList.setVisibility(View.GONE);
                setSummary();
            }
        }

        void setSummary() {
            tvSeatedTotal.setText(seatedTotal + " " + getResources().getString(R.string.seated));
            tvAverageWaitTime.setText(avgWaitTime + " " + getResources().getString(R.string.minutes) + " " + getResources().getString(R.string.waittime));
            tvWaitingTotal.setText(totalWaiting + " " + getResources().getString(R.string.waiting));
        }
    }
}