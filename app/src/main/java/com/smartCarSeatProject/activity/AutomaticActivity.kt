package com.smartCarSeatProject.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.View
import com.smartCarSeatProject.R
import com.smartCarSeatProject.data.*
import com.smartCarSeatProject.tcpInfo.SocketThreadManager
import kotlinx.android.synthetic.main.layout_automatic.*
import android.graphics.drawable.GradientDrawable
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.RadioGroup
import android.widget.TextView
import com.smartCarSeatProject.BuildConfig
import kotlinx.android.synthetic.main.layout_auto_seat.view.*


class AutomaticActivity: BaseActivity(), View.OnClickListener {


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

    }

    fun initUI() {
        imgBack.setOnClickListener {
            MainControlActivity.getInstance()?.finish()
        }

        cbAutoTiYa.setOnCheckedChangeListener(onChangeListener)

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
            onAutoSetPressByStatus()
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
        val isMan = getBooleanBySharedPreferences(SEX_MAN)
        val isCN = getBooleanBySharedPreferences(COUNTRY_CN)
        btnNan.tag = isMan
        btnNv.tag = !isMan
        btnDongF.tag = isCN
        btnXiF.tag = !isCN

        btnNan.setTextColor(getColor(R.color.black1))
        btnNv.setTextColor(getColor(R.color.black1))
        btnDongF.setTextColor(getColor(R.color.black1))
        btnXiF.setTextColor(getColor(R.color.black1))
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
                btnNan.setTextColor(getColor(R.color.colorWhite))
                btnNv.setTextColor(getColor(R.color.black1))
                saveBooleanBySharedPreferences(SEX_MAN,true)
            }
            R.id.btnNv ->{
                if (btnNv.tag as Boolean) {
                    return
                }
                btnNan.tag = false
                btnNv.tag = true
                btnNan.setTextColor(getColor(R.color.black1))
                btnNv.setTextColor(getColor(R.color.colorWhite))
                saveBooleanBySharedPreferences(SEX_MAN,false)
            }
            R.id.btnDongF ->{
                if (btnDongF.tag as Boolean) {
                    return
                }
                btnDongF.tag = true
                btnXiF.tag = false
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
                btnDongF.setTextColor(getColor(R.color.black1))
                btnXiF.setTextColor(getColor(R.color.colorWhite))
                saveBooleanBySharedPreferences(COUNTRY_CN,false)
            }
        }

        onAutoSetPressByStatus()
//        val strSendData = CreateCtrDataHelper.getCtrPeopleInfo(isNan,isCN)
//        SocketThreadManager.sharedInstance(this)?.StartSendData(strSendData)

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
                // 座椅状态
                if (strType.equals(BaseVolume.COMMAND_TYPE_SEAT_STATUS,true)) {
                    val iSeatStatus = deviceWorkInfo?.seatStatus
                }
                // 气压
                else if (strType.equals(BaseVolume.COMMAND_TYPE_PRESS,true)) {
                    updateSeatView()
                }
                // 男女,人种
                else if (strType.equals(BaseVolume.COMMAND_TYPE_SEX,true)) {
                    initData()
                }
            }
            // 控制回调
            else if (action == BaseVolume.BROADCAST_CTR_CALLBACK) {
                val strType = intent.getStringExtra(BaseVolume.BROADCAST_TYPE)
                val strMsg = intent.getStringExtra(BaseVolume.BROADCAST_MSG)
                if (strType.equals(BaseVolume.COMMAND_TYPE_SEX,true)) {
                    if (strMsg.equals(BaseVolume.COMMAND_ACK,true)) {
//                        DataAnalysisHelper.deviceState.nowSex = if(isNan) 1 else 2
//                        DataAnalysisHelper.deviceState.nowRace = if(isCN) 1 else 2
//                        initData()
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(myNetReceiver)

    }


}