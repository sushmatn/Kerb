package com.kerbless.kerb.fragments;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.BounceInterpolator;
import android.view.animation.Interpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.ui.IconGenerator;
import com.kerbless.kerb.R;
import com.kerbless.kerb.activities.ListingDetailActivity;
import com.kerbless.kerb.utils.ListingUtils;
import com.kerbless.kerb.models.Listing;
import com.kerbless.kerb.utils.Utilities;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */
public class MapFragment extends Fragment {

    MapView mMapView;
    private GoogleMap googleMap;
    CardView sliding_layout;
    int selectedMarkerIndex = 0;
    Map<Marker, Listing> mapMarkers;
    IconGenerator mIconfactory;

    ImageView ivImage;
    TextView tvWaitTime;
    TextView tvDistance;
    TextView tvRestoName;
    TextView tvAddress;
    TextView tvRating;


    public MapFragment() {
        // Required empty public constructor
    }

    public static MapFragment newInstance() {
        return new MapFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        mMapView = (MapView) view.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);

        sliding_layout = (CardView) view.findViewById(R.id.cardView);
        ivImage = (ImageView) view.findViewById(R.id.ivImage);
        tvWaitTime = (TextView) view.findViewById(R.id.tvWaitTime);
        tvDistance = (TextView) view.findViewById(R.id.tvDistance);
        tvRestoName = (TextView) view.findViewById(R.id.tvRestoName);
        tvAddress = (TextView) view.findViewById(R.id.tvAddress);
        tvRating = (TextView) view.findViewById(R.id.tvRating);
        mapMarkers = new HashMap<>();

        mMapView.onResume();// needed to get the map to display immediately

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap map) {
                googleMap = map;
                googleMap.setMyLocationEnabled(true);
                googleMap.getUiSettings().setZoomControlsEnabled(true);

                if (ListingUtils.currentLocation != null) {
                    CameraPosition cameraPosition = new CameraPosition.Builder()
                            .target(new LatLng(ListingUtils.currentLocation.getLatitude(), ListingUtils.currentLocation.getLongitude())).zoom(17).build();
                    googleMap.animateCamera(CameraUpdateFactory
                            .newCameraPosition(cameraPosition));
                }
                ((ListingUtils.RefreshListings) getActivity()).LoadData();
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    public void updateLocation(Location location) {
        //CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 17);
        //googleMap.animateCamera(cameraUpdate);
    }

    public void SetListings(String status) {
        googleMap.clear();
        mapMarkers.clear();
        sliding_layout.setX(-Utilities.screenWidth);
    }

    public void SetListings(ArrayList<Listing> listings, boolean bUpdateExisting) {
        if (!bUpdateExisting) {
            googleMap.clear();
            mapMarkers.clear();
        }

        if (listings == null)
            return;

        if (ListingUtils.currentLocation != null) {
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(ListingUtils.currentLocation.getLatitude(), ListingUtils.currentLocation.getLongitude())).zoom(18).build();
            googleMap.animateCamera(CameraUpdateFactory
                    .newCameraPosition(cameraPosition));
        }

        for (Listing listing : listings) {
            mIconfactory = new IconGenerator(getActivity());
            if (listing.getWaitTime() <= 20)
                mIconfactory.setStyle(IconGenerator.STYLE_GREEN);
            else if (listing.getWaitTime() > 20 && listing.getWaitTime() <= 40)
                mIconfactory.setStyle(IconGenerator.STYLE_ORANGE);
            else
                mIconfactory.setStyle(IconGenerator.STYLE_RED);

            mIconfactory.setContentPadding(2, 5, 2, 5);
            //mBubblefactory.setContentRotation(90);
            //mIconfactory.setTextAppearance(R.style.iconGenText);
            //mIconfactory.setBackground(getResources().getDrawable(R.drawable.btn_green_matte));
            Bitmap iconBitmap = mIconfactory.makeIcon(Integer.toString(listing.getWaitTime()) + " min");
            Marker marker = googleMap.addMarker(new MarkerOptions()
                    .position(new LatLng(listing.getLatitude(), listing.getLongitude()))
                    .icon(BitmapDescriptorFactory.fromBitmap(iconBitmap)));
            mapMarkers.put(marker, listing);

            googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {
                    ObjectAnimator.ofFloat(sliding_layout, View.X, -Utilities.screenWidth, 25).start();
                    Listing currentListing = mapMarkers.get(marker);
                    updateListing(currentListing);
                    bounceMarker(marker);
                    return true;
                }
            });

            googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                @Override
                public void onMapClick(LatLng latLng) {
                    ObjectAnimator.ofFloat(sliding_layout, View.X, 25, -Utilities.screenWidth).start();
                }
            });
        }
        if (!bUpdateExisting) {
            if (listings.size() > 0) {
                Listing listing = listings.get(0);
                updateListing(listing);
                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(new LatLng(listing.getLatitude(), listing.getLongitude())).zoom(18).build();
                googleMap.animateCamera(CameraUpdateFactory
                        .newCameraPosition(cameraPosition));
                sliding_layout.setX(-Utilities.screenWidth);
            }
        }
    }

    void updateListing(final Listing listing) {
        Picasso.with(getActivity()).load(listing.getPhotoResId()).into(ivImage);
        tvRestoName.setText(listing.getRestaurantName());
        tvAddress.setText(listing.getAddress());
        Location lcn = new Location(ListingUtils.currentLocation);
        lcn.setLatitude(listing.getLatitude());
        lcn.setLongitude(listing.getLongitude());
        tvWaitTime.setText(listing.getWaitTime() + " mins");
        tvRating.setText(listing.getRating() + "/5");
        double distance = ListingUtils.currentLocation.distanceTo(lcn);
        DecimalFormat df = new DecimalFormat("#.#");
        if (distance > 160) {
            tvDistance.setText(df.format(0.000621371 * distance) + " mi");
        } else {
            df = new DecimalFormat(("####"));
            tvDistance.setText(df.format(distance) + " mts");
        }
        sliding_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation((Activity) getActivity(), ivImage, "transition");
                Intent intent = new Intent(getActivity(), ListingDetailActivity.class);
                intent.putExtra("listing", listing);
                getActivity().startActivity(intent, optionsCompat.toBundle());
            }
        });
    }

    /*googleMap.addCircle(new CircleOptions()
                                .center(marker.getPosition())
                                .radius(200)
                                .strokeWidth(0f)
                                .fillColor(0x550000FF));*/

    void bounceMarker(final Marker marker) {
        //Make the marker bounce
        final Handler handler = new Handler();

        final long startTime = SystemClock.uptimeMillis();
        final long duration = 1200;

        Projection proj = googleMap.getProjection();
        final LatLng markerLatLng = marker.getPosition();
        Point startPoint = proj.toScreenLocation(markerLatLng);
        startPoint.offset(0, -100);
        final LatLng startLatLng = proj.fromScreenLocation(startPoint);

        final Interpolator interpolator = new BounceInterpolator();

        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - startTime;
                // Calculate t for bounce based on elapsed time
                float t = Math.max(
                        1 - interpolator.getInterpolation((float) elapsed
                                / duration), 0);
                // Set the anchor
                marker.setAnchor(0.5f, 1.0f + 2 * t);

                if (t > 0.0) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16);
                }
            }
        });
    }
}
