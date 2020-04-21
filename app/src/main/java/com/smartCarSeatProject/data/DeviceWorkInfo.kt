package com.smartCarSeatProject.data

import java.io.Serializable

class DeviceWorkInfo : Serializable{

    var strID = ""
    // 座椅当前状态
    var seatStatus = SeatStatus.press_unknown.iValue
//    var seatStatus = SeatStatus.press_automatic.iValue // 自动状态
//    var seatStatus = SeatStatus.press_automatic_manual.iValue // 手动状态
    // 控制气压8个（其中前三个，也是传感气压）
    var controlPressValueList :ArrayList<String> = arrayListOf("0","0","0","0","0","0","0","0")
    // 传感气压8个
    var sensePressValueListl :ArrayList<String> = arrayListOf("0","0","0","0","0","0","0","0","0","0","0")
    // 当前性别,1:男，2：女
    var nowSex = 1
    // 当前人种,1:东方人，2：西方人
    var nowRace = 1
    // 是否探测过
    var isProbe = false

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
        }

        nowSex = 1
        isProbe = false
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
        }
        for (i in 1..8) {
            controlPressValueList.add("255")
        }
    }


}