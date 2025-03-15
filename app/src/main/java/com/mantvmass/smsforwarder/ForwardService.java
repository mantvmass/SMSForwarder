package com.mantvmass.smsforwarder;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import static com.mantvmass.smsforwarder.App.CHANNEL_ID;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

public class ForwardService extends Service implements SMSListenerInterface {

    @Override
    public void onCreate() {
        super.onCreate();
        SMSBroadcastReceiver.bindListener(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("SMSForwarder Listening...")
                .setContentText("Developer: https://github.com/mantvmass")
                .setSmallIcon(R.drawable.ic_baseline_cell_tower_24)
                .setContentIntent(pendingIntent)
                .build();

        startForeground(1, notification);

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        SMSBroadcastReceiver.unbindListener();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void handleReceive(String from, String message) {
        onReceive(from, message);
    }

    private void onReceive(String from, String message) {

        JSONObject postData = new JSONObject();

        long timestamp = System.currentTimeMillis();

        Date date = new Date(timestamp);

        try {
            postData.put("from", from);
            postData.put("message", message);
            postData.put("timestamp", date.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        HTTPService.sendPostRequest(this, "http://192.168.1.8:5000", postData, new HTTPService.VolleyCallback() {
            @Override
            public void onSuccess(boolean success) {
                if (success) {
                    Toast.makeText(ForwardService.this, "Hook OK.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ForwardService.this, "Hook Failed.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
