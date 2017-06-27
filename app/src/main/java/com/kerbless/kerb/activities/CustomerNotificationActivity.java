package com.kerbless.kerb.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class CustomerNotificationActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent i = getIntent();

        AlertDialog.Builder notificationAlert = new AlertDialog.Builder(this);
        notificationAlert.setTitle(i.getStringExtra("title"));
        notificationAlert.setMessage(i.getStringExtra("customdata"));
        notificationAlert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finish();
            }
        });

        AlertDialog dialog = notificationAlert.create();
        dialog.show();

    }
}
