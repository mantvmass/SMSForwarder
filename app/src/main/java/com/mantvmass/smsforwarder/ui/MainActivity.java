package com.mantvmass.smsforwarder.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mantvmass.smsforwarder.R;
import com.mantvmass.smsforwarder.model.SMSMessage;
import com.mantvmass.smsforwarder.service.ForwardService;
import com.mantvmass.smsforwarder.service.SMSBroadcastReceiver;

import java.util.ArrayList;
import java.util.List;

import android.Manifest;
import android.content.pm.PackageManager;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_SMS_PERMISSION = 123;
    private static boolean isCanReceive = false;

    // Views
    private Button btnStart, btnStop;
    private RecyclerView rvSMSList;
    private SMSAdapter smsAdapter;
    private List<SMSMessage> smsList;

    private BroadcastReceiver smsReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        matchView();

        // ตั้งค่า RecyclerView สำหรับแสดงรายการ SMS
        smsList = new ArrayList<>();
        smsAdapter = new SMSAdapter(this, smsList, this::deleteSMS, this::reForwardSMS);
        rvSMSList.setLayoutManager(new LinearLayoutManager(this));
        rvSMSList.setAdapter(smsAdapter);

        // ลงทะเบียน BroadcastReceiver เพื่อรับ SMS ใหม่
        smsReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // รับข้อมูล SMS ใหม่จาก Broadcast
                String from = intent.getStringExtra("from");
                String message = intent.getStringExtra("message");
                String timestamp = intent.getStringExtra("timestamp");
                SMSMessage sms = new SMSMessage(null, from, message, timestamp);
                smsAdapter.addSMS(sms); // เพิ่ม SMS ใหม่ที่ด้านบนของรายการ
            }
        };
        registerReceiver(smsReceiver, new IntentFilter(SMSBroadcastReceiver.SMS_RECEIVED_ACTION));

        // ตรวจสอบ permission สำหรับการรับและอ่าน SMS
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.RECEIVE_SMS,
                    Manifest.permission.READ_SMS
            }, REQUEST_SMS_PERMISSION);
        } else {
            isCanReceive = true;
            loadSMS(); // โหลด SMS ถ้ามี permission แล้ว
        }

        // ตั้งค่าปุ่มเริ่มต้นและหยุดการทำงานของ ForwardService
        updateButtonVisibility(); // อัพเดทการแสดงปุ่ม Start/Stop
        btnStart.setOnClickListener(view -> {
            if (isCanReceive) {
                startForwardService();
                updateButtonVisibility();
            } else {
                Toast.makeText(MainActivity.this, "SMS permission denied.", Toast.LENGTH_SHORT).show();
            }
        });

        btnStop.setOnClickListener(view -> {
            stopService();
            updateButtonVisibility();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // ยกเลิกการลงทะเบียน BroadcastReceiver เมื่อ Activity ถูกทำลาย
        unregisterReceiver(smsReceiver);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_SMS_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // ถ้าผู้ใช้ให้ permission สำเร็จ
                isCanReceive = true;
                loadSMS(); // โหลด SMS หลังจากได้รับ permission
            } else {
                Toast.makeText(this, "SMS permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // เริ่มต้น ForwardService เพื่อส่งต่อ SMS
    private void startForwardService() {
        Toast.makeText(this, "Service Started", Toast.LENGTH_SHORT).show();
        Intent serviceIntent = new Intent(this, ForwardService.class);
        ContextCompat.startForegroundService(this, serviceIntent);
    }

    // หยุด ForwardService
    private void stopService() {
        Toast.makeText(this, "Service Stopped", Toast.LENGTH_SHORT).show();
        Intent serviceIntent = new Intent(this, ForwardService.class);
        stopService(serviceIntent);
    }

    // ผูก View จาก layout
    private void matchView() {
        btnStart = findViewById(R.id.btnStart);
        btnStop = findViewById(R.id.btnStop);
        rvSMSList = findViewById(R.id.rvSMSList);
        Button btnSettings = findViewById(R.id.btnSettings);
        btnSettings.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        });
    }

    // อัพเดทการแสดงปุ่ม Start/Stop ตามสถานะของ ForwardService
    private void updateButtonVisibility() {
        // ตรวจสอบว่า ForwardService กำลังรันอยู่หรือไม่
        boolean isServiceRunning = isServiceRunning();
        btnStart.setVisibility(isServiceRunning ? View.GONE : View.VISIBLE);
        btnStop.setVisibility(isServiceRunning ? View.VISIBLE : View.GONE);
    }

    // ตรวจสอบว่า ForwardService กำลังรันอยู่หรือไม่
    private boolean isServiceRunning() {
        android.app.ActivityManager manager = (android.app.ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (android.app.ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (ForwardService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    // โหลด SMS จากเครื่องและเรียงจากล่าสุดไปเก่าสุด
    private void loadSMS() {
        // ตรวจสอบ permission อีกครั้งเพื่อความปลอดภัย
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Cannot load SMS: READ_SMS permission denied", Toast.LENGTH_SHORT).show();
            return;
        }

        smsList.clear();
        Uri uri = Uri.parse("content://sms/inbox");
        // ใช้ sortOrder เพื่อเรียงจากล่าสุดไปเก่าสุด (date DESC)
        Cursor cursor = getContentResolver().query(uri, null, null, null, "date DESC");
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String id = cursor.getString(cursor.getColumnIndexOrThrow("_id"));
                String from = cursor.getString(cursor.getColumnIndexOrThrow("address"));
                String message = cursor.getString(cursor.getColumnIndexOrThrow("body"));
                String timestamp = cursor.getString(cursor.getColumnIndexOrThrow("date"));
                smsList.add(new SMSMessage(id, from, message, timestamp));
            }
            cursor.close();
        }
        smsAdapter.notifyDataSetChanged();
    }

    // ลบ SMS และอัพเดท UI
    private void deleteSMS(SMSMessage sms) {
        if (sms.getId() != null) {
            // ลบ SMS จากเครื่องโดยใช้ ContentResolver
            int deleted = getContentResolver().delete(
                    Uri.parse("content://sms/" + sms.getId()), null, null);
            if (deleted > 0) {
                // ถ้าลบสำเร็จ
                int position = smsList.indexOf(sms); // หาตำแหน่งของ SMS ในรายการ
                smsList.remove(sms); // ลบออกจากรายการ
                smsAdapter.notifyItemRemoved(position); // อัพเดท UI เฉพาะตำแหน่งที่ถูกลบ
                Toast.makeText(this, "SMS deleted", Toast.LENGTH_SHORT).show();
            } else {
                // ถ้าลบไม่สำเร็จ (อาจเพราะแอพไม่ได้เป็น default SMS app)
                Toast.makeText(this, "Failed to delete SMS. Ensure this app is the default SMS app or check permissions.", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, "Cannot delete SMS: Invalid ID", Toast.LENGTH_SHORT).show();
        }
    }

    // ส่ง SMS ไปยัง ForwardService เพื่อ Re-Forward
    private void reForwardSMS(SMSMessage sms) {
        if (!isServiceRunning()) {
            Toast.makeText(this, "Please start the Forward service first", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(this, ForwardService.class);
        intent.setAction("com.mantvmass.smsforwarder.RE_FORWARD");
        intent.putExtra("from", sms.getFrom());
        intent.putExtra("message", sms.getMessage());
        intent.putExtra("timestamp", sms.getTimestamp());
        startService(intent);
        Toast.makeText(this, "Re-forwarding SMS...", Toast.LENGTH_SHORT).show();
    }
}