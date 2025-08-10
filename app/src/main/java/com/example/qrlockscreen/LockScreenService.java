package com.example.qrlockscreen;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;
import androidx.core.app.NotificationCompat;

public class LockScreenService extends Service {

    private BroadcastReceiver screenOffReceiver;

    @Override
    public void onCreate() {
        super.onCreate();
        // Create a notification channel for the foreground service
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("lockscreen_service_channel",
                    "Lock Screen Service", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        // Start the service as a foreground service
        Notification notification = new NotificationCompat.Builder(this, "lockscreen_service_channel")
                .setContentTitle("QR Lock Screen")
                .setContentText("Service is running to show QR code on lock screen.")
                .setSmallIcon(R.mipmap.ic_launcher) // You should replace this with a real icon
                .build();
        startForeground(1, notification);

        // Register a receiver to listen for screen off events
        screenOffReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) {
                    Intent lockScreenIntent = new Intent(context, LockScreenActivity.class);
                    lockScreenIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(lockScreenIntent);
                }
            }
        };
        registerReceiver(screenOffReceiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Unregister the receiver when the service is destroyed
        if (screenOffReceiver != null) {
            unregisterReceiver(screenOffReceiver);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
