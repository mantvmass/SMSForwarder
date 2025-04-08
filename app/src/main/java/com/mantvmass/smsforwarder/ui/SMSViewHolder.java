package com.mantvmass.smsforwarder.ui;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.mantvmass.smsforwarder.R;

public class SMSViewHolder extends RecyclerView.ViewHolder {
    TextView tvFrom, tvMessage, tvTimestamp;
    Button btnDelete, btnReForward;

    public SMSViewHolder(View itemView) {
        super(itemView);
        tvFrom = itemView.findViewById(R.id.tvFrom);
        tvMessage = itemView.findViewById(R.id.tvMessage);
        tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
        btnDelete = itemView.findViewById(R.id.btnDelete);
        btnReForward = itemView.findViewById(R.id.btnReForward);
    }
}