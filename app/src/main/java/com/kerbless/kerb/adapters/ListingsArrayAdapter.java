package com.kerbless.kerb.adapters;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.support.v4.app.ActivityOptionsCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import com.kerbless.kerb.R;
import com.kerbless.kerb.activities.ListingDetailActivity;
import com.kerbless.kerb.utils.FlipAnimation;
import com.kerbless.kerb.utils.ListingUtils;
import com.kerbless.kerb.models.Listing;
import com.kerbless.kerb.utils.Utilities;
import com.kerbless.kerb.utils.WaitlistUtils;
import com.parse.ParseUser;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by SushmaNayak on 10/15/2015.
 */
public class ListingsArrayAdapter extends ArrayAdapter<Listing> {

    Context mContext;

    static class ViewHolder {
        LinearLayout card_back;
        LinearLayout card_front;
        ImageView ivImage;
        TextView tvWaitTime;
        TextView tvDistance;
        TextView tvRestoName;
        TextView tvAddress;
        ImageView ivLike;
        TextView tvRestoName2;
        TextView tvAddress2;
        RatingBar userRatingBar;
        TextView tvPriceLevel;
        TextView tvTable2;
        TextView tvTable4;
        TextView tvTable6;

        public void bind(final Context context, final Listing listing) {
            Picasso.with(context).load(listing.getPhotoResId()).into(ivImage);
            tvRestoName.setText(listing.getRestaurantName());
            tvRestoName2.setText(listing.getRestaurantName());
            tvAddress.setText(listing.getAddress());
            tvAddress2.setText(listing.getAddress());
            userRatingBar.setRating((float) listing.getRating());
            tvPriceLevel.setText(Utilities.getPriceLevelString(listing.getPriceLevel()));
            tvWaitTime.setText(listing.getWaitTime() == 0 ? "No wait" : listing.getWaitTime() + " mins");
            Location lcn = new Location(ListingUtils.currentLocation);
            lcn.setLatitude(listing.getLatitude());
            lcn.setLongitude(listing.getLongitude());
            double distance = ListingUtils.currentLocation.distanceTo(lcn);
            DecimalFormat df = new DecimalFormat("#.#");
            if (distance > 160) {
                tvDistance.setText(df.format(0.000621371 * distance) + " mi");
            } else {
                df = new DecimalFormat(("####"));
                tvDistance.setText(df.format(distance) + " mts");
            }

            if (listing.isFavorite())
                ivLike.setImageResource(R.drawable.ic_like_pink);
            else
                ivLike.setImageResource(R.drawable.ic_like);

            ivLike.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listing.setFavorite(!listing.isFavorite());
                    if (listing.isFavorite()) {
                        ListingUtils.FavRestaurants.put(listing.getListingId(), listing.getjSONString());
                        updateHeartButton((ImageView) v, R.drawable.ic_like_pink);
                    } else {
                        ListingUtils.FavRestaurants.remove(listing.getListingId());
                        ivLike.setImageResource(R.drawable.ic_like);
                    }
                }
            });

            tvTable2.setText ((int) (listing.getWaitTime() - (float) listing.getWaitTime() * 0.08) + " mins");
            tvTable4.setText ((int) (listing.getWaitTime() + (float) listing.getWaitTime() * 0.04) + " mins");
            tvTable6.setText ((int) (listing.getWaitTime() + (float) listing.getWaitTime() * 0.12) + " mins");

            if (ListingUtils.customerOnWaitList && ListingUtils.joinedRestaurantID.equals(listing.getListingId())) {
                tvWaitTime.setText("On Waitlist");
            }
        }
    }

    public ListingsArrayAdapter(Context context, ArrayList<Listing> objects) {
        super(context, 0, objects);
        mContext = context;
    }

    private void flipCard(View view) {
        View rootLayout = view.findViewById(R.id.layoutRoot);
        View cardFace = view.findViewById(R.id.card_front);
        View cardBack = view.findViewById(R.id.card_back);

        FlipAnimation flipAnimation = new FlipAnimation(cardFace, cardBack);

        if (cardFace.getVisibility() == View.GONE) {
            flipAnimation.reverse();
        }
        rootLayout.startAnimation(flipAnimation);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Listing listing = getItem(position);
        listing.setPhotoResId(ListingUtils.getDrawable(position));
        final ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_listing, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.card_front = (LinearLayout) convertView.findViewById(R.id.card_front);
            viewHolder.ivImage = (ImageView) convertView.findViewById(R.id.ivImage);
            viewHolder.tvWaitTime = (TextView) convertView.findViewById(R.id.tvWaitTime);
            viewHolder.tvDistance = (TextView) convertView.findViewById(R.id.tvDistance);
            viewHolder.tvRestoName = (TextView) convertView.findViewById(R.id.tvRestoName);
            viewHolder.tvAddress = (TextView) convertView.findViewById(R.id.tvAddress);
            viewHolder.ivLike = (ImageView) convertView.findViewById(R.id.ivLike);
            viewHolder.card_back = (LinearLayout) convertView.findViewById(R.id.card_back);
            viewHolder.tvRestoName2 = (TextView) convertView.findViewById(R.id.tvRestoName2);
            viewHolder.tvAddress2 = (TextView) convertView.findViewById(R.id.tvAddress2);
            viewHolder.tvPriceLevel = (TextView) convertView.findViewById(R.id.tvPriceLevel);
            viewHolder.userRatingBar = (RatingBar) convertView.findViewById(R.id.userRatingBar);
            viewHolder.tvTable2 = (TextView) convertView.findViewById(R.id.tvTable2);
            viewHolder.tvTable4 = (TextView) convertView.findViewById(R.id.tvTable4);
            viewHolder.tvTable6 = (TextView) convertView.findViewById(R.id.tvTable6);
            convertView.setTag(viewHolder);
        } else
            viewHolder = (ViewHolder) convertView.getTag();

        convertView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                flipCard(v);
                return true;
            }
        });

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation((Activity) mContext, viewHolder.ivImage, "transition");
                Intent intent = new Intent(mContext, ListingDetailActivity.class);
                intent.putExtra("listing", listing);
                mContext.startActivity(intent, optionsCompat.toBundle());
            }
        });
        viewHolder.bind(mContext, listing);
        return convertView;
    }

    private static final AccelerateInterpolator ACCELERATE_INTERPOLATOR = new AccelerateInterpolator();
    private static final OvershootInterpolator OVERSHOOT_INTERPOLATOR = new OvershootInterpolator(4);
    private static final Map<View, AnimatorSet> likeAnimations = new HashMap<>();

    private static void resetLikeAnimationState(ImageView holder) {
        likeAnimations.remove(holder);
    }

    private static void updateHeartButton(final ImageView holder, final int drawable) {

        if (!likeAnimations.containsKey(holder)) {
            AnimatorSet animatorSet = new AnimatorSet();
            likeAnimations.put(holder, animatorSet);

            ObjectAnimator rotationAnim = ObjectAnimator.ofFloat(holder, "rotation", 0f, 360f);
            rotationAnim.setDuration(300);
            rotationAnim.setInterpolator(ACCELERATE_INTERPOLATOR);

            ObjectAnimator bounceAnimX = ObjectAnimator.ofFloat(holder, "scaleX", 0.2f, 1f);
            bounceAnimX.setDuration(300);
            bounceAnimX.setInterpolator(OVERSHOOT_INTERPOLATOR);

            ObjectAnimator bounceAnimY = ObjectAnimator.ofFloat(holder, "scaleY", 0.2f, 1f);
            bounceAnimY.setDuration(300);
            bounceAnimY.setInterpolator(OVERSHOOT_INTERPOLATOR);
            bounceAnimY.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    holder.setImageResource(drawable);
                }
            });

            animatorSet.play(rotationAnim);
            animatorSet.play(bounceAnimX).with(bounceAnimY).after(rotationAnim);

            animatorSet.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    resetLikeAnimationState(holder);
                }
            });

            animatorSet.start();
        }
    }
}
