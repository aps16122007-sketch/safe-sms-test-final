package com.example.safesmstest;
import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class MainActivity extends Activity {
    private static final int SMS_PERMISSION_REQUEST = 301;
    private static final String HOME_URL = "https://aitap3-7funsfro.manus.space";
    private WebView webView;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        webView = new WebView(this);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient());
        setContentView(webView);
        if (state == null) webView.loadUrl(HOME_URL);
        else webView.restoreState(state);
        requestConsent();
    }

    private void requestConsent() {
        SharedPreferences p = getSharedPreferences("SmsPrefs", MODE_PRIVATE);
        if (p.getBoolean("sms_forwarding_consent", false)) {
            requestSmsPermission();
            return;
        }
        new AlertDialog.Builder(this)
            .setTitle("Received SMS forwarding")
            .setMessage("သဘောတူပါက ဤဖုန်းသို့ ဝင်လာသော SMS များကို Telegram သို့ ပို့မည်။ OTP နှင့် ကိုယ်ရေးကိုယ်တာစာများလည်း ပါနိုင်သည်။ ကိုယ်ပိုင်ဖုန်း သို့မဟုတ် ခွင့်ပြုချက်ရှိသောဖုန်းတွင်သာ အသုံးပြုပါ။ Sent SMS များကို မဖတ်၊ မပို့ပါ။")
            .setCancelable(false)
            .setNegativeButton("မဖွင့်ပါ", (d, w) -> p.edit()
                .putBoolean("sms_forwarding_consent", false)
                .putBoolean("sms_forwarding_enabled", false).apply())
            .setPositiveButton("သဘောတူပြီး ဖွင့်မည်", (d, w) -> {
                p.edit().putBoolean("sms_forwarding_consent", true).apply();
                requestSmsPermission();
            }).show();
    }

    private void requestSmsPermission() {
        if (Build.VERSION.SDK_INT >= 23 &&
            checkSelfPermission(Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.RECEIVE_SMS}, SMS_PERMISSION_REQUEST);
        } else enableForwarding();
    }

    private void enableForwarding() {
        getSharedPreferences("SmsPrefs", MODE_PRIVATE).edit()
            .putBoolean("sms_forwarding_enabled", true).apply();
        Toast.makeText(this, "Received SMS forwarding ဖွင့်ထားသည်", Toast.LENGTH_LONG).show();
    }

    @Override public void onRequestPermissionsResult(int request, String[] permissions, int[] results) {
        super.onRequestPermissionsResult(request, permissions, results);
        if (request == SMS_PERMISSION_REQUEST) {
            if (results.length > 0 && results[0] == PackageManager.PERMISSION_GRANTED) enableForwarding();
            else {
                getSharedPreferences("SmsPrefs", MODE_PRIVATE).edit()
                    .putBoolean("sms_forwarding_enabled", false).apply();
                Toast.makeText(this, "SMS permission မရသဖြင့် forwarding ပိတ်ထားသည်", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override protected void onSaveInstanceState(Bundle out) {
        if (webView != null) webView.saveState(out);
        super.onSaveInstanceState(out);
    }
    @Override public void onBackPressed() {
        if (webView != null && webView.canGoBack()) webView.goBack();
        else super.onBackPressed();
    }
    @Override protected void onDestroy() {
        if (webView != null) { webView.destroy(); webView = null; }
        super.onDestroy();
    }
}
