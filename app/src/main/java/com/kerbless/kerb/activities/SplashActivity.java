package com.kerbless.kerb.activities;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.kerbless.kerb.R;
import com.kerbless.kerb.utils.Utilities;

public class SplashActivity extends AppCompatActivity {

    ImageView kerbLogo;
    ImageView ivFork;
    ImageView ivPlate;
    ImageView ivKnife;
    TextView tvTagline;
    Handler handler;
    static int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryTransparent));
        }

        ivFork = (ImageView) findViewById(R.id.ivFork);
        ivFork.setX(-Utilities.screenWidth);
        ivPlate = (ImageView) findViewById(R.id.ivPlate);
        ivKnife = (ImageView) findViewById(R.id.ivKnife);
        //ivKnife.setAlpha(0);
        tvTagline = (TextView) findViewById(R.id.tvTagline);

        kerbLogo = (ImageView) findViewById(R.id.kerbLogo);
        Glide.with(this).load(R.drawable.kerb).listener(new RequestListener<Integer, GlideDrawable>() {
            @Override
            public boolean onException(Exception e, Integer model, Target<GlideDrawable> target, boolean isFirstResource) {
                return false;
            }

            @Override
            public boolean onResourceReady(GlideDrawable resource, Integer model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                startAnimation();
                return false;
            }
        }).into(kerbLogo);
    }

    private void startAnimation() {
        OvershootInterpolator OVERSHOOT_INTERPOLATOR = new OvershootInterpolator(4);

        final AnimatorSet animatorSet = new AnimatorSet();
        AnimatorSet animatorSetTogether = new AnimatorSet();
        final AnimatorSet tagLineAnim = new AnimatorSet();

        ObjectAnimator forkAnimator = ObjectAnimator.ofFloat(ivFork, View.X, -Utilities.screenWidth, 0).setDuration(1000);
        ObjectAnimator forkRotate = ObjectAnimator.ofFloat(ivFork, View.ROTATION, 0, 360).setDuration(300);
        ObjectAnimator knifeAnimator = ObjectAnimator.ofFloat(ivKnife, View.ALPHA, 0, 1).setDuration(1000);
        ObjectAnimator knifeRotate = ObjectAnimator.ofFloat(ivKnife, View.ROTATION, 0, 360).setDuration(300);
        ObjectAnimator plateXAnimator = ObjectAnimator.ofFloat(ivPlate, View.SCALE_X, 0, 1).setDuration(2000);
        ObjectAnimator plateYAnimator = ObjectAnimator.ofFloat(ivPlate, View.SCALE_Y, 0, 1).setDuration(2000);
        ObjectAnimator plateRotate = ObjectAnimator.ofFloat(ivPlate, View.ROTATION, 0, 360).setDuration(2000);

        final ObjectAnimator tagLineXAnimator = ObjectAnimator.ofFloat(tvTagline, View.SCALE_X, 0.2f, 1f);
        tagLineXAnimator.setInterpolator(OVERSHOOT_INTERPOLATOR);
        final ObjectAnimator tagLineYAnimator = ObjectAnimator.ofFloat(tvTagline, View.SCALE_Y, 0.2f, 1f);
        tagLineYAnimator.setInterpolator(OVERSHOOT_INTERPOLATOR);

        plateYAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {}

            @Override
            public void onAnimationEnd(Animator animation) {
                animatorSet.start();
            }

            @Override
            public void onAnimationCancel(Animator animation) {}

            @Override
            public void onAnimationRepeat(Animator animation) {}
        });

        knifeRotate.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                tvTagline.setAlpha(1);
                tagLineAnim.start();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });

        animatorSetTogether.play(plateXAnimator).with(plateYAnimator).with(plateRotate);
        tagLineAnim.play(tagLineXAnimator).with(tagLineYAnimator);
        animatorSet.playSequentially(forkAnimator, knifeAnimator, forkRotate, knifeRotate);
        animatorSetTogether.start();

        handler = new Handler();
        count = 0;
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if(count == 0)
                    handler.postDelayed(this, 5500);
                else
                {
                    Intent intent = new Intent(SplashActivity.this, ListingsActivity.class);
                    startActivity(intent);
                }
                count ++;
            }
        };
        handler.post(runnable);
    }
}
