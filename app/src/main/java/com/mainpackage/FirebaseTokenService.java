package com.mainpackage;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class FirebaseTokenService extends FirebaseMessagingService {

    @Override
    public void onNewToken(String s) {
        Log.d("MYMSG", "Token Generated " + s);
        final SharedPreferences myprefs = getSharedPreferences("myprefs", Context.MODE_PRIVATE);
        String num = myprefs.getString("session_phone", "");
        if (!num.isEmpty()) {
            FirebaseDatabase.getInstance().getReference("tokens").child(GlobalApp.phone_number).setValue(s).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    SharedPreferences.Editor edit = myprefs.edit();
                    edit.putString("token_generated", "true");
                    edit.commit();

                }
            });
        }
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, simpleNotification("Social App", remoteMessage.getData().get("title")));
        RemoteMessage.Notification notification = remoteMessage.getNotification();
    }

    public Notification simpleNotification(String title, String message) {
        String CHANNEL_ID = "CHANNEL222";
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID);
        builder.setContentTitle(title);
        builder.setContentText(message);
        builder.setSmallIcon(R.drawable.ic_ham);
        builder.setContentInfo("Con Info");
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_ham));

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
}
