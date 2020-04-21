package com.smartCarSeatProject.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import android.os.Bundle
import com.smartCarSeatProject.R
import com.smartCarSeatProject.data.BaseVolume
import com.smartCarSeatProject.view.LoadingDialog
import com.smartCarSeatProject.wifiInfo.WIFIConnectionManager
import kotlinx.android.synthetic.main.layout_connectwifi.*
import android.os.IBinder
import android.util.Log
import android.view.inputmethod.InputMethodManager
import com.smartCarSeatProject.wifiInfo.WIFIConnectionTest


class ConnectWifiActivity: BaseActivity() {

    var selectSSID = ""
    var wifiManager: WifiManager? = null
    var wifiConnectionTest:WIFIConnectionTest? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_connectwifi)

        wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

        wifiConnectionTest = WIFIConnectionTest(this,wifiManager)

        selectSSID = intent.getStringExtra("NowWifi")
        tvNowWifi.text = selectSSID
//        edPwd.text = Editable.Factory.getInstance().newEditable(selectSSID)

        imgBack.setOnClickListener {
            MainControlActivity.getInstance()?.ReturnBack("ConnectWifiActivity")
        }
        llContent.setOnClickListener {
            hideSoftInput(it.windowToken)
        }


        btnConnect.setOnClickListener {

            if (edPwd.text.toString().equals("")){
                return@setOnClickListener
            }
            loadingDialog?.show()
            Loge(this.localClassName,"开始连接！！！！SSID：$selectSSID ++++ PWD：${edPwd.text}")
//            WIFIConnectionManager.getInstance(this)?.startConnect(selectSSID,edPwd.text.toString())


            //createWifiConfig主要用于构建一个WifiConfiguration，代码中的例子主要用于连接不需要密码的Wifi
            //WifiManager的addNetwork接口，传入WifiConfiguration后，得到对应的NetworkId
            val netId = wifiManager?.addNetwork(wifiConnectionTest?.createWifiConfig(selectSSID, edPwd.text.toString(), WIFIConnectionTest.WIFICIPHER_WPA))

            //WifiManager的enableNetwork接口，就可以连接到netId对应的wifi了
            //其中boolean参数，主要用于指定是否需要断开其它Wifi网络
            val enable = wifiManager?.enableNetwork(netId!!, true)
            Log.d("ZJTest", "enable: $enable")

            //可选操作，让Wifi重新连接最近使用过的接入点
            //如果上文的enableNetwork成功，那么reconnect同样连接netId对应的网络
            //若失败，则连接之前成功过的网络
            val reconnect = wifiManager?.reconnect()
            Log.d("ZJTest", "reconnect: $reconnect")



        }

        // 添加网络监听
        val mFilter = IntentFilter()
        mFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(myNetReceiver, mFilter)
    }


    /****
     * 网络监听
     */
    private val myNetReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (action == ConnectivityManager.CONNECTIVITY_ACTION) {
                val mConnectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                val netInfo = mConnectivityManager
                        .activeNetworkInfo
                if (netInfo != null && netInfo.isAvailable) {
                    // wifi网络
                    if (netInfo.type == ConnectivityManager.TYPE_WIFI) {
                        // 不在等待连接状态，则不用理会
                        if(!loadingDialog?.isShowing()!!) {
                            return
                        }

                        var nowWifi = WIFIConnectionManager.getInstance(this@ConnectWifiActivity)?.nowConnectWifi

                        if (nowWifi.equals(selectSSID)) {
                            loadingDialog?.dismiss()
//                            sendBroadcast(Intent(BaseVolume.BROADCAST_UPDATA_WIFI_INFO))
//                            MainControlActivity.getInstance()?.ReturnBack("ConnectWifiActivity")
                            MainControlActivity.getInstance()?.finish()
                        }
                        else {

                            loadingDialog?.dismiss()
                            ToastMsg("Connection Fail！")
                        }


                    } else if (netInfo.type == ConnectivityManager.TYPE_ETHERNET) {
                    } else if (netInfo.type == ConnectivityManager.TYPE_MOBILE) {
                    }// 3G网络
                    // 2G网络
                } else {
                    // 网络断开了
//                    context.sendBroadcast(Intent(BaseVolume.BROADCAST_TCP_INFO)
//                            .putExtra(BaseVolume.BROADCAST_TYPE,BaseVolume.BROADCAST_TCP_CONNECT_CALLBACK)
//                            .putExtra(BaseVolume.BROADCAST_TCP_STATUS,false))
                }
            }
        }
    }

    /**
     * 隐藏软键盘
     */
    private fun hideSoftInput(token: IBinder?) {
        if (token != null) {
            val manager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            manager.hideSoftInputFromWindow(token, InputMethodManager.HIDE_NOT_ALWAYS)
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(myNetReceiver)


    }

}