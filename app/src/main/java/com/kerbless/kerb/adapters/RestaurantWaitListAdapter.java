package com.kerbless.kerb.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.swipe.SwipeLayout;
import com.kerbless.kerb.R;
import com.kerbless.kerb.utils.FlipAnimation;
import com.kerbless.kerb.utils.WaitlistUtils;
import com.kerbless.kerb.models.WaitLists2;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SendCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

/**
 * Created by alinc on 10/18/15.
 */
public class RestaurantWaitListAdapter extends RecyclerView.Adapter<RestaurantWaitListAdapter.ViewHolder> {

    private static String TAG = RestaurantWaitListAdapter.class.getName();
    private boolean emptyWaitList;
    private eventListener eventListener;
    private int lastPosition = -1;
    int startOffset = 0;
    private Context context;


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView tvCustomerName;
        public TextView tvPhoto;
        public TextView tvPartySize;
        public TextView tvRequest;
        public Chronometer chTimer;
        public Button rbtnNotify;
        public Button rbtnSeat;
        public TextView btnRemoveFromWaitList;
        public RelativeLayout backView;
        private final Context context;
        public TextView tvFlipCustomerName;
        public TextView tvFlipPartySize;
        public TextView tvEstimated;
        public FrameLayout container;

        public ViewHolder(final Context context, final View itemView) {
            super(itemView);
            tvCustomerName = (TextView) itemView.findViewById(R.id.tvCustomerName);
            tvPhoto = (TextView) itemView.findViewById(R.id.tvPhoto);
            tvPartySize = (TextView) itemView.findViewById(R.id.tvPartySize);
            tvRequest = (TextView) itemView.findViewById(R.id.tvRequest);
            chTimer = (Chronometer) itemView.findViewById(R.id.chTimer);
            rbtnNotify = (Button) itemView.findViewById(R.id.rbtnNotify);
            rbtnSeat = (Button) itemView.findViewById(R.id.rbtnSeat);
            btnRemoveFromWaitList = (TextView) itemView.findViewById(R.id.btnRemoveFromWaitList);
            backView = (RelativeLayout) itemView.findViewById(R.id.llBackView);
            tvFlipCustomerName = (TextView) itemView.findViewById(R.id.tvFlipCustomerName);
            tvFlipPartySize = (TextView) itemView.findViewById(R.id.tvFlipPartySize);
            tvEstimated = (TextView) itemView.findViewById(R.id.tvEstimated);
            container = (FrameLayout) itemView.findViewById(R.id.cvWaitList);

            this.context = context;
            itemView.setClickable(true);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    flipCard(v);
                }
            });

            rbtnNotify.setOnClickListener(this);
            rbtnSeat.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Drawable seated = context.getResources().getDrawable( R.drawable.ic_seat_green );
                    rbtnSeat.setCompoundDrawablesWithIntrinsicBounds(seated, null, null, null);
                    Snackbar.make(v, "Updating customer to seated", Snackbar.LENGTH_SHORT).show();
                    rbtnSeat.setPressed(true);
                    WaitLists2 wl = mWaitList.get(getLayoutPosition());
                    WaitlistUtils.updateCustomerStatus(wl.getCustomerID(), wl.getRestaurantID(), "seated");
                    tvCustomerName.setPaintFlags(tvCustomerName.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                    tvPartySize.setPaintFlags(tvPartySize.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                    tvPhoto.setBackground(context.getResources().getDrawable(R.drawable.dark_green));
                    eventListener.onSeated();
                    int position = getLayoutPosition();
                    eventListener.onRemove();
                    mWaitList.remove(position);
                    notifyItemRemoved(position);
                    eventListener.onUpdate();
                }
            });

        }

        @Override
        public void onClick(View view) {

            int position = getLayoutPosition();
            final WaitLists2 wl = mWaitList.get(position);
            if (!wl.getNotified().contentEquals("Y")) {
                Snackbar.make(view, "Sending for notification to  " + tvCustomerName.getText(), Snackbar.LENGTH_SHORT).show();
                //Toast.makeText(context, "Sending for notification to  " + tvCustomerName.getText(), Toast.LENGTH_SHORT).show();
                rbtnSeat.setVisibility(View.VISIBLE);
                rbtnSeat.setEnabled(true);

                //rbtnNotify.setButtonTintList(ColorStateList.valueOf(transitColor));
                JSONObject obj;
                try {
                    obj = new JSONObject();
                    obj.put("action", "com.kerbless.kerb.UPDATE_STATUS");
                    obj.put("alert", "Your table is ready, please come to the front desk!");
                    obj.put("customdata", "Your table is ready, please come to the front desk!");
                    obj.put("restaurantId", wl.getRestaurantID());
                    obj.put("title", "Kerb @ " + WaitlistUtils.getRestaurantName(wl.getRestaurantID()));

                    ParsePush push = new ParsePush();
                    ParseQuery query = ParseInstallation.getQuery();
                    ParseUser customer = WaitlistUtils.getCustomer(wl.getCustomerID());

                    query.whereEqualTo("user", customer);
                    push.setQuery(query);
                    push.setData(obj);
                    push.sendInBackground(new SendCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                //rbtnNotify.setChecked(true);
                                WaitlistUtils.updateNotification(wl.getObjectId());

                                notifySuccess(rbtnNotify);
                                rbtnNotify.setText("Notified");
                                rbtnNotify.setPressed(true);

                                Snackbar.make(rbtnNotify, "Notification sent to  " + tvCustomerName.getText(), Snackbar.LENGTH_SHORT).show();
                                //rbtnNotify.setButtonTintList(ColorStateList.valueOf(notifiedColor));
                                //rbtnNotify.setText(R.string.notified);
                                rbtnSeat.setVisibility(View.VISIBLE);
                                //tvPhoto.setBackground(context.getResources().getDrawable(R.drawable.green));
                            } else {
                                //rbtnNotify.setButtonTintList(ColorStateList.valueOf(exceptionColor));
                                notifyFailed(rbtnNotify);
                                Snackbar.make(rbtnNotify, "Unable to send notification to  " + tvCustomerName.getText(), Snackbar.LENGTH_SHORT).show();
                                Log.d(TAG, "Failed to push customer notification: ");
                                e.printStackTrace();
                            }
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(context, "Implement Resend " + position, Toast.LENGTH_SHORT).show();
                rbtnNotify.setActivated(true);
                //rbtnNotify.setChecked(true);
            }
        }
    }

    private List<WaitLists2> mWaitList;

    public RestaurantWaitListAdapter(List<WaitLists2> waitList, Context context) {
        mWaitList = waitList;
        this.context = context;
        this.eventListener = null;
    }

    @Override
    public RestaurantWaitListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View waitListView = inflater.inflate(R.layout.item_waitlist, parent, false);
        ViewHolder viewHolder = new ViewHolder(context, waitListView);

        return viewHolder;

    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final WaitLists2 waitLists2 = mWaitList.get(position);
        HashMap<String, String> customerInfoMap = WaitlistUtils.getCustomerDetails(waitLists2.getCustomerID(), waitLists2.getRestaurantID());

        final TextView tvCustomerName = holder.tvCustomerName;
        final TextView tvFlipCustomerName = holder.tvFlipCustomerName;
        final TextView tvRequest = holder.tvRequest;
        final TextView tvPhoto = holder.tvPhoto;
        final TextView tvPartySize = holder.tvPartySize;
        final TextView tvFlipPartySize = holder.tvFlipPartySize;
        final Chronometer chTimer = holder.chTimer;
        final TextView tvEstimated = holder.tvEstimated;
        final TextView btnRemoveFromWaitList = holder.btnRemoveFromWaitList;
        final RelativeLayout backView = holder.backView;
        final TextView rbtnNotified = holder.rbtnNotify;
        final TextView rbtnSeat = holder.rbtnSeat;

        tvRequest.setText("" + (waitLists2.getSpecialRequest() != null ? waitLists2.getSpecialRequest() : "") );

        final String restId = waitLists2.getRestaurantID();
        final String custId = waitLists2.getCustomerID();

        setAnimation(holder.container, position);

        if (customerInfoMap.size() == 0) {
            emptyWaitList = true;
            tvCustomerName.setText("No customers waiting.");
            tvCustomerName.setTypeface(tvCustomerName.getTypeface(), Typeface.ITALIC);
        } else {
            emptyWaitList = false;
            String firstName = customerInfoMap.get("firstName");
            String lastName = customerInfoMap.get("lastName");
            tvCustomerName.setText(firstName + " " + lastName);
            tvFlipCustomerName.setText(firstName + " " + lastName);
            tvEstimated.setText("Initial estimate: " + waitLists2.getEstimatedWaitTime() + " min");


            if (firstName != null & lastName != null) {
                String initials = ((firstName.length() > 0) ? String.valueOf(firstName.charAt(0)) : "");    // + ((lastName.length() > 0 ? String.valueOf(lastName.charAt(0)) : " "));
                tvPhoto.setText(initials);
            } else
                tvPhoto.setText("K");
            setBackground(tvPhoto, position);
            tvPartySize.setText("Party size of " + waitLists2.getPartySize() + "");
            tvFlipPartySize.setText("Party size of " + waitLists2.getPartySize() + "");
            chTimer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
                @Override
                public void onChronometerTick(Chronometer cArg) {
                    long time = WaitlistUtils.getCustomerWaitTime(restId, custId);
                    int h = (int) (time / 3600000);
                    int m = (int) (time - h * 3600000) / 60000;
                    int s = (int) (time - h * 3600000 - m * 60000) / 1000;
                    String hh = h < 10 ? "0" + h : h + "";
                    String mm = m < 10 ? "0" + m : m + "";
                    //String ss = s < 10 ? "0"+s: s+"";
                    if (h == 0)
                        cArg.setText(mm + " min");
                    else
                        cArg.setText(hh + " h " + mm + " min");
                }
            });
            chTimer.setBase(SystemClock.elapsedRealtime());
            chTimer.start();

            tvCustomerName.setPaintFlags(0);
            tvPartySize.setPaintFlags(0);
            notifyDefault(rbtnNotified);
            rbtnNotified.setText("Notify");
            seatDefault(rbtnSeat);
            rbtnSeat.setVisibility(View.INVISIBLE);
            rbtnSeat.setText("Seat");
            if(backView.getVisibility() == View.VISIBLE) {
                flipCard(holder.itemView);
            }

            if (waitLists2.getNotified().contentEquals("Y")) {
                notifySuccess(rbtnNotified);
                rbtnNotified.setText("Notified");
                rbtnSeat.setVisibility(View.VISIBLE);
                //tvPhoto.setBackground(context.getResources().getDrawable(R.drawable.green));
                rbtnSeat.setEnabled(true);
            } else {
                notifyDefault(rbtnNotified);
            }

            if (waitLists2.getStatus().contentEquals("seated")) {
                tvCustomerName.setPaintFlags(tvCustomerName.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                tvPartySize.setPaintFlags(tvPartySize.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                tvPhoto.setBackground(context.getResources().getDrawable(R.drawable.dark_green));
            }

            btnRemoveFromWaitList.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (custId != null && restId != null) {
                        WaitlistUtils.leaveWaitList(restId, custId);
                        btnRemoveFromWaitList.setEnabled(false);
                        btnRemoveFromWaitList.setTextColor(context.getResources().getColor(R.color.red));
                        mWaitList.remove(position);
                        notifyItemRemoved(position);
                        eventListener.onRemove();
                        Snackbar.make(v, waitLists2.getFirstName() + " " + waitLists2.getLastName() + " " + v.getContext().getString(R.string.snackbar_remove_item), Snackbar.LENGTH_LONG)
                                .setAction(R.string.snackbar_undo_remove, new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        mWaitList.add(waitLists2);
                                        RestaurantWaitListAdapter.this.notifyItemInserted(position);
                                        // TODO: The statement below to be enabled once archive is implemented. This will restore the item in the db.
                                        //WaitlistUtils.joinList(wl.getRestaurantID(), wl.getCustomerID(), wl.getPartySize(), wl.getFirstName(), wl.getLastName());
                                    }
                                })  // action text on the right side
                                .setActionTextColor(ContextCompat.getColor(v.getContext(), R.color.colorAccent))
                                .setDuration(5000).show();
                        return true;
                    }
                    return false;
                }
            });
        }
    }

    private void setBackground(TextView tvPhoto, int position) {
        int index = position % 5;
        if (index == 0)
            tvPhoto.setBackground(context.getResources().getDrawable(R.drawable.pink));
        else if (index == 1)
            tvPhoto.setBackground(context.getResources().getDrawable(R.drawable.blue));
        else if (index == 2)
            tvPhoto.setBackground(context.getResources().getDrawable(R.drawable.yellow));
        else if (index == 3)
            tvPhoto.setBackground(context.getResources().getDrawable(R.drawable.purple));
        else
            tvPhoto.setBackground(context.getResources().getDrawable(R.drawable.orange));
    }


    @Override
    public int getItemCount() {
        return mWaitList.size();
    }

    public void clearWaitList() {
        mWaitList.clear();
    }

    public void clear() {
        mWaitList.clear();
        notifyDataSetChanged();
    }

    // Add a list of items
    public int addAll(WaitLists2 object) {
        int position = mWaitList.size();
        mWaitList.add(object);

        //mWaitList.addAll(position, list);
        return position;
    }

    public int remove(String customerId) {
        int position = -1;
        for (int i = 0; i < mWaitList.size(); i++) {
            if (mWaitList.get(i).getCustomerID().contentEquals(customerId)) {
                position = i;
                break;
            }
        }
        if (position != -1) {
            mWaitList.remove(position);
            return position;
        }
        return position;
    }

    public interface eventListener {
        void onSeated();

        void onRemove();

        void onUpdate();

        void onWaiting();
        //public void onWaitTime();
    }

    public void setEventListener(eventListener listener) {
        this.eventListener = listener;
    }

    private void flipCard(View view)
    {
        View rootLayout =  view.findViewById(R.id.cvWaitList);
        View cardFace =  view.findViewById(R.id.rlCardView);
        View cardBack =  view.findViewById(R.id.llBackView);

        FlipAnimation flipAnimation = new FlipAnimation(cardFace, cardBack);

        if (cardFace.getVisibility() == View.GONE)
        {
            flipAnimation.reverse();
        }
        rootLayout.startAnimation(flipAnimation);
    }

    private void notifySuccess(final TextView view) {
        Drawable img = context.getResources().getDrawable( R.drawable.ic_action_green );
        view.setCompoundDrawablesWithIntrinsicBounds(img, null, null, null);
    }

    private void notifyFailed(final TextView view) {
        Drawable img = context.getResources().getDrawable( R.drawable.ic_action_red );
        view.setCompoundDrawablesWithIntrinsicBounds( img, null, null, null);
    }

    private void notifyDefault(final TextView view) {
        Drawable img = context.getResources().getDrawable( R.drawable.ic_action );
        view.setCompoundDrawablesWithIntrinsicBounds( img, null, null, null);
    }

    private void seatDefault(final TextView view) {
        Drawable img = context.getResources().getDrawable( R.drawable.ic_seat );
        view.setCompoundDrawablesWithIntrinsicBounds( img, null, null, null);
    }

    private void setAnimation(View viewToAnimate, int position)
    {
        // If the bound view wasn't previously displayed on screen, it's animated
        if (position > lastPosition)
        {

            Animation animation = AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left);
            if(position / 2 == 0) {
                animation = AnimationUtils.loadAnimation(context, android.R.anim.fade_in);
            }
            animation.setStartOffset(startOffset);
            startOffset = startOffset + 200;
            animation.setDuration(2000);
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
        }
    }
}