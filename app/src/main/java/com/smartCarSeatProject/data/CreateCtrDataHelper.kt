package com.smartCarSeatProject.data

import com.smartCarSeatProject.dao.MemoryDataInfo

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
        fun getCtrPressBy1(strNumber:String?,iValue:Int?) : String{
            return "W,3,"+strNumber+","+iValue+BaseVolume.COMMAND_END
        }

        /** 手动模式下，同时调整16个气压值 */
        fun getCtrPressBy16Manual(nowSelectMemory: MemoryDataInfo?) :String {
            var strPressInfo = ""
            strPressInfo+= "${nowSelectMemory?.strPress1},"
            strPressInfo+= "${nowSelectMemory?.strPress2},"
            strPressInfo+= "${nowSelectMemory?.strPress3},"
            strPressInfo+= "${nowSelectMemory?.strPress4},"
            strPressInfo+= "${nowSelectMemory?.strPress5},"
            strPressInfo+= "${nowSelectMemory?.strPress6},"
            strPressInfo+= "${nowSelectMemory?.strPress7},"
            strPressInfo+= "${nowSelectMemory?.strPress8},"
            strPressInfo+= "${nowSelectMemory?.strPressA},"
            strPressInfo+= "${nowSelectMemory?.strPressB},"
            strPressInfo+= "${nowSelectMemory?.strPressC},"
            strPressInfo+= "${nowSelectMemory?.strPressD},"
            strPressInfo+= "${nowSelectMemory?.strPressE},"
            strPressInfo+= "${nowSelectMemory?.strPressF},"
            strPressInfo+= "${nowSelectMemory?.strPressG},"
            strPressInfo+= "${nowSelectMemory?.strPressH}"
            return "W,3,"+strPressInfo+BaseVolume.COMMAND_END
        }

        /** 开发者模式下，同时调整16个气压值 */
        fun getCtrPressBy16Develop(strA:String,strSeat:String,strB:String) :String {
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
                strSendData += BaseVolume.COMMAND_CAN_5_8
            else if (iNumber in 8..11)
                strSendData += BaseVolume.COMMAND_CAN_9_12
            else if (iNumber >= 12)
                strSendData += BaseVolume.COMMAND_CAN_13_16

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


    }




}