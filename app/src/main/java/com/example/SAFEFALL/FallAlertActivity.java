package com.example.SAFEFALL;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.button.MaterialButton;
import com.google.auth.oauth2.GoogleCredentials;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class FallAlertActivity extends AppCompatActivity {

    private TextView tvCountdown;
    private CountDownTimer timer;
    private DBHelper db;
    private FusedLocationProviderClient fusedLocationClient;
    private String currentLocationsLink = "Location not available";
    private Ringtone alertTone;

    private static final String PROJECT_ID = "safefall-d31f1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fall_alert);

        tvCountdown = findViewById(R.id.tvCountdown);
        MaterialButton btnIAmOkay = findViewById(R.id.btnIAmOkay);
        db = new DBHelper(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            alertTone = RingtoneManager.getRingtone(getApplicationContext(), notification);
            if (alertTone != null) alertTone.play();
        } catch (Exception e) {
            Log.e("FallAlert", "Error playing siren", e);
        }

        getLastLocation();

        timer = new CountDownTimer(5000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                tvCountdown.setText(String.valueOf(millisUntilFinished / 1000));
            }

            @Override
            public void onFinish() {
                stopSiren();
                sendCaretakerAlert();
            }
        }.start();

        btnIAmOkay.setOnClickListener(v -> {
            stopSiren();
            if (timer != null) timer.cancel();
            Toast.makeText(this, "Alert Cancelled.", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private void stopSiren() {
        if (alertTone != null && alertTone.isPlaying()) alertTone.stop();
    }

    private void getLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) return;
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                currentLocationsLink = "https://maps.google.com/?q=" + location.getLatitude() + "," + location.getLongitude();
            }
        });
    }

    private void sendCaretakerAlert() {
        Cursor cursor = db.getAllGuardians();
        String myId = db.getSetting("my_id");
        
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                String caretakerId = cursor.getString(2);
                sendFcmNotificationV1(caretakerId, myId, currentLocationsLink);
            }
            
            String time = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date());
            db.insertHistory(time, "FALL DETECTED", currentLocationsLink);
            Toast.makeText(this, "Emergency Alerts Sent via Cloud!", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "No Caretaker ID linked!", Toast.LENGTH_LONG).show();
        }
        if (cursor != null) cursor.close();
        finish();
    }

    private void sendFcmNotificationV1(String caretakerId, String personId, String location) {
        new Thread(() -> {
            try {
                String accessToken = getAccessToken();
                if (accessToken == null) {
                    Log.e("FCM", "Missing service_account.json in res/raw. Notifications won't be sent.");
                    return;
                }
                
                OkHttpClient client = new OkHttpClient();
                MediaType jsonType = MediaType.parse("application/json; charset=utf-8");
                if (jsonType == null) return;

                JSONObject message = new JSONObject();
                JSONObject msgContent = new JSONObject();
                JSONObject data = new JSONObject();
                JSONObject notification = new JSONObject();

                data.put("person_id", personId);
                data.put("location", location);
                
                notification.put("title", "🚨 FALL DETECTED: " + personId);
                notification.put("body", "Emergency! A fall was detected. Tap for location.");

                msgContent.put("topic", "topic_" + caretakerId);
                msgContent.put("data", data);
                msgContent.put("notification", notification);

                message.put("message", msgContent);

                RequestBody body = RequestBody.create(message.toString(), jsonType);
                Request request = new Request.Builder()
                        .url("https://fcm.googleapis.com/v1/projects/" + PROJECT_ID + "/messages:send")
                        .post(body)
                        .addHeader("Authorization", "Bearer " + accessToken)
                        .build();

                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        Log.e("FCM", "v1 Failed: " + e.getMessage());
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                        if (response.body() != null) {
                            Log.d("FCM", "v1 Success: " + response.body().string());
                        }
                    }
                });
            } catch (Exception e) {
                Log.e("FCM", "v1 Error: " + e.getMessage());
            }
        }).start();
    }

    private String getAccessToken() throws IOException {
        int resId = getResources().getIdentifier("service_account", "raw", getPackageName());
        if (resId == 0) return null;

        InputStream is = getResources().openRawResource(resId);
        GoogleCredentials credentials = GoogleCredentials.fromStream(is)
                .createScoped(Collections.singletonList("https://www.googleapis.com/auth/firebase.messaging"));
        credentials.refreshIfExpired();
        return credentials.getAccessToken().getTokenValue();
    }

    @Override
    protected void onDestroy() {
        stopSiren();
        if (timer != null) timer.cancel();
        super.onDestroy();
    }
}
