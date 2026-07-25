package com.jslian.jule;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private static final String PREFS = "jule_omega_spaces";
    private static final String KEY_COUNT = "omega_count";
    private static final int DEFAULT_LAST_OMEGA = 8;

    private EditText addressBar;
    private WebView webView;
    private LinearLayout omegaContainer;
    private SharedPreferences prefs;
    private int lastOmega;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        addressBar = findViewById(R.id.addressBar);
        webView = findViewById(R.id.webView);
        omegaContainer = findViewById(R.id.omegaContainer);
        prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        lastOmega = Math.max(DEFAULT_LAST_OMEGA, prefs.getInt(KEY_COUNT, DEFAULT_LAST_OMEGA));

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);
        settings.setSupportMultipleWindows(false);
        settings.setAllowFileAccess(false);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                addressBar.setText(url);
            }
        });
        webView.setWebChromeClient(new WebChromeClient());

        findViewById(R.id.openButton).setOnClickListener(v -> openAddress());
        findViewById(R.id.backButton).setOnClickListener(v -> {
            if (webView.canGoBack()) webView.goBack();
        });
        findViewById(R.id.forwardButton).setOnClickListener(v -> {
            if (webView.canGoForward()) webView.goForward();
        });
        findViewById(R.id.reloadButton).setOnClickListener(v -> webView.reload());
        findViewById(R.id.homeButton).setOnClickListener(v -> openOmega(0));
        findViewById(R.id.addOmegaButton).setOnClickListener(v -> addOmega());

        addressBar.setOnEditorActionListener((TextView v, int actionId, KeyEvent event) -> {
            if (actionId == EditorInfo.IME_ACTION_GO ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                openAddress();
                return true;
            }
            return false;
        });

        renderOmegaButtons();
        String start = prefs.getString("omega_0", "https://www.jsl-ian.com");
        loadUrl(start);
    }

    private void openAddress() {
        String input = addressBar.getText().toString().trim();
        if (input.isEmpty()) return;
        hideKeyboard();
        loadUrl(normalize(input));
    }

    private String normalize(String input) {
        String lower = input.toLowerCase(Locale.US);
        if (lower.startsWith("http://") || lower.startsWith("https://")) return input;
        if (input.contains(" ") || !input.contains(".")) {
            return "https://www.google.com/search?q=" + android.net.Uri.encode(input);
        }
        return "https://" + input;
    }

    private void loadUrl(String url) {
        try {
            webView.loadUrl(url);
        } catch (Exception e) {
            Toast.makeText(this, "Unable to open this address.", Toast.LENGTH_SHORT).show();
        }
    }

    private void renderOmegaButtons() {
        omegaContainer.removeAllViews();
        for (int i = 0; i <= lastOmega; i++) {
            final int index = i;
            Button button = new Button(this);
            button.setText("Ω" + i);
            button.setAllCaps(false);
            button.setTextSize(14);
            button.setTextColor(Color.WHITE);
            button.setBackgroundResource(R.drawable.open_button_background);
            button.setGravity(Gravity.CENTER);
            button.setPadding(20, 4, 20, 4);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    dp(44));
            params.setMargins(dp(4), dp(4), dp(4), dp(4));
            button.setLayoutParams(params);

            button.setOnClickListener(v -> openOmega(index));
            button.setOnLongClickListener(v -> {
                saveCurrentToOmega(index);
                return true;
            });
            omegaContainer.addView(button);
        }
    }

    private void openOmega(int index) {
        String key = "omega_" + index;
        String url = prefs.getString(key, null);
        if (url == null || url.trim().isEmpty()) {
            saveCurrentToOmega(index);
            return;
        }
        loadUrl(url);
    }

    private void saveCurrentToOmega(int index) {
        String current = webView.getUrl();
        if (current == null || current.trim().isEmpty()) {
            current = normalize(addressBar.getText().toString().trim());
        }
        prefs.edit().putString("omega_" + index, current).apply();
        Toast.makeText(this, "Saved current page to Ω" + index, Toast.LENGTH_SHORT).show();
    }

    private void addOmega() {
        lastOmega++;
        prefs.edit().putInt(KEY_COUNT, lastOmega).apply();
        renderOmegaButtons();
        Toast.makeText(this, "Ω" + lastOmega + " created", Toast.LENGTH_SHORT).show();
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) imm.hideSoftInputFromWindow(addressBar.getWindowToken(), 0);
        addressBar.clearFocus();
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) webView.goBack();
        else super.onBackPressed();
    }
}
