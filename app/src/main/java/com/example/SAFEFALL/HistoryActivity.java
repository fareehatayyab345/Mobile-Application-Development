package com.example.SAFEFALL;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.google.android.material.appbar.MaterialToolbar;
import java.util.ArrayList;

public class HistoryActivity extends AppCompatActivity {

    ListView historyList;
    DBHelper db;
    ArrayList<AlertHistory> list;
    HistoryAdapter adapter;
    MaterialToolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        historyList = findViewById(R.id.historyList);
        db = new DBHelper(this);
        list = new ArrayList<>();

        loadHistory();

        // Open Google Maps when a history item is clicked
        historyList.setOnItemClickListener((parent, view, position, id) -> {
            String location = list.get(position).location;
            if (location != null && location.startsWith("https://")) {
                try {
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(location));
                    startActivity(mapIntent);
                } catch (Exception e) {
                    Toast.makeText(this, "Could not open map", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "No GPS link for this log", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadHistory() {
        list.clear();
        Cursor c = db.getAllHistory();
        if (c != null) {
            while (c.moveToNext()) {
                // Column 0 is ID, Column 1 is date_time, Column 3 is location (based on table schema)
                list.add(new AlertHistory(c.getInt(0), c.getString(1), c.getString(3)));
            }
            c.close();
        }

        adapter = new HistoryAdapter();
        historyList.setAdapter(adapter);
    }

    // Model Class
    class AlertHistory {
        int id;
        String dateTime, location;
        AlertHistory(int id, String dateTime, String location) {
            this.id = id;
            this.dateTime = dateTime;
            this.location = location;
        }
    }

    // Custom Adapter for UI
    class HistoryAdapter extends BaseAdapter {
        @Override
        public int getCount() { return list.size(); }
        @Override
        public Object getItem(int position) { return list.get(position); }
        @Override
        public long getItemId(int position) { return position; }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(HistoryActivity.this).inflate(R.layout.item_history, parent, false);
            }
            TextView tvDate = convertView.findViewById(R.id.tvHistoryDate);
            TextView tvLoc = convertView.findViewById(R.id.tvHistoryLocation);
            ImageView ivDelete = convertView.findViewById(R.id.ivDeleteHistory);

            AlertHistory h = list.get(position);
            tvDate.setText(h.dateTime);
            
            if (h.location != null && h.location.startsWith("https://")) {
                tvLoc.setText("📍 Tap to view on Map");
                tvLoc.setTextColor(ContextCompat.getColor(HistoryActivity.this, R.color.accent_blue));
            } else {
                tvLoc.setText(h.location);
                tvLoc.setTextColor(ContextCompat.getColor(HistoryActivity.this, R.color.text_sub));
            }

            // Delete specific history item
            ivDelete.setOnClickListener(v -> {
                db.deleteHistoryItem(h.id);
                loadHistory();
                Toast.makeText(HistoryActivity.this, "Log Deleted", Toast.LENGTH_SHORT).show();
            });

            return convertView;
        }
    }
}
