package com.smartCarSeatProject.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.wifi.ScanResult
import android.os.Bundle
import android.os.Handler
import android.text.format.DateUtils
import android.widget.ListView
import com.pulltorefreshlistview.PullToRefreshBase
import com.smartCarSeatProject.R
import com.smartCarSeatProject.adapter.WifiInfoAdapter
import com.smartCarSeatProject.dao.RemoteSQLInfo
import com.smartCarSeatProject.data.BaseVolume
import com.smartCarSeatProject.data.DeviceWorkInfo
import com.smartCarSeatProject.view.LoadingDialog
import com.smartCarSeatProject.view.SetValueAreaAddWindow
import com.smartCarSeatProject.wifiInfo.WIFIConnectionManager
import kotlinx.android.synthetic.main.layout_setwifi.*
import java.util.ArrayList

class SetWifiActivity: BaseActivity() {

    private var list = ArrayList<ScanResult>()
    private var wifiInfoAdapter: WifiInfoAdapter? = null
    private var scanResult: ScanResult? = null
    private var selectSSID = ""
    private var nowConnectedSSID = ""
    var setValueDialog : SetValueAreaAddWindow? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_setwifi)

        initUI()
        reciverBand()

        initWifiData()


    }

    fun initUI() {

        imgBack.setOnClickListener {
            MainControlActivity.getInstance()?.finish()
        }

        wifiInfoAdapter = WifiInfoAdapter(list, this,"")
        pull_to_refresh_listview.setAdapter(wifiInfoAdapter)

        pull_to_refresh_listview.setOnItemClickListener { adapterView, view, i, l ->
            scanResult = list[i-1]
            gotoConnectByWifi(scanResult!!.SSID)
        }

        //设定刷新监听
        pull_to_refresh_listview.setOnRefreshListener(PullToRefreshBase.OnRefreshListener<ListView> { refreshView ->
            val label = DateUtils.formatDateTime(applicationContext, System.currentTimeMillis(),
                    DateUtils.FORMAT_SHOW_TIME or DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_ABBREV_ALL)

            // 显示最后更新的时间
//            refreshView.loadingLayoutProxy.setLastUpdatedLabel(label)

            //代表下拉刷新
            if (refreshView.headerLayout.isShown) {
                object : Thread() {
                    override fun run() {
                        try {
                            Thread.sleep(500)

                            handler.sendEmptyMessage(99)

                        } catch (e: InterruptedException) {
                            e.printStackTrace()
                        }

                    }
                }.start()
            }
            //代表上拉刷新
            if (refreshView.footerLayout.isShown) {
                object : Thread() {
                    override fun run() {
                        try {
                            Thread.sleep(500)

                            handler.sendEmptyMessage(98)

                        } catch (e: InterruptedException) {
                            e.printStackTrace()
                        }

                    }
                }.start()
            }
        })

    }

    /** 监听广播  */
    private fun reciverBand() {
        val myIntentFilter = IntentFilter()
        myIntentFilter.addAction(BaseVolume.BROADCAST_UPDATA_WIFI_INFO)
        // 注册广播
        registerReceiver(myNetReceiver, myIntentFilter)
    }

    @Override
    override fun onResume() {
        super.onResume()

    }

    private fun initWifiData() {
        val ssid = WIFIConnectionManager.getInstance(this)?.nowConnectWifi
        nowConnectedSSID = ssid!!
        list = WIFIConnectionManager.getInstance(this)?.allWifiList ?:ArrayList<ScanResult>()
        wifiInfoAdapter!!.updateList(list,nowConnectedSSID)
        ToastMsg("Scan Wifi：${list.size}")
        //关闭刷新的动画
        pull_to_refresh_listview.onRefreshComplete()
    }

    private fun gotoConnectByWifi(selectSSID : String) {
        if (!(nowConnectedSSID.indexOf(selectSSID)<0)) {
            return
        }
        val intent = Intent()
        intent.setClass(this,ConnectWifiActivity::class.java)
        intent.putExtra("NowWifi",selectSSID)
        MainControlActivity.getInstance()?.GotoNewActivity("SetWifiActivity","ConnectWifiActivity",intent)

    }

    /****
     * 广播监听
     */
    private val myNetReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (action == BaseVolume.BROADCAST_UPDATA_WIFI_INFO) {
                initWifiData()
            }
        }
    }

    private val handler = object : Handler() {
        override fun handleMessage(msg: android.os.Message) {

            if (msg.what == 99) {
                initWifiData()
            }

            if (msg.what == 98) {
                initWifiData()
            }

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(myNetReceiver)
    }


}