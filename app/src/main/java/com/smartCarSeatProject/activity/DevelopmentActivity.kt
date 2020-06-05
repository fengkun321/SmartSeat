package com.smartCarSeatProject.activity

import ai.nuralogix.anura.sample.activities.ConfigActivity
import ai.nuralogix.anura.sample.activities.MeasurementActivity
import ai.nuralogix.anura.sample.settings.CameraConfigurationFragment
import ai.nuralogix.anurasdk.camera.CameraAdapter
import ai.nuralogix.anurasdk.config.DfxPipeConfiguration
import ai.nuralogix.anurasdk.core.*
import ai.nuralogix.anurasdk.error.AnuraError
import ai.nuralogix.anurasdk.face.FaceTrackerAdapter
import ai.nuralogix.anurasdk.network.DeepAffexDataSpec
import ai.nuralogix.anurasdk.network.DeepFXClient
import ai.nuralogix.anurasdk.render.Render
import ai.nuralogix.anurasdk.render.opengl.GLSurfaceViewTracker
import ai.nuralogix.anurasdk.utils.*
import ai.nuralogix.anurasdk.views.TrackerView
import ai.nuralogix.dfx.ChunkPayload
import ai.nuralogix.dfx.Collector
import ai.nuralogix.dfx.ConstraintResult
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.os.IBinder
import android.os.SystemClock
import android.support.v7.app.AlertDialog
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.smartCarSeatProject.R
import com.smartCarSeatProject.dao.DevelopDataInfo
import com.smartCarSeatProject.dao.DevelopInfoDao
import com.smartCarSeatProject.data.*
import com.smartCarSeatProject.view.AreaAddWindowHint
import com.smartCarSeatProject.view.SetValueAreaAddWindow
import kotlinx.android.synthetic.main.layout_develop.*
import kotlinx.android.synthetic.main.layout_people_info.*
import kotlinx.android.synthetic.main.layout_a_pmcm.view.*
import kotlinx.android.synthetic.main.layout_a_pmcm_child.view.*
import kotlinx.android.synthetic.main.layout_b_dmcm.view.*
import kotlinx.android.synthetic.main.layout_development_value.view.*
import android.widget.*
import com.ai.nuralogix.anura.sample.face.MNNFaceDetectorAdapter
import com.ai.nuralogix.anura.sample.utils.BundleUtils
import com.alibaba.android.mnnkit.monitor.MNNMonitor
import com.smartCarSeatProject.BuildConfig
import com.smartCarSeatProject.tcpInfo.SocketThreadManager
import com.smartCarSeatProject.utl.DateUtil
import com.smartCarSeatProject.view.AddMenuWindowDialog
import kotlinx.android.synthetic.main.layout_b_dmcm.*
import kotlinx.android.synthetic.main.layout_develop.tvReCanConnect
import kotlinx.android.synthetic.main.layout_em_info.*
import org.json.JSONArray
import org.opencv.core.Point


class DevelopmentActivity: BaseActivity(),View.OnClickListener,DfxPipeListener, VideoSignalAnalysisListener, TrackerView.OnSizeChangedListener{

    var seekBarList :MutableList<SeekBar> = ArrayList()
    var tvBValueList : MutableList<TextView> = ArrayList()
    var tvAValueList : MutableList<TextView> = ArrayList()
    var tvAOtherValueList : MutableList<TextView> = ArrayList()
    var tvJianList : MutableList<Button> = ArrayList()
    var tvJiaList : MutableList<Button> = ArrayList()
    var setValueDialog : SetValueAreaAddWindow? = null
    var nowDevelopDataInfo = DevelopDataInfo()
    var iNowSelectNumber = -1;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_develop)

        initUI()
        reciverBand()

        tvReCanConnect.visibility = View.GONE
        tvReLocConnect.visibility = View.GONE
        if (!SocketThreadManager.sharedInstance(mContext).isCanConnected()) {
            tvReCanConnect.visibility = View.VISIBLE
        }

        if (!SocketThreadManager.sharedInstance(mContext).isCan2Connected()) {
            tvReLocConnect.visibility = View.VISIBLE
        }


        // 人体采集相关
        AnuLogUtil.setShowLog(BuildConfig.DEBUG)
        MNNMonitor.setMonitorEnable(false)
        initNuralogixInfo()

    }

    fun initUI() {

        edAInitValue.setText(BaseVolume.strSensorInitValue)
        edSeatInitValue.setText(BaseVolume.strSeatInitValue)
        edBInitValue.setText(BaseVolume.strAdjustInitialValue)

        rgSex.check(R.id.rbMan)
        rgRace.check(R.id.rbCN)

        includeA.includeAAL.tvTitle.text = "A";includeA.includeAAR.tvTitle.text = "A"
        includeA.includeABL.tvTitle.text = "B";includeA.includeABR.tvTitle.text = "B"
        includeA.includeACL.tvTitle.text = "C";includeA.includeACR.tvTitle.text = "C"
        includeA.includeADL.tvTitle.text = "D";includeA.includeADR.tvTitle.text = "D"
        includeA.includeAEL.tvTitle.text = "E";includeA.includeAER.tvTitle.text = "E"
        includeA.includeAFL.tvTitle.text = "F";includeA.includeAFR.tvTitle.text = "F"
        includeA.includeAGL.tvTitle.text = "G";includeA.includeAGR.tvTitle.text = "G"
        includeA.includeAHL.tvTitle.text = "H";includeA.includeAHR.tvTitle.text = "H"

        includeA.includeASeat6.tvTitle.text = "6"
        includeA.includeASeat7.tvTitle.text = "7"
        includeA.includeASeat8.tvTitle.text = "8"

        // A面的部分参数值(11个。座垫3+靠背左测8)
        tvAValueList.add(includeA.includeASeat6.tvValueA)
        tvAValueList.add(includeA.includeASeat7.tvValueA)
        tvAValueList.add(includeA.includeASeat8.tvValueA)
        tvAValueList.add(includeA.includeAAL.tvValueA)
        tvAValueList.add(includeA.includeABL.tvValueA)
        tvAValueList.add(includeA.includeACL.tvValueA)
        tvAValueList.add(includeA.includeADL.tvValueA)
        tvAValueList.add(includeA.includeAEL.tvValueA)
        tvAValueList.add(includeA.includeAFL.tvValueA)
        tvAValueList.add(includeA.includeAGL.tvValueA)
        tvAValueList.add(includeA.includeAHL.tvValueA)

        // A面靠背右侧，参数和左侧一致（8个）
        tvAOtherValueList.add(includeA.includeAAR.tvValueA)
        tvAOtherValueList.add(includeA.includeABR.tvValueA)
        tvAOtherValueList.add(includeA.includeACR.tvValueA)
        tvAOtherValueList.add(includeA.includeADR.tvValueA)
        tvAOtherValueList.add(includeA.includeAER.tvValueA)
        tvAOtherValueList.add(includeA.includeAFR.tvValueA)
        tvAOtherValueList.add(includeA.includeAGR.tvValueA)
        tvAOtherValueList.add(includeA.includeAHR.tvValueA)

        val bViewList = arrayListOf(includeB.includeB1,includeB.includeB2,includeB.includeB3,includeB.includeB4,includeB.includeB5,includeB.includeB6,includeB.includeB7,includeB.includeB8)
        var iTag = 0
        bViewList.forEach {

            var btnJian = it.btnJian
            var btnJia = it.btnJia
            var tvValue = it.tvValueB
            var seekBar = it.seekBar
            var tvNumber = it.tvNumber

            tvNumber.text = "${iTag+1}："
            btnJian.tag = iTag
            btnJia.tag = iTag
            tvValue.tag = iTag
            seekBar.tag = iTag
            seekBar.max = BaseVolume.ProgressValueMax - BaseVolume.ProgressValueMin

            btnJian.isEnabled = false
            btnJia.isEnabled = false
            seekBar.isEnabled = false

            btnJian.setOnClickListener(onJianListenerProxy)
            btnJia.setOnClickListener(onJiaListenerProxy)
            seekBar.setOnSeekBarChangeListener(onSeekBarChangeListenerProxy)

            tvJianList.add(btnJian)
            tvJiaList.add(btnJia)
            tvBValueList.add(tvValue)
            seekBarList.add(seekBar)
            ++iTag
        }

        seekBarList.forEach {
            it.progress = 2000
        }

        tvReCanConnect.setOnClickListener(this)
        tvReLocConnect.setOnClickListener(this)

        imgBack.setOnClickListener(this)
        btnInitValue.setOnClickListener(this)
        btnSaveA.setOnClickListener(this)
        btnSaveAllData.setOnClickListener(this)
        btnHistory.setOnClickListener(this)
        llParent.setOnClickListener(this)
        rlLocation.setOnClickListener(this)

        initUIFromB()
    }


    fun initUIFromB() {
        val iColorResource = if(isSaveAData) resources.getColor(R.color.colorWhite) else resources.getColor(R.color.black1)

        btnSaveAllData.isEnabled = isSaveAData
        btnSaveAllData.setTextColor(iColorResource)

        seekBarList.forEach {
            it.isEnabled = isSaveAData
        }

        tvJianList.forEach {
            it.isEnabled = isSaveAData
            it.setTextColor(iColorResource)
        }

        tvJiaList.forEach {
            it.isEnabled = isSaveAData
            it.setTextColor(iColorResource)
        }

        tvBValueList.forEach{
            it.setTextColor(iColorResource)
        }

    }

    /** 监听广播  */
    private fun reciverBand() {
        val myIntentFilter = IntentFilter()
        myIntentFilter.addAction(BaseVolume.BROADCAST_RESULT_DATA_INFO)
        myIntentFilter.addAction(BaseVolume.BROADCAST_CTR_CALLBACK)
        myIntentFilter.addAction(BaseVolume.BROADCAST_SEND_INFO)
        myIntentFilter.addAction(BaseVolume.BROADCAST_TCP_INFO_CAN)
        myIntentFilter.addAction(BaseVolume.BROADCAST_TCP_INFO_CAN2)
        // 注册广播
        registerReceiver(myNetReceiver, myIntentFilter)
    }

    /** 减号的点击时间 */
    val onJianListenerProxy : View.OnClickListener = View.OnClickListener {
        var iTag = it.tag as Int
        var iValue = seekBarList[iTag].progress
        iValue -= 5
        if (iValue > 0)
            seekBarList[iTag].progress = iValue
        else
            seekBarList[iTag].progress = 0

        val iNowValue = tvBValueList[iTag].text.toString().toInt()
        controlPressValueByTag(iTag,iNowValue)
    }

    /** 加号的点击时间 */
    val onJiaListenerProxy : View.OnClickListener = View.OnClickListener {
        var iTag = it.tag as Int
        var iValue = seekBarList[iTag].progress
        iValue += 5
        if (iValue < seekBarList[iTag].max)
            seekBarList[iTag].progress = iValue
        else
            seekBarList[iTag].progress = seekBarList[iTag].max

        val iNowValue = tvBValueList[iTag].text.toString().toInt()
        controlPressValueByTag(iTag,iNowValue)

    }

    /** SeekBar值改变时间 */
    val onSeekBarChangeListenerProxy = object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, i: Int, b: Boolean) {
            var iProcess = seekBar?.progress!! +BaseVolume.ProgressValueMin
            var iTag = seekBar?.tag
            tvBValueList[iTag  as Int].text = "$iProcess"
        }
        override fun onStartTrackingTouch(seekBar: SeekBar?) {
        }
        override fun onStopTrackingTouch(seekBar: SeekBar?) {
            var iProcess = seekBar?.progress!! +BaseVolume.ProgressValueMin
            var iTag = seekBar?.tag  as Int
            controlPressValueByTag(iTag,iProcess)
        }
    }

    /** 提示语 */
    fun showHintDialog(strTitle:String,strContent:String,strName:String) {
        val areaAddWindowHint = AreaAddWindowHint(this,R.style.Dialogstyle,strTitle,
                object : AreaAddWindowHint.PeriodListener {
                    override fun refreshListener(string: String) {
                    }

                    override fun cancelListener() {
                    }
                },strContent)
        areaAddWindowHint.show()
    }

    /**
     * 广播监听
     */
    private val myNetReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (action == BaseVolume.BROADCAST_SEND_INFO) {
                val strType = intent.getStringExtra(BaseVolume.BROADCAST_TYPE)
                // 开始发送数据
                if (strType.equals(BaseVolume.BROADCAST_SEND_DATA_START)) {
                    if (!loadingDialog!!.isShowing) {
                        loadingDialog?.show()
                    }
                }
                // 停止发送
                else if (strType.equals(BaseVolume.BROADCAST_SEND_DATA_END)) {
                    loadingDialog?.dismiss()
                }
                // 发送超时
                else if (strType.equals(BaseVolume.BROADCAST_SEND_DATA_TIME_OUT)) {
                    loadingDialog?.dismiss()
                    ToastMsg("Send timeout！")
                }
            }
            // 数据回调
            else if (action == BaseVolume.BROADCAST_RESULT_DATA_INFO) {
                val strType = intent.getStringExtra(BaseVolume.BROADCAST_TYPE)
                val deviceWorkInfo = intent.getSerializableExtra(BaseVolume.BROADCAST_MSG) as DeviceWorkInfo
                // 通道状态
                if (strType == BaseVolume.COMMAND_TYPE_CHANNEL_STATUS) {
                    // 开发者模式
                    if (DataAnalysisHelper.deviceState.seatStatus == SeatStatus.develop.iValue && SocketThreadManager.isCheckChannelState) {
                        // 正在恢复，则不能重复触发
                        if (includeA.tvInitValue.text.toString().equals("Restoring initial value...",true)) {
                            // 初始化控制A面所有，B面座垫678，通道充气，所以只需要判断678，abcdefgh这几个气袋
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
                            // 已经全部恢复到Normal，则将显示初始化完成
                            else {
                                // 保存体征按钮，可用
                                btnSaveA.isEnabled = true
                                btnSaveA.setTextColor(resources.getColor(R.color.colorWhite))
                                includeA.tvInitValue.text = "Initialization completed!"
                                includeA.tvInitValue.setTextColor(resources.getColor(R.color.colorGreen))
                                SocketThreadManager.sharedInstance(mContext).startTimeOut(false)
                            }
                        }
                        // 控制某路气袋的返回
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

                                iNowSelectNumber = -1
                                SocketThreadManager.sharedInstance(mContext).startTimeOut(false)
                            }

                        }
                    }
                }
                // 气压
                else if (strType.equals(BaseVolume.COMMAND_TYPE_PRESS,true)) {
                    // 控制气压8个
                    val controlPressList = deviceWorkInfo?.controlPressValueList
                    // 传感气压11个
                    val sensePressList = deviceWorkInfo?.sensePressValueListl

                    // 如果识别数据已经保存，右侧的调节控件不再自动更新，以免会跳动。
                    // 识别数据还没有保存
                    if (!isSaveAData) {
                        for (iIndex in 0..7) {
                            seekBarList[iIndex].progress = controlPressList[iIndex].toInt() - BaseVolume.ProgressValueMin
                        }
                    }

                    for (iIndex in 0..10) {
                        // 传感气压11个
                        tvAValueList[iIndex].text = sensePressList[iIndex]
                        if (iIndex >= 3) {
                            // 靠背右侧的8个传感气压和左侧的值一样
                            tvAOtherValueList[iIndex-3].text = sensePressList[iIndex]
                        }
                    }
                }
            }
            // 控制回调
            else if (action == BaseVolume.BROADCAST_CTR_CALLBACK) {
                val strType = intent.getStringExtra(BaseVolume.BROADCAST_TYPE)
                val strMsg = intent.getStringExtra(BaseVolume.BROADCAST_MSG)
            }
            else if (action == BaseVolume.BROADCAST_TCP_INFO_CAN) {
                val strType = intent.getStringExtra(BaseVolume.BROADCAST_TYPE)
                // 开始连接
                if (strType.equals(BaseVolume.BROADCAST_TCP_CONNECT_START)) {
                }
                // 连接结果
                else if (strType.equals(BaseVolume.BROADCAST_TCP_CONNECT_CALLBACK)) {
                    val isConnected = intent.getBooleanExtra(BaseVolume.BROADCAST_TCP_STATUS, false)
                    val strMsg = intent.getStringExtra(BaseVolume.BROADCAST_MSG)

                    if (!isConnected) {
                        ToastMsg("Can Connect Fail！$strMsg")
                        tvReCanConnect.visibility = View.VISIBLE
                    }
                    else {
                        tvReCanConnect.visibility = View.GONE
                    }
                }
            }
            else if (action == BaseVolume.BROADCAST_TCP_INFO_CAN2) {
                val strType = intent.getStringExtra(BaseVolume.BROADCAST_TYPE)
                // 开始连接
                if (strType.equals(BaseVolume.BROADCAST_TCP_CONNECT_START)) {
                }
                // 连接结果
                else if (strType.equals(BaseVolume.BROADCAST_TCP_CONNECT_CALLBACK)) {
                    val isConnected = intent.getBooleanExtra(BaseVolume.BROADCAST_TCP_STATUS, false)
                    val strMsg = intent.getStringExtra(BaseVolume.BROADCAST_MSG)

                    if (!isConnected) {
                        ToastMsg("Can Connect Fail！$strMsg")
                        tvReLocConnect.visibility = View.VISIBLE
                    }
                    else {
                        tvReLocConnect.visibility = View.GONE
                    }
                }
            }
        }
    }

    // 是否已经保存了A面的数据 点击“记录A面数据”
    var isSaveAData = false
    override fun onClick(p0: View?) {
        when(p0?.id) {
            R.id.imgBack ->
                finish()
            R.id.btnInitValue ->
                setInitValue()
            R.id.btnSaveA ->
                saveADataBuffer()
            R.id.btnSaveAllData ->
                saveAllData()
            R.id.btnHistory ->
                startActivity(Intent(this, HistoryDataShowActivity::class.java))
            R.id.llParent ->
                hideSoftInput(p0?.windowToken)
            R.id.tvReCanConnect ->
                SocketThreadManager.sharedInstance(mContext).createCanSocket()
            R.id.tvReLocConnect ->
                SocketThreadManager.sharedInstance(mContext).createLocSocket()
            R.id.rlLocation ->
                deviceSelectMenu(true,tvLocation.text.toString(),locationList)

        }

    }
    val locationList = arrayListOf<String>("区域1","区域2","区域3","区域4","区域5","区域6")
    val massageModeList = arrayListOf<String>("模式1","模式2","模式3")
    /** 设备编辑菜单选择功能  */
    private fun deviceSelectMenu(isLocation : Boolean,strValue : String,list : ArrayList<String>) {
        val strTitle = if (isLocation) "位置调节" else "按摩模式"
        val dialog1 = AddMenuWindowDialog(mContext, R.style.LoadingDialogStyle, list, strTitle)
        dialog1.setListener(object : AddMenuWindowDialog.PeriodListener {
            override fun refreshListener(number: Int,strItem: String) {
                // 位置调节
                if (isLocation) {
                    tvLocation.text = strItem
                    setLocationData(number)
                }
                // 按摩模式
                else {
//                    tvMassageMode.text = strItem
                }
            }
        })
        dialog1.show()

    }

    /** 位置调节 */
    fun setLocationData (number: Int) {
        when(number) {
            0 ->
                SocketThreadManager.sharedInstance(mContext).StartSendDataByCan2(BaseVolume.COMMAND_CAN_LOCATION_1)
            1 ->
                SocketThreadManager.sharedInstance(mContext).StartSendDataByCan2(BaseVolume.COMMAND_CAN_LOCATION_2)
            2 ->
                SocketThreadManager.sharedInstance(mContext).StartSendDataByCan2(BaseVolume.COMMAND_CAN_LOCATION_3)
            3 ->
                SocketThreadManager.sharedInstance(mContext).StartSendDataByCan2(BaseVolume.COMMAND_CAN_LOCATION_4)
            4 ->
                SocketThreadManager.sharedInstance(mContext).StartSendDataByCan2(BaseVolume.COMMAND_CAN_LOCATION_5)
            5 ->
                SocketThreadManager.sharedInstance(mContext).StartSendDataByCan2(BaseVolume.COMMAND_CAN_LOCATION_6)
        }
        // 每次发完位置后，要再发一条清零指令才会动作
        SocketThreadManager.sharedInstance(mContext).StartSendDataByCan2(BaseVolume.COMMAND_CAN_LOCATION_0)

    }

    // 设置初始气压值
    fun setInitValue() {

        val strVA = edAInitValue.text.toString()
        val strVB = edBInitValue.text.toString()
        val strVSeat = edSeatInitValue.text.toString()

        if ((strVA == null || strVA.equals("")) || (strVB == null || strVB.equals("")) || (strVSeat == null || strVSeat.equals(""))) {
            ToastMsg("Please enter the initial value！")
            return
        }

        // 正在恢复，则不能重复触发
        if (includeA.tvInitValue.text.toString().equals("Restoring initial value...",true)) {
            return
        }

        includeA.tvInitValue.text = "Restoring initial value..."
        includeA.tvInitValue.visibility = View.VISIBLE
        includeA.tvInitValue.setTextColor(resources.getColor(R.color.colorWhite))

        // 保存体征按钮，不可用
        btnSaveA.isEnabled = false
        btnSaveA.setTextColor(resources.getColor(R.color.black1))

        isSaveAData = false
        initUIFromB()

        ToastMsg("Restoring initial value...")
        // 将A面,B面设为adjust
        SocketThreadManager.sharedInstance(mContext)?.StartChangeModelByCan(BaseVolume.COMMAND_CAN_MODEL_ADJUST_A_B)
        // 发指令，设置16个气压
        val NowSendDataList = CreateCtrDataHelper.getAllPressValueBy16(strVA,strVSeat,strVB)
        NowSendDataList.forEach {
            SocketThreadManager.sharedInstance(this@DevelopmentActivity)?.StartSendDataByCan(it)
        }

    }

    // 缓存当前所有体征值 初始值+靠背+座垫
    fun saveADataBuffer() {


        val strVA = edAInitValue.text.toString()
        val strVB = edBInitValue.text.toString()
        val strVSeat = edSeatInitValue.text.toString()

        if ((strVA == null || strVA.equals("")) || (strVB == null || strVB.equals("")) || (strVSeat == null || strVSeat.equals(""))) {
            ToastMsg("Please enter the initial value！")
            return
        }

        // 缓存A面的数据
        val strWeight = edWeight.text.toString()
        val strHeight = edHeight.text.toString()

        if ((strWeight == null || strWeight.equals("")) || (strHeight == null || strHeight.equals(""))) {
            ToastMsg("Please enter height and weight！")
            return
        }

        nowDevelopDataInfo.initData()
        //性别
        val sex =  findViewById<RadioButton>(rgSex.checkedRadioButtonId)
        //国家
        val nation = findViewById<RadioButton>(rgRace.checkedRadioButtonId)

        // 名称
        nowDevelopDataInfo.strName = edPS.text.toString()
        //性别
        nowDevelopDataInfo.m_gender   = sex.text.toString()
        //国家
        nowDevelopDataInfo.m_national = nation.text.toString()
        // 人员-体重
        nowDevelopDataInfo.m_weight =  edWeight.text.toString()
        // 人员-身高
        nowDevelopDataInfo.m_height =  edHeight.text.toString()
        // 备注
        nowDevelopDataInfo.strPSInfo =  edPS.text.toString()


        nowDevelopDataInfo.HeartRate = tvXinLv.text.toString()
        nowDevelopDataInfo.BreathRate = tvHuXiLv.text.toString()
        nowDevelopDataInfo.E_Index = tvQingXu.text.toString()
        nowDevelopDataInfo.Dia_BP = tvShuZhang.text.toString()
        nowDevelopDataInfo.Sys_BP = tvShouSuo.text.toString()
        nowDevelopDataInfo.l_location = tvLocation.text.toString()

        // 初始化A面8组气压
        nowDevelopDataInfo.p_init_back_A = strVA
        // 初始化靠背B面5组气压
        nowDevelopDataInfo.p_init_back_B = strVB
        // 初始化坐垫3组气压
        nowDevelopDataInfo.p_init_cushion = strVSeat

        // 识别后靠背A面8组
        nowDevelopDataInfo.p_recog_back_A =  includeA.includeAAL.tvValueA.text.toString()
        nowDevelopDataInfo.p_recog_back_B =  includeA.includeABL.tvValueA.text.toString()
        nowDevelopDataInfo.p_recog_back_C =  includeA.includeACL.tvValueA.text.toString()
        nowDevelopDataInfo.p_recog_back_D =  includeA.includeADL.tvValueA.text.toString()
        nowDevelopDataInfo.p_recog_back_E =  includeA.includeAEL.tvValueA.text.toString()
        nowDevelopDataInfo.p_recog_back_F =  includeA.includeAFL.tvValueA.text.toString()
        nowDevelopDataInfo.p_recog_back_G =  includeA.includeAGL.tvValueA.text.toString()
        nowDevelopDataInfo.p_recog_back_H =  includeA.includeAHL.tvValueA.text.toString()

        // 识别后坐垫3组
        nowDevelopDataInfo.p_recog_cushion_6 =  includeA.includeASeat6.tvValueA.text.toString()
        nowDevelopDataInfo.p_recog_cushion_7 =  includeA.includeASeat7.tvValueA.text.toString()
        nowDevelopDataInfo.p_recog_cushion_8 =  includeA.includeASeat8.tvValueA.text.toString()

        // 识别后靠背B面5组
        nowDevelopDataInfo.p_recog_back_1 =  includeB.includeB1.tvValueB.text.toString()
        nowDevelopDataInfo.p_recog_back_2 =  includeB.includeB2.tvValueB.text.toString()
        nowDevelopDataInfo.p_recog_back_3 =  includeB.includeB3.tvValueB.text.toString()
        nowDevelopDataInfo.p_recog_back_4 =  includeB.includeB4.tvValueB.text.toString()
        nowDevelopDataInfo.p_recog_back_5 =  includeB.includeB5.tvValueB.text.toString()

        ToastMsg("Adjust the pressure on B！")
        isSaveAData = true
        initUIFromB()
    }

    // 保存调整后的数据，并写入本地数据库
    fun saveAllData() {

        // 保存数据库！
        val developInfoDao = DevelopInfoDao(this)

        // 调节后靠背B面组
        nowDevelopDataInfo.p_adjust_cushion_1 =  includeB.includeB1.tvValueB.text.toString()
        nowDevelopDataInfo.p_adjust_cushion_2 =  includeB.includeB2.tvValueB.text.toString()
        nowDevelopDataInfo.p_adjust_cushion_3 =  includeB.includeB3.tvValueB.text.toString()
        nowDevelopDataInfo.p_adjust_cushion_4 =  includeB.includeB4.tvValueB.text.toString()
        nowDevelopDataInfo.p_adjust_cushion_5 =  includeB.includeB5.tvValueB.text.toString()
        // 调节后坐垫3组
        nowDevelopDataInfo.p_adjust_cushion_6 =  includeB.includeB6.tvValueB.text.toString()
        nowDevelopDataInfo.p_adjust_cushion_7 =  includeB.includeB7.tvValueB.text.toString()
        nowDevelopDataInfo.p_adjust_cushion_8 =  includeB.includeB8.tvValueB.text.toString()
        // 位置调节
        nowDevelopDataInfo.l_location = tvLocation.text.toString()
        // 数据类型
        nowDevelopDataInfo.dataType = DevelopDataInfo.DATA_TYPE_DEVELOP

        // 时间
        nowDevelopDataInfo.saveTime =  DateUtil.getNowDateTime()

        developInfoDao.insertSingleData(nowDevelopDataInfo)
        developInfoDao.closeDb()
        isSaveAData = false
        ToastMsg("Save successful！")

        initUIFromB()


    }

    /**
     * 控制气压
     * 先转换调压模式 A面normal，B面adjust
     * 再发送数据
     */
    private fun controlPressValueByTag(iTag : Int,iPressValue:Int) {
        iNowSelectNumber = iTag
        // 只调整B面的，所以将A面设为normal，B面设为adjust
        SocketThreadManager.sharedInstance(mContext)?.StartChangeModelByCan(CreateCtrDataHelper.getCtrModelAB(BaseVolume.COMMAND_CAN_MODEL_NORMAL,BaseVolume.COMMAND_CAN_MODEL_ADJUST))
        val strSendData = CreateCtrDataHelper.getCtrPressVaslueByNumber(iTag,iPressValue)
        SocketThreadManager.sharedInstance(mContext).StartSendDataByCan(strSendData)

    }

    /**
     * 隐藏软键盘
     */
    private fun hideSoftInput(token: IBinder?) {
        if (token != null) {
            val manager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            manager.hideSoftInputFromWindow(token, InputMethodManager.HIDE_NOT_ALWAYS)
        }
    }

    /** 初始化人体采集 */
    fun initNuralogixInfo() {
        val previewSize = ImagePreviewResolutionUtils.getFitImagePreviewResolution()
        IMAGE_WIDTH = previewSize.width.toFloat();
        IMAGE_HEIGHT = previewSize.height.toFloat();

        viewTracker = findViewById(R.id.tracker_opengl_view)
        trackerView = findViewById(R.id.tracker_ui_view)
        trackerView.setImageDimension(IMAGE_HEIGHT,IMAGE_WIDTH)
        trackerView.showMask(true)
        trackerView.setOnSizeChangedListener(this)

        val pref = getSharedPreferences(ConfigActivity.PREF_NAME, 0)
        MeasurementActivity.userToken = pref.getString(ConfigActivity.USER_TOKEN, null)
//        userToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJPcmdhbml6YXRpb25JRCI6ImYyMDA1ZGRiLTQ2NzEtNGY4Ny05MWFmLWFiYjkxZTczMDRmMSIsIklEIjoiMTE0ZDMyZDAtZDU0NS00NmQzLWIwMjYtM2RkODc4NDRiZTUwIiwiVHlwZSI6IlVzZXIiLCJEZXZpY2VJRCI6IjExYTdjNDA1LWIwMjAtNGFmZS04ODFhLTRkNTYwNDk3MDhmMCIsImV4cCI6MTU5MzA0MzIwMCwiaWF0IjoxNTg4ODM2NTgxLCJpc3MiOiJ1cm46ZGVlcGFmZmV4In0.1Q6pyvHEbgXLSU-YgKUAVh1_Vs2h4CVrOdfFt8nA5ig"
        // start pipeline
        if (MeasurementActivity.userToken != null) {
            DeepAffexDataSpec.REST_SERVER = MeasurementActivity.SAMPLE_REST_URL
            DeepAffexDataSpec.WS_SERVER = MeasurementActivity.SAMPLE_WS_URL
            DeepFXClient.getInstance().setTokenAuthorisation(MeasurementActivity.userToken)
            DeepFXClient.getInstance().connect()
            initPipeline()
        } else {
            Toast.makeText(this, resources.getString(R.string.Error_Server_Connection), Toast.LENGTH_LONG).show()
        }


    }

    private var IMAGE_WIDTH = 640.0f
    private var IMAGE_HEIGHT = 480.0f
    var MEASUREMENT_DURATION = 30.0
    var TOTAL_NUMBER_CHUNKS = 6
    private var faceIndex = 0
    private lateinit var core: Core
    private val constraintAverager = RollingConstraintAverager()
    private lateinit var render: Render
    private var viewTracker: GLSurfaceViewTracker? = null
    private val showHistogramAndRegions = true
    private lateinit var cloudAnalyzerListener: CloudAnalyzerListener
    private lateinit var trackerView: TrackerView
    private lateinit var dialog: AlertDialog
    private lateinit var cameraAdapter: CameraAdapter
    private lateinit var cloudAnalyzer: CloudAnalyzer
    private lateinit var cameraSource: VideoSource
    private lateinit var preprocessPipe: VideoPipe
    private lateinit var faceTrackerPipe: VideoPipe
    private lateinit var dfxPipe: DfxPipe
    private lateinit var signalAnalysisPipe: VideoPipe
    private lateinit var renderingVideoSink: RenderingVideoSink
    private lateinit var countdown: Countdown
    private lateinit var lastStatus: ConstraintResult.ConstraintStatus
    private var state = STATE.UNKNOWN
    private var firstFrameTimestamp = 0L
    private lateinit var lastConstraintReason: ConstraintResult.ConstraintReason
    val FACE_ENGINE_KEY = "face_engine"
    private fun initPipeline() {
        val faceIndex = intent.getIntExtra(MeasurementActivity.FACE_ENGINE_KEY, 0)
        core = Core.createAnuraCore(this)
        constraintAverager.setReasonSpan(60)
        val format = VideoFormat(VideoFormat.ColorFormat.BGRA, 30, MeasurementActivity.IMAGE_HEIGHT.toInt(), MeasurementActivity.IMAGE_WIDTH.toInt())

        //val videoFormat = VideoFormat(VideoFormat.VideoCodec.H264, VideoFormat.ColorFormat.BGRA, 30, IMAGE_HEIGHT.toInt(), IMAGE_WIDTH.toInt())

        render = Render.createGL20Render(format)
        viewTracker?.setRenderer(render as GLSurfaceView.Renderer)


        render.showFeatureRegion(showHistogramAndRegions)

        var visageFaceTracker: FaceTrackerAdapter = MNNFaceDetectorAdapter(baseContext)
        visageFaceTracker.setTrackingRegion(0, 0, MeasurementActivity.IMAGE_WIDTH.toInt(), MeasurementActivity.IMAGE_HEIGHT.toInt())

        cloudAnalyzerListener = object : CloudAnalyzerListener {
            override fun onStartAnalyzing() {
            }

            override fun onResult(result: AnalyzerResult) {
                val jsonResult = result.jsonResult
                AnuLogUtil.d(MeasurementActivity.TAG, "JSON result: $jsonResult index: ${result.resultIndex}")

                runOnUiThread {
                    // 人体数据： id & 信噪比 & 心跳 & 情绪值 & 低压 & 高压
                    val strPersonDataInfo =  "${result.measurementID}&${result.snr}&${result.heartRate}&${result.msi}&${result.bpDiastolic}&${result.bpSystolic}"
                    Loge("MenuSelectActivity","人体数据：id:${result.measurementID}&信噪比:${result.snr}&心跳:${result.heartRate}&情绪值:${result.msi}&低压:${result.bpDiastolic}&高压:${result.bpSystolic}&性别：${result.gender}")
                    tvXinLv.text = "${result.heartRate}"
                    tvHuXiLv.text = "0"
                    tvQingXu.text = "${result.msi}"
                    tvShuZhang.text = "${result.bpDiastolic}"
                    tvShouSuo.text = "${result.bpSystolic}"
                    if (result.resultIndex + 1 >= MeasurementActivity.TOTAL_NUMBER_CHUNKS) {
//                        Loge("MenuSelectActivity","人体数据：测量结束！开始计算身高体重")
                        stopMeasurement(true)
                    }
                }
            }

            override fun onError(error: AnuraError) {
                AnuLogUtil.e(MeasurementActivity.TAG, "CloudAnalyzerListener onError:" + error.name)
                runOnUiThread {
                    if (error == AnuraError.LOW_SNR) {
                        AnuLogUtil.e(MeasurementActivity.TAG, "SNR is less than 1, signal quality is not good")
                        Toast.makeText(baseContext, "SNR is less than 1, signal quality is not good", Toast.LENGTH_LONG).show()

                    }
                    stopMeasurement(true)
                }
            }
        }
        val dfxConfig = DfxPipeConfiguration(this, null)
        MeasurementActivity.TOTAL_NUMBER_CHUNKS = dfxConfig.getRuntimeParameterInt(DfxPipeConfiguration.RuntimeKey.TOTAL_NUMBER_CHUNKS, 6)
        val duration = MeasurementActivity.TOTAL_NUMBER_CHUNKS * dfxConfig.getRuntimeParameterFloat(DfxPipeConfiguration.RuntimeKey.DURATION_PER_CHUNK, 5f)
        MeasurementActivity.MEASUREMENT_DURATION = duration.toDouble()
        trackerView.setMeasurementDuration(duration.toDouble())

        cameraAdapter = CameraAdapter.createAndroidCamera2Adapter(intent.getStringExtra(CameraConfigurationFragment.CAMERA_ID_KEY), core, null)
        cloudAnalyzer = CloudAnalyzer.createCloudAnalyzer(core, DeepFXClient.getInstance(), cloudAnalyzerListener)
        cameraSource = VideoSource.createCameraSource("CameraSource", core, format, cameraAdapter)
        preprocessPipe = VideoPipe.createPreprocessPipe("PreprocessPipe", core, format)
        faceTrackerPipe = VideoPipe.createFaceTrackerPipe("FaceTrackerPipe", core, format, visageFaceTracker)
        renderingVideoSink = RenderingVideoSink.createRenderingVideoSink("RenderingSink", core, format, render)
        //dumpVideoSink = VideoSink.createDumpVideoSink("DumpVideoSink", core, format)


        val display = resources.displayMetrics
        val targetBox = ImageUtils.getFaceTargetBox(display.widthPixels, display.heightPixels, MeasurementActivity.IMAGE_HEIGHT.toInt(), MeasurementActivity.IMAGE_WIDTH.toInt())
        trackerView.setFaceTargetBox(targetBox)
        dfxConfig.setStartupParameter(DfxPipeConfiguration.StartupKey.BOX_CENTER_X_PCT, targetBox.boxCenterX_pct.toString())
        dfxConfig.setStartupParameter(DfxPipeConfiguration.StartupKey.BOX_CENTER_Y_PCT, targetBox.boxCenterY_pct.toString())
        dfxConfig.setStartupParameter(DfxPipeConfiguration.StartupKey.BOX_WIDTH_PCT, targetBox.boxWidth_pct.toString())
        dfxConfig.setStartupParameter(DfxPipeConfiguration.StartupKey.BOX_HEIGHT_PCT, targetBox.boxHeight_pct.toString())

        dfxPipe = DfxPipe.createDfxPipe("DfxPipe", core, format,
                core.createDFXFactory(getFilesDir().getAbsolutePath() + "/r21r23h-8.dat", "discrete")!!, dfxConfig.toJSONObject().toString(), cloudAnalyzer, this, renderingVideoSink)

        signalAnalysisPipe = VideoPipe.createVideoSignalAnalysisPipe("AnalysisPipe", core, format, this)

        cameraSource.connect(preprocessPipe)
        preprocessPipe.connect(signalAnalysisPipe)
        signalAnalysisPipe.connect(faceTrackerPipe)
        faceTrackerPipe.connect(dfxPipe)
        dfxPipe.connect(renderingVideoSink)
        //fileVideoSource.connect(renderingVideoSink)

        countdown = DefaultCountdown(3
                , object : Countdown.Listener {
            override fun onCountdownTick(value: Int) {
                runOnUiThread {
                    cancelled_tv.text = value.toString()
                }
            }

            override fun onCountdownEnd() {
                if (MeasurementActivity.userToken != null) {
                    runOnUiThread {
                        startMeasurement()
                    }
                } else {
                    runOnUiThread {
                        stopMeasurement(true)
                        countdown.start()
                        AnuLogUtil.d(MeasurementActivity.TAG, "No user token restart...")
                    }
                }
            }

            override fun onCountdownCancel() {

                runOnUiThread {
                    stopMeasurement(true)
                }
            }

        })

        trackerView.setMeasurementDuration(MeasurementActivity.MEASUREMENT_DURATION)
    }

    private fun startMeasurement() {
        if (lastStatus != ConstraintResult.ConstraintStatus.Good) {
            AnuLogUtil.e(MeasurementActivity.TAG, "lastStatus=" + lastStatus.name + " !=Good")
            state = STATE.IDLE
            return
        }
        cameraAdapter.lockExposure(true)
        cameraAdapter.lockWhiteBalance(true)
        cameraAdapter.lockFocus(true)
        firstFrameTimestamp = 0L
        trackerView.showMask(false)
        trackerView.showMeasurementProgress(true)
        trackerView.setMeasurementProgress(0.0f)

        state = STATE.MEASURING
        dfxPipe.startCollect("")
        cloudAnalyzer.startAnalyzing(MeasurementActivity.STUDY_ID, "")
        cancelled_tv.text = resources.getString(R.string.MEASUREMENT_STARTED)
    }

    private fun stopMeasurement(stopResult: Boolean) {
        AnuLogUtil.d(MeasurementActivity.TAG, "Stop measurement: $stopResult")
        if (!this::core.isInitialized) {
            return
        }
        MeasurementActivity.handler.removeCallbacksAndMessages(null)
        countdown.stop()
        cameraAdapter.lockWhiteBalance(false)
        cameraAdapter.lockExposure(false)
        cameraAdapter.lockFocus(false)

        render.showHistograms(false)
        render.showFeatureRegion(false)
        trackerView.showMask(true)
        trackerView.showMeasurementProgress(false)
        trackerView.setMeasurementProgress(0.0f)

        if (stopResult) {
            state = STATE.IDLE
            cloudAnalyzer.stopAnalyzing()
            dfxPipe.stopCollect()
            if (this::dialog.isInitialized && dialog.isShowing) {
                dialog.dismiss()
            }
        }
        cancelled_tv.text = ""
    }

    public enum class STATE {
        UNKNOWN,
        IDLE,
        RESTARTING,
        EXPOSURE,
        COUNTDOWN,
        MEASURING,
        DONE,
        CANCELED,
        NOT_SUPPORTED
    }

    private fun meanOfArray(jsonArr: JSONArray, multiplier: Double): Double {
        var meanValue = 0.0
        for (i in 0 until jsonArr.length()) {
            meanValue += jsonArr.getDouble(i)
        }

        meanValue /= jsonArr.length()
        meanValue /= multiplier

        return meanValue
    }


    private fun setCancelledReason(eReason: ConstraintResult.ConstraintReason) {

        var reason: String

        when (eReason) {
            ConstraintResult.ConstraintReason.UNKNOWN, ConstraintResult.ConstraintReason.FACE_NONE -> {
                reason = resources.getString(R.string.Message_Measurement_Default)
            }
            ConstraintResult.ConstraintReason.FACE_FAR ->
                reason = resources.getString(R.string.Warning_Measurement_Constraint_Distance_Too_Far)
            ConstraintResult.ConstraintReason.FACE_OFFTARGET ->
                reason = resources.getString(R.string.Warning_Measurement_Constraint_Distance_Face_Off_Target)
            ConstraintResult.ConstraintReason.FACE_DIRECTION ->
                reason = resources.getString(R.string.Warning_Measurement_Constraint_Position)
            ConstraintResult.ConstraintReason.FACE_MOVEMENT, ConstraintResult.ConstraintReason.CAMERA_MOVEMENT ->
                reason = resources.getString(R.string.Warning_Measurement_Constraint_Movement)
            ConstraintResult.ConstraintReason.LOW_FPS ->
                reason = resources.getString(R.string.Warning_Measurement_Constraint_FPS)
            ConstraintResult.ConstraintReason.IMAGE_BACKLIT ->
                reason = resources.getString(R.string.Warning_Measurement_Constraint_Backlight)
            ConstraintResult.ConstraintReason.IMAGE_DARK ->
                reason = resources.getString(R.string.Warning_Measurement_Constraint_Darkness)
            else -> reason = ""
        }

        if (!this::lastConstraintReason.isInitialized) {
            cameraAdapter.lockExposure(false)
            cameraAdapter.lockWhiteBalance(false)
            cameraAdapter.lockFocus(false)
        } else if (this::lastConstraintReason.isInitialized && lastConstraintReason != eReason) {
            AnuLogUtil.e(MeasurementActivity.TAG, reason)
            cameraAdapter.lockExposure(false)
            cameraAdapter.lockWhiteBalance(false)
            cameraAdapter.lockFocus(false)
        }
        lastConstraintReason = eReason

        runOnUiThread {
            cancelled_tv.text = reason
        }
    }

    override fun onResume() {
        super.onResume()
        if (this::core.isInitialized) {
            renderingVideoSink.start()
        }
        state = STATE.IDLE
        if (trackerView.visibility == View.GONE) {
            state = STATE.DONE
            cloudAnalyzer.stopAnalyzing()
            dfxPipe.stopCollect()
        }
    }

    override fun onNewIntent(intent: Intent?) {
        val configBundle = intent?.getBundleExtra(BundleUtils.DFX_BUNDLE_KEY)
        configBundle?.let {
            AnuLogUtil.d(MeasurementActivity.TAG, "on resume with bundle $configBundle")
            BundleUtils.updateRuntimeConfiguration(dfxPipe.configuration, configBundle)
            dfxPipe.updateRuntimeConfig()
            MeasurementActivity.TOTAL_NUMBER_CHUNKS = dfxPipe.configuration.getRuntimeParameterInt(DfxPipeConfiguration.RuntimeKey.TOTAL_NUMBER_CHUNKS, 6)
            val duration = MeasurementActivity.TOTAL_NUMBER_CHUNKS * dfxPipe.configuration.getRuntimeParameterFloat(DfxPipeConfiguration.RuntimeKey.DURATION_PER_CHUNK, 5f)
            MeasurementActivity.MEASUREMENT_DURATION = duration.toDouble()
            trackerView.setMeasurementDuration(duration.toDouble())
        }
        super.onNewIntent(intent)
    }

    override fun onPause() {
        super.onPause()
        if (state != STATE.UNKNOWN) {
            stopMeasurement(true)
            if (this::core.isInitialized) {
                renderingVideoSink.stop()
            }
        }
    }

    override fun onDCValuesReceived(dcResult: DCResult?) {
        if (state == STATE.EXPOSURE) {
            if (dcResult != null && !cameraAdapter.adjustExposure(dcResult.greenDC, 115.0f, 124.0f, 100)) {
                AnuLogUtil.d(MeasurementActivity.TAG, "Adjust exposure success.")
                countdown.start()
                state = STATE.COUNTDOWN
                runOnUiThread {
                    cancelled_tv.text = (resources.getString(R.string.PERFECT_HOLD_STILL))
                }
            } else if (dcResult == null) {
                AnuLogUtil.d(MeasurementActivity.TAG, "No dc value, no need to adjust exposure.")
                countdown.start()
                state = STATE.COUNTDOWN
                runOnUiThread {
                    cancelled_tv.text = (resources.getString(R.string.PERFECT_HOLD_STILL))
                }
            }
        }
    }

    override fun onHistogramReceived(regionCenters: MutableList<Point>?, histograms: FloatArray?) {
        if (null == regionCenters || null == histograms) {
            return
        }
        val centerArray = IntArray(regionCenters.size * 2)
        var i = 0
        for (point in regionCenters) {
            centerArray[i * 2 + 0] = point.x.toInt()
            centerArray[i * 2 + 1] = point.y.toInt()
            i++
        }
        runOnUiThread {
            trackerView.setHistograms(histograms, centerArray)
        }
    }

    override fun onConstraintReceived(status: ConstraintResult.ConstraintStatus, constraints: Map<String, ConstraintResult.ConstraintStatus>) {
        val eStatus = status
        for ((k, v) in constraints) {
            val reason = ConstraintResult.getConstraintReasonFromString(k)
            if (v != ConstraintResult.ConstraintStatus.Good) {
                constraintAverager.addReasonValue(reason.value)
            } else {
                constraintAverager.clearReason()
            }
        }

        val eReason = constraintAverager.maxOccurred

        if (this::lastStatus.isInitialized && lastStatus != eStatus) {
            AnuLogUtil.v(MeasurementActivity.TAG, "Constraints Status: $eStatus Reason: $eReason State: $state")
        }
        lastStatus = eStatus

        if (eStatus == ConstraintResult.ConstraintStatus.Good) {
            runOnUiThread {
                if (state == STATE.IDLE) {
                    render.showFeatureRegion(showHistogramAndRegions)
                    render.showHistograms(showHistogramAndRegions)
                    state = STATE.EXPOSURE
                }
            }
        } else if (eStatus == ConstraintResult.ConstraintStatus.Error) {
            setCancelledReason(ConstraintResult.ConstraintReason.values()[eReason])
            if (state == STATE.MEASURING) {
                runOnUiThread {
                    stopMeasurement(true)
                }
            } else if (state == STATE.COUNTDOWN) {
                cameraAdapter.lockWhiteBalance(false)
                cameraAdapter.lockExposure(false)
                cameraAdapter.lockFocus(false)
                countdown.cancel()
                state = STATE.IDLE
            } else {
                cameraAdapter.lockWhiteBalance(false)
                cameraAdapter.lockExposure(false)
                state = STATE.IDLE
            }
        } else {
            if (state == STATE.MEASURING) {
                runOnUiThread {
                    stopMeasurement(true)
                }
            }
            setCancelledReason(ConstraintResult.ConstraintReason.values()[eReason])
        }
    }

    override fun onChunkPayloadReceived(payload: ChunkPayload, collector: Collector) {
        AnuLogUtil.d(MeasurementActivity.TAG, "Receives chunk payload: ${payload.chunkNumber}")
        runOnUiThread {
            cancelled_tv.text = resources.getString(R.string.Analyzing_Data) + " " + (payload.chunkNumber + 1)
        }
    }

    override fun onFrameRateEvent(frameNumber: Long, frameRate: Float, frameTimestamp: Long) {
        if (state == STATE.MEASURING) {
            runOnUiThread {
                if (state == STATE.MEASURING) {
                    if (firstFrameTimestamp == 0L) {
                        firstFrameTimestamp = frameTimestamp
                    }
                    val totalFrameDuration = (frameTimestamp - firstFrameTimestamp) / 1000000000.0f
                    val progressPercent = totalFrameDuration * 100 / MeasurementActivity.MEASUREMENT_DURATION
                    if (progressPercent >= 100.0) {
                        state = STATE.DONE
                        stopMeasurement(false)
                        if (this::dialog.isInitialized && dialog.isShowing) {
                            dialog.dismiss()
                        }
                        val builder: AlertDialog.Builder? = mContext.let {
                            AlertDialog.Builder(it)
                        }
                        builder?.setMessage("Wait for final result...")?.setCancelable(false)
                        dialog = builder?.create()!!
                        dialog.show()
                    }
                    trackerView.setMeasurementProgress(progressPercent.toFloat())
                    if (frameNumber % 10 == 0L) {
                        AnuLogUtil.d(MeasurementActivity.TAG, "Frame number: $frameNumber total frame length: $totalFrameDuration seconds fps: $frameRate")
                    }
                }
            }
        } else {
            if (frameNumber % 10 == 0L) {
                AnuLogUtil.d(MeasurementActivity.TAG, "Frame number: $frameNumber seconds fps: $frameRate")
            }
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        AnuLogUtil.d(MeasurementActivity.TAG, "onSizeChanged w=" + w + " h=" + h)
        if (!this::dfxPipe.isInitialized) {
            return
        }
        val targetBox = ImageUtils.getFaceTargetBox(w, h, MeasurementActivity.IMAGE_HEIGHT.toInt(), MeasurementActivity.IMAGE_WIDTH.toInt())
        dfxPipe.configuration.setStartupParameter(DfxPipeConfiguration.StartupKey.BOX_CENTER_X_PCT, targetBox.boxCenterX_pct.toString())
        dfxPipe.configuration.setStartupParameter(DfxPipeConfiguration.StartupKey.BOX_CENTER_Y_PCT, targetBox.boxCenterY_pct.toString())
        dfxPipe.configuration.setStartupParameter(DfxPipeConfiguration.StartupKey.BOX_WIDTH_PCT, targetBox.boxWidth_pct.toString())
        dfxPipe.configuration.setStartupParameter(DfxPipeConfiguration.StartupKey.BOX_HEIGHT_PCT, targetBox.boxHeight_pct.toString())
        dfxPipe.updateStartUpConfig()
        trackerView.setFaceTargetBox(targetBox)
    }

    /** 释放和终止摄像头控件 */
    private fun destoryCamera() {
        AnuLogUtil.d(MeasurementActivity.TAG, "onDestroy")

        var closeStartTime = SystemClock.elapsedRealtime()
        trackerView.destoryView()
        if (state != STATE.UNKNOWN && this::core.isInitialized) {

            core.close()
            var closeStep0Time = SystemClock.elapsedRealtime()
            AnuLogUtil.d(MeasurementActivity.TAG, "Close step 0 consuming: ${closeStep0Time - closeStartTime}")
            cameraSource.close()
            var closeStep1Time = SystemClock.elapsedRealtime()
            AnuLogUtil.d(MeasurementActivity.TAG, "Close step 1 consuming: ${closeStep1Time - closeStep0Time}")
            preprocessPipe.close()
            faceTrackerPipe.close()
            var closeStep2Time = SystemClock.elapsedRealtime()
            AnuLogUtil.d(MeasurementActivity.TAG, "Close step 1 consuming: ${closeStep2Time - closeStep1Time}")
            dfxPipe.close()
            var closeStep3Time = SystemClock.elapsedRealtime()
            AnuLogUtil.d(MeasurementActivity.TAG, "Close step 2 consuming: ${closeStep3Time - closeStep2Time}")
            renderingVideoSink.close()
            var closeStep4Time = SystemClock.elapsedRealtime()
            AnuLogUtil.d(MeasurementActivity.TAG, "Close step 4 consuming: ${closeStep4Time - closeStep3Time}")
        }
        if (this::dialog.isInitialized && dialog.isShowing) {
            dialog.dismiss()
        }

        var closeStep5Time = SystemClock.elapsedRealtime()
        AnuLogUtil.d(MeasurementActivity.TAG, "Close step 5 consuming: ${closeStep5Time - closeStartTime}")
    }

    override fun onDestroy() {
        super.onDestroy()

        BaseVolume.strSensorInitValue = edAInitValue.text.toString()
        BaseVolume.strSeatInitValue = edSeatInitValue.text.toString()
        BaseVolume.strAdjustInitialValue = edBInitValue.text.toString()

        unregisterReceiver(myNetReceiver)

        destoryCamera()

    }



}