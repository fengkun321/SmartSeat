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

        /** 删除某个手动模式 */
//        fun getCtrDeleteDataByManual(sqlCtrlInfo: SQLCtrlInfo?):String {
//            val iKey = sqlCtrlInfo?.iKey
//            return "W,2,"+iKey+BaseVolume.COMMAND_END
//        }

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

    }


}