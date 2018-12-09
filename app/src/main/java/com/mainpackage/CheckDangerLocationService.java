package com.mainpackage;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class CheckDangerLocationService extends Service {

    ArrayList<Danger> dangerLocationList = new ArrayList<>();
    String phone;
    static boolean isStarted = true;
    static boolean isPresentInDanger = false;
    static String dangerLocationId;

    public CheckDangerLocationService() {

    }

    @Override
    public void onCreate() {
        SharedPreferences myprefs = getSharedPreferences("myprefs", Context.MODE_PRIVATE);
        phone = myprefs.getString("session_phone", "");
        isStarted = false;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getAction().trim().equals("START SERVICE")) {
            if (!isStarted) {
                startForegroundService();
                isStarted = true;
            }
        } else {
            stopForegroundService();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    public void startForegroundService() {
        Notification mynotif = simpleNotification("Social App", "Running");
        startForeground(2, mynotif);

        FirebaseDatabase.getInstance().getReference("danger").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot d : dataSnapshot.getChildren()) {
                    dangerLocationList.add(d.getValue(Danger.class));
                    if (dangerLocationList.size() == dataSnapshot.getChildrenCount()) {
                        Log.d("MYMSG", "List Size " + dangerLocationList.size());
                        checkDanger();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    public Notification simpleNotification(String title, String message) {
        String CHANNEL_ID = "CHANNEL222";
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID);
        builder.setContentTitle(title);
        builder.setContentText(message);
        builder.setSmallIcon(R.drawable.gole);
        builder.setContentInfo("Con Info");
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.gole));

        // EXTRA Code needed (for devcies < 8.0), since we are creating channels
        builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
        NotificationManager notificationManager = (NotificationManager) (getSystemService(NOTIFICATION_SERVICE));
        Notification notification = builder
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .build();

        //////////// EXTRA CODE  to Handle Oreo Devices   ///////////
        ////// Since Oreo Devices uses Notification Channels    /////
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "My Channel Name";

            String description = "My Channel Description";

            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system
            notificationManager.createNotificationChannel(channel);
            Log.d("MYMESSAGE", "NEW CODE Oreo");
        }
        return notification;
        // Dont Notify here, Foreground Service will do to
        //notificationManager.notify(20,notification);
    }

    private void stopForegroundService() {
        stopForeground(true);
        stopSelf();
    }

    public void checkDanger() {
        FirebaseDatabase.getInstance().getReference("users")
                .child(phone)
                .child("locations")
                .limitToLast(1)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            Log.d("MYMSG", "Ds" + dataSnapshot.toString());
                            LocationDAO locationDAO = dataSnapshot.getChildren().iterator().next().getValue(LocationDAO.class);
                            double latitude = locationDAO.getLatitude();
                            double longitude = locationDAO.getLongitude();
                            Location location = new Location("A");
                            location.setLatitude(latitude);
                            location.setLongitude(longitude);
                            for (Danger danger : dangerLocationList) {
                                Location dangerLocation = new Location("B");
                                dangerLocation.setLatitude(danger.getLat());
                                dangerLocation.setLongitude(danger.getLng());
                                float distance = location.distanceTo(dangerLocation) / 1000;
                                double radius = (danger.getRadius() * 2) / 15000;
                                if (distance <= radius) {
                                    Notification notification = simpleNotification("Social App", "You Are In Danger Location ( " + danger.getLocation() + " ) marked by " + danger.getHost() + " with reason " + danger.getReason().toUpperCase());
                                    NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                                    if (nm != null) {
                                        nm.notify(3, notification);
                                        isPresentInDanger = true;
                                    }
                                    break;
                                } else {
                                    if (isPresentInDanger) {
                                        Notification notification = simpleNotification("Social App", "You Are now out of danger location ");
                                        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                                        if (nm != null) {
                                            nm.notify(3, notification);
                                            isPresentInDanger = false;
                                        }
                                    }
                                    Log.d("MYMSG", "not in danger");
                                }
                            }

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }
}
