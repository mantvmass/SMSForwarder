package com.mantvmass.smsforwarder.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mantvmass.smsforwarder.R;
import com.mantvmass.smsforwarder.model.SMSMessage;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SMSAdapter extends RecyclerView.Adapter<SMSViewHolder> {
    private List<SMSMessage> smsList;
    private Context context;
    private OnDeleteClickListener deleteClickListener;
    private OnReForwardClickListener reForwardClickListener;

    public interface OnDeleteClickListener {
        void onDeleteClick(SMSMessage smsMessage);
    }

    public interface OnReForwardClickListener {
        void onReForwardClick(SMSMessage smsMessage);
    }

    public SMSAdapter(Context context, List<SMSMessage> smsList, OnDeleteClickListener deleteListener, OnReForwardClickListener reForwardListener) {
        this.context = context;
        this.smsList = smsList;
        this.deleteClickListener = deleteListener;
        this.reForwardClickListener = reForwardListener;
    }

    @NonNull
    @Override
    public SMSViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_sms, parent, false);
        return new SMSViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SMSViewHolder holder, int position) {
        SMSMessage sms = smsList.get(position);
        holder.tvFrom.setText("From: " + sms.getFrom());
        holder.tvMessage.setText("Message: " + sms.getMessage());

        // แปลง timestamp เป็นวันที่
        try {
            long timestamp = Long.parseLong(sms.getTimestamp());
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
            holder.tvTimestamp.setText("Time: " + sdf.format(new Date(timestamp)));
        } catch (NumberFormatException e) {
            holder.tvTimestamp.setText("Time: Unknown");
        }

        holder.btnDelete.setOnClickListener(v -> deleteClickListener.onDeleteClick(sms));
        holder.btnReForward.setOnClickListener(v -> reForwardClickListener.onReForwardClick(sms));
    }

    @Override
    public int getItemCount() {
        return smsList.size();
    }

    public void addSMS(SMSMessage sms) {
        smsList.add(0, sms); // เพิ่มที่ด้านบนสุด
        notifyItemInserted(0);
    }
}