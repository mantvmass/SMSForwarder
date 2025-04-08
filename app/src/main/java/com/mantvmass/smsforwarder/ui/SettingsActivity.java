package com.mantvmass.smsforwarder.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.mantvmass.smsforwarder.R;
import com.mantvmass.smsforwarder.utils.DatabaseHelper;

public class SettingsActivity extends AppCompatActivity {

    private EditText etUrl;
    private Button btnSave;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        etUrl = findViewById(R.id.etUrl);
        btnSave = findViewById(R.id.btnSave);
        dbHelper = new DatabaseHelper(this);

        // โหลด URL ปัจจุบันมาแสดง
        etUrl.setText(dbHelper.getUrl());

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = etUrl.getText().toString().trim();
                if (url.isEmpty()) {
                    Toast.makeText(SettingsActivity.this, "Please enter a URL", Toast.LENGTH_SHORT).show();
                    return;
                }
                dbHelper.saveUrl(url);
                Toast.makeText(SettingsActivity.this, "URL saved", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }
}