package com.smartCarSeatProject.data

import com.smartCarSeatProject.dao.DevelopDataInfo


class CreateCtrDataHelper {

    companion object {



        /** 设置某通道的气压值 */
        fun getCtrPressVaslueByNumber(iNumber:Int,iValue:Int):String {
            // 包头
            var strSendData = BaseVolume.COMMAND_HEAD
            // 数据类型
            if (iNumber < 4)
                strSendData += BaseVolume.COMMAND_CTR_1_4
            else if (iNumber in 4..7)
                strSendData += BaseVolume.COMMAND_CTR_5_8
            else if (iNumber in 8..11)
                strSendData += BaseVolume.COMMAND_CTR_9_12
            else if (iNumber >= 12)
                strSendData += BaseVolume.COMMAND_CTR_13_16

            // 要控制的通道位
            val iLoc = iNumber % 4
            // 内容8个字节，分别对应4个通道
            var valueList = arrayListOf<String>("0000","0000","0000","0000")
            val strV = String.format("%04x",iValue)
            valueList[iLoc] = strV

            valueList.forEach {
                strSendData += it
            }
            return strSendData
        }

        /** 手动模式下，设置16个通道的气压值 */
        fun getCtrPressBy16Manual(developDataInfo: DevelopDataInfo) : ArrayList<String>{
            var dataList = arrayListOf<String>()
            var strSendData1 = BaseVolume.COMMAND_HEAD + BaseVolume.COMMAND_CTR_1_4
            var strSendData2 = BaseVolume.COMMAND_HEAD + BaseVolume.COMMAND_CTR_5_8
            var strSendData3 = BaseVolume.COMMAND_HEAD + BaseVolume.COMMAND_CTR_9_12
            var strSendData4 = BaseVolume.COMMAND_HEAD + BaseVolume.COMMAND_CTR_13_16

            strSendData1 = strSendData1 +
                    String.format("%04x",developDataInfo.p_adjust_cushion_1.toInt())+
                    String.format("%04x",developDataInfo.p_adjust_cushion_2.toInt())+
                    String.format("%04x",developDataInfo.p_adjust_cushion_3.toInt())+
                    String.format("%04x",developDataInfo.p_adjust_cushion_4.toInt())

            strSendData2 = strSendData2 +
                    String.format("%04x",developDataInfo.p_adjust_cushion_5.toInt())+
                    String.format("%04x",developDataInfo.p_adjust_cushion_6.toInt())+
                    String.format("%04x",developDataInfo.p_adjust_cushion_7.toInt())+
                    String.format("%04x",developDataInfo.p_adjust_cushion_8.toInt())
            strSendData3 = strSendData3 +
                    String.format("%04x",developDataInfo.p_recog_back_A.toInt())+
                    String.format("%04x",developDataInfo.p_recog_back_B.toInt())+
                    String.format("%04x",developDataInfo.p_recog_back_C.toInt())+
                    String.format("%04x",developDataInfo.p_recog_back_D.toInt())
            strSendData4 = strSendData4 +
                    String.format("%04x",developDataInfo.p_recog_back_E.toInt())+
                    String.format("%04x",developDataInfo.p_recog_back_F.toInt())+
                    String.format("%04x",developDataInfo.p_recog_back_G.toInt())+
                    String.format("%04x",developDataInfo.p_recog_back_H.toInt())

            dataList.add(strSendData1)
            dataList.add(strSendData2)
            dataList.add(strSendData3)
            dataList.add(strSendData4)

            return dataList

        }

        /** 开发者模式下，同时设置16路通道的气压值 */
        fun getAllPressValueBy16(strA:String,strSeat:String,strB:String):ArrayList<String>{
            var dataList = arrayListOf<String>()
            var strSendData1 = BaseVolume.COMMAND_HEAD + BaseVolume.COMMAND_CTR_1_4
            var strSendData2 = BaseVolume.COMMAND_HEAD + BaseVolume.COMMAND_CTR_5_8
            var strSendData3 = BaseVolume.COMMAND_HEAD + BaseVolume.COMMAND_CTR_9_12
            var strSendData4 = BaseVolume.COMMAND_HEAD + BaseVolume.COMMAND_CTR_13_16

            strSendData1 = strSendData1 +
                    String.format("%04x",strSeat.toInt())+
                    String.format("%04x",strSeat.toInt())+
                    String.format("%04x",strSeat.toInt())+
                    String.format("%04x",strB.toInt())

            strSendData2 = strSendData2 +
                    String.format("%04x",strB.toInt())+
                    String.format("%04x",strB.toInt())+
                    String.format("%04x",strB.toInt())+
                    String.format("%04x",strB.toInt())
            strSendData3 = strSendData3 +
                    String.format("%04x",strA.toInt())+
                    String.format("%04x",strA.toInt())+
                    String.format("%04x",strA.toInt())+
                    String.format("%04x",strA.toInt())
            strSendData4 = strSendData4 +
                    String.format("%04x",strA.toInt())+
                    String.format("%04x",strA.toInt())+
                    String.format("%04x",strA.toInt())+
                    String.format("%04x",strA.toInt())

            dataList.add(strSendData1)
            dataList.add(strSendData2)
            dataList.add(strSendData3)
            dataList.add(strSendData4)

            return dataList
        }

        /** 调节AB面的模式 */
        fun getCtrModelAB(strModelA:String,strModelB:String):String {
            var strSendData = BaseVolume.COMMAND_HEAD+BaseVolume.COMMAND_CAN_MODEL_ID
            var strBinary = "00$strModelB$strModelA"
            var strHex = BaseVolume.binaryString2hexString(strBinary)
            strSendData = strSendData + "00000000"+strHex+"000000"
            return strSendData
        }

        /** 根据体重表，设置8路通道的气压值 */
        fun getCtrPressAllValueByPerson(controlPressInfo: ControlPressInfo):ArrayList<String>{
            var dataList = arrayListOf<String>()
            var strSendData1 = BaseVolume.COMMAND_HEAD + BaseVolume.COMMAND_CTR_1_4
            var strSendData2 = BaseVolume.COMMAND_HEAD + BaseVolume.COMMAND_CTR_5_8

            strSendData1 = strSendData1 +
                    String.format("%04x",controlPressInfo.press1)+
                    String.format("%04x",controlPressInfo.press2)+
                    String.format("%04x",controlPressInfo.press3)+
                    String.format("%04x",controlPressInfo.press4)

            strSendData2 = strSendData2 +
                    String.format("%04x",controlPressInfo.press5)+
                    String.format("%04x",controlPressInfo.press6)+
                    String.format("%04x",controlPressInfo.press7)+
                    String.format("%04x",controlPressInfo.press8)

            dataList.add(strSendData1)
            dataList.add(strSendData2)

            return dataList
        }


    }




}