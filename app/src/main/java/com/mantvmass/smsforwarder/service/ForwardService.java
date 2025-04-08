package com.mantvmass.smsforwarder.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.mantvmass.smsforwarder.R;
import com.mantvmass.smsforwarder.ui.MainActivity;
import com.mantvmass.smsforwarder.utils.Constants;
import com.mantvmass.smsforwarder.utils.DatabaseHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ForwardService extends Service implements SMSListenerInterface {

    private DatabaseHelper dbHelper;
    private ExecutorService executorService;

    @Override
    public void onCreate() {
        super.onCreate();
        SMSBroadcastReceiver.bindListener(this);
        dbHelper = new DatabaseHelper(this);
        // สร้าง Thread Pool สำหรับการ Forward SMS
        executorService = Executors.newFixedThreadPool(5); // จำกัด 5 threads
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        Notification notification = new NotificationCompat.Builder(this, Constants.CHANNEL_ID)
                .setContentTitle("SMSForwarder Listening...")
                .setContentText("Developer: https://github.com/mantvmass")
                .setSmallIcon(R.drawable.ic_baseline_cell_tower_24)
                .setContentIntent(pendingIntent)
                .setOngoing(true) // ทำให้ Notification ไม่สามารถปัดออกได้
                .build();

        startForeground(1, notification);

        // ตรวจสอบ Intent สำหรับ Re-Forward
        if (intent != null && "com.mantvmass.smsforwarder.RE_FORWARD".equals(intent.getAction())) {
            String from = intent.getStringExtra("from");
            String message = intent.getStringExtra("message");
            String timestamp = intent.getStringExtra("timestamp");
            forwardSMS(from, message, timestamp);
        }

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        SMSBroadcastReceiver.unbindListener();
        executorService.shutdown(); // ปิด Thread Pool
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void handleReceive(String from, String message) {
        forwardSMS(from, message, String.valueOf(System.currentTimeMillis()));
    }

    private void forwardSMS(String from, String message, String timestamp) {
        // สร้าง JSON สำหรับส่ง
        JSONObject postData = new JSONObject();
        Date date = new Date(Long.parseLong(timestamp));
        SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        try {
            postData.put("from", from);
            postData.put("message", message);
            postData.put("timestamp", isoFormat.format(date));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // ดึง URL จาก SQLite
        String url = dbHelper.getUrl();

        // ใช้ ExecutorService เพื่อรันการส่ง HTTP request ใน background thread
        executorService.execute(() -> {
            HTTPService.sendPostRequest(ForwardService.this, url, postData, new HTTPService.VolleyCallback() {
                @Override
                public void onSuccess(boolean success, String msg) {
                    // แสดง Toast ใน UI thread
                    runOnUiThread(() -> {
                        if (success) {
                            Toast.makeText(ForwardService.this, msg, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(ForwardService.this, msg, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        });
    }

    // ช่วยให้สามารถเรียก Toast จาก background thread ได้
    private void runOnUiThread(Runnable runnable) {
        new android.os.Handler(android.os.Looper.getMainLooper()).post(runnable);
    }
}