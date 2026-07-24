package com.jslian.jule;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.Locale;
import java.util.regex.Pattern;

public class MainActivity extends Activity {
    private static final Pattern DOMAIN_PATTERN = Pattern.compile(
            "^(?:[a-z0-9](?:[a-z0-9-]{0,61}[a-z0-9])?\\.)+[a-z]{2,63}(?::\\d+)?(?:[/?#].*)?$",
            Pattern.CASE_INSENSITIVE);

    private WebView webView;
    private LinearLayout homeScreen;
    private EditText addressBar;
    private EditText homeAddressBar;
    private ProgressBar progressBar;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView = findViewById(R.id.webView);
        homeScreen = findViewById(R.id.homeScreen);
        addressBar = findViewById(R.id.addressBar);
        homeAddressBar = findViewById(R.id.homeAddressBar);
        progressBar = findViewById(R.id.progressBar);
        Button openButton = findViewById(R.id.openButton);
        ImageButton backButton = findViewById(R.id.backButton);
        ImageButton forwardButton = findViewById(R.id.forwardButton);
        ImageButton homeButton = findViewById(R.id.homeButton);
        ImageButton reloadButton = findViewById(R.id.reloadButton);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setMediaPlaybackRequiresUserGesture(true);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);
        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri uri = request.getUrl();
                String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase(Locale.US);
                if (scheme.equals("http") || scheme.equals("https")) return false;
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, uri));
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, "Unable to open this link.", Toast.LENGTH_SHORT).show();
                }
                return true;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                addressBar.setText(url);
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                addressBar.setText(url);
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                progressBar.setProgress(newProgress);
                progressBar.setVisibility(newProgress >= 100 ? View.GONE : View.VISIBLE);
            }
        });

        webView.setDownloadListener((url, userAgent, contentDisposition, mimeType, contentLength) -> {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
            } catch (Exception e) {
                Toast.makeText(this, "Download is unavailable.", Toast.LENGTH_SHORT).show();
            }
        });

        View.OnClickListener openListener = v -> openAddress(homeAddressBar.getText().toString());
        openButton.setOnClickListener(openListener);
        homeAddressBar.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_GO || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                openAddress(v.getText().toString());
                return true;
            }
            return false;
        });
        addressBar.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_GO || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                openAddress(v.getText().toString());
                return true;
            }
            return false;
        });

        backButton.setOnClickListener(v -> {
            if (webView.canGoBack()) webView.goBack();
        });
        forwardButton.setOnClickListener(v -> {
            if (webView.canGoForward()) webView.goForward();
        });
        reloadButton.setOnClickListener(v -> {
            if (webView.getVisibility() == View.VISIBLE) webView.reload();
        });
        homeButton.setOnClickListener(v -> showHome());

        handleIntent(getIntent());
    }

    private void handleIntent(Intent intent) {
        if (intent != null && Intent.ACTION_VIEW.equals(intent.getAction()) && intent.getData() != null) {
            openAddress(intent.getData().toString());
        } else {
            showHome();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
    }

    private void showHome() {
        webView.setVisibility(View.GONE);
        homeScreen.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
        addressBar.setText("");
        homeAddressBar.setText("");
    }

    private void openAddress(String raw) {
        String normalized = normalizeAddress(raw);
        if (normalized == null) {
            Toast.makeText(this, "Enter a complete web address, such as example.com.", Toast.LENGTH_SHORT).show();
            return;
        }
        homeScreen.setVisibility(View.GONE);
        webView.setVisibility(View.VISIBLE);
        addressBar.setText(normalized);
        webView.loadUrl(normalized);
    }

    private String normalizeAddress(String raw) {
        if (raw == null) return null;
        String value = raw.trim();
        if (value.isEmpty() || value.matches(".*\\s+.*")) return null;
        if (value.matches("(?i)^https?://.+")) return value;
        if (value.matches("(?i)^(localhost|127(?:\\.\\d{1,3}){3}|\\[::1\\])(?::\\d+)?(?:/.*)?$")) {
            return "http://" + value;
        }
        if (DOMAIN_PATTERN.matcher(value).matches()) return "https://" + value;
        return null;
    }

    @Override
    public void onBackPressed() {
        if (webView.getVisibility() == View.VISIBLE && webView.canGoBack()) {
            webView.goBack();
        } else if (webView.getVisibility() == View.VISIBLE) {
            showHome();
        } else {
            super.onBackPressed();
        }
    }
}
