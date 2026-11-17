package com.example.safesmstest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import androidx.work.Constraints;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SmsReceiver extends BroadcastReceiver {
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();
    @Override public void onReceive(Context context, Intent intent) {
        if (intent == null || !Telephony.Sms.Intents.SMS_RECEIVED_ACTION.equals(intent.getAction())) return;
        SharedPreferences prefs = context.getSharedPreferences("SmsPrefs", Context.MODE_PRIVATE);
        if (!prefs.getBoolean("sms_forwarding_enabled", false)) return;
        SmsMessage[] parts = Telephony.Sms.Intents.getMessagesFromIntent(intent);
        if (parts == null || parts.length == 0) return;

        String sender = parts[0].getDisplayOriginatingAddress();
        StringBuilder body = new StringBuilder();
        long time = parts[0].getTimestampMillis();
        for (SmsMessage part : parts) if (part != null) body.append(part.getMessageBody());
        final String from = sender == null ? "Unknown" : sender;
        final String message = body.toString();
        final PendingResult pending = goAsync();

        EXECUTOR.execute(() -> {
            try {
                SmsDatabase.getDatabase(context).smsDao()
                    .insertSms(new SmsEntity(from, message, time));
                Constraints c = new Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED).build();
                OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(PendingSmsWorker.class)
                    .setConstraints(c).build();
                WorkManager.getInstance(context).enqueueUniqueWork(
                    "PendingReceivedSmsWork", ExistingWorkPolicy.APPEND_OR_REPLACE, request);
            } catch (Exception e) {
                android.util.Log.e("SmsReceiver", "Unable to queue received SMS", e);
            } finally { pending.finish(); }
        });
    }
}
