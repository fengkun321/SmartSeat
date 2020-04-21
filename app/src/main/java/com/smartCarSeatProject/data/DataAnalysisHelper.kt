package com.smartCarSeatProject.data

import android.content.Context
import android.content.Intent
import android.support.annotation.IntegerRes
import android.util.Log

class DataAnalysisHelper{
    private var strData: String? = null
    var context:Context? = null

    constructor(strID:String,context: Context) {
        deviceState = DeviceWorkInfo(strID)
        this.context = context
    }

    companion object {
        var deviceState = DeviceWorkInfo("")
        var dataAnalysisHelper: DataAnalysisHelper? = null

        fun getInstance(con:Context): DataAnalysisHelper?{
            if (dataAnalysisHelper == null) {
                dataAnalysisHelper = DataAnalysisHelper("",con)
            }
            return dataAnalysisHelper
        }

    }

    /** 开始数据解析  */
    fun startDataAnalysis(strData: String) {
        this.strData = strData
        val command_type = strData.substring(0,1)
        val type = strData.substring(2,3)
        val strContent = strData.substring(4,strData.indexOf(BaseVolume.COMMAND_END))
        // 读取的返回
        if (command_type.equals(BaseVolume.COMMAND_R,true)) {
            // 座椅状态
            if (type.equals(BaseVolume.COMMAND_TYPE_SEAT_STATUS,true)) {
                SeatStatus(strContent)
            }
            // 数据表
            else if (type.equals(BaseVolume.COMMAND_TYPE_SQL_CTR,true)) {
                SQLCtrlInfoAnalysis(strContent)
            }
            // 性别
            else if (type.equals(BaseVolume.COMMAND_TYPE_SEX,true)) {
                SexAnalySis(strContent)
            }

        }
        // 设置的返回
        else if (command_type.equals(BaseVolume.COMMAND_W,true)){
            // 座椅状态
            if (type.equals(BaseVolume.COMMAND_TYPE_SEAT_STATUS,true)) {
                context?.sendBroadcast(Intent(BaseVolume.BROADCAST_CTR_CALLBACK)
                        .putExtra(BaseVolume.BROADCAST_TYPE,BaseVolume.COMMAND_TYPE_SEAT_STATUS)
                        .putExtra(BaseVolume.BROADCAST_MSG,strContent))
            }
            // 数据表
            else if (type.equals(BaseVolume.COMMAND_TYPE_SQL_CTR,true)) {
                context?.sendBroadcast(Intent(BaseVolume.BROADCAST_CTR_CALLBACK)
                        .putExtra(BaseVolume.BROADCAST_TYPE,BaseVolume.COMMAND_TYPE_SQL_CTR)
                        .putExtra(BaseVolume.BROADCAST_MSG,strContent))
            }
            // 气压值
            else if (type.equals(BaseVolume.COMMAND_TYPE_PRESS,true)) {
                context?.sendBroadcast(Intent(BaseVolume.BROADCAST_CTR_CALLBACK)
                        .putExtra(BaseVolume.BROADCAST_TYPE,BaseVolume.COMMAND_TYPE_PRESS)
                        .putExtra(BaseVolume.BROADCAST_MSG,strContent))
            }
            // 性别，人种
            else if (type.equals(BaseVolume.COMMAND_TYPE_SEX,true)) {
                context?.sendBroadcast(Intent(BaseVolume.BROADCAST_CTR_CALLBACK)
                        .putExtra(BaseVolume.BROADCAST_TYPE,BaseVolume.COMMAND_TYPE_SEX)
                        .putExtra(BaseVolume.BROADCAST_MSG,strContent))
            }
        }
        // 主动上报的数据
        else if (command_type.equals(BaseVolume.COMMAND_S,true)){
            // 座椅状态
            if (type.equals(BaseVolume.COMMAND_TYPE_SEAT_STATUS,true)) {
                SeatStatus(strContent)
            }
            // 气压值
            else if (type.equals(BaseVolume.COMMAND_TYPE_PRESS,true)) {
                PressValue(strContent)
            }
        }
    }

    /** 性别解析 */
    fun SexAnalySis(strContent:String) {
        val strInfoList = strContent.split(",")
        deviceState.nowSex = strInfoList[0].toInt()
        deviceState.nowRace = strInfoList[1].toInt()
        deviceState.isProbe = (strInfoList[2].toInt()  == 1)
        context?.sendBroadcast(Intent(BaseVolume.BROADCAST_RESULT_DATA_INFO)
                .putExtra(BaseVolume.BROADCAST_TYPE,BaseVolume.COMMAND_TYPE_SEX)
                .putExtra(BaseVolume.BROADCAST_MSG,deviceState))
    }

    /** 座椅状态 */
    fun SeatStatus(strContent: String) {

        deviceState.seatStatus = strContent .toInt()
        Log.e("座椅状态 ", "接收到座椅状态值："+ deviceState.seatStatus)
        context?.sendBroadcast(Intent(BaseVolume.BROADCAST_RESULT_DATA_INFO)
                .putExtra(BaseVolume.BROADCAST_TYPE,BaseVolume.COMMAND_TYPE_SEAT_STATUS)
                .putExtra(BaseVolume.BROADCAST_MSG,deviceState))
    }

    /** 当前气压值变化 */
    fun PressValue(strContent: String) {
        // 属于在开发者模式下，调整到了初始气压值
        if (strContent.length == 1) {
            context?.sendBroadcast(Intent(BaseVolume.BROADCAST_RESULT_DATA_INFO)
                    .putExtra(BaseVolume.BROADCAST_MSG,deviceState)
                    .putExtra(BaseVolume.BROADCAST_TYPE,BaseVolume.COMMAND_INIT_VALUE_BY_DEVELOP))
            return
        }
        val pressValues = strContent.split(",")
        deviceState.controlPressValueList.clear()
        deviceState.sensePressValueListl.clear()
        // 前8个是设置气压
        for (iIndex in 0..7) {
            deviceState.controlPressValueList.add(pressValues[iIndex])
        }
        // 前三个也是传感气压
        deviceState.sensePressValueListl.add(pressValues[0])
        deviceState.sensePressValueListl.add(pressValues[1])
        deviceState.sensePressValueListl.add(pressValues[2])

        // 后8个是传感气压
        for (iIndex in 8..15) {
            deviceState.sensePressValueListl.add(pressValues[iIndex])
        }

        context?.sendBroadcast(Intent(BaseVolume.BROADCAST_RESULT_DATA_INFO)
                .putExtra(BaseVolume.BROADCAST_TYPE,BaseVolume.COMMAND_TYPE_PRESS)
                .putExtra(BaseVolume.BROADCAST_MSG,deviceState))
    }

    /** 数据表解析 */
    fun SQLCtrlInfoAnalysis(strContent: String) {
        val strInfoList = strContent.split(",")
        val iKey = strInfoList[0] .toInt()
        // 没有数据
        if (iKey == -1) {
            context?.sendBroadcast(Intent(BaseVolume.BROADCAST_RESULT_DATA_INFO)
                    .putExtra(BaseVolume.BROADCAST_TYPE,BaseVolume.COMMAND_TYPE_SQL_CTR)
                    .putExtra(BaseVolume.BROADCAST_MSG,deviceState))
            return
        }

        var strName = strInfoList[17]
        // hex转字节，然后转成String，存起来
//        strName = String(BaseVolume.hexStringToBytes(strName)!!)

        var pressList:ArrayList<String> = arrayListOf()
        // 遍历数组，拿到8个气压值
        for (i in 1..16) {
            pressList.add(strInfoList[i])
        }

        context?.sendBroadcast(Intent(BaseVolume.BROADCAST_RESULT_DATA_INFO)
                .putExtra(BaseVolume.BROADCAST_TYPE,BaseVolume.COMMAND_TYPE_SQL_CTR)
                .putExtra(BaseVolume.BROADCAST_MSG,deviceState))
    }

    /**
     * 根据Can返回的数据，解析当前气压值
     */
    fun analysisPressValueByCan(strData:String) {
        val strType= strData.substring(6,10)
        val strContent= strData.substring(10)
        // 前4个，其中1-3，既是传感气压也是控制气压
        if (strType == BaseVolume.COMMAND_CAN_1_4) {
            for (iIndex in 0..2) {
                val strValue = Integer.parseInt(strContent.substring(iIndex*4, (iIndex+1)*4), 16).toString()
                deviceState.sensePressValueListl.set(iIndex,strValue)
            }
            for (iIndex in 0..3) {
                val strValue = Integer.parseInt(strContent.substring(iIndex*4, (iIndex+1)*4), 16).toString()
                deviceState.controlPressValueList.set(iIndex,strValue)
            }
        }
        // 5-8，控制气压
        else if (strType == BaseVolume.COMMAND_CAN_5_8) {
            for (iIndex in 0..3) {
                val strValue = Integer.parseInt(strContent.substring(iIndex*4, (iIndex+1)*4), 16).toString()
                deviceState.controlPressValueList.set(iIndex+4,strValue)
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

}
