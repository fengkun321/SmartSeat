package com.smartCarSeatProject.data

import android.content.SharedPreferences
import android.util.Log
import java.io.Serializable
import kotlin.math.log

class DeviceWorkInfo : Serializable{

    var strID = ""
    // 座椅当前状态
    var seatStatus = SeatStatus.press_unknown.iValue
//    var seatStatus = SeatStatus.press_automatic.iValue // 自动状态
//    var seatStatus = SeatStatus.press_automatic_manual.iValue // 手动状态
    // 控制气压8个（其中前三个，也是传感气压）
    var controlPressValueList :ArrayList<String> = arrayListOf("0","0","0","0","0","0","0","0")
    // 传感气压11个
    var sensePressValueListl :ArrayList<String> = arrayListOf("0","0","0","0","0","0","0","0","0","0","0")

    // 缓存刚识别到人体的数据
    var recog_back_A_valueList = arrayListOf<String>()
    var recog_back_B_valueList = arrayListOf<String>()


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
    var HeartRate = ""
    // 信噪比
    var snr = ""
    // 呼吸率
    var BreathRate = ""
    // 情绪值
    var E_Index = ""
    // 舒张压
    var Dia_BP = ""
    // 收缩压
    var Sys_BP = ""

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
    var controlPressStatusList = arrayListOf<Int>()
    // 11个传感气压的状态
    var sensePressStatusList = arrayListOf<Int>()




    constructor(strID:String) {
        this.strID = strID
        // 初始化数据
        sensePressValueListl.clear()
        controlPressValueList.clear()

        for (i in 1..11) {
            sensePressValueListl.add("255")
            sensePressStatusList.add(STATUS_NORMAL)
        }
        for (i in 1..8) {
            controlPressValueList.add("255")
            recog_back_A_valueList.add("255")
            recog_back_B_valueList.add("255")
            controlPressStatusList.add(STATUS_NORMAL)
        }

        seatStatus = SeatStatus.press_unknown.iValue
//        seatStatus = SeatStatus.press_automatic.iValue // 自动状态
//        seatStatus = SeatStatus.press_automatic_manual.iValue // 手动状态

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

    /**
     * 保存当前16个传感器所有的值，用于收集数据！
      */
    fun saveRecogPressValue() {

        recog_back_B_valueList.addAll(controlPressValueList)

        for (i in 3..10) {
            recog_back_A_valueList.add(sensePressValueListl[i])
        }

    }




}