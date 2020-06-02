package com.smartCarSeatProject.dao

class DevelopDataInfo {

    companion object{
        val DATA_TYPE_USE = "DATA_TYPE_USE"
        val DATA_TYPE_DEVELOP = "DATA_TYPE_DEVELOP"
    }



    var iID = -1

    // 名称
    var strName = ""

    // 初始化A面8组气压
    var p_init_back_A = "1000"
    // 初始化靠背B面5组气压
    var p_init_back_B = "0"
    // 初始化坐垫3组气压
    var p_init_cushion = "0"

    // 识别后靠背A面8组
    var p_recog_back_A = "0"
    var p_recog_back_B = "0"
    var p_recog_back_C = "0"
    var p_recog_back_D = "0"
    var p_recog_back_E = "0"
    var p_recog_back_F = "0"
    var p_recog_back_G = "0"
    var p_recog_back_H = "0"

    // 识别后坐垫3组
    var p_recog_cushion_6 = "0"
    var p_recog_cushion_7 = "0"
    var p_recog_cushion_8 = "0"

    // 识别后靠背B面5组
    var p_recog_back_1 = "0"
    var p_recog_back_2 = "0"
    var p_recog_back_3 = "0"
    var p_recog_back_4 = "0"
    var p_recog_back_5 = "0"

    // 调节后坐垫3组
    var p_adjust_cushion_6 = "0"
    var p_adjust_cushion_7 = "0"
    var p_adjust_cushion_8 = "0"

    // 调节后靠背B面5组
    var p_adjust_cushion_1 = "0"
    var p_adjust_cushion_2 = "0"
    var p_adjust_cushion_3 = "0"
    var p_adjust_cushion_4 = "0"
    var p_adjust_cushion_5 = "0"

    // 位置调节
    @JvmField
    var l_location = "1"

    // 人员-性别
    var m_gender = "1"
    // 人员-国别
    var m_national = "1"
    // 人员-体重
    var m_weight = "0"
    // 人员-身高
    var m_height = "0"
    // 备注
    var strPSInfo = ""
    // 时间
    var saveTime = ""
    // 数据类型
    var dataType = ""
    // 心率
    var HeartRate = ""
    // 呼吸率
    var BreathRate = ""
    // 情绪值
    var E_Index = ""
    // 舒张压
    var Dia_BP = ""
    // 收缩压
    var Sys_BP = ""

    /** 初始化数据 */
    fun initData() {
        strName = ""

        // 初始化A面8组气压
        p_init_back_A = "0"
        // 初始化靠背B面5组气压
        p_init_back_B = "0"
        // 初始化坐垫3组气压
        p_init_cushion = "0"

        // 识别后靠背A面8组
        p_recog_back_A = "0"
        p_recog_back_B = "0"
        p_recog_back_C = "0"
        p_recog_back_D = "0"
        p_recog_back_E = "0"
        p_recog_back_F = "0"
        p_recog_back_G = "0"
        p_recog_back_H = "0"

        // 识别后坐垫3组
        p_recog_cushion_6 = "0"
        p_recog_cushion_7 = "0"
        p_recog_cushion_8 = "0"

        // 识别后靠背B面5组
        p_recog_back_1 = "0"
        p_recog_back_2 = "0"
        p_recog_back_3 = "0"
        p_recog_back_4 = "0"
        p_recog_back_5 = "0"

        // 调节后坐垫3组
        p_adjust_cushion_6 = "0"
        p_adjust_cushion_7 = "0"
        p_adjust_cushion_8 = "0"

        // 调节后靠背B面5组
        p_adjust_cushion_1 = "0"
        p_adjust_cushion_2 = "0"
        p_adjust_cushion_3 = "0"
        p_adjust_cushion_4 = "0"
        p_adjust_cushion_5 = "0"

        // 人员-性别
        m_gender = "1"
        // 人员-国别
        m_national = "1"
        // 人员-体重
        m_weight = "0"
        // 人员-身高
        m_height = "0"
        // 备注
        strPSInfo = ""
        // 时间
        saveTime = ""
        // 数据类型
        dataType = DATA_TYPE_USE
        // 位置调节
        l_location = "1"


    }



}