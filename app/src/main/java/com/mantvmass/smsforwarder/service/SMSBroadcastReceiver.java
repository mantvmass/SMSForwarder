package com.mantvmass.smsforwarder.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.widget.Toast;

public class SMSBroadcastReceiver extends BroadcastReceiver {

    private static SMSListenerInterface mListener;
    public static final String SMS_BUNDLE = "pdus";
    public static final String SMS_RECEIVED_ACTION = "com.mantvmass.smsforwarder.SMS_RECEIVED";

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle intentExtras = intent.getExtras();
        if (intentExtras != null) {
            Object[] sms = (Object[]) intentExtras.get(SMS_BUNDLE);
            for (int i = 0; i < sms.length; ++i) {
                // ใช้รูปแบบใหม่ของ createFromPdu ที่รับ format เพิ่มเติม
                String format = intentExtras.getString("format");
                SmsMessage smsMessage = null;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    smsMessage = SmsMessage.createFromPdu((byte[]) sms[i], format);
                }
                String from = smsMessage.getDisplayOriginatingAddress();
                String message = smsMessage.getMessageBody();
                String timestamp = String.valueOf(smsMessage.getTimestampMillis());

                // ส่งข้อมูล SMS ไปยัง MainActivity ผ่าน Intent
                Intent smsIntent = new Intent(SMS_RECEIVED_ACTION);
                smsIntent.putExtra("from", from);
                smsIntent.putExtra("message", message);
                smsIntent.putExtra("timestamp", timestamp);
                context.sendBroadcast(smsIntent);

                // ส่งต่อไปยัง ForwardService
                if (mListener != null) {
                    mListener.handleReceive(from, message);
                }
            }
        } else {
            Toast.makeText(context, "EXTRA IS NULL", Toast.LENGTH_SHORT).show();
        }
    }

    public static void bindListener(SMSListenerInterface listener) {
        mListener = listener;
    }

    public static void unbindListener() {
        mListener = null;
    }
}