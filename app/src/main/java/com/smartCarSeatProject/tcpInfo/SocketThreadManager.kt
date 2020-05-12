package com.smartCarSeatProject.tcpInfo

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Message
import android.util.Log
import com.smartCarSeatProject.data.BaseVolume
import java.util.HashMap


class SocketThreadManager() {

    var tcpClient:TCPClientS? = null
    var canClient:CanTCPClientS? = null


    /** 发送次数  */
    private var iSendCount = 0
    /** 当前发送的指令  */
    private var strNowSendData = ""

    var handler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
        }
    }

    /** 定时发送数据  */
    private val runnableSendTime = object : Runnable {
        override fun run() {
            if (iSendCount > 3) {
                // 连续发送4次都超时了，提示用户。
                Log.e(TAG, "连续发送4次都超时了，提示用户！")
                mContext.sendBroadcast(Intent(BaseVolume.BROADCAST_SEND_INFO).putExtra(BaseVolume.BROADCAST_TYPE,BaseVolume.BROADCAST_SEND_DATA_TIME_OUT))
                return
            }
            ++iSendCount
            if (tcpClient != null && tcpClient!!.isConnect) {
//                Log.e(TAG, "发送数据：$strNowSendData")
                tcpClient!!.sendHexText(strNowSendData)
                handler.postDelayed(this, 2000)// 延时2秒后，重新发送
            }
        }
    }

    fun createAllSocket() {
        if (tcpClient == null) {
            mContext.sendBroadcast(Intent(BaseVolume.BROADCAST_TCP_INFO).putExtra(BaseVolume.BROADCAST_TYPE,BaseVolume.BROADCAST_TCP_CONNECT_START))
            tcpClient = TCPClientS( "",BaseVolume.HostIp, BaseVolume.HostListenningPort, mContext)
        }
        else {
            if (tcpClient!!.isConnect) {
                // 已经连接的，则不用理会
            }
            else {
                mContext.sendBroadcast(Intent(BaseVolume.BROADCAST_SEND_INFO).putExtra(BaseVolume.BROADCAST_TYPE,BaseVolume.BROADCAST_SEND_DATA_START))
                tcpClient!!.doConnect()
            }
        }

        // Can盒连接
        createCanSocket()

    }

    /**
     * 创建Device连接
     */
    fun createDeviceSocket() {
        if (tcpClient == null) {
            mContext.sendBroadcast(Intent(BaseVolume.BROADCAST_TCP_INFO).putExtra(BaseVolume.BROADCAST_TYPE,BaseVolume.BROADCAST_TCP_CONNECT_START))
            tcpClient = TCPClientS( "",BaseVolume.HostIp, BaseVolume.HostListenningPort, mContext)
        }
        else {
            if (tcpClient!!.isConnect) {
                // 已经连接的，则不用理会
            }
            else {
                mContext.sendBroadcast(Intent(BaseVolume.BROADCAST_SEND_INFO).putExtra(BaseVolume.BROADCAST_TYPE,BaseVolume.BROADCAST_SEND_DATA_START))
                tcpClient!!.doConnect()
            }
        }
    }

    /**
     * 创建Can盒连接
     */
    fun createCanSocket() {
        if (canClient == null) {
            mContext.sendBroadcast(Intent(BaseVolume.BROADCAST_TCP_INFO_CAN).putExtra(BaseVolume.BROADCAST_TYPE,BaseVolume.BROADCAST_TCP_CONNECT_START))
            canClient = CanTCPClientS(mContext)
        }
        else {
            if (canClient!!.isConnect) {
                // 已经连接的，则不用理会
            }
            else {
                mContext.sendBroadcast(Intent(BaseVolume.BROADCAST_TCP_INFO_CAN).putExtra(BaseVolume.BROADCAST_TYPE,BaseVolume.BROADCAST_SEND_DATA_START))
                canClient!!.doConnect()
            }
        }
    }

    fun isCanConnected():Boolean {
        if (canClient == null)
            return false
        else
            return canClient!!.isConnect
    }

    fun isDeviceConnected():Boolean {
        if (tcpClient == null)
            return false
        else
            return tcpClient!!.isConnect
    }

    fun isTCPAllConnected(): Boolean? {
        if (tcpClient == null || canClient == null)
            return false
        else
            return (tcpClient!!.isConnect && canClient!!.isConnect)
    }

    fun clearAllTCPClient() {
        tcpClient?.closeTCPSocket()
        canClient?.closeTCPSocket()
    }

    /** 开始发送数据  */
    fun StartSendData(strData: String) {
        if (tcpClient == null || !(tcpClient!!.isConnect)) {
            mContext.sendBroadcast(Intent(BaseVolume.BROADCAST_TCP_INFO)
                    .putExtra(BaseVolume.BROADCAST_TYPE,BaseVolume.BROADCAST_TCP_CONNECT_CALLBACK)
                    .putExtra(BaseVolume.BROADCAST_TCP_STATUS,false))
            return
        }
        mContext.sendBroadcast(Intent(BaseVolume.BROADCAST_SEND_INFO)
                .putExtra(BaseVolume.BROADCAST_TYPE,BaseVolume.BROADCAST_SEND_DATA_START))
        iSendCount = 0
        strNowSendData = strData
        handler.post(runnableSendTime)
    }

    /** 停止发送  */
    fun StopSendData() {
        handler.removeCallbacks(runnableSendTime)
        mContext.sendBroadcast(Intent(BaseVolume.BROADCAST_SEND_INFO)
                .putExtra(BaseVolume.BROADCAST_TYPE,BaseVolume.BROADCAST_SEND_DATA_END))
    }

    /** 通过Can盒发送数据（不带超时机制） */
    fun StartSendDataByCan(strData: String) {
        if (canClient == null || !(canClient!!.isConnect)) {
            mContext.sendBroadcast(Intent(BaseVolume.BROADCAST_TCP_INFO_CAN)
                    .putExtra(BaseVolume.BROADCAST_TYPE,BaseVolume.BROADCAST_TCP_CONNECT_CALLBACK)
                    .putExtra(BaseVolume.BROADCAST_TCP_STATUS,false))
            return
        }
        canClient!!.sendHexText(strNowSendData)
    }


    companion object {

        protected val TAG = "SocketThreadManager"
        lateinit var s_SocketManager: SocketThreadManager
        lateinit var mContext : Context

        fun sharedInstance(context: Context): SocketThreadManager {
            mContext = context
            if (!this::s_SocketManager.isInitialized) {
                s_SocketManager = SocketThreadManager()
            }
            return s_SocketManager
        }
    }




}
