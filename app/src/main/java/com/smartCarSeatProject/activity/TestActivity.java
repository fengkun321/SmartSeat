package com.smartCarSeatProject.activity;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.NetworkSpecifier;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
//import android.net.wifi.WifiNetworkSpecifier;
import android.os.Bundle;
import android.os.PatternMatcher;
import android.util.Log;
import android.view.View;
import com.smartCarSeatProject.R;
import com.smartCarSeatProject.dao.DBManager;
import com.smartCarSeatProject.dao.RemoteSQLInfo;
import com.smartCarSeatProject.data.ControlPressInfo;
import com.smartCarSeatProject.isometric.ColorM;
import com.smartCarSeatProject.isometric.Isometric;
import com.smartCarSeatProject.isometric.IsometricView;
import com.smartCarSeatProject.isometric.Point;
import com.smartCarSeatProject.isometric.shapes.Prism;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;


public class TestActivity extends BaseActivity {

    IsometricView isometricView;

    double iBei = 0.5;
    public static ColorM colorMBai = new ColorM(255, 255, 255);
    public static ColorM colorM = new ColorM(5, 122, 205);
    public static ColorM colorMZi = new ColorM(162, 77, 245);
    private String url = "jdbc:mysql://47.101.160.149:3306/nbsmartdb";
    private String user = "root";
    private String passwd = "nbserver";
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_test);

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        test0();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    Class.forName("com.mysql.jdbc.Driver");
                    Connection conn = DriverManager.getConnection(url,user,passwd);

                    Statement st = conn.createStatement();
                    String sql = "SELECT * FROM DevelopDataInfo";

                    final ResultSet rs = st.executeQuery(sql);



                    //Move to first data
                    rs.first();



                    conn.close();

                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).start();



//        test2();


    }

    public void test0() {
        isometricView = findViewById(R.id.isometricView);
        // point:坐标系， dx,dy,dz：图形大小，iTag：标记，translate：偏移量,colorM：颜色
        isometricView.add(new Prism(new Point(-0.8*iBei, 0.5*iBei, 0*iBei),1*iBei,1*iBei,0.3*iBei,0), colorM);
        isometricView.add(new Prism(new Point(-0.8*iBei, 1.8*iBei, 0*iBei),1*iBei,1*iBei,0.3*iBei,1), colorM);
        isometricView.add(new Prism(new Point(0.5*iBei, 0.5*iBei, 0*iBei),1*iBei,2.3*iBei,0.3*iBei,2), colorM);
        isometricView.add(new Prism(new Point(2.5*iBei, 0.5*iBei, -0.5*iBei),0.3*iBei,1*iBei,1*iBei,3), colorM);
        isometricView.add(new Prism(new Point(2.5*iBei, 1.8*iBei, -0.5*iBei),0.3*iBei,1*iBei,1*iBei,4), colorM);
        isometricView.add(new Prism(new Point(2.7*iBei, 0.5*iBei, 0.7*iBei),0.3*iBei,1*iBei,1*iBei,5), colorM);
        isometricView.add(new Prism(new Point(2.7*iBei, 1.8*iBei, 0.7*iBei),0.3*iBei,1*iBei,1*iBei,6), colorM);
        isometricView.add(new Prism(new Point(2.9*iBei, 0.5*iBei, 1.9*iBei),0.3*iBei,1*iBei,1*iBei,7), colorM);
        isometricView.add(new Prism(new Point(2.9*iBei, 1.8*iBei, 1.9*iBei),0.3*iBei,1*iBei,1*iBei,8), colorM);
        isometricView.add(new Prism(new Point(3.2*iBei, 0.5*iBei, 3.1*iBei),0.3*iBei,1*iBei,1*iBei,9), colorM);
        isometricView.add(new Prism(new Point(3.2*iBei, 1.8*iBei, 3.1*iBei),0.3*iBei,1*iBei,1*iBei,10), colorM);
        isometricView.add(new Prism(new Point(3.5*iBei, 0.5*iBei, 4.3*iBei),0.3*iBei,1*iBei,1*iBei,11), colorM);
        isometricView.add(new Prism(new Point(3.5*iBei, 1.8*iBei, 4.3*iBei),0.3*iBei,1*iBei,1*iBei,12), colorM);
        // 倾斜角度
        isometricView.setRotation(8);
        isometricView.setiTag(7788);


        isometricView.setClickListener(new IsometricView.OnItemClickListener() {
            @Override
            public void onClick(Isometric.Item item,int iTag) {
                Loge("",item.toString());
                Loge("",item.getOriginalShape().toString()
                );
                Loge("",item.getPath().toString());
                int iBuTag = item.getPath().getiTag();
                Loge("","选中图形的tag："+iBuTag);
                ToastMsg("座椅："+iTag+"，选中图形的tag："+iBuTag);

                isometricView.changeColorByTag(iBuTag,colorMZi);
                isometricView.invalidate();

            }
        });

        findViewById(R.id.btnBai).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isometricView.changeColorByTag(4,colorMBai);
                isometricView.invalidate();
            }
        });

        findViewById(R.id.btnLan).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isometricView.changeColorByTag(4,colorM);
                isometricView.invalidate();
            }
        });

        findViewById(R.id.btnZi).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                isometricView.changeColorByTag(4,colorMZi);
//                isometricView.invalidate();

//                connectWIFI();

//                enableWifi();

                wifiConnect();

            }
        });
    }

    private void connectWIFI() {
        WifiManager mWifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiConfiguration config = new WifiConfiguration();
        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();

        // 指定对应的SSID
        config.SSID = "\"" + wifiName + "\"";

        config.preSharedKey = "\"" + wifiPassword + "\"";
        config.hiddenSSID = true;
        config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
        config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
        config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
        config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
        config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
        config.status = WifiConfiguration.Status.ENABLED;

        int netId = mWifiManager.addNetwork(config);
        Log.e("TAG", "addNetworkID："+netId);
        // 这个方法的第一个参数是需要连接wifi网络的networkId，第二个参数是指连接当前wifi网络是否需要断开其他网络
        // 无论是否连接上，都返回true。。。。
        mWifiManager.enableNetwork(netId, true);

    }

    WifiManager wifiManager = null;
    private void enableWifi(){
        if(!wifiManager.isWifiEnabled()){
            wifiManager.setWifiEnabled(true);
            Log.i("Wifi", "Wifi enabled");
        }
        if(getNetworkId() == -1){
            addNetwork();
        }

        wifiManager.disconnect();
        wifiManager.enableNetwork(getNetworkId(),true);
        wifiManager.reconnect();
        Log.e("Wifi", "Connecting to Hotspot");
        try {
            Thread.sleep(10*1000);
        } catch (InterruptedException e) {
            Log.e("Wifi", "Thread interrupted: " + e);
        }

        Log.e("Wifi", "Connected to Hotspot");
    }

    String wifiName = "TP-LINK_FK";
    String wifiPassword = "fk12345678";
    private int getNetworkId(){
        List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
        for( WifiConfiguration i : list ) {
            if(i.SSID != null && i.SSID.equals("\"" + wifiName + "\"")) {
                return i.networkId;
            }
        }
        return -1;
    }

    private void addNetwork(){
        WifiConfiguration wifiConfiguration = new WifiConfiguration();
        wifiConfiguration.SSID = "\\" + wifiName + "\\";
        wifiConfiguration.preSharedKey = "\\" + wifiPassword + "\\";
        wifiManager.addNetwork(wifiConfiguration);
        Log.i("Wifi", "Added Hotspot to configured Networks");
    }

    public void wifiConnect()
    {
//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q)
//        {
//            NetworkSpecifier specifier =
//                    new WifiNetworkSpecifier.Builder()
//                            .setSsidPattern(new PatternMatcher(wifiName, PatternMatcher.PATTERN_PREFIX))
//                            .setWpa2Passphrase(wifiPassword)
//                            .build();
//
//            NetworkRequest request =
//                    new NetworkRequest.Builder()
//                            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
//                            .removeCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
//                            .setNetworkSpecifier(specifier)
//                            .build();
//
//            ConnectivityManager connectivityManager = (ConnectivityManager)
//                    mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
//
//            ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback() {
//                @Override
//                public void onAvailable(Network network) {
//                    // do success processing here..
//                    Log.e("NetworkCallback", "onAvailable: ");
//                }
//
//                @Override
//                public void onUnavailable() {
//                    Log.e("NetworkCallback", "onUnavailable:");
//                }
//            };
//            connectivityManager.requestNetwork(request, networkCallback);
            // Release the request when done.
            // connectivityManager.unregisterNetworkCallback(networkCallback);
//        }
    }




    public void test2() {

        findViewById(R.id.btnData).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String strTableName = "t_body_10";
                DBManager dbManager = new DBManager(getMContext());
                List<ControlPressInfo> city = dbManager.queryLikeWeight(strTableName,66);
                for (ControlPressInfo city1 : city) {
                    Log.e("test2", "test2: cityInof:"+city1.toString());
                }
                dbManager.closeDb();
            }
        });

    }

}
