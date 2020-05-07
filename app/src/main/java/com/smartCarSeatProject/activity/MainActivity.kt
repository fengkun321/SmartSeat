package com.smartCarSeatProject.activity

import android.net.wifi.ScanResult
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.widget.EditText
import com.smartCarSeatProject.R
import com.smartCarSeatProject.adapter.WifiInfoAdapter
import com.smartCarSeatProject.wifiInfo.WIFIConnectionManager
import kotlinx.android.synthetic.main.activity_main.*
import java.util.ArrayList


class MainActivity : BaseActivity(){

    private var list = ArrayList<ScanResult>()
    private var deviceAdapter: WifiInfoAdapter? = null
    private var scanResult: ScanResult? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Lambda写法，无参数，且无返回值  直接 执行语句
        // Lambda写法，一个参数view，且无返回值  直接 view -> 执行语句
        // Lambda写法，多个参数view1，view2，且无返回值  直接 (view1,view2) -> 执行语句
        // Lambda写法，多个参数view1，view2，且有返回值，要用大括号括起来 (view1,view2) -> { 执行语句，，，，且 return 值 }

        btnRefresh.setOnClickListener {
            view -> initWifiData()

        }

        deviceAdapter = WifiInfoAdapter(list, this,"")
        list_view.adapter = deviceAdapter

        list_view.setOnItemClickListener { adapterView, view, i, l ->
            scanResult = list[i]
            showEditPwd()
        }

        initWifiData()

    }



    private fun initWifiData() {
        list = WIFIConnectionManager.getInstance(this)?.allWifiList ?:ArrayList<ScanResult>()
        deviceAdapter!!.updateList(list,"")
        ToastMsg("搜索到：" + list.size + "个热点！")
        val ssid = WIFIConnectionManager.getInstance(this)?.nowConnectWifi
        tvNowWifi!!.text = "当前已连接：$ssid"
    }


    private fun showEditPwd() {

        val inputServer = EditText(this)
        val builder = AlertDialog.Builder(this)
        builder.setTitle("请输入" + scanResult!!.SSID + "的密码").setIcon(android.R.drawable.ic_dialog_info).setView(inputServer)
                .setNegativeButton("Cancel", null)
        builder.setPositiveButton("Ok") { dialog, which ->
            ToastMsg("开始连接热点：" + scanResult!!.SSID + "！")
            val strSSID = scanResult!!.SSID
            val strPwd = inputServer.text.toString()
//            WIFIConnectionManager.getInstance(mContext)?.startConnect(strSSID, strPwd)
        }
        builder.show()

    }


}
