package com.mainpackage;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class FetchContinousLocationService extends Service {
    NotificationCompat.Builder builder;
    Notification notification;
    boolean flag;
    private final String TAG = "MYMSG";
    LocationManager locationManager;
    CustomLocationListener mylocationlistenerobj;

    public FetchContinousLocationService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;

    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "ON Start Command Called");
        Log.d(TAG, "ACTION: " + intent.getAction());

        if (intent.getAction().trim().equals("START SIGNAL")) {
            startForegroundService();
        } else {
            stopForegroundService();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @SuppressLint("MissingPermission")
    private void startForegroundService() {
        Log.d("MYMESSAGE", "START Foreground Service Called");

        Notification mynotif = simpleNotification("Service Running", "This is Message of Foreground Notification");

        // Start foreground service
        // This Method Actually Starts Foreground Service and also creates an ONGOING Notifcation
        startForeground(1, mynotif);

        //new Thread(new myjob()).start();


        ////////   Logic to get CURRENT LOCATIONS /////////////////


        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);


        //---check if GPS_PROVIDER is enabled---

        boolean gpsStatus = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);


        //---check if NETWORK_PROVIDER is enabled---



        //int flag=0;


        mylocationlistenerobj = new CustomLocationListener();


        // check which provider is enabled

        if (gpsStatus == false )

        {

            Toast.makeText(this, "GPS is disabled", Toast.LENGTH_LONG).show();


            //---display the "Location services" settings page---

            Intent in = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);

            startActivity(in);

        }


        if (gpsStatus == true)

        {

            Toast.makeText(this, "GPS is Enabled, using it", Toast.LENGTH_LONG).show();

            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, mylocationlistenerobj);

        }


    }



    public Notification simpleNotification(String title,String message)

    {

        String CHANNEL_ID=  "CHANNEL222";
            NotificationCompat.Builder builder=new NotificationCompat.Builder(this, CHANNEL_ID);
        builder.setContentTitle(title);
        builder.setContentText(message);
        builder.setSmallIcon(R.drawable.ic_ham);
        builder.setContentInfo("Con Info");
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(),R.drawable.ic_ham));

        // EXTRA Code needed (for devcies < 8.0), since we are creating channels
        builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
        NotificationManager notificationManager = (NotificationManager)(getSystemService(NOTIFICATION_SERVICE));
        Notification notification =  builder.build();

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
            Log.d("MYMESSAGE","NEW CODE Oreo");
        }
        return notification;
        // Dont Notify here, Foreground Service will do to
        //notificationManager.notify(20,notification);
    }
    private void stopForegroundService()
    {
        Log.d("MYMESSAGE", "Stop foreground service.");
        locationManager.removeUpdates(mylocationlistenerobj);

        // Stop foreground service and remove the notification.
        stopForeground(true);

        // Stop the foreground service.
        stopSelf();
    }





class CustomLocationListener implements LocationListener {

    @Override
    public void onLocationChanged(Location location) {
        double lat, lon;
        lat = location.getLatitude();
        lon = location.getLongitude();

        Log.d("MYMSG: ", "Latitude: " + lat + " Longitude:" + lon);

        new Thread(new AddLocationOnFirebase(lat, lon)).start();

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
}

///////////////////////////////////////////////////////////////////////////////////////////

public class AddLocationOnFirebase implements Runnable {

    double lat, lon;

    public AddLocationOnFirebase(double lat, double lon) {
        this.lat = lat;
        this.lon = lon;
    }

    @Override
    public void run() {

        SharedPreferences sharedPreferences = getSharedPreferences("myprefs", MODE_PRIVATE);
        String phoneNumber = sharedPreferences.getString("session_phone", null);

        final long time = System.currentTimeMillis();
        java.sql.Date d = new java.sql.Date(time);
        final String date = d.toString();


        DatabaseReference locationsReference = FirebaseDatabase.getInstance().getReference("users").child(phoneNumber).child("locations");
        locationsReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<LocationDAO> my_locations = new ArrayList<>();
                if (dataSnapshot.exists()) {
                    my_locations = (ArrayList<LocationDAO>) dataSnapshot.getValue();
                }
                my_locations.add(new LocationDAO(date, time, lat, lon));
                dataSnapshot.getRef().setValue(my_locations);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("MYMSG", "ERROR_FROM_SERVICE" + databaseError.getMessage());

            }
        });

        FirebaseDatabase
                .getInstance()
                .getReference("users")
                .child(phoneNumber)
                .child("last_latitude").setValue(lat).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isComplete()) {
                    Log.d("MYMSG", "Latitude Updated to Firebase");
                } else {
                    Log.e("MYMSG", "Failed Latitude Update to Firebase");

                }
            }
        });
        FirebaseDatabase
                .getInstance()
                .getReference("users")
                .child(phoneNumber)
                .child("last_longitude").setValue(lon).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isComplete()) {
                    Log.d("MYMSG", "last_longitude Updated to Firebase");
                } else {
                    Log.e("MYMSG", "Failed last_longitude Update to Firebase");

                }
            }
        });

    }
////////////////////////////////////////////
}

}
