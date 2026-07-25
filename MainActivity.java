package com.jslian.jule;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
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
import java.util.Locale;

public class MainActivity extends Activity {
    private static final String PREFS = "jule_omega_v2";
    private static final String KEY_LAST_OMEGA = "last_omega";
    private static final int DEFAULT_LAST_OMEGA = 8;
    private static final String DEFAULT_HOME = "https://www.jsl-ian.com";

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
        lastOmega = Math.max(DEFAULT_LAST_OMEGA, prefs.getInt(KEY_LAST_OMEGA, DEFAULT_LAST_OMEGA));
        configureWebView();
        findViewById(R.id.openButton).setOnClickListener(v -> openAddress());
        findViewById(R.id.backButton).setOnClickListener(v -> { if (webView.canGoBack()) webView.goBack(); });
        findViewById(R.id.forwardButton).setOnClickListener(v -> { if (webView.canGoForward()) webView.goForward(); });
        findViewById(R.id.reloadButton).setOnClickListener(v -> webView.reload());
        findViewById(R.id.homeButton).setOnClickListener(v -> openOmega(0));
        findViewById(R.id.addOmegaButton).setOnClickListener(v -> addOmega());
        addressBar.setOnEditorActionListener((TextView v, int actionId, KeyEvent event) -> {
            if (actionId == EditorInfo.IME_ACTION_GO || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                openAddress(); return true;
            }
            return false;
        });
        renderOmegaButtons();
        loadUrl(prefs.getString(keyFor(0), DEFAULT_HOME));
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void configureWebView() {
        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true); s.setDomStorageEnabled(true); s.setDatabaseEnabled(true);
        s.setLoadWithOverviewMode(true); s.setUseWideViewPort(true); s.setBuiltInZoomControls(true);
        s.setDisplayZoomControls(false); s.setSupportMultipleWindows(false);
        webView.setWebViewClient(new WebViewClient(){ @Override public void onPageFinished(WebView v, String url){ addressBar.setText(url); }});
        webView.setWebChromeClient(new WebChromeClient());
    }

    private void openAddress(){
        String input = addressBar.getText().toString().trim();
        if(input.isEmpty()) return;
        hideKeyboard(); loadUrl(normalize(input));
    }

    private String normalize(String input){
        String lower = input.toLowerCase(Locale.US);
        if(lower.startsWith("http://") || lower.startsWith("https://")) return input;
        if(input.contains(" ") || !input.contains(".")) return "https://www.google.com/search?q=" + android.net.Uri.encode(input);
        return "https://" + input;
    }

    private void loadUrl(String url){
        try { webView.loadUrl(url); }
        catch(Exception e){ Toast.makeText(this,"Unable to open this address.",Toast.LENGTH_SHORT).show(); }
    }

    private void renderOmegaButtons(){
        omegaContainer.removeAllViews();
        for(int i=0;i<=lastOmega;i++){
            final int index=i;
            Button b=new Button(this);
            b.setText("Ω"+i); b.setAllCaps(false); b.setTextSize(14); b.setTextColor(Color.WHITE);
            b.setBackgroundResource(R.drawable.open_button_background); b.setGravity(Gravity.CENTER);
            b.setPadding(dp(16),0,dp(16),0);
            LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,dp(44));
            p.setMargins(dp(4),dp(4),dp(4),dp(4)); b.setLayoutParams(p);
            b.setOnClickListener(v -> openOmega(index));
            b.setOnLongClickListener(v -> { saveCurrentPage(index); return true; });
            omegaContainer.addView(b);
        }
    }

    private void openOmega(int index){
        String saved=prefs.getString(keyFor(index),null);
        if(saved==null || saved.trim().isEmpty()){ saveCurrentPage(index); return; }
        loadUrl(saved);
    }

    private void saveCurrentPage(int index){
        String current=webView.getUrl();
        if(current==null || current.trim().isEmpty()){
            String entered=addressBar.getText().toString().trim();
            if(entered.isEmpty()){ Toast.makeText(this,"Open a page before saving.",Toast.LENGTH_SHORT).show(); return; }
            current=normalize(entered);
        }
        prefs.edit().putString(keyFor(index),current).apply();
        Toast.makeText(this,"Saved to Ω"+index,Toast.LENGTH_SHORT).show();
    }

    private void addOmega(){
        lastOmega++; prefs.edit().putInt(KEY_LAST_OMEGA,lastOmega).apply(); renderOmegaButtons();
        Toast.makeText(this,"Ω"+lastOmega+" created",Toast.LENGTH_SHORT).show();
    }

    private String keyFor(int index){ return "omega_"+index; }
    private void hideKeyboard(){
        InputMethodManager imm=(InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        if(imm!=null) imm.hideSoftInputFromWindow(addressBar.getWindowToken(),0);
        addressBar.clearFocus();
    }
    private int dp(int value){ return Math.round(value*getResources().getDisplayMetrics().density); }
    @Override public void onBackPressed(){ if(webView.canGoBack()) webView.goBack(); else super.onBackPressed(); }
}
