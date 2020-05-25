package com.smartCarSeatProject.tcpInfo

import com.smartCarSeatProject.data.DeviceWorkInfo

interface ConnectAndDataListener {

    /** 开始连接 */
    fun onStartConnect()
    /** 连接回调 */
    fun onConnecteCallBack(isConnected: Boolean,strMsg:String)
    /** 开始发送数据 */
    fun onStartSendData()
    /** 停止发送数据 */
    fun onStopSendData()
    /** 发送超时 */
    fun onTimeOutSendData()
    /** 数据回调（数据类型，解析后的数据对象）*/
    fun onResultDataByType(strType:String,deviceWorkInfo: DeviceWorkInfo?)
    /** 控制回调（数据类型，结果）*/
    fun onCtrCallBack(strType:String,strMsg: String)


}