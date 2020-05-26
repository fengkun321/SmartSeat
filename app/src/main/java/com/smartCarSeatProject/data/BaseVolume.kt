package com.smartCarSeatProject.data

import android.graphics.Color


class BaseVolume {

    companion object {

        // 该版本的打包时间
        const val StartTime = "2019-10-15 00:00:00"

        // IP
        const val CanHostIp = "192.168.1.10"
        const val HostIp = "10.10.10.254"
        // Port
        const val CanHostListenningPort = 4001
        const val LocHostListenningPort = 4002
        const val HostListenningPort = 9999
        // WIFI标志
        const val WIFI_SIGN = "HLK_OpenWrt"
//        const val WIFI_SIGN = "TP-LINK_FK"
        // 拖拽进度条最小值
        const val ProgressValueMin = 255
        // 拖拽进度条最大值
        const val ProgressValueMax = 3600
        // 气压最小值
        const val pressValueMin = 25
        // 气压最大值
        const  val pressValueMax = 500

        // 各个通道气压的偏差值
        val deviationValueMap = hashMapOf<Int,Int>(
                1 to 0, 2 to 0, 3 to 0, 4 to 0,
                5 to 0, 6 to 0, 7 to 0, 8 to 0,
                9 to 0, 10 to 0, 11 to 0, 12 to 0,
                13 to 0, 14 to 0, 15 to 0, 16 to 0)

        // 各个通道设置偏差值
        val setDeviationValueMap = hashMapOf<Int,Int>(
                1 to 0, 2 to 0, 3 to 0, 4 to 0,
                5 to 0, 6 to 0, 7 to 0, 8 to 0,
                9 to 0, 10 to 0, 11 to 0, 12 to 0,
                13 to 0, 14 to 0, 15 to 0, 16 to 0)

        // 各个通道气压的偏差值
//        val deviationValueMap = hashMapOf<Int,Int>(
//                1 to -2, 2 to 3, 3 to 34, 4 to 0,
//                5 to 20, 6 to 6, 7 to 0, 8 to 22,
//                9 to 16, 10 to 12, 11 to 13, 12 to 16,
//                13 to 5, 14 to 5, 15 to -10, 16 to 10)
//
//        // 各个通道设置偏差值
//        val setDeviationValueMap = hashMapOf<Int,Int>(
//                1 to 14, 2 to -21, 3 to -238, 4 to 0,
//                5 to -140, 6 to -42, 7 to 0, 8 to -154,
//                9 to -112, 10 to -84, 11 to -91, 12 to -112,
//                13 to -35, 14 to -35, 15 to 70, 16 to -70)


        var strSensorInitValue = ""
        var strSeatInitValue = ""
        var strAdjustInitialValue = ""



        const val BROADCAST_UPDATA_WIFI_INFO = "BROADCAST_UPDATA_WIFI_INFO"
        const val BROADCAST_FINISH_APPLICATION = "BROADCAST_FINISH_APPLICATION"
        const val BROADCAST_RESET_ACTION = "BROADCAST_RESET_ACTION"

        const val BROADCAST_TCP_INFO = "BROADCAST_TCP_INFO"
        const val BROADCAST_TCP_INFO_CAN = "BROADCAST_TCP_INFO_CAN"
        const val BROADCAST_TCP_INFO_CAN2 = "BROADCAST_TCP_INFO_CAN2"
        const val BROADCAST_TCP_CONNECT_START = "BROADCAST_TCP_START_CONNECT"
        const val BROADCAST_TCP_CONNECT_CALLBACK = "BROADCAST_TCP_CONNECT_CALLBACK"
        const val BROADCAST_TCP_STATUS = "BROADCAST_TCP_CONNECT_STATUS"
        const val BROADCAST_SEND_INFO = "BROADCAST_SEND_INFO"
        const val BROADCAST_SEND_DATA_START = "BROADCAST_SEND_DATA_START"
        const val BROADCAST_SEND_DATA_END = "BROADCAST_SEND_DATA_END"
        const val BROADCAST_SEND_DATA_TIME_OUT = "BROADCAST_SEND_DATA_TIME_OUT"
        const val BROADCAST_RESULT_DATA_INFO = "BROADCAST_RESULT_DATA_INFO"
        const val BROADCAST_CTR_CALLBACK = "BROADCAST_CTR_CALLBACK"
        const val BROADCAST_GOBACK_MENU = "BROADCAST_GOBACK_MENU"
        const val BROADCAST_MSG = "BROADCAST_MSG"
        const val BROADCAST_TYPE = "BROADCAST_TYPE"

        // 连接已断开
        const val TCP_CONNECT_STATE_DISCONNECT = -1

        // 连接成功
        const val TCP_CONNECT_STATE_CONNECTED = 1

        // 连接中
        const val TCP_CONNECT_STATE_CONNECTTING = 0


        /** 读取座椅当前状态 */
        const val COMMAND_READ_SEAT_STATUS = "R,1\r\n"

        /** 设置座椅的状态-开始探测 */
        const val COMMAND_SET_STATUS_PROBE = "W,1,0\r\n"
        /** 设置座椅的状态-保压 */
        const val COMMAND_SET_STATUS_KEEP = "W,1,1\r\n"
        /** 设置座椅的状态-重置 */
        const val COMMAND_SET_STATUS_RESET = "W,1,2\r\n"

        /** 设置座椅的模式-自动 */
        const val COMMAND_SET_MODE_AUTO = "W,5,1\r\n"
        /** 设置座椅的模式-手动 */
        const val COMMAND_SET_MODE_MANUAL = "W,5,2\r\n"
        /** 设置座椅的模式-开发者 */
        const val COMMAND_SET_MODE_DEVELOP = "W,5,3\r\n"

        /** Can盒的数据头 */
        const val COMMAND_HEAD = "080000"
        const val COMMAND_CAN_1_4 = "0201"
        const val COMMAND_CAN_5_8 = "0202"
        const val COMMAND_CAN_9_12 = "0221"
        const val COMMAND_CAN_13_16 = "0222"

        /** Can盒的状态 */
        const val COMMAND_CAN_STATUS_1_8 = "0203"
        const val COMMAND_CAN_STATUS_9_16 = "0223"

        /** 控制16个通道的数据头 */
        val COMMAND_CTR_1_4 = "03bb"
        val COMMAND_CTR_5_8 = "03cc"
        val COMMAND_CTR_9_12 = "03dd"
        val COMMAND_CTR_13_16 = "03ee"

        /** 电机位置1-6 结构：包头+ID+数据 */
        const val COMMAND_CAN_LOCATION_ID = "02f5"
        const val COMMAND_CAN_LOCATION_1 = COMMAND_HEAD+COMMAND_CAN_LOCATION_ID+"1100000000000000"
        const val COMMAND_CAN_LOCATION_2 = COMMAND_HEAD+COMMAND_CAN_LOCATION_ID+"1200000000000000"
        const val COMMAND_CAN_LOCATION_3 = COMMAND_HEAD+COMMAND_CAN_LOCATION_ID+"1300000000000000"
        const val COMMAND_CAN_LOCATION_4 = COMMAND_HEAD+COMMAND_CAN_LOCATION_ID+"1400000000000000"
        const val COMMAND_CAN_LOCATION_5 = COMMAND_HEAD+COMMAND_CAN_LOCATION_ID+"1500000000000000"
        const val COMMAND_CAN_LOCATION_6 = COMMAND_HEAD+COMMAND_CAN_LOCATION_ID+"1600000000000000"
        // 每次发完位置后，要再发一条清零指令才会动作
        const val COMMAND_CAN_LOCATION_0 = COMMAND_HEAD+COMMAND_CAN_LOCATION_ID+"0000000000000000"

        /** 通道状态 */
        const val CHANNEL_STATUS = "CHANNEL_STATUS"

        /** 数据尾 */
        const val COMMAND_END = "\r\n"
        /** 命令类型：主动上报 */
        const val COMMAND_S = "S"
        /** 命令类型：设置 */
        const val COMMAND_W = "W"
        /** 命令类型：读取 */
        const val COMMAND_R = "R"
        /** 操作结果：成功 */
        const val COMMAND_ACK = "ACK"
        /** 分隔符 */
        const val COMMAND_FenGe = ","

        /** 指令：座椅状态 */
        const val COMMAND_TYPE_SEAT_STATUS = "1"
        /** 指令：数据库相关 */
        const val COMMAND_TYPE_SQL_CTR = "2"
        /** 指令：气压相关 */
        const val COMMAND_TYPE_PRESS = "3"
        /** 指令：性别 */
        const val COMMAND_TYPE_SEX = "4"
        /** 指令：座椅模式 */
        const val COMMAND_TYPE_SEX_MODE = "5"
        /** 指令：通道状态 */
        const val COMMAND_TYPE_CHANNEL_STATUS = "6"

        /** 开发者模式下，恢复到了初始气压值 */
        const val COMMAND_INIT_VALUE_BY_DEVELOP = "COMMAND_INIT_VALUE_BY_DEVELOP"

        /**
         * 16进制字符串 转换成byte字节数组
         * @param hexString 16进制字符串
         * @return byte字节数组
         */
        @JvmStatic
        fun hexStringToBytes(hexString: String?): ByteArray? {
            var hexString = hexString
            if (hexString == null || hexString == "") {
                return null
            }
            hexString = hexString.toUpperCase()
            val length = hexString.length / 2
            val hexChars = hexString.toCharArray()
            val d = ByteArray(length)
            for (i in 0 until length) {
                val pos = i * 2
                d[i] = ((charToByte(hexChars[pos]).toInt() shl 4) or charToByte(hexChars[pos + 1]).toInt()).toByte()
            }
            return d
        }

        /**
         * Convert char to byte
         * @param c char
         * @return byte
         */
        private fun charToByte(c: Char): Byte {
            return ("0123456789ABCDEF".indexOf(c)).toByte()
        }

        /**
         * byte字节数组转换成16进制字符串
         * @param byte[] 字节数组
         * @return 16进制字符串
         */
        fun bytesToHexString(src: ByteArray?): String {
            val stringBuilder = StringBuilder("")
            if (src == null || src.size <= 0) {
                return ""
            }
            for (i in src.indices) {
                val v = src[i].toInt() and 0xFF
                val hv = Integer.toHexString(v)
                if (hv.length < 2) {
                    stringBuilder.append(0)
                }
                stringBuilder.append(hv)
            }
            return stringBuilder.toString()
        }

        /**
         * 根据通道计算实际最大值或最小值
         * iValue：最终参考值 50/500
         */
        fun getPressMinMaxByChannel(iChannel: Int,iValue:Int):Int {
            // 偏差值
            var iChaZhi = deviationValueMap[iChannel]
            if (iChaZhi == null)
                iChaZhi = 0
            var press = 0

            // press*0.14262 - 11.904+iChaZhi = iValue
            press = Math.round(((iValue-iChaZhi)+11.904)/0.14262).toInt()

            return press
        }


        /**
         * 根据数值，转换为对的压力值
         */
        fun getPressByValue(iValue:Int,iChannel:Int):Int {
            // 偏差值
            var iChaZhi = deviationValueMap[iChannel]
            if (iChaZhi == null)
                iChaZhi = 0
            var iNowPress = iValue*0.14262 - 11.904
            iNowPress += iChaZhi

            // 四舍五入
            var iPress = Math.round(iNowPress).toInt()

            return iPress
        }

        /**
         * 根据气压值（50-500），转换对应的颜色
         */
        fun getColorByPressValue(iPress:Int,iChannel:Int):Int{

            var iA = 127
            var iR = 0
            var iG = 0
            var iB = 0

            val iMidVale = (pressValueMin+ pressValueMax)/2
            // 最大值为红色
            if (iPress >= pressValueMax) {
                iR = 255
                iG = 0
                iB = 0
            }
            // 最小值为蓝色
            else if (iPress <= pressValueMin) {
                iR = 0
                iG = 0
                iB = 255
            }
            // 中间值为白色
            else if (iPress == iMidVale) {
                iR = 255
                iG = 255
                iB = 255
            }
            // 蓝白之间
            else if (iPress < iMidVale) {
                val lV = iPress/iMidVale.toFloat()
                iR = (lV*255.0f).toInt()
                iG = (lV*255.0f).toInt()
                iB = 255
            }
            // 白红之间
            else if (iPress > iMidVale) {
                val lV = (iPress-iMidVale)/iMidVale.toFloat()
                iR = 255
                iG = 255 - (lV*255.0f).toInt()
                iB = 255 - (lV*255.0f).toInt()
            }

            val color = Color.argb(iA, iR, iG, iB)
            return color
        }

        /**
         * 根据数值（255-3600），转换对应的颜色
         */
        fun getColorByValue(iValue:Int,iChannel:Int):Int{

            // 根据数值，转换为对的压力值
            var iPress = getPressByValue(iValue,iChannel)

            var iA = 255
            var iR = 0
            var iG = 0
            var iB = 0

            val iMidVale = (pressValueMin+ pressValueMax)/2

            if (iPress == iMidVale) {
                iR = 255
                iG = 255
                iB = 255
            }
            else if (iPress < iMidVale) {
                val lV = iPress/iMidVale.toFloat()
                iR = (lV*255.0f).toInt()
                iG = (lV*255.0f).toInt()
                iB = 255
            }
            else if (iPress > iMidVale) {
                val lV = (iPress-iMidVale)/iMidVale.toFloat()
                iR = 255
                iG = 255 - (lV*255.0f).toInt()
                iB = 255 - (lV*255.0f).toInt()
            }

            val color = Color.argb(iA, iR, iG, iB)
            return color
        }




    }




    /**
     * 计算校验
     * @return
     */
    fun getCheck(bData: ByteArray): String {
        var check = ""
        var num = 0
        for (b in bData) {
            num = num + b
        }
        if (num > 255) {
            num = num % 256
        }
        // 十进制转十六进制
        num = 255 - num + 1
        check = Integer.toHexString(num)
        // 十六进制长度为1，则补加“0”
        if (check.length < 2) {
            check = "0$check"
        }

        return check
    }







}