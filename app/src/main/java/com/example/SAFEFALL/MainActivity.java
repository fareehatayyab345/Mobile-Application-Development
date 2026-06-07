package com.example.SAFEFALL;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.splashscreen.SplashScreen;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private LinearLayout panelPerson, panelCaretaker;
    private MaterialButton btnManualEmergency;
    private TextView tvMyId, tvStatusDescription;
    private MaterialButtonToggleGroup toggleRole;
    private DBHelper db;
    private boolean keep = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Handle splash screen
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
        
        // Keep splash screen visible for 3 seconds for testing
        splashScreen.setKeepOnScreenCondition(() -> keep);
        new Handler(Looper.getMainLooper()).postDelayed(() -> keep = false, 3000);
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            db = new DBHelper(this);
            
            // Initialize Views
            panelPerson = findViewById(R.id.panelPerson);
            panelCaretaker = findViewById(R.id.panelCaretaker);
            btnManualEmergency = findViewById(R.id.btnManualEmergency);
            tvMyId = findViewById(R.id.tvMyId);
            tvStatusDescription = findViewById(R.id.tvStatusDescription);
            toggleRole = findViewById(R.id.toggleRole);

            setupRoleManagement();
            checkAndRequestPermissions();

            // Set up Click Listeners
            findViewById(R.id.btnAlert).setOnClickListener(v -> startActivity(new Intent(this, StatusActivity.class)));
            findViewById(R.id.btnGuardian).setOnClickListener(v -> startActivity(new Intent(this, GuardianActivity.class)));
            findViewById(R.id.btnHistory).setOnClickListener(v -> startActivity(new Intent(this, HistoryActivity.class)));
            
            if (btnManualEmergency != null) {
                btnManualEmergency.setOnClickListener(v -> startActivity(new Intent(this, FallAlertActivity.class)));
            }
        } catch (Exception e) {
            Log.e("MainActivity", "Init error", e);
        }
    }

    private void setupRoleManagement() {
        String myId = db.getSetting("my_id");
        String currentRole = db.getSetting("user_role");

        if (tvMyId != null) tvMyId.setText("My ID: " + (myId != null ? myId : "Unknown"));

        // Set initial UI based on saved role
        if ("Caretaker".equals(currentRole)) {
            toggleRole.check(R.id.btnRoleCaretaker);
            updateUIForCaretaker(myId);
        } else {
            toggleRole.check(R.id.btnRolePerson);
            updateUIForPerson();
        }

        // Handle Role Changes
        toggleRole.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.btnRolePerson) {
                    db.updateSetting("user_role", "Person");
                    FirebaseMessaging.getInstance().unsubscribeFromTopic("topic_" + myId);
                    updateUIForPerson();
                    startFallService();
                } else if (checkedId == R.id.btnRoleCaretaker) {
                    db.updateSetting("user_role", "Caretaker");
                    updateUIForCaretaker(myId);
                    stopFallService();
                }
            }
        });
    }

    private void updateUIForPerson() {
        if (panelPerson != null) panelPerson.setVisibility(View.VISIBLE);
        if (panelCaretaker != null) panelCaretaker.setVisibility(View.GONE);
        if (tvStatusDescription != null) tvStatusDescription.setText("Monitoring Active");
    }

    private void updateUIForCaretaker(String myId) {
        if (panelPerson != null) panelPerson.setVisibility(View.GONE);
        if (panelCaretaker != null) panelCaretaker.setVisibility(View.VISIBLE);

        if (myId != null) {
            FirebaseMessaging.getInstance().subscribeToTopic("topic_" + myId);
        }
    }

    private void checkAndRequestPermissions() {
        List<String> permissionsNeeded = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
        if (!permissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsNeeded.toArray(new String[0]), 101);
        } else {
            startFallService();
        }
    }

    private void startFallService() {
        if ("Person".equals(db.getSetting("user_role"))) {
            Intent intent = new Intent(this, FallDetectionService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent);
            } else {
                startService(intent);
            }
        }
    }

    private void stopFallService() {
        stopService(new Intent(this, FallDetectionService.class));
    }
}
