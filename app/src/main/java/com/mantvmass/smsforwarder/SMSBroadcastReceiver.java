package com.mantvmass.smsforwarder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

public class SMSBroadcastReceiver extends BroadcastReceiver {

    private static SMSListenerInterface mListener;
    public static final String SMS_BUNDLE = "pdus";

    @Override
    public void onReceive(Context context, Intent intent) {

        Bundle intentExtras = intent.getExtras();
        if (mListener != null) {
            if (intentExtras != null) {
                Object[] sms = (Object[]) intentExtras.get(SMS_BUNDLE);
                for (int i = 0; i < sms.length; ++i) {
                    SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) sms[i]);

                    // String from = smsMessage.getOriginatingAddress();
                    String from = smsMessage.getDisplayOriginatingAddress();
                    String message = smsMessage.getMessageBody();

                    mListener.handleReceive(from, message);
                }
            } else {
                Toast.makeText(context, "EXTRA IS NULL", Toast.LENGTH_SHORT).show();
            }
        }

    }

    public static void bindListener(SMSListenerInterface listener) {
        mListener = listener;
    }

    public static void unbindListener() {
        mListener = null;
    }

}