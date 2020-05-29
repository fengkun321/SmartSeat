package com.smartCarSeatProject.data

enum class SeatStatus {

    press_wait_reserve(-999,"待初始化"),
    press_resume_reserve(0,"正在初始化"),
    press_reserve(1,"初始化完成"),
    press_auto_probe(2,"正在进行自动探测"),
    press_normal(3,"默认状态"),
    press_automatic(4,"自动状态"),
    press_manual(5,"手动状态"),
    develop(6,"开发者模式");

    var iValue = -1
    var strInfo = ""

    constructor(iValue:Int,strInfo:String) {
        this.iValue = iValue
        this.strInfo = strInfo

    }


}