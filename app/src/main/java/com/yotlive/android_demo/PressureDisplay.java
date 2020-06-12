package com.yotlive.android_demo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;

import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.ValueCallback;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.smartCarSeatProject.R;
import com.smartCarSeatProject.data.AreaAnalysis;
import com.yotlive.matx.MatXDataMessage;
import com.yotlive.matx.MatXService;
import com.yotlive.matx.MatXStateMessage;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;


public class PressureDisplay extends Activity {

    private WebView mWebView;
    private ProgressBar spinner;
    private int snum;

    // To show dialogs.
    public static AlertDialog.Builder builder;
    public static AlertDialog dialog;
    public static StringBuffer message;
    public static  String TAG = "PressureDisplay";
    // MatX service properties.
    private MatXService.MatXServiceBinder mBinder;
    private ServiceConnection conn = new ServiceConnection(){
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBinder = (MatXService.MatXServiceBinder) service;
            Log.i(TAG,"MainActivity --- onServiceConnected.");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.i(TAG,"MainActivity --- onServiceDisconnected.");
        }
    };

    // Eventbus showing event notifications.
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void showMessageDialog(MatXStateMessage msg){
        int code = msg.getState().getCode();
        //TODO Handle Event notification
        switch(code){
            case 10003:
                //TODO QUit from wifi config
                break;
            case 10004:
                //TODO timeout in wifi config
                break;
            case 11000: // device found (this code will keep presenting as long as searching is active, i.e. app is not closed)
                break;
            case 11001: // device connected (this code will be presented once when heartbeat is established)
                break;
            case 11006: // disconnected (note: disconnection is confirmed only when heartbeat in LAN is lost)
                break;
            default:
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void showData(MatXDataMessage msg){
        int code = msg.getDeviceCode();
        if(snum == code){
            displayData(msg.getData());
        }
    }

    @Override
    protected void onDestroy() {
        try {
            mBinder.stopSampling(snum);
        }catch (Exception e){
            // do nothing as sampling was not started
        }
        unbindService(conn);
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        Bundle extras = getIntent().getExtras();
        snum = extras.getInt("product_id");

        // Initialize webview to display data.
        spinner = findViewById(R.id.progressBar);
        mWebView = findViewById(R.id.webview);
        mWebView.setWebViewClient(new WebViewClient());
        //mWebView.setWebChromeClient(new WebChromeClient());
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        spinner.setVisibility(ProgressBar.VISIBLE);
        // Bind service and register event bus.
        final Intent intent = new Intent(this, MatXService.class);
        bindService(intent, conn, Context.BIND_AUTO_CREATE);
        EventBus.getDefault().register(this);
        // To show dialogs.
        message  =  new StringBuffer(256);
        builder = new AlertDialog.Builder(this);
        dialog = builder.create();
        // Hide the keyboard.
        try {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        } catch (Exception e) {
            // Do nothing if the keyboard is not present.
        }
        mWebView.loadUrl("file:///android_asset/web/html/mat_89.html");
    }

    // Sticky immersive fullscreen mode enabled.
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        }
    }

    // Immersive fullscreen mode.
    private void hideSystemUI() {
        // Enables sticky immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "regular immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        // Set the content to appear under the system bars so that the
                        // content doesn't resize when the system bars hide and show.
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        // Hide the nav bar and status bar
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    // To display data using html.
    private void displayData(int[][] data){
        if(spinner.isShown()) {
            spinner.setVisibility(ProgressBar.GONE);
        }

        Log.d(TAG, Integer.toString(data.length) + "---" +Integer.toString(data[0].length));

        final JSONArray jsonArray = new JSONArray();
        //   Random rand =new Random(25);
        //  int i = 0;
        for (int param1Int = 0; param1Int < data.length; param1Int++) {
            JSONArray jSONArray1 = new JSONArray();
            for (byte b = 0; b < data[param1Int].length; b++) {
                jSONArray1.add(Integer.valueOf(data[param1Int][b]));
                //   i = rand.nextInt(45);
                //   jSONArray1.add(i);
            }
            jsonArray.add(jSONArray1);
        }
        Log.d(TAG,"random : "+jsonArray.toJSONString());

        AreaAnalysis areaAnalysis = new AreaAnalysis(data,0);


        runOnUiThread(new Runnable() {
            public void run() {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("getData('");
                stringBuilder.append(JSON.toJSONString(jsonArray));
                stringBuilder.append("')");
                mWebView.evaluateJavascript(stringBuilder.toString(), new ValueCallback<String>() {
                    public void onReceiveValue(String param3String) {
                    }
                });
            }
        });
    }

}
