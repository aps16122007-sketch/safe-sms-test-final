package com.example.safesmstest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import androidx.work.Constraints;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

public class BootReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context context, Intent intent) {
        if (intent == null || !Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) return;
        SharedPreferences p = context.getSharedPreferences("SmsPrefs", Context.MODE_PRIVATE);
        if (!p.getBoolean("sms_forwarding_enabled", false)) return;
        Constraints c = new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build();
        OneTimeWorkRequest r = new OneTimeWorkRequest.Builder(PendingSmsWorker.class)
            .setConstraints(c).build();
        WorkManager.getInstance(context).enqueueUniqueWork(
            "PendingReceivedSmsWork", ExistingWorkPolicy.APPEND_OR_REPLACE, r);
    }
}
