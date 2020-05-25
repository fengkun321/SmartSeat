package com.smartCarSeatProject.data

import com.smartCarSeatProject.dao.DevelopDataInfo


class CreateCtrDataHelper {

    companion object {

        /** 设置人的属性 */
        fun getCtrPeopleInfo(isMan: Boolean?,isCN:Boolean?) : String{
            var strSex = if (isMan!!) "1" else "2"
            var strRace = if (isCN!!) "1" else "2"

            return "W,4,"+strSex+","+strRace+BaseVolume.COMMAND_END
        }

        /** 手动模式下，操作数据库 */
//        fun getCtrSQLDataByIndex(sqlCtrlInfo: SQLCtrlInfo?) :String {
//            val iKey = sqlCtrlInfo?.iKey
//            // 名称转为字节，然后转为hex
////            val byteName = sqlCtrlInfo?.strName?.toByteArray()
////            val hexName = BaseVolume.bytesToHexString(byteName)
//            val pressList:ArrayList<String>? = sqlCtrlInfo?.pressValueList
//            var strPressInfo = ""
//            // 8个为控制气压
//            pressList?.forEach {
//                strPressInfo += "$it,"
//            }
////            return "W,2,"+iKey+","+strPressInfo+hexName+BaseVolume.COMMAND_END
//            return "W,2,"+iKey+","+strPressInfo+sqlCtrlInfo?.strName+BaseVolume.COMMAND_END
//        }

        /** 单独调整1个气压值 */
        fun getCtrPressBy1O(strNumber:String?,iValue:Int?) : String{
            return "W,3,"+strNumber+","+iValue+BaseVolume.COMMAND_END
        }

        /** 手动模式下，同时调整16个气压值 */
        fun getCtrPressBy16ManualO(nowSelectMemory: DevelopDataInfo?) :String {
            var strPressInfo = ""
            strPressInfo+= "${nowSelectMemory?.p_adjust_cushion_1},"
            strPressInfo+= "${nowSelectMemory?.p_adjust_cushion_2},"
            strPressInfo+= "${nowSelectMemory?.p_adjust_cushion_3},"
            strPressInfo+= "${nowSelectMemory?.p_adjust_cushion_4},"
            strPressInfo+= "${nowSelectMemory?.p_adjust_cushion_5},"
            strPressInfo+= "${nowSelectMemory?.p_adjust_cushion_6},"
            strPressInfo+= "${nowSelectMemory?.p_adjust_cushion_7},"
            strPressInfo+= "${nowSelectMemory?.p_adjust_cushion_8},"
            strPressInfo+= "${nowSelectMemory?.p_recog_back_A},"
            strPressInfo+= "${nowSelectMemory?.p_recog_back_B},"
            strPressInfo+= "${nowSelectMemory?.p_recog_back_C},"
            strPressInfo+= "${nowSelectMemory?.p_recog_back_D},"
            strPressInfo+= "${nowSelectMemory?.p_recog_back_E},"
            strPressInfo+= "${nowSelectMemory?.p_recog_back_F},"
            strPressInfo+= "${nowSelectMemory?.p_recog_back_G},"
            strPressInfo+= "${nowSelectMemory?.p_recog_back_H}"
            return "W,3,"+strPressInfo+BaseVolume.COMMAND_END
        }

        /** 开发者模式下，同时调整16个气压值 */
        fun getCtrPressBy16DevelopO(strA:String,strSeat:String,strB:String) :String {
//            var iAV = strA.toInt()
//            var iSeatV = strSeat.toInt()
//            var iBV = strB.toInt()
            // 1-3:座垫气压，4-8：B面设置气压，9-16：A面传感气压
            var pressList:ArrayList<String> = arrayListOf(strSeat,strSeat,strSeat,strB,strB,strB,strB,strB,strA,strA,strA,strA,strA,strA,strA,strA)
            var newPressList:ArrayList<Int> = arrayListOf()
            for (iNumber in 0..15) {
                var iV = pressList[iNumber].toInt()
                var iCha = BaseVolume.setDeviationValueMap[(iNumber+1)]
                var iZhi = iV + iCha!!
                newPressList.add(iZhi)

            }

            var strPressInfo = ""
            newPressList?.forEach {
                strPressInfo += "$it,"
            }
            strPressInfo = strPressInfo.substring(0,strPressInfo.length - 1)
            return "W,3,"+strPressInfo+BaseVolume.COMMAND_END
        }


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
                    String.format("%04x",developDataInfo.p_adjust_cushion_1)+
                    String.format("%04x",developDataInfo.p_adjust_cushion_2)+
                    String.format("%04x",developDataInfo.p_adjust_cushion_3)+
                    String.format("%04x",developDataInfo.p_adjust_cushion_4)

            strSendData2 = strSendData2 +
                    String.format("%04x",developDataInfo.p_adjust_cushion_5)+
                    String.format("%04x",developDataInfo.p_adjust_cushion_6)+
                    String.format("%04x",developDataInfo.p_adjust_cushion_7)+
                    String.format("%04x",developDataInfo.p_adjust_cushion_8)
            strSendData3 = strSendData3 +
                    String.format("%04x",developDataInfo.p_recog_back_A)+
                    String.format("%04x",developDataInfo.p_recog_back_B)+
                    String.format("%04x",developDataInfo.p_recog_back_C)+
                    String.format("%04x",developDataInfo.p_recog_back_D)
            strSendData4 = strSendData4 +
                    String.format("%04x",developDataInfo.p_recog_back_E)+
                    String.format("%04x",developDataInfo.p_recog_back_F)+
                    String.format("%04x",developDataInfo.p_recog_back_G)+
                    String.format("%04x",developDataInfo.p_recog_back_H)

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
                    String.format("%04x",strSeat)+
                    String.format("%04x",strSeat)+
                    String.format("%04x",strSeat)+
                    String.format("%04x",strB)

            strSendData2 = strSendData2 +
                    String.format("%04x",strB)+
                    String.format("%04x",strB)+
                    String.format("%04x",strB)+
                    String.format("%04x",strB)
            strSendData3 = strSendData3 +
                    String.format("%04x",strA)+
                    String.format("%04x",strA)+
                    String.format("%04x",strA)+
                    String.format("%04x",strA)
            strSendData4 = strSendData4 +
                    String.format("%04x",strA)+
                    String.format("%04x",strA)+
                    String.format("%04x",strA)+
                    String.format("%04x",strA)

            dataList.add(strSendData1)
            dataList.add(strSendData2)
            dataList.add(strSendData3)
            dataList.add(strSendData4)

            return dataList
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