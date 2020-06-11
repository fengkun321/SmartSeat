package com.smartCarSeatProject.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.drawable.GradientDrawable
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.TextView
import com.smartCarSeatProject.R
import com.smartCarSeatProject.data.*
import com.smartCarSeatProject.tcpInfo.SocketThreadManager
import com.smartCarSeatProject.view.AreaAddWindowHint
import kotlinx.android.synthetic.main.layout_auto_seat.view.*
import kotlinx.android.synthetic.main.layout_automatic.*
import java.util.*


class AutomaticActivity: BaseActivity(), View.OnClickListener{


    // 样式的集合
    var drawableList = arrayListOf<GradientDrawable>()
    // 视图的集合
    var viewList = arrayListOf<TextView>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_automatic)


        initUI()
        initData()
        reciverBand()
        checkNowAutoProgress()

    }

    fun initUI() {
        imgBack.setOnClickListener {
            MainControlActivity.getInstance()?.finish()
        }

        cbAutoTiYa.isChecked = DataAnalysisHelper.deviceState.isAutoTiYa
        cbAutoWeiZhi.isChecked = DataAnalysisHelper.deviceState.isAutoWeiZhi
        cbJianKang.isChecked = DataAnalysisHelper.deviceState.isAutoJianKang

        cbAutoTiYa.setOnCheckedChangeListener(onChangeListener)
        cbAutoWeiZhi.setOnCheckedChangeListener(onChangeListener)
        cbJianKang.setOnCheckedChangeListener(onChangeListener)

        btnNan.setOnClickListener(this)
        btnNv.setOnClickListener(this)
        btnDongF.setOnClickListener(this)
        btnXiF.setOnClickListener(this)

        initShape(autoSeat.view0)
        initShape(autoSeat.view1)
        initShape(autoSeat.view2)
        initShape(autoSeat.viewL3)
        initShape(autoSeat.viewR3)
        initShape(autoSeat.viewL4)
        initShape(autoSeat.viewR4)
        initShape(autoSeat.viewL5)
        initShape(autoSeat.viewR5)
        initShape(autoSeat.viewL6)
        initShape(autoSeat.viewR6)
        initShape(autoSeat.viewL7)
        initShape(autoSeat.viewR7)
        initShape(autoSeat.viewL8)
        initShape(autoSeat.viewR8)
        initShape(autoSeat.viewL9)
        initShape(autoSeat.viewR9)
        initShape(autoSeat.viewL10)
        initShape(autoSeat.viewR10)

        updateSeatView()
    }

    private val onChangeListener = object:CompoundButton.OnCheckedChangeListener{
        override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
            if (buttonView?.tag.toString().equals("press")) {
                DataAnalysisHelper.deviceState.isAutoTiYa = isChecked
//                onAutoSetPressByStatus()
            }

            else if (buttonView?.tag.toString().equals("location")) {
                DataAnalysisHelper.deviceState.isAutoWeiZhi = isChecked
//                onAutoSetLocationByStatus()
            }
            else if (buttonView?.tag.toString().equals("massage")) {
                DataAnalysisHelper.deviceState.isAutoJianKang = isChecked
//                onAutoSetMassageByStatus()
            }
        }

    }


    /**
     * 初始化样式
     * 是否是小view
     * 如果是小view,则有左右之分
     */
    fun initShape(view: TextView) {
        // shape
        var drawable = GradientDrawable()
        // 四个角度
        drawable.cornerRadius = 10f
        // 边框：宽，颜色
        drawable.setStroke(0, resources.getColor(R.color.colorWhite))
        // 填充色
        drawable.setColor(resources.getColor(R.color.colorTransparency))
        view.setBackgroundDrawable(drawable)

        drawableList.add(drawable)
        viewList.add(view)

    }

    fun initData() {
//        val isMan = getBooleanBySharedPreferences(SEX_MAN)
        val isMan = DataAnalysisHelper.deviceState.m_gender
        val isCN = getBooleanBySharedPreferences(COUNTRY_CN)
        btnNan.tag = isMan
        btnNv.tag = !isMan
        btnDongF.tag = isCN
        btnXiF.tag = !isCN

        btnNan.setTextColor(getColor(R.color.black1))
        btnNv.setTextColor(getColor(R.color.black1))
        btnDongF.setTextColor(getColor(R.color.black1))
        btnXiF.setTextColor(getColor(R.color.black1))
        DataAnalysisHelper.deviceState.m_gender = isMan
        DataAnalysisHelper.deviceState.m_national = isCN
        // 男
        if (isMan) {
            btnNan.tag = true
            btnNan.setTextColor(getColor(R.color.colorWhite))
        }
        else {
            btnNv.tag = true
            btnNv.setTextColor(getColor(R.color.colorWhite))
        }

        // 东方人
        if (isCN) {
            btnDongF.tag = true
            btnDongF.setTextColor(getColor(R.color.colorWhite))
        }
        else {
            btnXiF.tag = true
            btnXiF.setTextColor(getColor(R.color.colorWhite))
        }

    }

    /** 监听广播  */
    private fun reciverBand() {
        val myIntentFilter = IntentFilter()
        myIntentFilter.addAction(BaseVolume.BROADCAST_RESULT_DATA_INFO)
        myIntentFilter.addAction(BaseVolume.BROADCAST_CTR_CALLBACK)
        myIntentFilter.addAction(BaseVolume.BROADCAST_AUTO_MODEL)
        myIntentFilter.addAction(BaseVolume.BROADCAST_STOP_LOCATION_TIMEOUT)
        myIntentFilter.addAction(BaseVolume.BROADCAST_PERSON_INFO)
        // 注册广播
        registerReceiver(myNetReceiver, myIntentFilter)
    }


    /** 根据选择状态自动调节各个气袋气压
     *  男女
     *  国别
     *  体压自适应
     *  位置自适应
     *  健康自适应
     * */
    private fun onAutoSetPressByStatus() {
        ToastMsg("自动调整气压！")
        // 体压自适应启动
        if (cbAutoTiYa.isChecked) {
            // 根据男女，身高体重，获取气压表
            DataAnalysisHelper.deviceState.iNowAutoProgress = 3
            val isMan = DataAnalysisHelper.deviceState.m_gender
            val isCN = DataAnalysisHelper.deviceState.m_national
            // 设置气压，并提示用户，正在自动调整
            val willCtrPressValue = DataAnalysisHelper.getInstance(mContext)?.getAutoCtrPressByPersonStyle(isMan,isCN)
            val sendData = CreateCtrDataHelper.getCtrPressAllValueByPerson(willCtrPressValue!!)
            Log.e("DeviceWorkInfo","计算数据：自动设置B面的气压值：1-4:${sendData[0]} & 5-8:${sendData[1]}")
            // 只调整B面的，所以将A面设为normal，B面设为adjust
            SocketThreadManager.sharedInstance(mContext)?.StartChangeModelByCan(CreateCtrDataHelper.getCtrModelAB(BaseVolume.COMMAND_CAN_MODEL_NORMAL,BaseVolume.COMMAND_CAN_MODEL_ADJUST))
            SocketThreadManager.sharedInstance(mContext)?.StartSendDataByCan(sendData[0])
            SocketThreadManager.sharedInstance(mContext)?.StartSendDataByCan(sendData[1])
        }
        else {
            DataAnalysisHelper.deviceState.iNowAutoProgress = 4
            onAutoSetMassageByStatus()
        }

        checkNowAutoProgress()



    }

    /**
     * 位置自适应
     */
    private fun onAutoSetLocationByStatus() {
        // 位置自适应
        if (cbAutoWeiZhi.isChecked) {
            locationTimeOut(true)
            ToastMsg("自动调整位置！")
            DataAnalysisHelper.deviceState.iNowAutoProgress = 2
            if (DataAnalysisHelper.deviceState.nowHeight <= 160)
                SocketThreadManager.sharedInstance(mContext)?.StartSendDataByCan2(BaseVolume.COMMAND_CAN_LOCATION_1)
            else if (DataAnalysisHelper.deviceState.nowHeight > 160 && DataAnalysisHelper.deviceState.nowHeight <= 170)
                SocketThreadManager.sharedInstance(mContext)?.StartSendDataByCan2(BaseVolume.COMMAND_CAN_LOCATION_2)
            else if (DataAnalysisHelper.deviceState.nowHeight > 170 && DataAnalysisHelper.deviceState.nowHeight <= 175)
                SocketThreadManager.sharedInstance(mContext)?.StartSendDataByCan2(BaseVolume.COMMAND_CAN_LOCATION_3)
            else if (DataAnalysisHelper.deviceState.nowHeight > 175 && DataAnalysisHelper.deviceState.nowHeight <= 180)
                SocketThreadManager.sharedInstance(mContext)?.StartSendDataByCan2(BaseVolume.COMMAND_CAN_LOCATION_4)
            else if (DataAnalysisHelper.deviceState.nowHeight > 180 && DataAnalysisHelper.deviceState.nowHeight <= 185)
                SocketThreadManager.sharedInstance(mContext)?.StartSendDataByCan2(BaseVolume.COMMAND_CAN_LOCATION_5)
            else if (DataAnalysisHelper.deviceState.nowHeight > 185)
                SocketThreadManager.sharedInstance(mContext)?.StartSendDataByCan2(BaseVolume.COMMAND_CAN_LOCATION_6)

            SocketThreadManager.sharedInstance(mContext)?.StartSendDataByCan2(BaseVolume.COMMAND_CAN_LOCATION_0)
        }
        else {
            DataAnalysisHelper.deviceState.iNowAutoProgress = 3
            onAutoSetPressByStatus()
        }

        checkNowAutoProgress()

    }

    /**
     * 健康自适应
     */
    private fun onAutoSetMassageByStatus() {
        // 健康自适应,信噪比为负数，当前值不可信，不用执行
//        if (cbJianKang.isChecked && DataAnalysisHelper.deviceState.snr.toFloat() > 0) {
        if (cbJianKang.isChecked) {
            ToastMsg("健康监测！")
            DataAnalysisHelper.deviceState.iNowAutoProgress = 4
            runMassageInfo()
        }
        // 关闭按摩自适应
        else {
            DataAnalysisHelper.deviceState.iNowAutoProgress = 5
        }
        checkNowAutoProgress()
    }

    /** 执行massage */
    private fun runMassageInfo() {

        var strSendData = BaseVolume.COMMAND_CAN_MODEL_NORMAL_A_B
        // 1.心率/血压/情绪值 正常状态( 60<心率<100 and 90<收缩压< 139 and 60<舒张压<89 and 心理压力值<4 )
        // 字体绿色显示，启动A面轻度按摩,播放音乐
        if ((DataAnalysisHelper.deviceState.HeartRate.toFloat()>60 && DataAnalysisHelper.deviceState.HeartRate.toFloat()<100)
                && (DataAnalysisHelper.deviceState.Sys_BP.toFloat()>90 && DataAnalysisHelper.deviceState.Sys_BP.toFloat()<139)
                && (DataAnalysisHelper.deviceState.Dia_BP.toFloat()>60 && DataAnalysisHelper.deviceState.Dia_BP.toFloat()<89)
                && DataAnalysisHelper.deviceState.E_Index.toFloat()<4) {
            strSendData = CreateCtrDataHelper.getCtrModelAB(BaseVolume.COMMAND_CAN_MODEL_MASG_1,BaseVolume.COMMAND_CAN_MODEL_NORMAL)
            MainControlActivity.getInstance()?.changePersonInfoTextColor(R.color.colorGreen)
            // 播放音乐
            MainControlActivity.getInstance()?.playOrPauseMedia("Alohal_release.mp3",true,1000*60)
        }
        // 2.心率/血压/情绪值 轻度超标 （40<心率<60 or 100<心率<160 or 140<收缩压<159 or 90<舒张压<99 or 4<心理压力值<5 )
        // 字体黄色显示，同时启动座椅A面及B面中度按摩，播放音乐
        else if ((DataAnalysisHelper.deviceState.HeartRate.toFloat()>40 && DataAnalysisHelper.deviceState.HeartRate.toFloat()<60)
                || (DataAnalysisHelper.deviceState.HeartRate.toFloat()>100 && DataAnalysisHelper.deviceState.HeartRate.toFloat()<160)
                || (DataAnalysisHelper.deviceState.Sys_BP.toFloat()>140 && DataAnalysisHelper.deviceState.Sys_BP.toFloat()<159)
                || (DataAnalysisHelper.deviceState.Dia_BP.toFloat()>90 && DataAnalysisHelper.deviceState.Dia_BP.toFloat()<99)
                || (DataAnalysisHelper.deviceState.E_Index.toFloat()>4 && DataAnalysisHelper.deviceState.E_Index.toFloat()<5)) {
            strSendData = CreateCtrDataHelper.getCtrModelAB(BaseVolume.COMMAND_CAN_MODEL_MASG_2,BaseVolume.COMMAND_CAN_MODEL_NORMAL)
            MainControlActivity.getInstance()?.changePersonInfoTextColor(R.color.colorYellow)
            // 播放音乐
            MainControlActivity.getInstance()?.playOrPauseMedia("Alohal_release.mp3",true,1000*60*3)

        }
        // 3.心率/血压/情绪值 中度超标（心率<40 or 心率>160 or 160<收缩压 or 100<舒张压 or 心理压力值>5 )
        // 字体红色显示，提示停车，座椅归位出厂状态，并启动A面B面同时3级按摩
        else if (DataAnalysisHelper.deviceState.HeartRate.toFloat()<40
                || DataAnalysisHelper.deviceState.HeartRate.toFloat()>160
                || DataAnalysisHelper.deviceState.Sys_BP.toFloat()>160
                || DataAnalysisHelper.deviceState.Dia_BP.toFloat()>100
                || DataAnalysisHelper.deviceState.E_Index.toFloat()>5) {
            // AB面同时按摩3
            strSendData = CreateCtrDataHelper.getCtrModelAB(BaseVolume.COMMAND_CAN_MODEL_MASG_3,BaseVolume.COMMAND_CAN_MODEL_NORMAL)
            // 座椅恢复到出厂设置
            SocketThreadManager.sharedInstance(mContext).StartSendDataByCan2(BaseVolume.COMMAND_CAN_LOCATION_DEFAULT)
            // 字显示红色
            MainControlActivity.getInstance()?.changePersonInfoTextColor(R.color.device_red)
            // 播放音乐
            MainControlActivity.getInstance()?.playOrPauseMedia("Alohal.mp3",true,1000*60*3)
            showDangerDialog()

        }
        // 其他范围，就按摩一下吧，
        else {
            ToastMsg("不在健康诊断范围内，也放个音乐，按摩一下吧！")
            strSendData = CreateCtrDataHelper.getCtrModelAB(BaseVolume.COMMAND_CAN_MODEL_MASG_1,BaseVolume.COMMAND_CAN_MODEL_NORMAL)
            MainControlActivity.getInstance()?.changePersonInfoTextColor(R.color.colorGreen)
            // 播放音乐
            MainControlActivity.getInstance()?.playOrPauseMedia("Alohal_release.mp3",true,1000*60)
        }
        Log.e("ManualActivity", "当前按摩指令：$strSendData")
        SocketThreadManager.sharedInstance(mContext).StartChangeModelByCan(strSendData)
    }

    /** 危险提醒 */
    var areaDangerDialog:AreaAddWindowHint? = null
    private fun showDangerDialog() {
        if (areaDangerDialog == null) {
            areaDangerDialog = AreaAddWindowHint(this,R.style.Dialogstyle,"System",
                    object : AreaAddWindowHint.PeriodListener {
                        override fun refreshListener(string: String) {
                        }
                        override fun cancelListener() {
                        }
                    },"Fatigue driving, please take a rest!",true)
        }
        if (!areaDangerDialog!!.isShowing)
            areaDangerDialog!!.show()
    }

    /** 判断当前自动模式的进度 */
    private fun checkNowAutoProgress() {
        MainControlActivity.getInstance()?.changeLeftBtnTounch(false)
        imgBack.isEnabled = false
        cbAutoWeiZhi.isEnabled = false
        cbAutoTiYa.isEnabled = false
        cbJianKang.isEnabled = false
        cbAutoWeiZhi.setTextColor(mContext.getColor(R.color.black1))
        cbAutoTiYa.setTextColor(mContext.getColor(R.color.black1))

        cbJianKang.setTextColor(mContext.getColor(R.color.black1))
        when(DataAnalysisHelper.deviceState.iNowAutoProgress) {
            2 -> {
                cbAutoWeiZhi.setTextColor(mContext.getColor(R.color.colorWhite))
            }
            3 -> {
                cbAutoWeiZhi.setTextColor(mContext.getColor(R.color.colorWhite))
                cbAutoTiYa.setTextColor(mContext.getColor(R.color.colorWhite))
                cbAutoWeiZhi.isEnabled = true
            }
            4 -> {
                cbAutoWeiZhi.setTextColor(mContext.getColor(R.color.colorWhite))
                cbAutoTiYa.setTextColor(mContext.getColor(R.color.colorWhite))
                cbJianKang.setTextColor(mContext.getColor(R.color.colorWhite))
                cbAutoTiYa.isEnabled = true
                cbAutoWeiZhi.isEnabled = true
            }
            5 -> {
                imgBack.isEnabled = true
                MainControlActivity.getInstance()?.changeLeftBtnTounch(true)
                cbAutoTiYa.setTextColor(mContext.getColor(R.color.colorWhite))
                cbAutoWeiZhi.setTextColor(mContext.getColor(R.color.colorWhite))
                cbJianKang.setTextColor(mContext.getColor(R.color.colorWhite))
                cbAutoTiYa.isEnabled = true
                cbAutoWeiZhi.isEnabled = true
                cbJianKang.isEnabled = true

            }
        }

    }



    /**
     * 根据当前气压值，改变view颜色
     */
    private fun updateSeatView() {
        // 传感气压11个
        val pressList = DataAnalysisHelper.deviceState.sensePressValueListl
        // 这里实际只用到了8个气压值
        for (iNumber in 0 until viewList.size) {
            val iTag = viewList[iNumber].tag.toString().toInt()
            var drawable = drawableList[iNumber]
            val iValue = pressList[iTag].toInt()
            // 通道序号
            var iChannelNumber = 0
            if (iTag < 3) {
                iChannelNumber = iTag+1
            }
            else {
                iChannelNumber = iTag+6
            }
            val iPressV = BaseVolume.getPressByValue(iValue,iChannelNumber)
            viewList[iNumber].text = ""
            viewList[iNumber].text = iPressV.toString()
            // 根据气压值，改变填充色
            val colorValue = BaseVolume.getColorByPressValue(iPressV,iChannelNumber)
            drawable.setColor(colorValue)
        }
    }

    override fun onClick(p0: View?) {
        when(p0?.id) {
            R.id.btnNan ->{
                if (btnNan.tag as Boolean) {
                    return
                }
                btnNan.tag = true
                btnNv.tag = false
                DataAnalysisHelper.deviceState.m_gender = true
                btnNan.setTextColor(getColor(R.color.colorWhite))
                btnNv.setTextColor(getColor(R.color.black1))
//                saveBooleanBySharedPreferences(SEX_MAN,true)
            }
            R.id.btnNv ->{
                if (btnNv.tag as Boolean) {
                    return
                }
                btnNan.tag = false
                btnNv.tag = true
                DataAnalysisHelper.deviceState.m_gender = false
                btnNan.setTextColor(getColor(R.color.black1))
                btnNv.setTextColor(getColor(R.color.colorWhite))
//                saveBooleanBySharedPreferences(SEX_MAN,false)
            }
            R.id.btnDongF ->{
                if (btnDongF.tag as Boolean) {
                    return
                }
                btnDongF.tag = true
                btnXiF.tag = false
                DataAnalysisHelper.deviceState.m_national = true
                btnDongF.setTextColor(getColor(R.color.colorWhite))
                btnXiF.setTextColor(getColor(R.color.black1))
                saveBooleanBySharedPreferences(COUNTRY_CN,true)
            }
            R.id.btnXiF ->{
                if (btnXiF.tag as Boolean) {
                    return
                }
                btnDongF.tag = false
                btnXiF.tag = true
                DataAnalysisHelper.deviceState.m_national = false
                btnDongF.setTextColor(getColor(R.color.black1))
                btnXiF.setTextColor(getColor(R.color.colorWhite))
                saveBooleanBySharedPreferences(COUNTRY_CN,false)
            }
        }

        onAutoSetPressByStatus()

    }

    /****
     * 广播监听
     */
    private val myNetReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            // 数据回调
            if (action == BaseVolume.BROADCAST_RESULT_DATA_INFO) {
                val strType = intent.getStringExtra(BaseVolume.BROADCAST_TYPE)
                val deviceWorkInfo = intent.getSerializableExtra(BaseVolume.BROADCAST_MSG) as DeviceWorkInfo
                // 通道状态
                if (strType == BaseVolume.COMMAND_TYPE_CHANNEL_STATUS) {
                    // 自动模式控制B面所有气袋气压
                    if (DataAnalysisHelper.deviceState.seatStatus == SeatStatus.press_automatic.iValue && SocketThreadManager.isCheckChannelState) {
                        // 这是按摩结束后，自动调整所有气压时判断所有气袋状态
                        if (SocketThreadManager.isStopMassageAutoCtrPress) {
                            var isAllNormal = true
                            // A面所有气袋 abcdefgh
                            for (iState in DataAnalysisHelper.deviceState.sensePressStatusList) {
                                // 正在充气，说明还没完成
                                if (iState == DeviceWorkInfo.STATUS_SETTING)
                                    return
                                if (iState == DeviceWorkInfo.STATUS_SETTED)
                                    isAllNormal = false
                            }
                            for (iNumber in 5 .. 7) {
                                val iState = DataAnalysisHelper.deviceState.controlPressStatusList[iNumber]
                                // 正在充气，说明还没完成
                                if (iState == DeviceWorkInfo.STATUS_SETTING)
                                    return
                                if (iState == DeviceWorkInfo.STATUS_SETTED)
                                    isAllNormal = false
                            }
                            // 全部恢复到Normal
                            if (!isAllNormal) {
                                SocketThreadManager.sharedInstance(mContext).StartChangeModelByCan(BaseVolume.COMMAND_CAN_MODEL_NORMAL_A_B)
                            }
                            // 已经全部恢复到Normal，则将座椅切到恢复成功状态
                            else {
                                // 恢复Normal
                                SocketThreadManager.sharedInstance(mContext).startTimeOut(false)
                            }
                        }
                        // 体压自适应的调压状态
                        else {
                            var isBNormal = true
                            // B面所有气袋 12345678
                            for (iState in DataAnalysisHelper.deviceState.controlPressStatusList) {
                                // 正在充气，说明还没完成
                                if (iState == DeviceWorkInfo.STATUS_SETTING)
                                    return
                                if (iState == DeviceWorkInfo.STATUS_SETTED)
                                    isBNormal = false
                            }

                            // 全部恢复到Normal
                            if (!isBNormal) {
                                SocketThreadManager.sharedInstance(mContext).StartChangeModelByCan(BaseVolume.COMMAND_CAN_MODEL_NORMAL_A_B)
                            }
                            // 已经全部恢复到Normal，则将座椅切到恢复成功状态
                            else {
                                loadingDialog.dismiss()
                                SocketThreadManager.sharedInstance(mContext).startTimeOut(false)
                                if (DataAnalysisHelper.deviceState.iNowAutoProgress == 3) {
                                    // 气压调节完成，开始按摩调节
                                    onAutoSetMassageByStatus()
                                }
                            }
                        }

                    }
                }
                // 气压值
                else if (strType == BaseVolume.COMMAND_TYPE_PRESS) {
                    updateSeatView()
                }
            }
            // 控制回调
            else if (action == BaseVolume.BROADCAST_CTR_CALLBACK) {

            }
            // 自动调整座椅气压和位置
            else if (action == BaseVolume.BROADCAST_AUTO_MODEL) {
                // 当前自动模式的步骤没执行过，则开始执行
                if (DataAnalysisHelper.deviceState.iNowAutoProgress <= 1) {
                    // 先判断位置自适应
                    onAutoSetLocationByStatus()
                }
                else {
                    // 更新当前进度
                    checkNowAutoProgress()
                }

            }
            // 停止位置调节，倒计时
            else if (action == BaseVolume.BROADCAST_STOP_LOCATION_TIMEOUT) {
                locationTimeOut(false)
            }
            // 实时判断人体数据
            else if (action == BaseVolume.BROADCAST_PERSON_INFO) {
                if (cbJianKang.isChecked && DataAnalysisHelper.deviceState.iNowAutoProgress == 5) {
                    runMassageInfo()
                }
            }
        }
    }

    var timerLocation:Timer? = null
    /**
     * 开启/关闭计时器
     */
    fun locationTimeOut(isRun : Boolean) {
        if (isRun) {
            timerLocation?.cancel()
            timerLocation = null
            timerLocation = Timer()
            timerLocation?.schedule(object : TimerTask() {
                override fun run() {
                    Log.e("AutoMaticActivity", "10秒时间到！位置调节结束，开始调节气压")
                    mHandler.post {
                        onAutoSetPressByStatus()
                    }
                }
            }, (10 * 1000))
        }
        else {
            timer?.cancel()
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(myNetReceiver)
        locationTimeOut(false)
    }




}