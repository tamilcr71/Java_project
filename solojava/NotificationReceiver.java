package com.example.solojava;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String title   = intent.getStringExtra("title");
        String message = intent.getStringExtra("message");
        int code       = intent.getIntExtra("code", 1001);

        if (title == null) title = "⚡ System Alert";
        if (message == null) message = "Your daily quests await, Hunter!";

        NotificationHelper.createChannel(context);
        NotificationHelper.showNotification(context, title, message, code);
    }
}