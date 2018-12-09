package com.mainpackage;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ib.custom.toast.CustomToast;

import java.util.Date;

public class FetchContinousLocationService extends Service {
    NotificationCompat.Builder builder;
    Notification notification;
    boolean flag;
    private final String TAG = "MYMSG";
    LocationManager locationManager;
    CustomLocationListener mylocationlistenerobj;
    static boolean isServiceStarted;
    String phoneNumber;

    public FetchContinousLocationService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;

    }

    @Override
    public void onCreate() {
        Log.d("MYMSG", "In On Create");
        isServiceStarted = false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "ON Start Command Called");
        Log.d(TAG, "ACTION: " + intent.getAction());

        if (intent.getAction().trim().equals("START SIGNAL")) {
            if (!isServiceStarted) {
                startForegroundService();
                isServiceStarted = true;
            }
        } else {
            stopForegroundService();
        }
        return super.onStartCommand(intent, flags, startId);
    }


    private void startForegroundService() {
        Notification mynotif = simpleNotification("Service Running", "Live Tracking Enabled");
        startForeground(1, mynotif);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        boolean gpsStatus = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean networkStatus = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        mylocationlistenerobj = new CustomLocationListener();

        if (!gpsStatus) {
            CustomToast.makeErrorToast(this, "GPS is disabled", View.VISIBLE).show();
            Intent in = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            in.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(in);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            CustomToast.makeInfoToast(FetchContinousLocationService.this, "Location Not Granted ! Live Tracking Will Not Work ", View.VISIBLE).show();
            return;
        } else {
            if (gpsStatus) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 0.5f, mylocationlistenerobj);
            } else if (networkStatus) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10000, 0.5f, mylocationlistenerobj);
            }
        }


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
        Notification notification = builder.build();

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
        locationManager.removeUpdates(mylocationlistenerobj);
        stopForeground(true);
        stopSelf();
    }

    class CustomLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {
            double lat, lon;
            lat = location.getLatitude();
            lon = location.getLongitude();

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

    public class AddLocationOnFirebase implements Runnable {

        double lat, lon;

        public AddLocationOnFirebase(double lat, double lon) {
            this.lat = lat;
            this.lon = lon;
        }

        @Override
        public void run() {

            SharedPreferences sharedPreferences = getSharedPreferences("myprefs", MODE_PRIVATE);
            phoneNumber = sharedPreferences.getString("session_phone", null);

            final String format = GlobalApp.simpleDateFormat.format(new Date());

            final DatabaseReference locationsReference = FirebaseDatabase.getInstance().getReference("users").child(phoneNumber).child("locations");
            locationsReference.push().setValue(new LocationDAO(format, lat, lon));
            updateLastLocation(lat, lon);
        }

    }

    void updateLastLocation(double lat, double lon) {
        FirebaseDatabase
                .getInstance()
                .getReference("users")
                .child(phoneNumber)
                .child("last_latitude").setValue(lat);
        FirebaseDatabase
                .getInstance()
                .getReference("users")
                .child(phoneNumber)
                .child("last_longitude").setValue(lon);
    }
}
