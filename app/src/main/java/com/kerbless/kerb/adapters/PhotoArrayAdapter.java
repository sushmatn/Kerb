package com.kerbless.kerb.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.kerbless.kerb.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by SushmaNayak on 11/11/2015.
 */
public class PhotoArrayAdapter extends ArrayAdapter<String> {
    Context mContext;

    public PhotoArrayAdapter(Context context, ArrayList<String> objects) {
        super(context, 0, objects);
        mContext = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        String photo = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_photo, parent, false);
        }
        ImageView ivPhoto = (ImageView)convertView.findViewById(R.id.ivPhoto);
        Glide.with(convertView.getContext())
                .load(photo)
                .dontTransform()
                .dontAnimate()
                .into(ivPhoto);
        return convertView;
    }
}
