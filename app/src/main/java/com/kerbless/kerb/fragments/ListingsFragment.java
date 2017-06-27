package com.kerbless.kerb.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.etsy.android.grid.StaggeredGridView;
import com.kerbless.kerb.R;
import com.kerbless.kerb.adapters.ListingsArrayAdapter;
import com.kerbless.kerb.utils.ListingUtils;
import com.kerbless.kerb.models.Listing;
import com.kerbless.kerb.utils.EndlessScrollListener;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class ListingsFragment extends Fragment {

    StaggeredGridView lvListings;
    ArrayList<Listing> listings;
    ListingsArrayAdapter adapter;
    SwipeRefreshLayout swipeContainer;

    private final static int MAX_PAGES = 3;
    private final static int MAX_LISTINGS = 60;


    public ListingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        listings = new ArrayList<>();
        adapter = new ListingsArrayAdapter(getActivity(), listings);
    }

    public static ListingsFragment newInstance() {
        return new ListingsFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_listings, container, false);
        swipeContainer = (SwipeRefreshLayout) view.findViewById(R.id.swipeContainer);
        lvListings = (StaggeredGridView) view.findViewById(R.id.lvListings);
        lvListings.setAdapter(adapter);

        lvListings.setOnScrollListener(new EndlessScrollListener() {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                if (page <= MAX_PAGES && totalItemsCount <= MAX_LISTINGS)
                    ((ListingUtils.RefreshListings) getActivity()).RefreshList(true);
            }
        });

        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                ((ListingUtils.RefreshListings) getActivity()).RefreshList(false);
                swipeContainer.setRefreshing(false);
            }
        });

        TextView emptyView = (TextView) view.findViewById(R.id.emptyElement);
        lvListings.setEmptyView(emptyView);

        return view;
    }

    public void updateListing() {
        /*for (Listing listing : listings) {
            int position = adapter.getPosition(listing);
            adapter.remove(adapter.getItem(position));
            adapter.insert(listing, position);
        }*/
    }

    public void SetListings(ArrayList<Listing> listings, boolean bUpdateExisting) {
        if (!bUpdateExisting)
            adapter.clear();

        if (listings != null)
            adapter.addAll(listings);
    }

    public void SetListings(String status) {
        adapter.clear();

        // Create and set the EmptyView
        TextView emptyView = (TextView) swipeContainer.findViewById(R.id.emptyElement);
        emptyView.setText("No results returned. \n" + status);
    }
}
