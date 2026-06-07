package com.example.SAFEFALL;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.util.Log;

public class FallDetector implements SensorEventListener {

    public interface OnFallListener {
        void onFallDetected();
    }

    private OnFallListener listener;
    private static final float GRAVITY = 9.8f;
    private static final float PEAK_THRESHOLD = 3.0f * GRAVITY; // > 3g (Approx 29.4)
    private static final float LOW_ACTIVITY_THRESHOLD = 0.5f * GRAVITY; // Low movement
    private static final long ANALYSIS_BUFFER_MS = 500; // 0.5 second buffer

    private boolean peakDetected = false;
    private long peakTime = 0;

    public FallDetector(OnFallListener listener) {
        this.listener = listener;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        // 3. Calculate movement magnitude: sqrt(x*x + y*y + z*z)
        double magnitude = Math.sqrt(x * x + y * y + z * z);

        // 4. Detect peak acceleration (Impact)
        if (magnitude > PEAK_THRESHOLD) {
            peakDetected = true;
            peakTime = System.currentTimeMillis();
            Log.d("FallDetector", "Peak impact detected: " + magnitude);
        }

        // 5. Check for period of low activity after peak (The 0.5s Buffer)
        if (peakDetected) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - peakTime > ANALYSIS_BUFFER_MS) {
                // If magnitude is now very low (lying still), confirm fall
                if (magnitude < (GRAVITY + 2.0f) && magnitude > (GRAVITY - 2.0f)) {
                    peakDetected = false; // Reset
                    if (listener != null) {
                        listener.onFallDetected();
                    }
                } else if (currentTime - peakTime > 2000) {
                    // If after 2 seconds they are still moving a lot, it was just a bump
                    peakDetected = false;
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}
}
