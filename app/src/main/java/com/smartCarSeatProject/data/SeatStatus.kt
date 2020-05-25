package com.smartCarSeatProject.data

enum class SeatStatus {

    press_unknown(1,"未知状态"),
    press_manual(2,"手动状态"),
    press_resume_reserve(3,"恢复预充气状态"),
    // 1-4,座椅自动完成
    press_reserve(4,"预充气完成"),
    // app触发5，座椅判断是否有人，如果有人，则开始探测，并上报状态5
    press_auto_probe(5,"正在进行自动探测"),
    // 座椅探测完成
    press_automatic(6,"自动状态"),
    press_automatic_manual(7,"自动状态下的手动调整"),
    develop(8,"开发者模式"),
    short_check(9,"临时自检");

    var iValue = -1
    var strInfo = ""

    constructor(iValue:Int,strInfo:String) {
        this.iValue = iValue
        this.strInfo = strInfo

    }


}