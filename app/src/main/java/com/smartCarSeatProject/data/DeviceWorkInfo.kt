package com.smartCarSeatProject.data

import android.content.SharedPreferences
import android.util.Log
import java.io.Serializable
import kotlin.math.log

class DeviceWorkInfo : Serializable{

    var strID = ""
    // 座椅当前状态
    var seatStatus = SeatStatus.press_wait_reserve.iValue
    // 控制气压8个（其中后三个，也是传感气压）
    var controlPressValueList :ArrayList<String> = arrayListOf("0","0","0","0","0","0","0","0")
    // 传感气压11个
    var sensePressValueListl :ArrayList<String> = arrayListOf("0","0","0","0","0","0","0","0","0","0","0")

    // 控制缓存气压8个（其中后三个，也是传感气压）
    var controlPressValueBufferList :ArrayList<String> = arrayListOf("0","0","0","0","0","0","0","0")
    // 传感缓存气压8个
    var sensePressValueBufferListl :ArrayList<String> = arrayListOf("0","0","0","0","0","0","0","0")

    // 缓存刚识别到人体的数据
    var recog_back_A_valueList = arrayListOf<String>() // 8个
    var recog_back_B_valueList = arrayListOf<String>() // 8个


    // 位置调节
    var l_location = "1"
    // 人员-性别 男
    var m_gender = true
    // 人员-国别 东方
    var m_national = true

    // 身高
    var nowHeight = -1.0
    // 体重
    var nowWeight = -1.0
    // BMI
    var nowBMI = -1.0

    // 心率
    var HeartRate = "0.0"
    // 信噪比
    var snr = "0.0"
    // 呼吸率
    var BreathRate = "0.0"
    // 情绪值
    var E_Index = "0.0"
    // 舒张压
    var Dia_BP = "0.0"
    // 收缩压
    var Sys_BP = "0.0"

    companion object{

        // 恢复默认
        val STATUS_NORMAL = 0
        // 正在动作
        val STATUS_SETTING = 1
        // 动作完成
        val STATUS_SETTED = 2
        // 按摩
        val STATUS_MASSAGE = 3

    }

    // 8个传感气压的状态
    var controlPressStatusList = arrayListOf<Int>()// 8个
    // 8个传感气压的状态
    var sensePressStatusList = arrayListOf<Int>()// 8个




    constructor(strID:String) {
        this.strID = strID
        // 初始化数据
        sensePressValueListl.clear()
        controlPressValueList.clear()

        for (i in 1..11) {
            sensePressValueListl.add("255")
        }
        for (i in 1..8) {
            controlPressValueList.add("255")
            recog_back_A_valueList.add("0")
            recog_back_B_valueList.add("0")
            sensePressStatusList.add(STATUS_NORMAL)
            controlPressStatusList.add(STATUS_NORMAL)
        }

//        seatStatus = SeatStatus.press_wait_reserve.iValue

    }

    fun clearPressData() {
        // 初始化数据
        sensePressValueListl.clear()
        controlPressValueList.clear()
        for (i in 1..11) {
            sensePressValueListl.add("255")
            sensePressStatusList.add(STATUS_NORMAL)
        }
        for (i in 1..8) {
            controlPressValueList.add("255")
            controlPressStatusList.add(STATUS_NORMAL)
        }
    }

    /** 根据传感气压，计算身高体重 */
    fun measureHeightWeight(iSensePressValueListl : ArrayList<Int>) {

        var strSensePressInfo = ""
        for (iNumber in 0 until iSensePressValueListl.size) {
            strSensePressInfo += ""+(iNumber+1)+"："+ iSensePressValueListl[iNumber]+","
        }

        Log.e("DeviceWorkInfo","计算数据：获取当前A面的压力平均值：${strSensePressInfo}")

        // 体重
        nowWeight =  -43.107-
                0.01886* iSensePressValueListl[0]+
                0.053896* iSensePressValueListl[1]-
                0.0030395* iSensePressValueListl[2]+
                0.024802* iSensePressValueListl[3]-
                0.030553* iSensePressValueListl[4]+
                0.064134* iSensePressValueListl[5]+
                0.029931* iSensePressValueListl[6]+
                0.061179* iSensePressValueListl[7]-
                0.034076* iSensePressValueListl[8]+
                0.040492* iSensePressValueListl[9]-
                0.013437* iSensePressValueListl[10]

        // 身高
        nowHeight = 113.69-
                0.045598* iSensePressValueListl[0]+
                0.050396* iSensePressValueListl[1]-
                0.0031454* iSensePressValueListl[2]+
                0.022727* iSensePressValueListl[3]+
                0.0068805* iSensePressValueListl[4]+
                0.062379* iSensePressValueListl[5]-
                0.001087* iSensePressValueListl[6]-
                0.08063* iSensePressValueListl[7]+
                0.021199* iSensePressValueListl[8]+
                0.050612* iSensePressValueListl[9]-
                0.00043809* iSensePressValueListl[10]



        DataAnalysisHelper.deviceState.nowBMI = (DataAnalysisHelper.deviceState.nowWeight*1000)/ (DataAnalysisHelper.deviceState.nowHeight* DataAnalysisHelper.deviceState.nowHeight)

        Log.e("DeviceWorkInfo","计算数据：身高：${DataAnalysisHelper.deviceState.nowHeight}，体重：${DataAnalysisHelper.deviceState.nowWeight},BMI：${DataAnalysisHelper.deviceState.nowBMI}")

        val strWeight = String.format("%.2f",nowWeight)
        val strHeight = String.format("%.2f",nowHeight)
        nowWeight = strWeight.toDouble()
        nowHeight = strHeight.toDouble()
        // 缓存数据
        saveRecogPressValue(iSensePressValueListl)

    }

    /**
     * 保存当前16个传感器所有的值，用于收集数据！
      */
    fun saveRecogPressValue(iStatDataList:ArrayList<Int>) {

        recog_back_B_valueList.clear()
        recog_back_B_valueList.addAll(controlPressValueList)

        for (i in 0 .. 2) {
            recog_back_B_valueList[5+i] = iStatDataList[i].toString()
        }

        for (i in 3 until iStatDataList.size) {
            recog_back_A_valueList[i-3] = iStatDataList[i].toString()
        }

    }

    /**
     * 判断有人没人
     */
    fun isCheckHavePerson() : Boolean {
        val offset = 150
        var iPressInside = sensePressValueListl[6].toInt()+ sensePressValueListl[8].toInt()
        var iPressOut = sensePressValueListl[5].toInt()+ sensePressValueListl[7].toInt()
        if ((iPressInside > iPressOut - offset) and (iPressInside < iPressOut + offset)) {
            return false
        }
        return true
    }




}