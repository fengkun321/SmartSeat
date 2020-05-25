package com.smartCarSeatProject.data

import android.content.Context
import android.content.Intent
import android.support.annotation.IntegerRes
import android.util.Log
import com.smartCarSeatProject.dao.DBManager
import com.smartCarSeatProject.data.DataAnalysisHelper.Companion.deviceState

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

    /**
     * 根据Can返回的数据，解析当前通道状态
     */
    fun analysisPressStatusByCan(strData:String) {
        val strType= strData.substring(6,10)
        val strContent= strData.substring(10,14)
        val iChannelStatus1_4 = strContent.substring(0,2).toInt()
        val iChannelStatus5_8 = strContent.substring(2).toInt()
        // 通道1-8
        if (strType == BaseVolume.COMMAND_CAN_STATUS_1_8) {

            val iChannel1 = iChannelStatus1_4 and 0x03
            val iChannel2 = iChannelStatus1_4 and 0x0c
            val iChannel3 = iChannelStatus1_4 and 0x30
            val iChannel4 = iChannelStatus1_4 and 0xc0
            val iChannel5 = iChannelStatus5_8 and 0x03
            val iChannel6 = iChannelStatus5_8 and 0x0c
            val iChannel7 = iChannelStatus5_8 and 0x30
            val iChannel8 = iChannelStatus5_8 and 0xc0

            deviceState.sensePressStatusList[0] = iChannel1
            deviceState.sensePressStatusList[1] = iChannel2
            deviceState.sensePressStatusList[2] = iChannel3

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
            val iChannel10 = iChannelStatus1_4 and 0x0c
            val iChannel11 = iChannelStatus1_4 and 0x30
            val iChannel12 = iChannelStatus1_4 and 0xc0
            val iChannel13 = iChannelStatus5_8 and 0x03
            val iChannel14 = iChannelStatus5_8 and 0x0c
            val iChannel15 = iChannelStatus5_8 and 0x30
            val iChannel16 = iChannelStatus5_8 and 0xc0

            deviceState.sensePressStatusList[3] = iChannel9
            deviceState.sensePressStatusList[4] = iChannel10
            deviceState.sensePressStatusList[5] = iChannel11
            deviceState.sensePressStatusList[6] = iChannel12
            deviceState.sensePressStatusList[7] = iChannel13
            deviceState.sensePressStatusList[8] = iChannel14
            deviceState.sensePressStatusList[9] = iChannel15
            deviceState.sensePressStatusList[10] = iChannel16
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

    }

    /** 遍历所有通道状态，只要有一个在动作，就不能控制 */
    fun getAllChannelStatus() : Int {
        for (iNumber in 1 .. deviceState.controlPressStatusList.size) {
            val it = deviceState.controlPressStatusList[iNumber-1]
            if (it == DeviceWorkInfo.STATUS_SETTING)
                return iNumber
        }

        for (iNumber in 1 .. deviceState.sensePressStatusList.size) {
            val it = deviceState.sensePressStatusList[iNumber-1]
            if (it == DeviceWorkInfo.STATUS_SETTING) {
                // 传感气压的通道号，其实是1,2,3，9，10,11，12，13,14，15,16
                var iTag = iNumber
                if (iNumber > 3) {
                    iTag += 5
                }
                return iNumber
            }
        }
        return -1
    }

    /** 根据传感气压，计算身高体重 */
    fun measureHeightWeight() {
        // 体重
        deviceState.nowWeight =  -43.107-
                0.01886*deviceState.sensePressValueListl[0].toInt()+
                0.053896*deviceState.sensePressValueListl[1].toInt()-
                0.0030395*deviceState.sensePressValueListl[2].toInt()+
                0.024802*deviceState.sensePressValueListl[3].toInt()-
                0.030553*deviceState.sensePressValueListl[4].toInt()+
                0.064134*deviceState.sensePressValueListl[5].toInt()+
                0.029931*deviceState.sensePressValueListl[6].toInt()+
                0.061179*deviceState.sensePressValueListl[7].toInt()-
                0.034076*deviceState.sensePressValueListl[8].toInt()+
                0.040492*deviceState.sensePressValueListl[9].toInt()-
                0.013437*deviceState.sensePressValueListl[10].toInt()

        // 身高
        deviceState.nowWeight = 113.69-
                0.045598*deviceState.sensePressValueListl[0].toInt()+
                0.050396*deviceState.sensePressValueListl[1].toInt()-
                0.0031454*deviceState.sensePressValueListl[2].toInt()+
                0.022727*deviceState.sensePressValueListl[3].toInt()+
                0.0068805*deviceState.sensePressValueListl[4].toInt()+
                0.062379*deviceState.sensePressValueListl[5].toInt()-
                0.001087*deviceState.sensePressValueListl[6].toInt()-
                0.08063*deviceState.sensePressValueListl[7].toInt()+
                0.021199*deviceState.sensePressValueListl[8].toInt()+
                0.050612*deviceState.sensePressValueListl[9].toInt()-
                0.00043809*deviceState.sensePressValueListl[10].toInt()

        deviceState.nowBMI = (deviceState.nowWeight*1000)/ (deviceState.nowHeight*deviceState.nowHeight)

        Log.e("DeviceWorkInfo","身高：${deviceState.nowHeight}，体重：${deviceState.nowWeight},BMI：${deviceState.nowBMI}")

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
            Log.e("test2", "test2: cityInof:$city1")
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




}
