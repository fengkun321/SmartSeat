package com.smartCarSeatProject.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.SeekBar
import android.widget.TextView
import com.smartCarSeatProject.R
import com.smartCarSeatProject.dao.MemoryDataInfo
import com.smartCarSeatProject.dao.MemoryInfoDao
import com.smartCarSeatProject.data.*
import com.smartCarSeatProject.isometric.Color
import com.smartCarSeatProject.tcpInfo.SocketThreadManager
import com.smartCarSeatProject.view.AreaAddWindowHint
import com.smartCarSeatProject.view.AreaAddWindowHint.PeriodListener
import com.smartCarSeatProject.view.SetValueAreaAddWindow
import kotlinx.android.synthetic.main.layout_auto_seat_transparent.view.*
import kotlinx.android.synthetic.main.layout_manual.*
import kotlinx.android.synthetic.main.layout_manual_heat.*
import kotlinx.android.synthetic.main.layout_manual_heat.view.*
import kotlinx.android.synthetic.main.layout_manual_location.*
import kotlinx.android.synthetic.main.layout_manual_location.view.*
import kotlinx.android.synthetic.main.layout_manual_prop.view.*
import kotlinx.android.synthetic.main.layout_manual_prop.view.view0
import kotlinx.android.synthetic.main.layout_manual_prop.view.view1
import kotlinx.android.synthetic.main.layout_manual_prop.view.view2

class ManualActivity: BaseActivity(), View.OnClickListener{

    var setValueDialog : SetValueAreaAddWindow? = null
    var nowMemoryInfo = MemoryDataInfo()

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
        seekBar.max = ProgressValueMax - ProgressValueMin
        seekBar.setOnSeekBarChangeListener(seekBarChangeListener)

        seekBar.isEnabled = false
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
    var iNowPressValue = -1
    var ProgressValueMin = 255
    var ProgressValueMax = 3600
    // 减按钮集合
    var dimBtnArray = arrayListOf<TextView>()
    // 加按钮集合
    var addBtnArray = arrayListOf<TextView>()
    // 操作视图集合
    var rlViewArray = arrayListOf<RelativeLayout>()
    // 样式的集合
    var drawableList = arrayListOf<GradientDrawable>()
    // 视图的集合
    var viewList = arrayListOf<TextView>()
    fun initSeatInfo() {
        initShape(manualSeat.view0,false,false)
        initShape(manualSeat.view1,true,false)
        initShape(manualSeat.view2,true,true)
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

        rlViewArray.add(manualSeat.rl0);rlViewArray.add(manualSeat.rl1);rlViewArray.add(manualSeat.rl2);rlViewArray.add(manualSeat.rl3)
        rlViewArray.add(manualSeat.rl4);rlViewArray.add(manualSeat.rl5);rlViewArray.add(manualSeat.rl6);rlViewArray.add(manualSeat.rl7)

        rlViewArray.forEach {
            it.setOnClickListener {changeSelectBtn(it)}
        }

        dimBtnArray.forEach {
            it.setOnClickListener(onClickDimListener)
        }

        addBtnArray.forEach {
            it.setOnClickListener (onClickAddListener)
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
    // 视图的集合
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

        manualHeat.imgMo1.setOnClickListener(onClickAnMoListener)
        manualHeat.imgMo2.setOnClickListener(onClickAnMoListener)
        manualHeat.imgMo3.setOnClickListener(onClickAnMoListener)

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
        view.setOnClickListener { changeSelectBtn(it) }

        drawableList.add(drawable)
        viewList.add(view)

    }

    /** 减号，监听事件 */
    val onClickDimListener = View.OnClickListener {

        if (!isCanControl()) {
            return@OnClickListener
        }

        iNowPressValue -= 5
        if (iNowPressValue < ProgressValueMin)
            iNowPressValue = ProgressValueMin

        val iTag = it.tag.toString().toInt()

        viewList[iTag].text = iNowPressValue.toString()
        if (iTag == 3) {
            viewList[8].text = iNowPressValue.toString()
        }
        else if (iTag == 4){
            viewList[9].text = iNowPressValue.toString()
        }

        // 保存控制缓存值
        MainControlActivity.getInstance()?.setValueBufferByChannel?.put(iNowSelectNumber+1,iNowPressValue.toString())

        val strSendData = CreateCtrDataHelper.getCtrPressVaslueByNumber(iTag,iNowPressValue)
        SocketThreadManager.sharedInstance(this)?.StartSendDataNoTime(strSendData)

//        val strSendData = CreateCtrDataHelper.getCtrPressBy1((iNowSelectNumber+1).toString(),iNowPressValue)
//        SocketThreadManager.sharedInstance(this@ManualActivity)?.StartSendData(strSendData)


    }

    /** 加号，监听事件 */
    val onClickAddListener = View.OnClickListener {

        if (!isCanControl()) {
            return@OnClickListener
        }

        iNowPressValue += 5
        if (iNowPressValue > ProgressValueMax)
            iNowPressValue = ProgressValueMax

        val iTag = it.tag.toString().toInt()

        viewList[iTag].text = iNowPressValue.toString()
        if (iTag == 3) {
            viewList[8].text = iNowPressValue.toString()
        }
        else if (iTag == 4){
            viewList[9].text = iNowPressValue.toString()
        }

        // 保存控制缓存值
        MainControlActivity.getInstance()?.setValueBufferByChannel?.put(iNowSelectNumber+1,iNowPressValue.toString())

        val strSendData = CreateCtrDataHelper.getCtrPressVaslueByNumber(iTag,iNowPressValue)
        SocketThreadManager.sharedInstance(this)?.StartSendDataNoTime(strSendData)

    }


    /** 位置调节 */
    val onClickLoactionListener = View.OnClickListener {
        val iTag = it.tag.toString().toInt()
        if (iTag == iNowSelectLocation) {
            return@OnClickListener
        }
        locationList.forEach {
            it.setBackgroundColor(getColor(R.color.colorBlack))
        }
        locationList[iTag].setBackgroundColor(getColor(R.color.colorLightBlue))

    }

    val onClickAnMoListener = View.OnClickListener {
        if (it.tag.toString().toBoolean())
            return@OnClickListener

        imgMo1.setImageResource(R.drawable.img_anmo_1_false)
        imgMo2.setImageResource(R.drawable.img_anmo_2_false)
        imgMo3.setImageResource(R.drawable.img_anmo_3_false)
        imgMo1.tag = false
        imgMo2.tag = false
        imgMo3.tag = false

        when(it.id) {
            R.id.imgMo1 -> {
                imgMo1.tag = true
                imgMo1.setImageResource(R.drawable.img_anmo_1)
            }
            R.id.imgMo2 -> {
                imgMo2.tag = true
                imgMo2.setImageResource(R.drawable.img_anmo_2)
            }
            R.id.imgMo3 -> {
                imgMo3.tag = true
                imgMo3.setImageResource(R.drawable.img_anmo_3)
            }
        }

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
            val iPressV = BaseVolume.getPressByValue(iValue,iChannelNumber)
            viewSenseList[iNumber].text = ""
            viewSenseList[iNumber].text = iPressV.toString()
            // 根据气压值，改变填充色
            val colorValue = BaseVolume.getColorByPressValue(iPressV,iChannelNumber)
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

                val iPressV = BaseVolume.getPressByValue(iNowValue,(iNowSelectNumber+1))
                tvSeekBarValue.text = iPressV.toString()
//                viewList[iNowSelectNumber].text = iPressV.toString()
//                val colorValue = BaseVolume.getColorByPressValue(iPressV,(iNowSelectNumber+1))
//                drawableList[iNowSelectNumber].setColor(colorValue)

            }
        }

        override fun onStartTrackingTouch(p0: SeekBar?) {
        }

        override fun onStopTrackingTouch(p0: SeekBar?) {
            var iNowValue = p0?.progress!! +ProgressValueMin
            if (iNowSelectNumber != -1) {
//                val iPressV = BaseVolume.getPressByValue(iNowValue,(iNowSelectNumber+1))
//                viewList[iNowSelectNumber].text = iPressV.toString()

                // 通道1，最大值3580
                if (iNowSelectNumber == 0) {
                    if (iNowValue > 3580) {
                        iNowValue = 3580
                    }
                }

                // 保存控制缓存值
                MainControlActivity.getInstance()?.setValueBufferByChannel?.put(iNowSelectNumber+1,iNowValue.toString())

                val strSendData = CreateCtrDataHelper.getCtrPressBy1((iNowSelectNumber+1).toString(),iNowValue)
                SocketThreadManager.sharedInstance(this@ManualActivity)?.StartSendData(strSendData)
            }
        }

    }

    override fun onClick(p0: View?) {
        when(p0?.id) {
            R.id.imgBack -> {
                MainControlActivity.getInstance()?.finish()
            }
            R.id.btnJian -> {
                var iValue = seekBar.progress
                iValue -= 5
                if (iValue > 0)
                    seekBar.progress = iValue
                else
                    seekBar.progress = 0

                var iNowValue = seekBar.progress+ProgressValueMin
                if (iNowSelectNumber != -1) {
                    val iPressV = BaseVolume.getPressByValue(iNowValue,(iNowSelectNumber+1))
//                    viewList[iNowSelectNumber].text = iPressV.toString()
//                    if (iNowSelectNumber == 3) {
//                        view3Left?.text = iPressV.toString()
//                    }
                    // 通道1，最大值3580
                    if (iNowSelectNumber == 0) {
                        if (iNowValue > 3580) {
                            iNowValue = 3580
                        }
                    }
                    // 保存控制缓存值
                    MainControlActivity.getInstance()?.setValueBufferByChannel?.put(iNowSelectNumber+1,iNowValue.toString())

                    val strSendData = CreateCtrDataHelper.getCtrPressBy1((iNowSelectNumber+1).toString(),iNowValue)
                    SocketThreadManager.sharedInstance(this@ManualActivity)?.StartSendData(strSendData)
                }

            }
            R.id.btnJia -> {
                var iValue = seekBar.progress
                iValue += 5
                if (iValue < seekBar.max)
                    seekBar.progress = iValue
                else
                    seekBar.progress = seekBar.max

                var iNowValue = seekBar.progress+ProgressValueMin
                if (iNowSelectNumber != -1) {
                    val iPressV = BaseVolume.getPressByValue(iNowValue,(iNowSelectNumber+1))
//                    viewList[iNowSelectNumber].text = iPressV.toString()
//                    if (iNowSelectNumber == 3) {
//                        view3Left?.text = iPressV.toString()
//                    }
                    // 通道1，最大值3580
                    if (iNowSelectNumber == 0) {
                        if (iNowValue > 3580) {
                            iNowValue = 3580
                        }
                    }

                    // 保存控制缓存值
                    MainControlActivity.getInstance()?.setValueBufferByChannel?.put(iNowSelectNumber+1,iNowValue.toString())

                    val strSendData = CreateCtrDataHelper.getCtrPressBy1((iNowSelectNumber+1).toString(),iNowValue)
                    SocketThreadManager.sharedInstance(this@ManualActivity)?.StartSendData(strSendData)
                }

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
                changeSelectBtn(p0)
            }
            R.id.btnProp -> {
                switchCtrView(1)
            }
            R.id.btnLocation -> {
                switchCtrView(2)
            }
            R.id.btnHeat -> {
                switchCtrView(3)
            }
        }
    }

    var iNowShowPageNumber = 1;
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
    fun changeSelectBtn(view: View) {
        val iTag = view.tag.toString().toInt()
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

            if (iTag == -1) {
                return
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
            }
            else if (iTag == 4){
                viewList[9].text = iNowValue.toString()
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
            if (iTag == 3 && iNumber == 8) {
                continue
            }
            if (iTag == 4 && iNumber == 9) {
                continue
            }


            var drawable = drawableList[iNumber]
            val iValue = pressList[iTag].toInt()
            val iPressV = BaseVolume.getPressByValue(iValue,(iTag+1))
//            viewList[iNumber].text = iPressV.toString()
            // 根据气压值，改变填充色
            val colorValue = BaseVolume.getColorByPressValue(iPressV,(iTag+1))
            drawable.setColor(colorValue)

        }
    }

    /**
     * 保存
     */
    fun saveNowValue() {

        if (setValueDialog == null) {
            setValueDialog = SetValueAreaAddWindow(this, R.style.Dialogstyle,
                    "Please enter the name", object : SetValueAreaAddWindow.PeriodListener {
                override fun confirmListener(string: String) {

                    nowMemoryInfo = MemoryDataInfo()
                    nowMemoryInfo.strDataName = string
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

                    // 传感数据
                    for (iNumber in 3 .. 10) {
                        updatePressValueByNumber(iNumber+6,(DataAnalysisHelper.deviceState.sensePressValueListl[iNumber]))
                    }

                    val memoryInfoDao = MemoryInfoDao(this@ManualActivity)
                    val isHave = memoryInfoDao.isHaveByName(string)
                    // 该名称已存在，则提示用户
                    if (isHave) {
                        memoryInfoDao.closeDb()
                        showHintDialog("System","The name already exists. Do not replace it？",string)
                    }
                    // 直接添加
                    else {
                        // 数据库操作
                        val isResult = memoryInfoDao.insertSingleData(nowMemoryInfo)
                        if (isResult < 0)
                            ToastMsg("Manual mode, data saved fault！")
                        else
                            ToastMsg("Manual mode, data saved successfully！")
                        memoryInfoDao.closeDb()
                    }
                }
                override fun cancelListener() {

                }
            }, "", false)

            setValueDialog!!.setCancelable(false)
        }
        setValueDialog!!.setNowValue("")
        setValueDialog!!.show()
    }

    // 根据序号更新值
    fun updatePressValueByNumber(iNumber : Int,strValue:String) {
        when(iNumber) {
            1 ->
                nowMemoryInfo.strPress1 = strValue
            2 ->
                nowMemoryInfo.strPress2 = strValue
            3 ->
                nowMemoryInfo.strPress3 = strValue
            4 ->
                nowMemoryInfo.strPress4 = strValue
            5 ->
                nowMemoryInfo.strPress5 = strValue
            6 ->
                nowMemoryInfo.strPress6 = strValue
            7 ->
                nowMemoryInfo.strPress7 = strValue
            8 ->
                nowMemoryInfo.strPress8 = strValue
            9 ->
                nowMemoryInfo.strPressA = strValue
            10 ->
                nowMemoryInfo.strPressB = strValue
            11 ->
                nowMemoryInfo.strPressC = strValue
            12 ->
                nowMemoryInfo.strPressD = strValue
            13 ->
                nowMemoryInfo.strPressE = strValue
            14 ->
                nowMemoryInfo.strPressF = strValue
            15 ->
                nowMemoryInfo.strPressG = strValue
            16 ->
                nowMemoryInfo.strPressH = strValue
        }
    }

    /** 提示语 */
    fun showHintDialog(strTitle:String,strContent:String,strName:String) {

        val areaAddWindowHint = AreaAddWindowHint(this,R.style.Dialogstyle,strTitle,
                object : PeriodListener {
                    override fun refreshListener(string: String) {
                        val memoryInfoDao = MemoryInfoDao(this@ManualActivity)
                        // 数据库操作
                        val isResult = memoryInfoDao.updateDataByName(nowMemoryInfo)
                        if (!isResult)
                            ToastMsg("Manual mode, data saved fault！")
                        else
                            ToastMsg("Manual mode, data saved successfully！")
                        memoryInfoDao.closeDb()
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
                // 座椅状态
                if (strType.equals(BaseVolume.COMMAND_TYPE_SEAT_STATUS,true)) {
                    val iSeatStatus = deviceWorkInfo?.seatStatus
                }
                // 气压
                else if (strType.equals(BaseVolume.COMMAND_TYPE_PRESS,true)) {
                    // 当前是支撑调节界面，则更新控制气压
                    if (iNowSelectNumber == 1) {
                        updateSeatView()
                    }
                    // 当前是通风加热按摩，则更新传感气压
                    else if (iNowSelectNumber == 3) {
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