package com.smartCarSeatProject.udpInfo

import android.content.Context
import android.os.Bundle
import android.os.Message
import android.util.Log
import android.widget.Toast
import com.smartCarSeatProject.tcpInfo.SocketThreadManager

class ScanDeviceUDP{

    companion object {
        var scanDeviceUDP : ScanDeviceUDP?= null
        fun getInstance(context: Context) : ScanDeviceUDP {
            if (scanDeviceUDP == null) {
                synchronized(ScanDeviceUDP::class) {
                    if (scanDeviceUDP == null) {
                        scanDeviceUDP = ScanDeviceUDP(context)
                    }
                }
            }
            return scanDeviceUDP as ScanDeviceUDP
        }
    }

    var context : Context? = null
    private val findDeviceIP : FindDeviceIP = FindDeviceIP()
    private var iScanCount = 0
    private var findDeviceList : ArrayList<String> = ArrayList()
    constructor(context: Context){
        this.context = context
        scanDeviceUDP = this
    }

    /**
     * 开始搜索
     */
    fun startScanDevice() {
        iScanCount = 0
        findDeviceList.clear()
        mHandler.post(runnableFindDevice)
    }

    /** 局域网搜索设备  */
    internal var runnableFindDevice: Runnable = object : Runnable {
        override fun run() {
            if (iScanCount < 3) {
                ++iScanCount
                context?.let { findDeviceIP.startFindCommand(it, mHandler) }
                // 1秒执行一次
                mHandler.postDelayed(this, (1000 * 1).toLong())
            }
            else {
                Log.e("","搜索停止！共搜索到：${findDeviceList.size}个设备！")
                Toast.makeText(context,"共搜索到：${findDeviceList.size}个设备！", Toast.LENGTH_SHORT).show()
            }
        }
    }

    internal var mHandler: android.os.Handler = object : android.os.Handler() {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            // 搜素到设备
            if (msg.what == FindDeviceIP.FindDeivceIPReuslt) {
                var bundle : Bundle = msg.data as Bundle
                val strIP = bundle.get("IP") as String
                val strName = bundle.get("Name") as String
                val strMac = bundle.get("Mac") as String

//                if (findDeviceList.indexOf(strMac) < 0) {
//                    findDeviceList.add(strMac)
//                    if (context?.let { SocketThreadManager.sharedInstance(it)?.getTCPClient(strMac) } == null)
//                        context?.let { SocketThreadManager.sharedInstance(it)?.createSocket(strMac,strIP,8080) }
//                    else
//                        Log.e("","该设备的连接已经存在！！！！！$strMac")
//                }

            }
        }
    }

    /**
     * 停止搜索
     */
    fun stopScan() {
        mHandler.removeCallbacks(runnableFindDevice)
    }

}