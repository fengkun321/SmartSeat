package com.smartCarSeatProject.tcpInfo

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Message
import android.util.Log
import com.smartCarSeatProject.data.BaseVolume
import java.util.HashMap


class SocketThreadManager() {


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
            if (isDeviceConnected()) {
//                Log.e(TAG, "发送数据：$strNowSendData")
                TaskCenter.sharedCenter(mContext).sendHexText(strNowSendData)
                handler.postDelayed(this, 5000)// 延时5秒后，重新发送
            }
        }
    }

    /**
     * 创建Device连接
     */
    fun createDeviceSocket() {
        if (TaskCenter.sharedCenter(mContext).iConnectState == BaseVolume.TCP_CONNECT_STATE_DISCONNECT) {
            mContext.sendBroadcast(Intent(BaseVolume.BROADCAST_TCP_INFO).putExtra(BaseVolume.BROADCAST_TYPE,BaseVolume.BROADCAST_TCP_CONNECT_START))
            TaskCenter.sharedCenter(mContext).connect(BaseVolume.HostIp,BaseVolume.HostListenningPort)
        }
    }

    /**
     * 创建Can盒 1控制气压的连接
     */
    fun createCanSocket() {

        if (CanTaskCenter.sharedCenter(mContext).iConnectState == BaseVolume.TCP_CONNECT_STATE_DISCONNECT) {
            mContext.sendBroadcast(Intent(BaseVolume.BROADCAST_TCP_INFO_CAN).putExtra(BaseVolume.BROADCAST_TYPE,BaseVolume.BROADCAST_TCP_CONNECT_START))
            CanTaskCenter.sharedCenter(mContext).connect(BaseVolume.CanHostIp,BaseVolume.CanHostListenningPort)
        }

    }

    /**
     * 创建Can盒 2调节位置的连接
     */
    fun createLocSocket() {

        if (LocTaskCenter.sharedCenter(mContext).iConnectState == BaseVolume.TCP_CONNECT_STATE_DISCONNECT) {
            mContext.sendBroadcast(Intent(BaseVolume.BROADCAST_TCP_INFO_CAN).putExtra(BaseVolume.BROADCAST_TYPE,BaseVolume.BROADCAST_TCP_CONNECT_START))
            LocTaskCenter.sharedCenter(mContext).connect(BaseVolume.CanHostIp,BaseVolume.LocHostListenningPort)
        }

    }

    fun isTCPAllConnected():Boolean {
        return isCanConnected() && isCan2Connected() && isDeviceConnected()
    }

    fun isCanConnected():Boolean {
        if (CanTaskCenter.sharedCenter(mContext).iConnectState != BaseVolume.TCP_CONNECT_STATE_CONNECTED)
            return false
        else
            return CanTaskCenter.sharedCenter(mContext).isConnected
    }

    fun isCan2Connected():Boolean {
        if (LocTaskCenter.sharedCenter(mContext).iConnectState != BaseVolume.TCP_CONNECT_STATE_CONNECTED)
            return false
        else
            return LocTaskCenter.sharedCenter(mContext).isConnected
    }

    fun isDeviceConnected():Boolean {
        if (TaskCenter.sharedCenter(mContext).iConnectState != BaseVolume.TCP_CONNECT_STATE_CONNECTED)
            return false
        else
            return TaskCenter.sharedCenter(mContext).isConnected
    }


    fun clearAllTCPClient() {
        TaskCenter.sharedCenter(mContext).disconnect()
        CanTaskCenter.sharedCenter(mContext).disconnect()
        LocTaskCenter.sharedCenter(mContext).disconnect()
    }

    /** 开始发送数据  */
    fun StartSendData(strData: String) {
        if (!isDeviceConnected()) {
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
        if (!isCanConnected()) {
            mContext.sendBroadcast(Intent(BaseVolume.BROADCAST_TCP_INFO_CAN)
                    .putExtra(BaseVolume.BROADCAST_TYPE,BaseVolume.BROADCAST_TCP_CONNECT_CALLBACK)
                    .putExtra(BaseVolume.BROADCAST_TCP_STATUS,false))
            return
        }
        CanTaskCenter.sharedCenter(mContext).sendHexText(strData)
    }

    /** 通过Can盒 2发送数据（调节位置） */
    fun StartSendDataByCan2(strData: String) {
        if (!isCan2Connected()) {
            mContext.sendBroadcast(Intent(BaseVolume.BROADCAST_TCP_INFO_CAN)
                    .putExtra(BaseVolume.BROADCAST_TYPE,BaseVolume.BROADCAST_TCP_CONNECT_CALLBACK)
                    .putExtra(BaseVolume.BROADCAST_TCP_STATUS,false))
            return
        }
        LocTaskCenter.sharedCenter(mContext).sendHexText(strData)
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
