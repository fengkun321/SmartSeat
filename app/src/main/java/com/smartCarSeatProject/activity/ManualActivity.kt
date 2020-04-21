package com.smartCarSeatProject.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import android.widget.Button
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
import kotlinx.android.synthetic.main.layout_manual.*
import kotlinx.android.synthetic.main.layout_manual_seat.*
import kotlinx.android.synthetic.main.layout_manual_seat.view.*

class ManualActivity: BaseActivity(), View.OnClickListener{

    var setValueDialog : SetValueAreaAddWindow? = null
    var nowMemoryInfo = MemoryDataInfo()

    // 样式的集合
    var drawableList = arrayListOf<GradientDrawable>()
    // 视图的集合
    var viewList = arrayListOf<TextView>()
    // 视图3Left的样式
    var drawable3Left : GradientDrawable? = null

    // 当前选中的序号
    var iNowSelectNumber = -1
    var ProgressValueMin = 255
    var ProgressValueMax = 3600

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_manual)

        initUI()
        reciverBand()

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
        btnSave.setOnClickListener(this)
        btnMemory.setOnClickListener(this)
        rlParent.setOnClickListener(this)


        for (i in 0..7) {
            val iValue = DataAnalysisHelper.deviceState.controlPressValueList[i]
        }

        initShape(manualSeat.view0,false,false)
        initShape(manualSeat.view1,true,false)
        initShape(manualSeat.view2,true,true)
        initShape(manualSeat.view3,false,false)
        initShape(manualSeat.view4,false,false)
        initShape(manualSeat.view5,false,false)
        initShape(manualSeat.view6,false,false)
        initShape(manualSeat.view7,false,false)

        initShareByLeft()

        updateSeatView()
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
        drawable.setStroke(2, resources.getColor(R.color.colorWhite))
        // 填充色
        drawable.setColor(resources.getColor(R.color.colorTransparency))
        view.setBackgroundDrawable(drawable)
        view.setOnClickListener { changeSelectBtn(it) }

        drawableList.add(drawable)
        viewList.add(view)

    }

    // 气袋4,左侧视图
    fun initShareByLeft() {
        var drawable = GradientDrawable()
        // 四个角度
        drawable.cornerRadius = 10f
        // 边框：宽，颜色
        drawable.setStroke(2, resources.getColor(R.color.colorWhite))
        // 填充色
        drawable.setColor(resources.getColor(R.color.colorTransparency))
        view3Left.setBackgroundDrawable(drawable)
        view3Left.setOnClickListener { changeSelectBtn(it) }
        drawable3Left = drawable

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
        }
    }

    /**
     * 选中某个view
     */
    fun changeSelectBtn(view: View) {
        // 全部边框变白
        drawableList.forEach {
            it.setStroke(2, resources.getColor(R.color.colorWhite))
        }
        drawable3Left?.setStroke(2, resources.getColor(R.color.colorWhite))

        // 选中的边框变红
        iNowSelectNumber = view.tag.toString().toInt()

        if (iNowSelectNumber == -1) {
            tvSeekBarValue.visibility = View.INVISIBLE
            seekBar.isEnabled = false
            btnJian.isEnabled = false
            btnJia.isEnabled = false
            btnJian.setTextColor(resources.getColor(R.color.black1))
            btnJia.setTextColor(resources.getColor(R.color.black1))
            return
        }

        seekBar.isEnabled = true
        btnJian.isEnabled = true
        btnJia.isEnabled = true
        btnJian.setTextColor(resources.getColor(R.color.colorWhite))
        btnJia.setTextColor(resources.getColor(R.color.colorWhite))
        drawableList[iNowSelectNumber].setStroke(4, resources.getColor(R.color.device_red))
        if (iNowSelectNumber == 3) {
            drawable3Left?.setStroke(4, resources.getColor(R.color.device_red))
        }

        tvSeekBarValue.visibility = View.VISIBLE


        ProgressValueMin = BaseVolume.getPressMinMaxByChannel((iNowSelectNumber+1),BaseVolume.pressValueMin)
        ProgressValueMax = BaseVolume.getPressMinMaxByChannel((iNowSelectNumber+1),BaseVolume.pressValueMax)

        Loge("ManualActivity","通道："+(iNowSelectNumber+1)+"，最小值："+ProgressValueMin+"，最大值："+ProgressValueMax)

        seekBar.max = ProgressValueMax - ProgressValueMin

        val iNowValue:Int = DataAnalysisHelper.deviceState.controlPressValueList[iNowSelectNumber].toInt()
        seekBar.progress = iNowValue-ProgressValueMin

    }

    /**
     * 根据当前气压值，改变view颜色
     */
    private fun updateSeatView() {
        // 控制气压8个
        val pressList = DataAnalysisHelper.deviceState.controlPressValueList
        // 这里实际只用到了8个气压值
        for (iNumber in 0 until viewList.size) {
            // 如果当前在调节，那被调节的按钮不可动，不然就乱套了
//            if (iNumber == iNowSelectNumber) {
//                continue
//            }
            val iTag = viewList[iNumber].tag.toString().toInt()
            var drawable = drawableList[iNumber]
            val iValue = pressList[iTag].toInt()
            val iPressV = BaseVolume.getPressByValue(iValue,(iNumber+1))
            viewList[iNumber].text = iPressV.toString()
            // 根据气压值，改变填充色
            val colorValue = BaseVolume.getColorByPressValue(iPressV,(iNumber+1))
            drawable.setColor(colorValue)

            if (iNumber == 3) {
                view3Left.text = iPressV.toString()
                drawable3Left?.setColor(colorValue)
            }

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
                    updateSeatView()
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