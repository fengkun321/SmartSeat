package com.smartCarSeatProject.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import com.smartCarSeatProject.R
import com.smartCarSeatProject.dao.DevelopDataInfo
import com.smartCarSeatProject.dao.DevelopInfoDao
import com.smartCarSeatProject.dao.RemoteSQLInfo
import com.smartCarSeatProject.data.*
import com.smartCarSeatProject.isometric.Color
import com.smartCarSeatProject.tcpInfo.SocketThreadManager
import com.smartCarSeatProject.utl.DateUtil
import com.smartCarSeatProject.view.AreaAddWindowHint
import com.smartCarSeatProject.view.AreaAddWindowHint.PeriodListener
import com.smartCarSeatProject.view.SetValueAreaAddWindow
import com.smartCarSeatProject.view.SureSaveValueWindow
import kotlinx.android.synthetic.main.layout_auto_seat_transparent.view.*
import kotlinx.android.synthetic.main.layout_manual.*
import kotlinx.android.synthetic.main.layout_manual_heat.*
import kotlinx.android.synthetic.main.layout_manual_heat.view.*
import kotlinx.android.synthetic.main.layout_manual_location.view.*
import kotlinx.android.synthetic.main.layout_manual_prop.view.*
import kotlinx.android.synthetic.main.layout_manual_prop.view.view0
import kotlinx.android.synthetic.main.layout_manual_prop.view.view1
import kotlinx.android.synthetic.main.layout_manual_prop.view.view2

class ManualActivity: BaseActivity(), View.OnClickListener{

    var nowMemoryInfo = DevelopDataInfo()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_manual)

        initUI()
        initSeatInfo()
        initLocationInfo()
        initHeatInfo()
        reciverBand()
        updateSeatView()
    }

    fun initUI() {
        llSeekBar.visibility = View.VISIBLE
        btnJian.isEnabled = false
        btnJia.isEnabled = false
        btnJian.setTextColor(resources.getColor(R.color.black1))
        btnJia.setTextColor(resources.getColor(R.color.black1))

        imgBack.setOnClickListener(this)
        btnJian.setOnClickListener(this)
        btnJia.setOnClickListener(this)
        btnProp.setOnClickListener(this)
        btnLocation.setOnClickListener(this)
        btnHeat.setOnClickListener(this)
        btnJia.setOnClickListener(this)
        btnJia.setOnClickListener(this)
        btnSave.setOnClickListener(this)
        btnMemory.setOnClickListener(this)
        rlParent.setOnClickListener(this)

        for (i in 0..7) {
            val iValue = DataAnalysisHelper.deviceState.controlPressValueList[i]
        }


    }

    /** 初始化支撑调节 */
    // 当前选中的序号
    var iNowSelectNumber = -1
    // 当前页面 1：支撑 2：位置 3：通风加热按摩
    var iNowShowPageNumber = 1
    var iNowPressValue = -1
    var ProgressValueMin = 255
    var ProgressValueMax = 3600
    // 减按钮集合
    var dimBtnArray = arrayListOf<TextView>()
    // 加按钮集合
    var addBtnArray = arrayListOf<TextView>()
    // 操作视图-父类集合
    var rlViewArray = arrayListOf<View>()
    // 样式的集合
    var drawableList = arrayListOf<GradientDrawable>()
    // 控制视图-显示集合
    var viewList = arrayListOf<TextView>()
    // 滑竿的集合
    var seekBarList = arrayListOf<SeekBar>()
    fun initSeatInfo() {
        initShape(manualSeat.view0,false,false)
        initShape(manualSeat.view1,false,false)
        initShape(manualSeat.view2,false,false)
        initShape(manualSeat.view3,false,false)
        initShape(manualSeat.view4,false,false)
        initShape(manualSeat.view5,false,false)
        initShape(manualSeat.view6,false,false)
        initShape(manualSeat.view7,false,false)
        initShape(manualSeat.view3Left,false,false)
        initShape(manualSeat.view4Left,false,false)

        dimBtnArray.add(manualSeat.tvL0);dimBtnArray.add(manualSeat.tvL1);dimBtnArray.add(manualSeat.tvL2);dimBtnArray.add(manualSeat.tvL3)
        dimBtnArray.add(manualSeat.tvL4);dimBtnArray.add(manualSeat.tvL5);dimBtnArray.add(manualSeat.tvL6);dimBtnArray.add(manualSeat.tvL7)

        addBtnArray.add(manualSeat.tvR0);addBtnArray.add(manualSeat.tvR1);addBtnArray.add(manualSeat.tvR2);addBtnArray.add(manualSeat.tvR3)
        addBtnArray.add(manualSeat.tvR4);addBtnArray.add(manualSeat.tvR5);addBtnArray.add(manualSeat.tvR6);addBtnArray.add(manualSeat.tvR7)

        rlViewArray.add(manualSeat.rl0);rlViewArray.add(manualSeat.rl1);rlViewArray.add(manualSeat.rl2);rlViewArray.add(manualSeat.rl3);
        rlViewArray.add(manualSeat.rl4);rlViewArray.add(manualSeat.rl5);rlViewArray.add(manualSeat.rl6or7);rlViewArray.add(manualSeat.rl6or7);

        seekBarList.add(manualSeat.seekBar0);seekBarList.add(manualSeat.seekBar1);seekBarList.add(manualSeat.seekBar2);seekBarList.add(manualSeat.seekBar3);
        seekBarList.add(manualSeat.seekBar4);seekBarList.add(manualSeat.seekBar5);seekBarList.add(manualSeat.seekBar6);seekBarList.add(manualSeat.seekBar7);

        rlViewArray.forEach {
            if (it.tag.toString().toInt() != 6677) {
                it.setOnClickListener {changeSelectBtn(it.tag.toString().toInt())}
            }
        }

        dimBtnArray.forEach {
            it.setOnClickListener(onClickDimListener)
        }

        addBtnArray.forEach {
            it.setOnClickListener (onClickAddListener)
        }

        seekBarList.forEach {
            it.max = ProgressValueMax - ProgressValueMin
            it.setOnSeekBarChangeListener(seekBarChangeListener)
        }

    }




    /** 初始化位置调节 */
    var iNowSelectLocation = -1;
    var locationList = arrayListOf<TextView>()
    fun initLocationInfo() {
        locationList.add(manualLocation.tvLoc1);locationList.add(manualLocation.tvLoc2);locationList.add(manualLocation.tvLoc3)
        locationList.add(manualLocation.tvLoc4);locationList.add(manualLocation.tvLoc5);locationList.add(manualLocation.tvLoc6)
        locationList.forEach {
            it.setOnClickListener(onClickLoactionListener)
        }
    }
    /** 初始化通风调节 */
    // 传感样式的集合
    var drawableSenseList = arrayListOf<GradientDrawable>()
    // 传感视图的集合
    var viewSenseList = arrayListOf<TextView>()
    fun initHeatInfo() {

        initShapeBySense(manualHeat.autoSeat.view0)
        initShapeBySense(manualHeat.autoSeat.view1)
        initShapeBySense(manualHeat.autoSeat.view2)
        initShapeBySense(manualHeat.autoSeat.viewL3)
        initShapeBySense(manualHeat.autoSeat.viewR3)
        initShapeBySense(manualHeat.autoSeat.viewL4)
        initShapeBySense(manualHeat.autoSeat.viewR4)
        initShapeBySense(manualHeat.autoSeat.viewL5)
        initShapeBySense(manualHeat.autoSeat.viewR5)
        initShapeBySense(manualHeat.autoSeat.viewL6)
        initShapeBySense(manualHeat.autoSeat.viewR6)
        initShapeBySense(manualHeat.autoSeat.viewL7)
        initShapeBySense(manualHeat.autoSeat.viewR7)
        initShapeBySense(manualHeat.autoSeat.viewL8)
        initShapeBySense(manualHeat.autoSeat.viewR8)
        initShapeBySense(manualHeat.autoSeat.viewL9)
        initShapeBySense(manualHeat.autoSeat.viewR9)
        initShapeBySense(manualHeat.autoSeat.viewL10)
        initShapeBySense(manualHeat.autoSeat.viewR10)

        manualHeat.imgMoA1.setOnClickListener(onClickAnMoListenerA)
        manualHeat.imgMoA2.setOnClickListener(onClickAnMoListenerA)
        manualHeat.imgMoA3.setOnClickListener(onClickAnMoListenerA)

        updateSenseSeatView()

    }


    /***
     * 初始化样式
     * 是否是小view
     * 如果是小view,则有左右之分
     */
    fun initShape(view: TextView,isSmall:Boolean,isRight:Boolean) {
        // shape
        var drawable = GradientDrawable()
        // 四个角度
        drawable.cornerRadius = 10f
        // 小view,分左右
        if (isSmall) {
            // 左侧圆角
            val fl = floatArrayOf(10f,10f,0f,0f,0f,0f,10f,10f)
            drawable.cornerRadii = fl
            // 右侧圆角
            if (isRight) {
                val f2 = floatArrayOf(0f,0f,10f,10f,10f,10f,0f,0f)
                drawable.cornerRadii = f2
            }
        }
        // 边框：宽，颜色
        drawable.setStroke(0, resources.getColor(R.color.colorWhite))
        // 填充色
        drawable.setColor(resources.getColor(R.color.colorTransparency))
        view.setBackgroundDrawable(drawable)
        view.setOnClickListener { changeSelectBtn(it.tag.toString().toInt()) }

        drawableList.add(drawable)
        viewList.add(view)

    }

    /** 减号，监听事件 */
    val onClickDimListener = View.OnClickListener {

        iNowPressValue -= 5
        if (iNowPressValue < ProgressValueMin)
            iNowPressValue = ProgressValueMin

        val iTag = it.tag.toString().toInt()

        seekBarList[iTag].progress = (iNowPressValue - ProgressValueMin)

        // 保存控制缓存值
        MainControlActivity.getInstance()?.setValueBufferByChannel?.put(iNowSelectNumber+1,iNowPressValue.toString())
        controlPressValueByTag(iTag,iNowPressValue)
    }

    /** 加号，监听事件 */
    val onClickAddListener = View.OnClickListener {

        iNowPressValue += 5
        if (iNowPressValue > ProgressValueMax)
            iNowPressValue = ProgressValueMax

        val iTag = it.tag.toString().toInt()

        seekBarList[iTag].progress = (iNowPressValue - ProgressValueMin)

        // 保存控制缓存值
        MainControlActivity.getInstance()?.setValueBufferByChannel?.put(iNowSelectNumber+1,iNowPressValue.toString())

        controlPressValueByTag(iTag,iNowPressValue)

    }

    /**
     * 控制气压
     * 先转换调压模式 A面normal，B面adjust
     * 再发送数据
     */
    private fun controlPressValueByTag(iTag : Int,iPressValue:Int) {
        // 只调整B面的，所以将A面设为normal，B面设为adjust
        SocketThreadManager.sharedInstance(mContext)?.StartChangeModelByCan(CreateCtrDataHelper.getCtrModelAB(BaseVolume.COMMAND_CAN_MODEL_NORMAL,BaseVolume.COMMAND_CAN_MODEL_ADJUST))
        val strSendData = CreateCtrDataHelper.getCtrPressVaslueByNumber(iTag,iPressValue)
        SocketThreadManager.sharedInstance(mContext).StartSendDataByCan(strSendData)
    }

    /** 位置调节 */
    val onClickLoactionListener = View.OnClickListener { it0 ->
        val isRun = it0.tag.toString().toBoolean()
        locationList.forEach {
            it.tag = false
            it.setBackgroundColor(getColor(R.color.colorBlack))
        }
        // 取消位置调节
        if (isRun) {
            DataAnalysisHelper.deviceState.l_location = "0"
            // 默认位置
            SocketThreadManager.sharedInstance(mContext).StartSendDataByCan2(BaseVolume.COMMAND_CAN_LOCATION_DEFAULT)
            SocketThreadManager.sharedInstance(mContext).StartSendDataByCan2(BaseVolume.COMMAND_CAN_LOCATION_0)
            return@OnClickListener
        }

        it0.tag = !isRun
        it0.setBackgroundColor(getColor(R.color.colorLightBlue))
        when(it0.id) {
            R.id.tvLoc1 -> {
                DataAnalysisHelper.deviceState.l_location = "1"
                SocketThreadManager.sharedInstance(mContext).StartSendDataByCan2(BaseVolume.COMMAND_CAN_LOCATION_1)
            }
            R.id.tvLoc2 ->{
                DataAnalysisHelper.deviceState.l_location = "2"
                SocketThreadManager.sharedInstance(mContext).StartSendDataByCan2(BaseVolume.COMMAND_CAN_LOCATION_2)
            }
            R.id.tvLoc3 -> {
                DataAnalysisHelper.deviceState.l_location = "3"
                SocketThreadManager.sharedInstance(mContext).StartSendDataByCan2(BaseVolume.COMMAND_CAN_LOCATION_3)
            }
            R.id.tvLoc4 -> {
                DataAnalysisHelper.deviceState.l_location = "4"
                SocketThreadManager.sharedInstance(mContext).StartSendDataByCan2(BaseVolume.COMMAND_CAN_LOCATION_4)
            }
            R.id.tvLoc5 -> {
                DataAnalysisHelper.deviceState.l_location = "5"
                SocketThreadManager.sharedInstance(mContext).StartSendDataByCan2(BaseVolume.COMMAND_CAN_LOCATION_5)
            }
            R.id.tvLoc6 -> {
                DataAnalysisHelper.deviceState.l_location = "6"
                SocketThreadManager.sharedInstance(mContext).StartSendDataByCan2(BaseVolume.COMMAND_CAN_LOCATION_6)
            }

        }
        // 每次发完位置后，要再发一条清零指令才会动作
        SocketThreadManager.sharedInstance(mContext).StartSendDataByCan2(BaseVolume.COMMAND_CAN_LOCATION_0)


    }

    val onClickAnMoListenerA = View.OnClickListener {
        val isRun = it.tag.toString().toBoolean()
        imgMoA1.tag = false
        imgMoA2.tag = false
        imgMoA3.tag = false
        imgMoA1.setImageResource(R.drawable.img_anmo_1_false)
        imgMoA2.setImageResource(R.drawable.img_anmo_2_false)
        imgMoA3.setImageResource(R.drawable.img_anmo_3_false)
        if (!isRun) {
            it.tag = true
            when(it.id) {
                R.id.imgMoA1 -> imgMoA1.setImageResource(R.drawable.img_anmo_1)
                R.id.imgMoA2 -> imgMoA2.setImageResource(R.drawable.img_anmo_2)
                R.id.imgMoA3 -> imgMoA3.setImageResource(R.drawable.img_anmo_3)
            }
        }
        // 如果按摩启动，则左侧导航栏要变灰，不能操作，true:可点，false:不可点
        MainControlActivity.getInstance()?.changeLeftBtnTounch(isRun)
        checkMassageState()
    }



    /**
     * 选择按摩方式
     * A / B 面
     * 按摩程度
     * 是否按摩
     */
    private fun checkMassageState() {
        var iNumberA = BaseVolume.COMMAND_CAN_MODEL_MASG_OFF
        var iNumberB = BaseVolume.COMMAND_CAN_MODEL_NORMAL
        if (imgMoA1.tag.toString().toBoolean()) {
            iNumberA = BaseVolume.COMMAND_CAN_MODEL_MASG_1
        }
        if (imgMoA2.tag.toString().toBoolean()) {
            iNumberA = BaseVolume.COMMAND_CAN_MODEL_MASG_2
        }
        if (imgMoA3.tag.toString().toBoolean()) {
            iNumberA = BaseVolume.COMMAND_CAN_MODEL_MASG_3
        }

        val strSendData = CreateCtrDataHelper.getCtrModelAB(iNumberA,iNumberB)
        Log.e("ManualActivity", "当前按摩指令：$strSendData")
        SocketThreadManager.sharedInstance(mContext).StartChangeModelByCan(strSendData)

    }

    /**
     * 初始化样式
     * 是否是小view
     * 如果是小view,则有左右之分
     */
    fun initShapeBySense(view: TextView) {
        // shape
        var drawable = GradientDrawable()
        // 四个角度
        drawable.cornerRadius = 10f
        // 边框：宽，颜色
        drawable.setStroke(0, resources.getColor(R.color.colorWhite))
        // 填充色
        drawable.setColor(resources.getColor(R.color.colorTransparency))
        view.setBackgroundDrawable(drawable)

        drawableSenseList.add(drawable)
        viewSenseList.add(view)

    }

    /**
     * 根据当前气压值，改变view颜色
     */
    private fun updateSenseSeatView() {
        // 传感气压11个
        val pressList = DataAnalysisHelper.deviceState.sensePressValueListl
        // 这里实际只用到了8个气压值
        for (iNumber in 0 until viewSenseList.size) {
            val iTag = viewSenseList[iNumber].tag.toString().toInt()
            var drawable = drawableSenseList[iNumber]
            val iValue = pressList[iTag].toInt()
            // 通道序号
            var iChannelNumber = 0
            if (iTag < 3) {
                iChannelNumber = iTag+1
            }
            else {
                iChannelNumber = iTag+6
            }
            val iPressV = BaseVolume.getPressByValueB(iValue,iChannelNumber)
            viewSenseList[iNumber].text = ""
            viewSenseList[iNumber].text = iPressV.toString()
            // 根据气压值，改变填充色
            val colorValue = BaseVolume.getColorByPressValue(iPressV,iChannelNumber)
            drawable.setStroke(0, resources.getColor(R.color.colorWhite))
            drawable.setColor(colorValue)

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

    var seekBarChangeListener = object :SeekBar.OnSeekBarChangeListener{
        override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
            var iNowValue = p0?.progress!! +ProgressValueMin
            if (iNowSelectNumber != -1) {
                val iPressV = BaseVolume.getPressByValueB(iNowValue,(iNowSelectNumber+1))
                tvSeekBarValue.text = iPressV.toString()
                viewList[p0.tag.toString().toInt()].text = "$iNowValue"
                if (p0.tag.toString().toInt() == 3)
                    viewList[8].text = "$iNowValue"
                else if (p0.tag.toString().toInt() == 4)
                    viewList[9].text = "$iNowValue"
            }
        }

        override fun onStartTrackingTouch(p0: SeekBar?) {
        }

        override fun onStopTrackingTouch(p0: SeekBar?) {
            var iNowValue = p0?.progress!! +ProgressValueMin

            if (iNowSelectNumber != -1) {
//                val iPressV = BaseVolume.getPressByValueB(iNowValue,(iNowSelectNumber+1))
//                viewList[iNowSelectNumber].text = iPressV.toString()

                // 通道1，最大值3580
                if (iNowSelectNumber == 0) {
                    if (iNowValue > 3580) {
                        iNowValue = 3580
                    }
                }
            }
            iNowPressValue = iNowValue
            // 保存控制缓存值
            MainControlActivity.getInstance()?.setValueBufferByChannel?.put(iNowSelectNumber+1,iNowPressValue.toString())
            controlPressValueByTag(p0.tag.toString().toInt(),iNowPressValue)
        }

    }

    override fun onClick(p0: View?) {
        when(p0?.id) {
            R.id.imgBack -> {
                MainControlActivity.getInstance()?.finish()
            }
            R.id.btnSave -> {
                saveNowValue()
            }
            R.id.btnMemory -> {
                val intent = Intent()
                intent.setClass(this,SetMemoryActivity::class.java)
                MainControlActivity.getInstance()?.GotoNewActivity("ManualActivity","SetMemoryActivity",intent)
            }
            R.id.rlParent -> {
                changeSelectBtn(p0.tag.toString().toInt())
            }
            R.id.btnProp -> {
                if (iNowShowPageNumber != 1) {
                    // 如果按摩启动，则左侧导航栏要变灰，不能操作，true:可点，false:不可点
                    MainControlActivity.getInstance()?.changeLeftBtnTounch(true)
                    // 当前如果正在按摩，则需要把A面泄气
                    if (imgMoA1.tag.toString().toBoolean() || imgMoA2.tag.toString().toBoolean() || imgMoA3.tag.toString().toBoolean()) {
                        imgMoA1.tag = false
                        imgMoA2.tag = false
                        imgMoA3.tag = false
                        imgMoA1.setImageResource(R.drawable.img_anmo_1_false)
                        imgMoA2.setImageResource(R.drawable.img_anmo_2_false)
                        imgMoA3.setImageResource(R.drawable.img_anmo_3_false)
                        releaseAPress()
                    }
                    switchCtrView(1)
                }

            }
            R.id.btnLocation -> {
                if (iNowShowPageNumber != 2) {
                    // 如果按摩启动，则左侧导航栏要变灰，不能操作，true:可点，false:不可点
                    MainControlActivity.getInstance()?.changeLeftBtnTounch(true)
                    // 当前如果正在按摩，则需要把A面泄气
                    if (imgMoA1.tag.toString().toBoolean() || imgMoA2.tag.toString().toBoolean() || imgMoA3.tag.toString().toBoolean()) {
                        imgMoA1.tag = false
                        imgMoA2.tag = false
                        imgMoA3.tag = false
                        imgMoA1.setImageResource(R.drawable.img_anmo_1_false)
                        imgMoA2.setImageResource(R.drawable.img_anmo_2_false)
                        imgMoA3.setImageResource(R.drawable.img_anmo_3_false)
                        releaseAPress()
                    }
                    switchCtrView(2)
                }
            }
            R.id.btnHeat -> {
                switchCtrView(3)
            }

        }
    }

    fun switchCtrView(iNumber: Int) {

        iNowShowPageNumber = iNumber
        manualSeat.visibility = View.GONE
        manualLocation.visibility = View.GONE
        manualHeat.visibility = View.GONE

        btnProp.setBackgroundColor(getColor(R.color.colorBlack))
        btnLocation.setBackgroundColor(getColor(R.color.colorBlack))
        btnHeat.setBackgroundColor(getColor(R.color.colorBlack))

        when(iNumber){
            1 -> {
                manualSeat.visibility = View.VISIBLE
                btnProp.setBackgroundColor(getColor(R.color.colorLightBlue))
            }
            2 -> {
                manualLocation.visibility = View.VISIBLE
                btnLocation.setBackgroundColor(getColor(R.color.colorLightBlue))
            }
            3 -> {
                manualHeat.visibility = View.VISIBLE
                btnHeat.setBackgroundColor(getColor(R.color.colorLightBlue))
            }
        }
    }

    /**
     * 选中某个view
     */
    fun changeSelectBtn(iTag : Int) {
        if (iNowSelectNumber != iTag) {
            iNowSelectNumber = iTag
            viewList.forEach {
                it.text = ""
            }
            rlViewArray.forEach {
                it.setBackgroundResource(R.drawable.channel_border_no)
            }
            dimBtnArray.forEach {
                it.isEnabled = false
            }
            addBtnArray.forEach {
                it.isEnabled = false
            }
            seekBarList.forEach {
                it.visibility = View.GONE
            }

            if (iTag == -1) {
                return
            }


            // 控制气压8个
            val pressList = DataAnalysisHelper.deviceState.controlPressValueList
            // 这里实际只用到了8个气压值
            for (iNumber in 0 until viewList.size) {
                val iTag = viewList[iNumber].tag.toString().toInt()
                var drawable = drawableList[iNumber]
                val iValue = pressList[iTag].toInt()
                val iPressV = BaseVolume.getPressByValueB(iValue,(iTag+1))
//            viewList[iNumber].text = iPressV.toString()
                // 根据气压值，改变填充色
                drawable.setStroke(0, resources.getColor(R.color.colorWhite))
                val colorValue = BaseVolume.getColorByPressValue(iPressV,(iTag+1))
                drawable.setColor(colorValue)

            }

            rlViewArray[iTag].setBackgroundResource(R.drawable.channel_border_yes)
            dimBtnArray[iTag].isEnabled = true
            addBtnArray[iTag].isEnabled = true

            // 当前通道的值
            val iNowValue:Int = DataAnalysisHelper.deviceState.controlPressValueList[iNowSelectNumber].toInt()
            iNowPressValue = iNowValue
            viewList[iTag].text = iNowValue.toString()
            if (iTag == 3) {
                viewList[8].text = iNowValue.toString()
                drawableList[8].setStroke(3, resources.getColor(R.color.colorAccent))

            }
            else if (iTag == 4){
                viewList[9].text = iNowValue.toString()
                drawableList[9].setStroke(3, resources.getColor(R.color.colorAccent))
            }

            drawableList[iTag].setStroke(3, resources.getColor(R.color.colorAccent))

            seekBarList[iTag].visibility = View.VISIBLE
            seekBarList[iTag].progress = (iNowPressValue - ProgressValueMin)
            if (iTag == 6) {
                seekBarList[6].visibility = View.VISIBLE
                dimBtnArray[6].visibility = View.VISIBLE
                addBtnArray[6].visibility = View.VISIBLE
                seekBarList[7].visibility = View.GONE
                dimBtnArray[7].visibility = View.GONE
                addBtnArray[7].visibility = View.GONE
            }
            else if (iTag == 7) {
                seekBarList[7].visibility = View.VISIBLE
                dimBtnArray[7].visibility = View.VISIBLE
                addBtnArray[7].visibility = View.VISIBLE
                seekBarList[6].visibility = View.GONE
                dimBtnArray[6].visibility = View.GONE
                addBtnArray[6].visibility = View.GONE
            }
        }


    }

    /**
     * 根据当前气压值，改变view颜色
     */
    private fun updateSeatView() {
        // 控制气压8个
        val pressList = DataAnalysisHelper.deviceState.controlPressValueList
        // 这里实际只用到了8个气压值
        for (iNumber in 0 until viewList.size) {
            val iTag = viewList[iNumber].tag.toString().toInt()
            // 如果当前在调节，那被调节的按钮不可动，不然就乱套了
            if (iTag == iNowSelectNumber) {
                continue
            }
//            if (iTag == 3 && iNumber == 8) {
//                continue
//            }
//            if (iTag == 4 && iNumber == 9) {
//                continue
//            }


            var drawable = drawableList[iNumber]
            val iValue = pressList[iTag].toInt()
            val iPressV = BaseVolume.getPressByValueB(iValue,(iTag+1))
//            viewList[iNumber].text = iPressV.toString()
            // 根据气压值，改变填充色
            val colorValue = BaseVolume.getColorByPressValue(iPressV,(iTag+1))
            drawable.setStroke(0, resources.getColor(R.color.colorWhite))
            drawable.setColor(colorValue)

        }
    }

    /**
     * 保存
     */
    fun saveNowValue() {

        var setValueDialog = SureSaveValueWindow(this, R.style.Dialogstyle,
                "Please enter the name", object : SureSaveValueWindow.PeriodListener {
            override fun confirmListener(deviceWorkInfo : DeviceWorkInfo,strName: String) {

                val isMan = deviceWorkInfo.m_gender == 1
                val isCN = deviceWorkInfo.m_national

                nowMemoryInfo = DevelopDataInfo()
                nowMemoryInfo.initData()
                nowMemoryInfo.dataType = DevelopDataInfo.DATA_TYPE_USE

                nowMemoryInfo.p_init_cushion_A1 = DataAnalysisHelper.deviceState.init_cushion_valueList[0].toString()
                nowMemoryInfo.p_init_cushion_A2 = DataAnalysisHelper.deviceState.init_cushion_valueList[1].toString()
                nowMemoryInfo.p_init_cushion_B1 = DataAnalysisHelper.deviceState.init_cushion_valueList[2].toString()
                nowMemoryInfo.p_init_cushion_B2 = DataAnalysisHelper.deviceState.init_cushion_valueList[3].toString()
                nowMemoryInfo.p_init_cushion_C1 = DataAnalysisHelper.deviceState.init_cushion_valueList[4].toString()
                nowMemoryInfo.p_init_cushion_C2 = DataAnalysisHelper.deviceState.init_cushion_valueList[5].toString()

                nowMemoryInfo.p_recog_cushion_A1 =  DataAnalysisHelper.deviceState.recog_cushion_valueList[0].toString()
                nowMemoryInfo.p_recog_cushion_A2 =  DataAnalysisHelper.deviceState.recog_cushion_valueList[1].toString()
                nowMemoryInfo.p_recog_cushion_B1 =  DataAnalysisHelper.deviceState.recog_cushion_valueList[2].toString()
                nowMemoryInfo.p_recog_cushion_B2 =  DataAnalysisHelper.deviceState.recog_cushion_valueList[3].toString()
                nowMemoryInfo.p_recog_cushion_C1 =  DataAnalysisHelper.deviceState.recog_cushion_valueList[4].toString()
                nowMemoryInfo.p_recog_cushion_C2 =  DataAnalysisHelper.deviceState.recog_cushion_valueList[5].toString()

                // 名称
                nowMemoryInfo.strName = strName
                nowMemoryInfo.strPSInfo = strName
                // 人员-体重
                nowMemoryInfo.m_weight = "${deviceWorkInfo.nowWeight}"
                // 人员-身高
                nowMemoryInfo.m_height = "${deviceWorkInfo.nowHeight}"
                // 心率
                nowMemoryInfo.HeartRate = deviceWorkInfo.HeartRate
                // 呼吸率
                nowMemoryInfo.BreathRate = deviceWorkInfo.BreathRate
                // 情绪值
                nowMemoryInfo.E_Index = deviceWorkInfo.E_Index
                // 舒张压
                nowMemoryInfo.Dia_BP = deviceWorkInfo.Dia_BP
                // 收缩压
                nowMemoryInfo.Sys_BP = deviceWorkInfo.Sys_BP
                // 信噪比
                nowMemoryInfo.Snr = deviceWorkInfo.snr

                // 男女
                if (isMan)
                    nowMemoryInfo.m_gender = "M"
                else
                    nowMemoryInfo.m_gender = "F"
                // 国别
                if (isCN)
                    nowMemoryInfo.m_national = "CN"
                else
                    nowMemoryInfo.m_national = "EU"

                // 位置
                nowMemoryInfo.l_location = "区域"+deviceWorkInfo.l_location

                // 时间
                nowMemoryInfo.saveTime =  DateUtil.getNowDateTime()

                // 控制数据
                for (iNumber in 1..8) {
                    updatePressValueByNumber(iNumber,DataAnalysisHelper.deviceState.controlPressValueList[iNumber-1])
                }
                // 控制过的通道值
                MainControlActivity.getInstance()?.setValueBufferByChannel?.forEach {
                    val iNumber = it.key
                    val strValue = it.value
                    updatePressValueByNumber(iNumber,strValue)
                }

                // 传感数据A面
                for (iNumber in 1 .. 8) {
                    updateRecogPressValueByNumber(iNumber+8,(DataAnalysisHelper.deviceState.recog_back_A_valueList[iNumber-1]))
                }
                // 传感数据B面.
                for (iNumber in 1 .. 8) {
                    updateRecogPressValueByNumber(iNumber,(DataAnalysisHelper.deviceState.recog_back_B_valueList[iNumber-1]))
                }

                val memoryInfoDao = DevelopInfoDao(this@ManualActivity)
                val isHave = memoryInfoDao.isHaveByName(strName)
                // 该名称已存在，则提示用户
                if (isHave) {
                    memoryInfoDao.closeDb()
                    showHintDialog("System","The name already exists. Do not replace it？",strName)
                }
                // 直接添加
                else {
                    // 数据库操作
                    val isResult = memoryInfoDao.insertSingleData(nowMemoryInfo)
                    memoryInfoDao.closeDb()

                    Thread(Runnable {
                        val remoteSQLInfo = RemoteSQLInfo()
                        val strResult = remoteSQLInfo.insertDataByManualData(nowMemoryInfo)
                        mHandler.post {
                            if (strResult.equals(""))
                                ToastMsg("Save successful！")
                            else
                                ToastMsg(strResult)
                        }
                    }).start()
                }
            }
            override fun cancelListener() {

            }
        }, DataAnalysisHelper.deviceState)

        setValueDialog!!.setCancelable(false)
        setValueDialog!!.show()
    }

    // 根据序号更新值
    fun updatePressValueByNumber(iNumber : Int,strValue:String) {
        when(iNumber) {
            1 ->
                nowMemoryInfo.p_adjust_cushion_1 = strValue
            2 ->
                nowMemoryInfo.p_adjust_cushion_2 = strValue
            3 ->
                nowMemoryInfo.p_adjust_cushion_3 = strValue
            4 ->
                nowMemoryInfo.p_adjust_cushion_4 = strValue
            5 ->
                nowMemoryInfo.p_adjust_cushion_5 = strValue
            6 ->
                nowMemoryInfo.p_adjust_cushion_6 = strValue
            7 ->
                nowMemoryInfo.p_adjust_cushion_7 = strValue
            8 ->
                nowMemoryInfo.p_adjust_cushion_8 = strValue

        }
    }

    // 根据序号更新值R
    fun updateRecogPressValueByNumber(iNumber : Int,strValue:String) {
        when(iNumber) {
            1 ->
                nowMemoryInfo.p_recog_back_1 = strValue
            2 ->
                nowMemoryInfo.p_recog_back_2 = strValue
            3 ->
                nowMemoryInfo.p_recog_back_3 = strValue
            4 ->
                nowMemoryInfo.p_recog_back_4 = strValue
            5 ->
                nowMemoryInfo.p_recog_back_5 = strValue
            6 ->
                nowMemoryInfo.p_recog_cushion_6 = strValue
            7 ->
                nowMemoryInfo.p_recog_cushion_7 = strValue
            8 ->
                nowMemoryInfo.p_recog_cushion_8 = strValue
            9 ->
                nowMemoryInfo.p_recog_back_A = strValue
            10 ->
                nowMemoryInfo.p_recog_back_B = strValue
            11 ->
                nowMemoryInfo.p_recog_back_C = strValue
            12 ->
                nowMemoryInfo.p_recog_back_D = strValue
            13 ->
                nowMemoryInfo.p_recog_back_E = strValue
            14 ->
                nowMemoryInfo.p_recog_back_F = strValue
            15 ->
                nowMemoryInfo.p_recog_back_G = strValue
            16 ->
                nowMemoryInfo.p_recog_back_H = strValue
        }
    }

    /** 提示语 */
    fun showHintDialog(strTitle:String,strContent:String,strName:String) {

        val areaAddWindowHint = AreaAddWindowHint(this,R.style.Dialogstyle,strTitle,
                object : PeriodListener {
                    override fun refreshListener(string: String) {
                        val memoryInfoDao = DevelopInfoDao(this@ManualActivity)
                        // 数据库操作
                        val isResult = memoryInfoDao.updateDataByName(nowMemoryInfo)
                        memoryInfoDao.closeDb()

                        Thread(Runnable {
                            val remoteSQLInfo = RemoteSQLInfo()
                            val strResult = remoteSQLInfo.insertDataByManualData(nowMemoryInfo)
                            mHandler.post {
                                if (strResult.equals(""))
                                    ToastMsg("Save successful！")
                                else
                                    ToastMsg(strResult)
                            }
                        }).start()

                    }

                    override fun cancelListener() {
                    }
                },strContent)
        areaAddWindowHint.show()

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
                    // 手动模式
                    if (DataAnalysisHelper.deviceState.seatStatus == SeatStatus.press_manual.iValue && SocketThreadManager.isCheckChannelState) {
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
                        // 单独控制某个气袋后的状态判断
                        else {
                            // 判断当前控制的气袋状态
                            if (iNowSelectNumber == -1) {
                                return
                            }
                            val iCtrState = DataAnalysisHelper.deviceState.controlPressStatusList[iNowSelectNumber]
                            if (iCtrState == DeviceWorkInfo.STATUS_SETTING) {
                                return
                            }
                            else if (iCtrState == DeviceWorkInfo.STATUS_SETTED) {
                                // 恢复Normal
                                SocketThreadManager.sharedInstance(mContext).StartChangeModelByCan(BaseVolume.COMMAND_CAN_MODEL_NORMAL_A_B)
                            }
                            else if (iCtrState == DeviceWorkInfo.STATUS_NORMAL) {
                                SocketThreadManager.sharedInstance(mContext).startTimeOut(false)
                                // 控制结束，将所有高亮按钮灰掉
                                changeSelectBtn(-1)
                            }
                        }



                    }
                }
                // 气压值
                else if (strType == BaseVolume.COMMAND_TYPE_PRESS) {
                    // 当前是支撑调节界面，则更新控制气压
                    if (iNowShowPageNumber == 1) {
                        updateSeatView()
                    }
                    // 当前是通风加热按摩，则更新传感气压
                    else if (iNowShowPageNumber == 3) {
                        updateSenseSeatView()
                    }
                }
            }
            // 控制回调
            else if (action == BaseVolume.BROADCAST_CTR_CALLBACK) {
                val strType = intent.getStringExtra(BaseVolume.BROADCAST_TYPE)
                val strMsg = intent.getStringExtra(BaseVolume.BROADCAST_MSG)

            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(myNetReceiver)

    }

}