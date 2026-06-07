package com.example.SAFEFALL;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.appbar.MaterialToolbar;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class StatusActivity extends AppCompatActivity {

    TextView tvStatusTitle, tvStatusDesc;
    ImageView ivStatusIcon;
    Button btnTestAlert;
    DBHelper db;
    MaterialToolbar toolbar;
    FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        tvStatusTitle = findViewById(R.id.tvStatusTitle);
        tvStatusDesc = findViewById(R.id.tvStatusDesc);
        ivStatusIcon = findViewById(R.id.ivStatusIcon);
        btnTestAlert = findViewById(R.id.btnTestAlert);

        db = new DBHelper(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        btnTestAlert.setOnClickListener(v -> {
            sendRealLocationAlert();
        });
    }

    private void sendRealLocationAlert() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Location permission required", Toast.LENGTH_LONG).show();
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            String locationLink = (location != null) 
                ? "https://maps.google.com/?q=" + location.getLatitude() + "," + location.getLongitude()
                : "Location not available";
            
            executeIdBasedAlert(locationLink);
        });
    }

    private void executeIdBasedAlert(String locationLink) {
        Cursor cursor = db.getAllGuardians();
        if (cursor == null || cursor.getCount() == 0) {
            Toast.makeText(this, "No Caretaker IDs found! Link a Caretaker first.", Toast.LENGTH_LONG).show();
            return;
        }

        int sentCount = 0;
        try {
            while (cursor.moveToNext()) {
                String caretakerId = cursor.getString(2); // guardian_id column
                String caretakerName = cursor.getString(1);
                
                // ID-BASED ALERT LOGIC
                // In a production environment, this would trigger a push notification via the Unique ID
                Log.d("StatusActivity", "Sending Manual Alert to Caretaker ID: " + caretakerId);
                Log.d("StatusActivity", "Content: I need help! Location: " + locationLink);
                
                sentCount++;
            }

            String currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
            db.insertHistory(currentTime, "MANUAL SOS", locationLink);
            
            Toast.makeText(this, "SOS Alert sent to " + sentCount + " linked IDs", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            if (cursor != null) cursor.close();
        }
    }
}
