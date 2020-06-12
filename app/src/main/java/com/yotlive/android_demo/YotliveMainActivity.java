package com.yotlive.android_demo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.smartCarSeatProject.R;
import com.yotlive.matx.MatXService;
import com.yotlive.matx.MatXStateMessage;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;


public class YotliveMainActivity extends AppCompatActivity {
    static String TAG = "Yotlive";
    EditText etPassword;
    Button btn1, btn2;
    WebView mWebView;
    TextView feedback, ssidTxt, snumTxt;

    StringBuffer dataBuffer;
    StringBuffer message;
    AlertDialog.Builder builder;
    AlertDialog dialog;
    int snum;

    private WifiManager wifiManager;
    private BroadcastReceiver receiverWifi;

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
            case 10001:
                dialog.setMessage("已接入AP");
                dialog.show();
                break;
            case 10002:
                dialog.setMessage("正在配网");
                dialog.show();
                break;
            case 10003:
                dialog.setMessage("配网完成");
                dialog.show();
                break;
            case 10004:
                //TODO timeout in wifi config
                break;
            case 11000: // device found (this code will keep presenting as long as searching is active, i.e. app is not closed)
                // You can move this connection step to another place if you want to avoid auto connection.
                mBinder.connect(msg.getDeviceCode());
                break;
            case 11001: // device connected (this code will be presented once when heartbeat is established)
                snumTxt.setText(Integer.toString(msg.getDeviceCode()));
                Log.d(TAG, "device connected");
                break;
            case 11006: // disconnected (note: disconnection is confirmed only when heartbeat in LAN is lost)
                break;
            default:
//                message.setLength(0);
//                message.append(msg.getMessage());
//                dialog.setMessage("device:"+msg.getDeviceCode()+"---"+message);
//                dialog.show();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(receiverWifi);
        unbindService(conn);
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_yotlive);
        // Force the orientation to portrait when starting the app.
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        snum = -1;

        // Setup EditText.
        ssidTxt = findViewById(R.id.ssid_txt);
        etPassword = findViewById(R.id.et_password);
        snumTxt = findViewById(R.id.snum_txt);

        // Setup Button.
        btn1 = findViewById(R.id.net_add);
        btn2 = findViewById(R.id.heatmap);

        // Setup WebView.
        mWebView = findViewById(R.id.webview);

        // Setup TextView.
        feedback = findViewById(R.id.log_display);

        // Bind service and register event bus.
        final Intent intent = new Intent(this, MatXService.class);
        bindService(intent, conn, Context.BIND_AUTO_CREATE);
        EventBus.getDefault().register(this);

        dataBuffer = new StringBuffer(2304);
        message  =  new StringBuffer(256);
        builder = new AlertDialog.Builder(YotliveMainActivity.this);
        dialog = builder.create();

        // To make Wifi manager work.
        // Please add your own notification if the user
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0x01);

        // Get network information and update the displayed SSID.
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        receiverWifi = new BroadcastReceiver() {
            @Override
            public void onReceive(Context c, Intent intent) {
                WifiInfo info = wifiManager.getConnectionInfo();
                String ssid = info.getSSID();
                if(!ssid.equals("<unknown ssid>")){
                    ssidTxt.setText(ssid.substring(1,ssid.length()-1));
                }else{
                    ssidTxt.setText("");
                }
            }
        };
        registerReceiver(receiverWifi, new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION));

        btn1.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                // Hide the keyboard.
                try  {
                    InputMethodManager imm = (InputMethodManager)getSystemService(
                            INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                } catch (Exception e) {
                    // Do nothing if the keyboard is not present.
                }
                if (!(ssidTxt.getText().toString().trim().length() == 0)){
                    String ssid = ssidTxt.getText().toString().trim();
                    String passwd = etPassword.getText().toString().trim();
                    mBinder.addWiFiConfig(ssid, passwd);
                    Log.d(TAG, "successfully added");
                    dialog.setMessage("路由器信息更新成功");
                    dialog.show();
                }
            }
        });

        btn2.setOnClickListener(new View.OnClickListener(){
            @SuppressLint("SourceLockedOrientationActivity")
            @Override
            public void onClick(View v) {
                Log.d(TAG, Integer.toString(snum));
                if (snumTxt.getText().toString().trim().length() != 0) {
                    snum = Integer.valueOf(snumTxt.getText().toString());

                    // Force the orientation to landscape when switching to WebView.
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                    // Hide the status bar.
                    getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                    // Hide the keyboard.
                    try {
                        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                    } catch (Exception e) {
                        // Do nothing if the keyboard is not present.
                    }

                    // Connect the device.
                    // Once connected, a code will be returned from eventbus. Then mBinder.startSampling will be
                    // called. You can move it to another action to receive data.
                    mBinder.startSampling(snum);

                    // Go to the display activity.
                    Intent display_intent = new Intent(getApplicationContext(),
                            PressureDisplay.class);
                    display_intent.putExtra("product_id", snum);
                    startActivity(display_intent);
                }
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        int size = permissions.length;
        for (int i = 0; i < size; ++i) {
            String permission = permissions[i];
            int grant = grantResults[i];

            if (permission.equals(Manifest.permission.ACCESS_FINE_LOCATION)) {
                if (grant == PackageManager.PERMISSION_GRANTED) {
                    // Update displayed SSID immediately.
                    receiverWifi.onReceive(null, null);
                }
            }
        }
    }

}
