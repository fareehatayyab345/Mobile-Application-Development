package com.example.SAFEFALL;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;
import androidx.core.app.NotificationCompat;

public class FallDetectionService extends Service implements SensorEventListener {

    private static final String CHANNEL_ID = "FallDetectionChannel";
    private SensorManager sensorManager;
    private Sensor accelerometer;

    // High-Sensitivity Constants
    private static final float G_FORCE = 9.81f;
    private static final float PEAK_THRESHOLD = 2.5f * G_FORCE; // Reduced to 2.5g for easier testing
    private static final float STATIONARY_THRESHOLD = 2.0f; 
    private static final long BUFFER_MS = 500; 

    private boolean isPeakDetected = false;
    private long peakTimestamp = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        startAsForeground();
        initSensors();
    }

    private void startAsForeground() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Fall Detection Active", NotificationManager.IMPORTANCE_LOW);
            getSystemService(NotificationManager.class).createNotificationChannel(channel);
        }

        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("SafeFall: Monitoring Active")
                .setContentText("Your safety system is running in the background.")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .build();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_HEALTH);
        } else {
            startForeground(1, notification);
        }
    }

    private void initSensors() {
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            if (accelerometer != null) {
                // CHANGED: SENSOR_DELAY_GAME is much faster than NORMAL. Better for detecting falls.
                sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
            }
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        double magnitude = Math.sqrt(x * x + y * y + z * z);

        // Detect Impact
        if (magnitude > PEAK_THRESHOLD) {
            isPeakDetected = true;
            peakTimestamp = System.currentTimeMillis();
            Log.d("FallDetection", "Peak Detected: " + magnitude);
        }

        if (isPeakDetected) {
            long timeDiff = System.currentTimeMillis() - peakTimestamp;
            
            if (timeDiff > BUFFER_MS) {
                // If magnitude is near 1g (gravity), user is stationary
                if (Math.abs(magnitude - G_FORCE) < STATIONARY_THRESHOLD) {
                    isPeakDetected = false; 
                    triggerFallAlert();
                } else if (timeDiff > 2000) {
                    isPeakDetected = false;
                }
            }
        }
    }

    private void triggerFallAlert() {
        // We use MainLooper to ensure Toast shows up correctly from a background service
        new Handler(Looper.getMainLooper()).post(() -> 
            Toast.makeText(getApplicationContext(), "⚠️ FALL DETECTED! Starting Countdown...", Toast.LENGTH_LONG).show()
        );

        Intent alertIntent = new Intent(this, FallAlertActivity.class);
        alertIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(alertIntent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) { return START_STICKY; }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    @Override
    public void onDestroy() {
        if (sensorManager != null) sensorManager.unregisterListener(this);
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }
}
