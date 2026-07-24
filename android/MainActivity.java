package com.jslian.jule;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.URLUtil;
import android.webkit.ValueCallback;
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

/**
 * Public JULE browser shell.
 *
 * All normal web activities (HTTP/HTTPS, redirects, target=_blank and window.open)
 * are retained inside the JULE frame. Proprietary JULE execution technology is
 * not included in this public application source.
 */
public class MainActivity extends Activity {
    private static final int FILE_CHOOSER_REQUEST = 4101;
    private static final Pattern DOMAIN_PATTERN = Pattern.compile(
            "^(?:[a-z0-9](?:[a-z0-9-]{0,61}[a-z0-9])?\\.)+[a-z]{2,63}(?::\\d+)?(?:[/?#].*)?$",
            Pattern.CASE_INSENSITIVE);

    private WebView webView;
    private LinearLayout homeScreen;
    private EditText addressBar;
    private EditText homeAddressBar;
    private ProgressBar progressBar;
    private ValueCallback<Uri[]> fileChooserCallback;

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

        configureWebView();

        openButton.setOnClickListener(v -> openAddress(homeAddressBar.getText().toString()));
        installGoAction(homeAddressBar);
        installGoAction(addressBar);

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

    @SuppressLint("SetJavaScriptEnabled")
    private void configureWebView() {
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
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setSupportMultipleWindows(true);

        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);

        webView.setWebViewClient(new JuleWebViewClient());

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                progressBar.setProgress(newProgress);
                progressBar.setVisibility(newProgress >= 100 ? View.GONE : View.VISIBLE);
            }

            /**
             * target=_blank and JavaScript window.open are redirected into the
             * same controlled JULE frame instead of another browser.
             */
            @Override
            public boolean onCreateWindow(
                    WebView view,
                    boolean isDialog,
                    boolean isUserGesture,
                    android.os.Message resultMsg) {

                WebView temporaryView = new WebView(MainActivity.this);
                temporaryView.setWebViewClient(new WebViewClient() {
                    @Override
                    public boolean shouldOverrideUrlLoading(WebView child, WebResourceRequest request) {
                        routeUriInsideJule(request.getUrl());
                        child.destroy();
                        return true;
                    }

                    @Override
                    public boolean shouldOverrideUrlLoading(WebView child, String url) {
                        routeUriInsideJule(Uri.parse(url));
                        child.destroy();
                        return true;
                    }

                    @Override
                    public void onPageStarted(WebView child, String url, Bitmap favicon) {
                        if (url != null && !url.equals("about:blank")) {
                            routeUriInsideJule(Uri.parse(url));
                            child.stopLoading();
                            child.destroy();
                        }
                    }
                });

                WebView.WebViewTransport transport =
                        (WebView.WebViewTransport) resultMsg.obj;
                transport.setWebView(temporaryView);
                resultMsg.sendToTarget();
                return true;
            }

            @Override
            public boolean onShowFileChooser(
                    WebView webView,
                    ValueCallback<Uri[]> filePathCallback,
                    FileChooserParams fileChooserParams) {

                if (fileChooserCallback != null) {
                    fileChooserCallback.onReceiveValue(null);
                }
                fileChooserCallback = filePathCallback;

                Intent chooserIntent;
                try {
                    chooserIntent = fileChooserParams.createIntent();
                    startActivityForResult(chooserIntent, FILE_CHOOSER_REQUEST);
                    return true;
                } catch (Exception error) {
                    fileChooserCallback = null;
                    Toast.makeText(MainActivity.this,
                            "File selection is unavailable.",
                            Toast.LENGTH_SHORT).show();
                    return false;
                }
            }
        });

        webView.setDownloadListener(createDownloadListener());
    }

    private final class JuleWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            return routeUriInsideJule(request.getUrl());
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return routeUriInsideJule(Uri.parse(url));
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            addressBar.setText(url);
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            addressBar.setText(url);
            view.requestFocus();
        }
    }

    /**
     * Returns true when JULE handled the activity.
     * HTTP and HTTPS always remain in the JULE frame.
     */
    private boolean routeUriInsideJule(Uri uri) {
        if (uri == null) return true;

        String scheme = uri.getScheme() == null
                ? ""
                : uri.getScheme().toLowerCase(Locale.US);

        if ("http".equals(scheme) || "https".equals(scheme)) {
            showBrowserFrame();
            webView.loadUrl(uri.toString());
            return true;
        }

        if ("about".equals(scheme) || "data".equals(scheme) || "blob".equals(scheme)) {
            showBrowserFrame();
            webView.loadUrl(uri.toString());
            return true;
        }

        if ("intent".equals(scheme)) {
            try {
                Intent parsedIntent = Intent.parseUri(uri.toString(), Intent.URI_INTENT_SCHEME);
                String fallback = parsedIntent.getStringExtra("browser_fallback_url");
                if (fallback != null && (fallback.startsWith("http://") || fallback.startsWith("https://"))) {
                    showBrowserFrame();
                    webView.loadUrl(fallback);
                    return true;
                }
                launchRequiredSystemActivity(parsedIntent);
            } catch (Exception error) {
                Toast.makeText(this, "This activity is unavailable.", Toast.LENGTH_SHORT).show();
            }
            return true;
        }

        // Telephone, mail, SMS, maps and other OS-required actions are controlled
        // exceptions. The browser itself does not launch another web browser.
        Intent externalIntent = new Intent(Intent.ACTION_VIEW, uri);
        launchRequiredSystemActivity(externalIntent);
        return true;
    }

    private void launchRequiredSystemActivity(Intent intent) {
        try {
            startActivity(intent);
        } catch (Exception error) {
            Toast.makeText(this, "This activity is unavailable.", Toast.LENGTH_SHORT).show();
        }
    }

    private DownloadListener createDownloadListener() {
        return (url, userAgent, contentDisposition, mimeType, contentLength) -> {
            try {
                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                request.setMimeType(mimeType);
                request.addRequestHeader("User-Agent", userAgent);
                request.addRequestHeader("Cookie",
                        CookieManager.getInstance().getCookie(url));
                request.setTitle(URLUtil.guessFileName(url, contentDisposition, mimeType));
                request.setDescription("Downloading with JULE");
                request.setNotificationVisibility(
                        DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                request.setDestinationInExternalPublicDir(
                        Environment.DIRECTORY_DOWNLOADS,
                        URLUtil.guessFileName(url, contentDisposition, mimeType));

                DownloadManager manager =
                        (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                manager.enqueue(request);
                Toast.makeText(this, "Download started in JULE.", Toast.LENGTH_SHORT).show();
            } catch (Exception error) {
                Toast.makeText(this, "Download is unavailable.", Toast.LENGTH_SHORT).show();
            }
        };
    }

    private void installGoAction(EditText field) {
        field.setOnEditorActionListener((v, actionId, event) -> {
            boolean keyboardGo = actionId == EditorInfo.IME_ACTION_GO;
            boolean enterKey = event != null
                    && event.getAction() == KeyEvent.ACTION_DOWN
                    && event.getKeyCode() == KeyEvent.KEYCODE_ENTER;
            if (keyboardGo || enterKey) {
                openAddress(v.getText().toString());
                return true;
            }
            return false;
        });
    }

    private void handleIntent(Intent intent) {
        if (intent != null
                && Intent.ACTION_VIEW.equals(intent.getAction())
                && intent.getData() != null) {
            routeUriInsideJule(intent.getData());
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

    private void showBrowserFrame() {
        homeScreen.setVisibility(View.GONE);
        webView.setVisibility(View.VISIBLE);
    }

    private void openAddress(String raw) {
        String normalized = normalizeAddress(raw);
        if (normalized == null) {
            Toast.makeText(this,
                    "Enter a complete web address, such as example.com.",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        showBrowserFrame();
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == FILE_CHOOSER_REQUEST) {
            Uri[] results = null;
            if (resultCode == RESULT_OK && data != null) {
                String dataString = data.getDataString();
                if (dataString != null) {
                    results = new Uri[]{Uri.parse(dataString)};
                }
            }
            if (fileChooserCallback != null) {
                fileChooserCallback.onReceiveValue(results);
                fileChooserCallback = null;
            }
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
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

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.stopLoading();
            webView.destroy();
        }
        super.onDestroy();
    }
}
