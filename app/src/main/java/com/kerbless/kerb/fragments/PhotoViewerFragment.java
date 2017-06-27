package com.kerbless.kerb.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.alexvasilkov.foldablelayout.FoldableListLayout;
import com.kerbless.kerb.R;
import com.kerbless.kerb.adapters.PhotoArrayAdapter;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class PhotoViewerFragment extends DialogFragment {


    FoldableListLayout foldableListLayout;
    TextView tvRestoName;
    ImageView ivClose;
    PhotoArrayAdapter adapter;
    ArrayList<String> photos;
    String restaurantName;
    private static final String PHOTO_LIST = "Kerb.PhotoList";
    private static final String RESTAURANT = "Kerb.RestaurantName";

    public PhotoViewerFragment() {
        // Required empty public constructor
    }

    public static PhotoViewerFragment newInstance(ArrayList<String> photos, String restaurantName) {
        PhotoViewerFragment fragment = new PhotoViewerFragment();
        Bundle args = new Bundle();
        args.putSerializable(PHOTO_LIST, photos);
        args.putSerializable(RESTAURANT, restaurantName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_photo_viewer, container, false);
        tvRestoName = (TextView) view.findViewById(R.id.tvRestoName);
        ivClose = (ImageView) view.findViewById(R.id.ivClose);
        photos = (ArrayList<String>) getArguments().getSerializable(PHOTO_LIST);
        restaurantName = (String) getArguments().getSerializable(RESTAURANT);
        tvRestoName.setText(restaurantName);
        foldableListLayout = (FoldableListLayout) view.findViewById(R.id.foldable_list);
        adapter = new PhotoArrayAdapter(getActivity(), photos);
        foldableListLayout.setAdapter(adapter);

        ivClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        return view;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);

        // request a window without the title
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }
}
