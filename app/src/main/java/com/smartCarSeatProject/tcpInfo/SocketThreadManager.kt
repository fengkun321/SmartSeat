package com.smartCarSeatProject.tcpInfo

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Message
import android.util.Log
import com.smartCarSeatProject.data.BaseVolume
import com.smartCarSeatProject.data.CreateCtrDataHelper
import com.smartCarSeatProject.data.DataAnalysisHelper
import com.smartCarSeatProject.data.DeviceWorkInfo
import java.util.*


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
        return isCanConnected() && isCan2Connected()
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



    fun clearAllTCPClient() {
        CanTaskCenter.sharedCenter(mContext).disconnect()
        LocTaskCenter.sharedCenter(mContext).disconnect()
    }


    /** 通过Can盒发送数据（不带超时机制） */
    fun StartChangeModelByCan(strData: String) {
//        if (!isCanConnected()) {
//            mContext.sendBroadcast(Intent(BaseVolume.BROADCAST_TCP_INFO_CAN)
//                    .putExtra(BaseVolume.BROADCAST_TYPE,BaseVolume.BROADCAST_TCP_CONNECT_CALLBACK)
//                    .putExtra(BaseVolume.BROADCAST_TCP_STATUS,false))
//            return
//        }
//        NowActionModel = Action_ChangModel
//        startTimeOut(false)
//        CanTaskCenter.sharedCenter(mContext).sendHexText(strData)
    }

    /** 通过Can盒调试通道气压（带保护机制：30秒后，如果还是调压模式，则强制切换到Normal模式） */
    fun StartSendDataByCan(strData: String) {
//        if (!isCanConnected()) {
//            mContext.sendBroadcast(Intent(BaseVolume.BROADCAST_TCP_INFO_CAN)
//                    .putExtra(BaseVolume.BROADCAST_TYPE,BaseVolume.BROADCAST_TCP_CONNECT_CALLBACK)
//                    .putExtra(BaseVolume.BROADCAST_TCP_STATUS,false))
//            return
//        }
//        NowActionModel = Action_CtrPress
//        startTimeOut(true)
//        CanTaskCenter.sharedCenter(mContext).sendHexText(strData)
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
        // 当前动作方式 切换模式
        val Action_ChangModel = 111
        // 控制气压
        val Action_CtrPress = 222
        var NowActionModel = Action_ChangModel

        fun sharedInstance(context: Context): SocketThreadManager {
            mContext = context
            if (!this::s_SocketManager.isInitialized) {
                s_SocketManager = SocketThreadManager()
            }
            return s_SocketManager
        }
    }


    var timer:Timer? = null
    /**
     * 开启/关闭计时器
     */
    fun startTimeOut(isRun : Boolean) {
        if (isRun) {
            timer?.cancel()
            timer = null
            timer = Timer()
            timer?.schedule(object : TimerTask() {
                override fun run() {
                    Log.e(TAG, "30秒时间到！强制切换为Normal模式")
                    StartChangeModelByCan(BaseVolume.COMMAND_CAN_MODEL_NORMAL_A_B)
                }
            }, (30 * 1000))
        }
        else {
            timer?.cancel()
        }
    }




}
