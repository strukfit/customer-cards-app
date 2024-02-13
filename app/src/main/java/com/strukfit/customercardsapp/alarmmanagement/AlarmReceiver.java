package com.strukfit.customercardsapp.alarmmanagement;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;


public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("aboba","Alarm received");
        Intent serviceIntent = new Intent(context, BirthdaysCheckService.class);
        context.startService(serviceIntent);
    }
}
