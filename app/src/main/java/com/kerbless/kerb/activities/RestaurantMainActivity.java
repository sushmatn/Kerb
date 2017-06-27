package com.kerbless.kerb.activities;

import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListener;
import android.support.v4.view.ViewPropertyAnimatorListenerAdapter;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gitonway.lee.niftymodaldialogeffects.lib.Effectstype;
import com.gitonway.lee.niftymodaldialogeffects.lib.NiftyDialogBuilder;
import com.kerbless.kerb.R;
import com.kerbless.kerb.fragments.AddUserToWaitlistFragment;
import com.kerbless.kerb.fragments.RestaurantNewListFragment;
import com.kerbless.kerb.fragments.RestaurantWaitListFragment;
import com.kerbless.kerb.utils.FlipAnimation;
import com.kerbless.kerb.utils.Utilities;
import com.kerbless.kerb.utils.WaitlistUtils;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.squareup.picasso.Picasso;

public class RestaurantMainActivity extends AppCompatActivity implements RestaurantNewListFragment.OnItemSelectedListener, RestaurantWaitListFragment.OnAddNewUserToWaitlistListener {


    private static String TAG = RestaurantMainActivity.class.getName();

    private DrawerLayout dlDrawer;
    private Toolbar toolbar;
    private ActionBarDrawerToggle drawerToggle;
    private String title = "";
    TextView tvRestaurantName;

    private FrameLayout frameLayout;
    NavigationView nvDrawer;
    private boolean isDrawerLocked = false;
    private String restaurantID = "";
    RestaurantWaitListFragment waitListFragment;
    private boolean refreshActivity = false;
    private boolean mShowingBack = false;

    //Animation animFadeOut;
    //View cardRoot;
    //Boolean isWaitlistOpen = false;
    ImageView ivCardFace;
    ImageView ivCardBack;
    FrameLayout cardRoot;
    ImageView ivDoor;
    RelativeLayout doorContainer;
    FrameLayout rootLayout;
    RelativeLayout fragmentRoot;
    FrameLayout doorRoot;
    FrameLayout doorBack;
    FrameLayout ivResImage;
    RestaurantWaitListFragment defaultFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_main);

        /*if (savedInstanceState == null) {
            Fragment cardFrontFragment = new CardFrontFragment();
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.add(R.id.placardContainer,cardFrontFragment).commit();
        }*/
        ivCardFace = (ImageView) findViewById(R.id.ivCardFace);
        ivCardBack = (ImageView) findViewById(R.id.ivCardBack);
        ivDoor = (ImageView) findViewById(R.id.ivDoor);
        cardRoot = (FrameLayout) findViewById(R.id.cardRoot);
        doorContainer = (RelativeLayout) findViewById(R.id.doorContainer);
        rootLayout = (FrameLayout) findViewById(R.id.rootLayout);
        fragmentRoot = (RelativeLayout) findViewById(R.id.fragmentRoot);
        tvRestaurantName = (TextView) findViewById(R.id.tvRestaurantName);
        doorRoot = (FrameLayout) findViewById(R.id.doorRoot);
        doorBack = (FrameLayout) findViewById(R.id.doorBack);
        ivResImage = (FrameLayout) findViewById(R.id.ivResImage);

        cardRoot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(getContext(), "wooden card clicked", Toast.LENGTH_SHORT).show();
                onCardClick(v);
            }
        });

        //frameLayout = (FrameLayout)findViewById(R.id.activityFrameLayout);
        nvDrawer = (NavigationView) findViewById(R.id.nvDrawer);
        setupDrawerContent(nvDrawer);

        Intent intent = getIntent();
        this.restaurantID = intent.getStringExtra("restaurantID");

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitleTextColor(getResources().getColor(R.color.colorWhite));
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setHomeButtonEnabled(false);
            getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setDisplayShowHomeEnabled(false);
            getSupportActionBar().setHomeAsUpIndicator(null);
        }

        String restaurantName = WaitlistUtils.getRestaurantName(restaurantID);
        getSupportActionBar().setTitle("KERB");
        setSupportActionBar(toolbar);
        tvRestaurantName.setText(restaurantName);

        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryTransparent));
        }

        //fragmentRoot.setY(Utilities.screenHeight);

        dlDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        ImageView headerView = (ImageView) nvDrawer.findViewById(R.id.ivHeader);
        Picasso.with(this).load(WaitlistUtils.getRestaurantLogoURL(restaurantID)).placeholder(R.drawable.header).resize(240, 240).centerInside().into(headerView);
        drawerToggle = setupDrawerToggle();
        if (!isDrawerLocked) {
            dlDrawer.setDrawerListener(drawerToggle);
        }

        // Tie DrawerLayout events to the ActionBarToggle
        dlDrawer.setDrawerListener(drawerToggle);

        // Set default fragment
        nvDrawer.getMenu().getItem(0).setChecked(true);
        FragmentManager fragmentManager = getSupportFragmentManager();
        defaultFragment = new RestaurantWaitListFragment();
        Bundle bundle = new Bundle();
        bundle.putString("restaurantID", restaurantID);
        defaultFragment.setArguments(bundle);
        fragmentManager.beginTransaction().replace(R.id.activityFrameLayout, defaultFragment, "wlsFragment").commit();
    }

    private ActionBarDrawerToggle setupDrawerToggle() {
        return new ActionBarDrawerToggle(this, dlDrawer, toolbar, R.string.drawer_open, R.string.drawer_close) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                //getActionBar().setTitle(title);
                //((FragmentInterface)fragment).showMenuActions();
                invalidateOptionsMenu();
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                //getActionBar().setTitle("Select Option");
                //((FragmentInterface)fragment).hideMenuActions();
                invalidateOptionsMenu();
            }
        };

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_createWaitList) {
            newListDialog();
            return true;
        }
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void setupDrawerContent(final NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        selectDrawerItem(menuItem);
                        return true;
                    }
                });
    }

    public void selectDrawerItem(MenuItem menuItem) {
        // Create a new fragment and specify the planet to show based on
        // position
        Fragment fragment = null;

        Class fragmentClass;
        switch (menuItem.getItemId()) {
            case R.id.currentList:
                fragmentClass = RestaurantWaitListFragment.class;
                break;
            /*case R.id.newList:
                fragmentClass = RestaurantNewListFragment.class;
                break;
            case R.id.archive:
                fragmentClass = RestaurantArchive.class;
                break;
            case R.id.analytics:
                fragmentClass = RestaurantAnalytics.class;
            case R.id.settings:
                fragmentClass = RestaurantSettings.class; */
            case R.id.logout:

                ParseInstallation installation = ParseInstallation.getCurrentInstallation();
                installation.remove(ParseUser.getCurrentUser().getObjectId());
                installation.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e != null) {
                            Log.e(TAG, e.getMessage());
                        }
                    }
                });
                ParseUser.logOut();

                Intent i = new Intent(this, MainActivity.class);
                startActivity(i);
            default:
                fragmentClass = RestaurantWaitListFragment.class;
        }

        try {
            fragment = (Fragment) fragmentClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Insert the fragment by replacing any existing fragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        Bundle bundle = new Bundle();
        bundle.putString("restaurantID", restaurantID);
        fragment.setArguments(bundle);
        fragmentManager.beginTransaction().replace(R.id.activityFrameLayout, fragment, "wlsFragment").commit();

        // Highlight the selected item, update the title, and close the drawer
        menuItem.setChecked(true);
        setTitle(menuItem.getTitle());
        //dlDrawer.closeDrawers();
        if (!isDrawerLocked) {
            dlDrawer.closeDrawer(nvDrawer);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate menu resource file.
        getMenuInflater().inflate(R.menu.menu_restaurant_main, menu);
        return true;
    }

    private void newListDialog() {

        final NiftyDialogBuilder dialogBuilder = NiftyDialogBuilder.getInstance(this);
        dialogBuilder
                .withTitle(getResources().getString(R.string.new_waitlist))
                .withTitleColor(R.color.colorPrimary)
                .withDividerColor("#0e91a1")
                .withDialogColor("#FEFEFE")
                .withMessage("Enter current wait time:")
                .withMessageColor(R.color.divider_color)
                .withEffect(Effectstype.Slidetop)
                .withButton1Text("OK")
                .withButtonDrawable(R.drawable.dialog_bg)
                .withButton2Text("Cancel")
                .isCancelableOnTouchOutside(true)
                .setCustomView(R.layout.new_waitlist_fragment, this)
                .setButton1Click(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int defaultTime = 10;
                        final EditText etDefaultWaitTime = (EditText) dialogBuilder.findViewById(R.id.etDefaultWaitTime);
                        String min = etDefaultWaitTime.getText().toString();
                        if (min != null && !min.isEmpty()) {
                            defaultTime = Integer.valueOf(min);
                        }
                        WaitlistUtils.openWaitList(restaurantID, defaultTime);
                        onCreateWaitList();
                        dialogBuilder.dismiss();
                    }
                })
                .setButton2Click(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialogBuilder.dismiss();
                    }
                })
                .show();

        /*FragmentManager fm = getSupportFragmentManager();
        RestaurantNewListFragment newListDialog = RestaurantNewListFragment.newInstance("Create new Wait List", restaurantID);
        newListDialog.show(fm, "new_waitlist_fragment");*/
    }

    public String getRestaurantID() {
        return restaurantID;
    }


    @Override
    public void onCreateWaitList() {

        waitListFragment = (RestaurantWaitListFragment) getSupportFragmentManager()
                .findFragmentByTag("wlsFragment");
        if (waitListFragment != null) {
            //waitListFragment.refreshWaitListAdapter();
            getSupportFragmentManager()
                    .beginTransaction()
                    .detach(waitListFragment)
                    .attach(waitListFragment)
                    .commit();
            waitListFragment.LoadFragmentData();
        }
    }

    @Override
    public void onAddNewUser() {
        FragmentManager fm = getSupportFragmentManager();
        AddUserToWaitlistFragment editNameDialog = AddUserToWaitlistFragment.newInstance("Add Customer", restaurantID);
        editNameDialog.show(fm, "add_user_to_waitlist_fragment");

    }

    /*private void refreshFragment(Fragment fragment) {
        final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.detach(fragment);
        ft.attach(fragment);
        ft.commit();
    }*/


    public void onCardClick(View view) {
        FlipAnimation flipAnimation = new FlipAnimation(ivCardFace, ivCardBack);

        if (ivCardFace.getVisibility() == View.GONE) {
            flipAnimation.reverse();
        }
        flipAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                ViewCompat.animate(doorContainer).scaleXBy(5).scaleYBy(2.5f).alpha(0).setDuration(1500).setListener(new ViewPropertyAnimatorListenerAdapter() {

                    @Override
                    public void onAnimationEnd(View view) {
                        doorContainer.setVisibility(View.GONE);
                        dlDrawer.setVisibility(View.VISIBLE);
                        Animation topDown = AnimationUtils.loadAnimation(RestaurantMainActivity.this,
                                R.anim.top_down);
                        ivResImage.startAnimation(topDown);
                        ivResImage.setVisibility(View.VISIBLE);

                        topDown.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {
                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                Animation bottomUp = AnimationUtils.loadAnimation(RestaurantMainActivity.this,
                                        R.anim.bottom_up);
                                fragmentRoot.startAnimation(bottomUp);
                                fragmentRoot.setVisibility(View.VISIBLE);

                                bottomUp.setAnimationListener(new Animation.AnimationListener() {
                                    @Override
                                    public void onAnimationStart(Animation animation) {

                                    }

                                    @Override
                                    public void onAnimationEnd(Animation animation) {
                                        defaultFragment.LoadFragmentData();
                                    }

                                    @Override
                                    public void onAnimationRepeat(Animation animation) {

                                    }
                                });
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {

                            }
                        });
                    }
                });
               /* FlipAnimation flipAnimation = new FlipAnimation(doorContainer, doorBack);
                flipAnimation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                doorRoot.startAnimation(flipAnimation);*/


                //ViewCompat.animate(doorContainer).setDuration(1000).xBy(-Utilities.screenWidth);
                //ViewCompat.animate(fragmentRoot).setStartDelay(1500).setDuration(900).yBy(Utilities.screenHeight);

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        cardRoot.startAnimation(flipAnimation);

    }

}



