package com.limwernjie.basophobiaphone;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.hardware.SensorEvent;
import android.media.MediaActionSound;
import android.media.MediaPlayer;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    boolean isFalling = false;

    MovementDetector movementDetector;
    MediaPlayer screamPlayer;
    MediaPlayer painPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final MainActivity activity = this;

        createPersistentNotification();

        screamPlayer = MediaPlayer.create(MainActivity.this, R.raw.scream);
        screamPlayer.setVolume(1.0f, 1.0f);

        painPlayer = MediaPlayer.create(MainActivity.this, R.raw.pain);
        painPlayer.setVolume(1.0f, 1.0f);

        movementDetector = MovementDetector.getInstance(this);
        movementDetector.addListener(new MovementDetector.ActivityListener() {
            @Override
            public void onMotionActivityUpdated(SensorEvent event, float acceleration) {
                if (acceleration < 1) {
                    if (!isFalling) {
                        isFalling = true;

                        activity.findViewById(R.id.dummyView).getRootView().setBackgroundColor(Color.RED);


                        if (painPlayer.isPlaying()) {
                            painPlayer.stop();
                        }
                        if (!screamPlayer.isPlaying()) {
                            screamPlayer = MediaPlayer.create(MainActivity.this, R.raw.scream);
                            screamPlayer.start();
                        }
                    }
                } else {
                    if (isFalling) {
                        isFalling = false;

                        activity.findViewById(R.id.dummyView).getRootView().setBackgroundColor(Color.WHITE);

                        if (screamPlayer.isPlaying()) {
                            screamPlayer.stop();
                        }
                        if (!painPlayer.isPlaying()) {
                            painPlayer = MediaPlayer.create(MainActivity.this, R.raw.pain);
                            painPlayer.start();
                        }
                    }
                }

            }
        });

        movementDetector.start();

    }

    public void createPersistentNotification() {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 01, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        Notification.Builder builder = new Notification.Builder(getApplicationContext());
        builder.setContentTitle("Basophobia Phone running");
        builder.setContentText("Your phone will scream anytime the phone is in free fall.");
        builder.setSubText("I'm not liable for any damages done to your phone.");
        builder.setNumber(101);
        builder.setContentIntent(pendingIntent);
        builder.setSmallIcon(R.mipmap.ic_launcher_round);
        builder.setAutoCancel(true);
        builder.setPriority(Notification.PRIORITY_DEFAULT);
        builder.setOngoing(true);
        Notification notification = builder.build();
        NotificationManager notificationManger =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManger.notify(01, notification);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        movementDetector.stop();
    }
}
