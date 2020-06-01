package com.smartCarSeatProject.data

import android.content.Context
import android.content.Intent
import android.support.annotation.IntegerRes
import android.util.Log
import com.smartCarSeatProject.dao.DBManager
import com.smartCarSeatProject.data.DataAnalysisHelper.Companion.deviceState
import com.smartCarSeatProject.tcpInfo.SocketThreadManager

class DataAnalysisHelper{
    private var strData: String? = null
    lateinit var context:Context

    constructor(strID:String,context: Context) {
        deviceState = DeviceWorkInfo(strID)
        this.context = context
    }

    companion object {
        var deviceState = DeviceWorkInfo("")
        lateinit var dataAnalysisHelper: DataAnalysisHelper

        fun getInstance(con:Context): DataAnalysisHelper{
            if (!this::dataAnalysisHelper.isInitialized) {
                dataAnalysisHelper = DataAnalysisHelper("",con)
            }
            return dataAnalysisHelper
        }

    }


    /**
     * 根据Can返回的数据，解析当前气压值
     */
    fun analysisPressValueByCan(strData:String) {
        val strType= strData.substring(6,10)
        val strContent= strData.substring(10)
        // 1-3，控制气压
        if (strType == BaseVolume.COMMAND_CAN_1_4) {
            for (iIndex in 0..3) {
                val strValue = Integer.parseInt(strContent.substring(iIndex*4, (iIndex+1)*4), 16).toString()
                deviceState.controlPressValueList.set(iIndex,strValue)
            }
        }
        // 5-8，其中6,7,8，既是传感气压也是控制气压
        else if (strType == BaseVolume.COMMAND_CAN_5_8) {
            for (iIndex in 0..3) {
                val strValue = Integer.parseInt(strContent.substring(iIndex*4, (iIndex+1)*4), 16).toString()
                deviceState.controlPressValueList.set(iIndex+4,strValue)
            }
            // 传感气压
            for (iIndex in 1..3) {
                val strValue = Integer.parseInt(strContent.substring(iIndex*4, (iIndex+1)*4), 16).toString()
                deviceState.sensePressValueListl.set(iIndex-1,strValue)
            }

        }
        // 9-12，传感气压
        else if (strType == BaseVolume.COMMAND_CAN_9_12) {
            for (iIndex in 0..3) {
                val strValue = Integer.parseInt(strContent.substring(iIndex*4, (iIndex+1)*4), 16).toString()
                deviceState.sensePressValueListl.set(iIndex+3,strValue)
            }
        }
        // 13-16，传感气压
        else if (strType == BaseVolume.COMMAND_CAN_13_16) {
            for (iIndex in 0..3) {
                val strValue = Integer.parseInt(strContent.substring(iIndex*4, (iIndex+1)*4), 16).toString()
                deviceState.sensePressValueListl.set(iIndex+7,strValue)
            }
        }

        var strLog = ""

        for (iNumber in 0..7) {
            strLog += ""+(iNumber+1)+"："+ deviceState.controlPressValueList[iNumber]+","
        }

        for (iNumber in 3..10) {
            strLog += ""+(iNumber+6)+"："+ deviceState.sensePressValueListl[iNumber]+","
        }

        Log.e("气压值 ", strLog)

        context?.sendBroadcast(Intent(BaseVolume.BROADCAST_RESULT_DATA_INFO)
                .putExtra(BaseVolume.BROADCAST_TYPE,BaseVolume.COMMAND_TYPE_PRESS)
                .putExtra(BaseVolume.BROADCAST_MSG,deviceState))

    }

    /**
     * 根据Can返回的数据，解析当前通道状态
     */
    fun analysisPressStatusByCan(strData:String) {
        val strType= strData.substring(6,10)
        val strContent= strData.substring(10,14)

        val iChannelStatus1_4 = Integer.parseInt(strContent.substring(0,2),16);
        val iChannelStatus5_8 = Integer.parseInt(strContent.substring(2,4),16);
        // 通道1-8
        if (strType == BaseVolume.COMMAND_CAN_STATUS_1_8) {

            val iChannel1 = iChannelStatus1_4 and 0x03
            val iChannel2 = iChannelStatus1_4 and 0x0c shr 2
            val iChannel3 = iChannelStatus1_4 and 0x30 shr 4
            val iChannel4 = iChannelStatus1_4 and 0xc0 shr 6
            val iChannel5 = iChannelStatus5_8 and 0x03
            val iChannel6 = iChannelStatus5_8 and 0x0c shr 2
            val iChannel7 = iChannelStatus5_8 and 0x30 shr 4
            val iChannel8 = iChannelStatus5_8 and 0xc0 shr 6
            deviceState.controlPressStatusList[0] = iChannel1
            deviceState.controlPressStatusList[1] = iChannel2
            deviceState.controlPressStatusList[2] = iChannel3
            deviceState.controlPressStatusList[3] = iChannel4
            deviceState.controlPressStatusList[4] = iChannel5
            deviceState.controlPressStatusList[5] = iChannel6
            deviceState.controlPressStatusList[6] = iChannel7
            deviceState.controlPressStatusList[7] = iChannel8
        }
        // 通道9-16
        else if (strType == BaseVolume.COMMAND_CAN_STATUS_9_16) {

            val iChannel9 = iChannelStatus1_4 and 0x03
            val iChannel10 = iChannelStatus1_4 and 0x0c shr 2
            val iChannel11 = iChannelStatus1_4 and 0x30 shr 4
            val iChannel12 = iChannelStatus1_4 and 0xc0 shr 6
            val iChannel13 = iChannelStatus5_8 and 0x03
            val iChannel14 = iChannelStatus5_8 and 0x0c shr 2
            val iChannel15 = iChannelStatus5_8 and 0x30 shr 4
            val iChannel16 = iChannelStatus5_8 and 0xc0 shr 6

            deviceState.sensePressStatusList[0] = iChannel9
            deviceState.sensePressStatusList[1] = iChannel10
            deviceState.sensePressStatusList[2] = iChannel11
            deviceState.sensePressStatusList[3] = iChannel12
            deviceState.sensePressStatusList[4] = iChannel13
            deviceState.sensePressStatusList[5] = iChannel14
            deviceState.sensePressStatusList[6] = iChannel15
            deviceState.sensePressStatusList[7] = iChannel16
        }


        var strLog = ""

        for (iNumber in 0..7) {
            strLog += ""+(iNumber+1)+"："+ deviceState.controlPressStatusList[iNumber]+","
        }

        for (iNumber in 3..10) {
            strLog += ""+(iNumber+6)+"："+ deviceState.sensePressStatusList[iNumber]+","
        }

        Log.e("通道状态 ", strLog)


        context?.sendBroadcast(Intent(BaseVolume.BROADCAST_RESULT_DATA_INFO)
                .putExtra(BaseVolume.BROADCAST_TYPE,BaseVolume.COMMAND_TYPE_CHANNEL_STATUS)
                .putExtra(BaseVolume.BROADCAST_MSG,deviceState))

//        var iSettedCountA = 0
//        var iNormalCountA = 0
//        for (iState in deviceState.sensePressStatusList) {
//            if (iState == DeviceWorkInfo.STATUS_SETTING || iState == DeviceWorkInfo.STATUS_MASSAGE)
//                return
//            if (iState == DeviceWorkInfo.STATUS_SETTED)
//                ++iSettedCountA
//            if (iState == DeviceWorkInfo.STATUS_NORMAL)
//                ++iNormalCountA
//        }
////
//        var iSettedCountB = 0
//        var iNormalCountB = 0
//        for (iState in deviceState.controlPressStatusList) {
//            if (iState == DeviceWorkInfo.STATUS_SETTING || iState == DeviceWorkInfo.STATUS_MASSAGE)
//                return
//            if (iState == DeviceWorkInfo.STATUS_SETTED)
//                ++iSettedCountB
//            if (iState == DeviceWorkInfo.STATUS_NORMAL)
//                ++iNormalCountB
//        }
//
//        // 如果是在调压
//        if (SocketThreadManager.NowActionModel == SocketThreadManager.Action_CtrPress) {
//            // 如果已经调完，且存在没切换到Normal的，则强制切换
//            if (iSettedCountA > 0 || iSettedCountB > 0) {
//                SocketThreadManager.sharedInstance(context).StartChangeModelByCan(BaseVolume.COMMAND_CAN_MODEL_NORMAL_A_B)
//            }
//        }
    }




    // 人体气压查询表:男女2→国别2→胖瘦3
    var bodyPressByType = arrayOf(
            // 男
            arrayOf(arrayListOf("t_body_1","t_body_2","t_body_3"),// 中国/瘦，匀称，胖
                    arrayListOf("t_body_4","t_body_5","t_body_6")),// 国外/瘦，匀称，胖,
            // 女
            arrayOf(arrayListOf("t_body_7","t_body_8","t_body_9"),// 中国/瘦，匀称，胖
                    arrayListOf("t_body_10","t_body_11","t_body_12")) // 国外/瘦，匀称，胖
    )
    /** 根据男女，身高体重，BMI等获取对应的控制气压值 */
    fun getAutoCtrPressByPersonStyle(isMan:Boolean,isCN:Boolean): ControlPressInfo?{
        var iOne = 0
        var iTwo = 0
        var iThree = 0
        val nowBMI = deviceState.nowBMI
        val nowWeight = deviceState.nowWeight

        iOne = if(isMan) 0 else 1
        iTwo = if(isCN) 0 else 1

        if (nowBMI <= 18.4 ) // 瘦
            iThree = 0
        else if (nowBMI > 18.4 && nowBMI < 28) // 匀称
            iThree = 1
        else // 胖
            iThree = 2

        val strTableName = bodyPressByType[iOne][iTwo][iThree]
        val dbManager = DBManager(context)
        val pressInfo = dbManager.queryLikeWeight(strTableName, nowWeight)
        for (city1 in pressInfo) {
            Log.e("getPressInfo", "推荐数据表： pressInfo:$city1")
        }
        dbManager.closeDb()
        return if (pressInfo.size > 0) pressInfo[0] else null

    }

    /**
     * 判断有人没人
     */
    fun isCheckHavePerson() : Boolean {
        val offset = 150
        var iPressInside = deviceState.sensePressStatusList[6]+ deviceState.sensePressStatusList[8]
        var iPressOut = deviceState.sensePressStatusList[5]+ deviceState.sensePressStatusList[7]
        if ((iPressInside > iPressOut - offset) and (iPressInside < iPressOut + offset)) {
            return false
        }
        return true
    }

    /** 判断A面座椅是否已经进入某模式 */
    fun isCheckAllStateModeA(referState:Int):Boolean {
        for (iState in deviceState.sensePressStatusList) {
            if (iState != referState)
                return false
        }
        return true
    }

    /** 判断B面座椅是否已经进入某模式 */
    fun isCheckAllStateModeB(referState:Int):Boolean {
        for (iState in deviceState.controlPressStatusList) {
            if (iState != referState)
                return false
        }
        return true
    }




}
