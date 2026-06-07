package com.example.SAFEFALL;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;

public class GuardianAlertReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "CaretakerAlertChannel";

    @Override
    public void onReceive(Context context, Intent intent) {
        DBHelper db = new DBHelper(context);
        String role = db.getSetting("user_role");

        // ONLY show notification if this device is set to Caretaker Mode
        if ("Caretaker".equals(role)) {
            String location = intent.getStringExtra("location");
            String personId = intent.getStringExtra("person_id");

            showNotification(context, personId, location);
        }
    }

    private void showNotification(Context context, String personId, String location) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Emergency Alerts", NotificationManager.IMPORTANCE_HIGH);
            channel.enableVibration(true);
            manager.createNotificationChannel(channel);
        }

        Intent mapIntent = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse(location));
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, mapIntent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentTitle("🚨 FALL DETECTED!")
                .setContentText("User " + personId + " has fallen. Tap to see location.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        manager.notify(100, builder.build());
    }
}
