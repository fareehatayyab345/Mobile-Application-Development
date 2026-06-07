package com.example.SAFEFALL;

import android.database.Cursor;
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
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import java.util.ArrayList;

public class GuardianActivity extends AppCompatActivity {

    TextInputEditText etName, etPhone;
    MaterialButton btnSave;
    ListView listView;
    DBHelper db;
    ArrayList<Guardian> guardianList;
    GuardianAdapter adapter;
    MaterialToolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guardian);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        etName = findViewById(R.id.etName);
        etPhone = findViewById(R.id.etPhone);
        btnSave = findViewById(R.id.btnSave);
        listView = findViewById(R.id.guardianList);
        db = new DBHelper(this);
        guardianList = new ArrayList<>();

        loadData();

        btnSave.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();
            if (!name.isEmpty() && !phone.isEmpty()) {
                db.insertGuardian(name, phone);
                etName.setText("");
                etPhone.setText("");
                loadData();
                Toast.makeText(this, "Guardian Added Successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Please enter all details", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadData() {
        guardianList.clear();
        Cursor c = db.getAllGuardians();
        if (c != null) {
            while (c.moveToNext()) {
                guardianList.add(new Guardian(c.getInt(0), c.getString(1), c.getString(2)));
            }
            c.close();
        }
        adapter = new GuardianAdapter();
        listView.setAdapter(adapter);
    }

    class Guardian {
        int id;
        String name, phone;
        Guardian(int id, String name, String phone) {
            this.id = id;
            this.name = name;
            this.phone = phone;
        }
    }

    class GuardianAdapter extends BaseAdapter {
        @Override
        public int getCount() { return guardianList.size(); }
        @Override
        public Object getItem(int position) { return guardianList.get(position); }
        @Override
        public long getItemId(int position) { return position; }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(GuardianActivity.this).inflate(R.layout.item_guardian, parent, false);
            }
            TextView tvName = convertView.findViewById(R.id.tvGuardianName);
            TextView tvPhone = convertView.findViewById(R.id.tvGuardianPhone);
            ImageView ivDelete = convertView.findViewById(R.id.ivDelete);

            Guardian g = guardianList.get(position);
            tvName.setText(g.name);
            tvPhone.setText(g.phone);

            ivDelete.setOnClickListener(v -> {
                db.deleteGuardian(g.id);
                loadData();
                Toast.makeText(GuardianActivity.this, "Deleted", Toast.LENGTH_SHORT).show();
            });

            return convertView;
        }
    }
}
