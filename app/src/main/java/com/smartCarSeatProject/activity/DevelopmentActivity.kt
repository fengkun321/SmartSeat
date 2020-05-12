package com.smartCarSeatProject.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.IBinder
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
import com.smartCarSeatProject.tcpInfo.SocketThreadManager
import com.smartCarSeatProject.utl.DateUtil
import com.smartCarSeatProject.view.AddMenuWindowDialog
import kotlinx.android.synthetic.main.layout_b_dmcm.*
import kotlinx.android.synthetic.main.layout_b_dmcm.view.rlLocation


class DevelopmentActivity: BaseActivity(),View.OnClickListener{

    var seekBarList :MutableList<SeekBar> = ArrayList()
    var tvBValueList : MutableList<TextView> = ArrayList()
    var tvAValueList : MutableList<TextView> = ArrayList()
    var tvAOtherValueList : MutableList<TextView> = ArrayList()
    var tvJianList : MutableList<Button> = ArrayList()
    var tvJiaList : MutableList<Button> = ArrayList()
    var setValueDialog : SetValueAreaAddWindow? = null
    var nowDevelopDataInfo = DevelopDataInfo()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_develop)

        initUI()
        reciverBand()

        if (SocketThreadManager.sharedInstance(this@DevelopmentActivity)?.isTCPAllConnected()!!) {
            tvReCanConnect.visibility = View.GONE
            tvReDeviceConnect.visibility = View.GONE
        }
        else {
            tvReCanConnect.visibility = View.GONE
            tvReDeviceConnect.visibility = View.GONE
            if (!SocketThreadManager.sharedInstance(this@DevelopmentActivity)?.isCanConnected()!!)
                tvReCanConnect.visibility = View.VISIBLE
            if (!SocketThreadManager.sharedInstance(this@DevelopmentActivity)?.isDeviceConnected()!!)
                tvReDeviceConnect.visibility = View.VISIBLE
        }

    }

    fun initUI() {

        edAInitValue.setText(BaseVolume.strSensorInitValue)
        edSeatInitValue.setText(BaseVolume.strSeatInitValue)
        edBInitValue.setText(BaseVolume.strAdjustInitialValue)
        edWeight.setText(BaseVolume.strWeight)
        edHeight.setText(BaseVolume.strHight)

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

        includeA.includeASeat1.tvTitle.text = "1"
        includeA.includeASeat2.tvTitle.text = "2"
        includeA.includeASeat3.tvTitle.text = "3"

        // A面的部分参数值(11个。座垫3+靠背左测8)
        tvAValueList.add(includeA.includeASeat1.tvValueA)
        tvAValueList.add(includeA.includeASeat2.tvValueA)
        tvAValueList.add(includeA.includeASeat3.tvValueA)
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

            iTag += 1

        }

        seekBarList.forEach {
            it.progress = 2000
        }

        tvReCanConnect.setOnClickListener(this)
        tvReDeviceConnect.setOnClickListener(this)

        imgBack.setOnClickListener(this)
        btnInitValue.setOnClickListener(this)
        btnSaveA.setOnClickListener(this)
        btnSaveAllData.setOnClickListener(this)
        btnHistory.setOnClickListener(this)
        llParent.setOnClickListener(this)
        rlLocation.setOnClickListener(this)
        rlMassageMode.setOnClickListener(this)

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
        myIntentFilter.addAction(BaseVolume.BROADCAST_TCP_INFO)
        myIntentFilter.addAction(BaseVolume.BROADCAST_TCP_INFO_CAN)
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
        val strSendData = CreateCtrDataHelper.getCtrPressVaslueByNumber(iTag,iNowValue)
        SocketThreadManager.sharedInstance(this@DevelopmentActivity).StartSendData(strSendData)
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
        val strSendData = CreateCtrDataHelper.getCtrPressVaslueByNumber(iTag,iNowValue)
        SocketThreadManager.sharedInstance(this@DevelopmentActivity).StartSendData(strSendData)

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
            iTag += 1
            val strSendData = CreateCtrDataHelper.getCtrPressVaslueByNumber(iTag,iProcess)
            SocketThreadManager.sharedInstance(this@DevelopmentActivity).StartSendData(strSendData)
        }
    }

    /** 提示语 */
    fun showHintDialog(strTitle:String,strContent:String,strName:String) {
        val areaAddWindowHint = AreaAddWindowHint(this,R.style.Dialogstyle,strTitle,
                object : AreaAddWindowHint.PeriodListener {
                    override fun refreshListener(string: String) {
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
                // 座椅状态
                if (strType.equals(BaseVolume.COMMAND_TYPE_SEAT_STATUS,true)) {
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
                // 座椅恢复到初始气压值
                else if (strType.equals(BaseVolume.COMMAND_INIT_VALUE_BY_DEVELOP,true)) {
                    includeA.tvInitValue.text = "Recovery is complete！"
                    includeA.tvInitValue.setTextColor(resources.getColor(R.color.colorGreen))

                    // 保存体征按钮可用！
                    btnSaveA.isEnabled = true
                    btnSaveA.setTextColor(resources.getColor(R.color.colorWhite))

                }
            }
            // 控制回调
            else if (action == BaseVolume.BROADCAST_CTR_CALLBACK) {
                val strType = intent.getStringExtra(BaseVolume.BROADCAST_TYPE)
                val strMsg = intent.getStringExtra(BaseVolume.BROADCAST_MSG)
                // 停止定时发送
                SocketThreadManager.sharedInstance(this@DevelopmentActivity)?.StopSendData()
            }
            else if (action == BaseVolume.BROADCAST_TCP_INFO) {
                val strType = intent.getStringExtra(BaseVolume.BROADCAST_TYPE)
                // 开始连接
                if (strType.equals(BaseVolume.BROADCAST_TCP_CONNECT_START)) {
                    loadingDialog?.show()
                    ToastMsg("Connecting....")
                }
                // 连接结果
                else if (strType.equals(BaseVolume.BROADCAST_TCP_CONNECT_CALLBACK)) {
                    val isConnected = intent.getBooleanExtra(BaseVolume.BROADCAST_TCP_STATUS,false)
                    val strMsg = intent.getStringExtra(BaseVolume.BROADCAST_MSG)
                    loadingDialog?.dismiss()
                    if (!isConnected) {
                        tvReDeviceConnect.visibility = View.VISIBLE
                        ToastMsg("Connect Fail！$strMsg")
                    }
                    else {
                        tvReDeviceConnect.visibility = View.GONE
                        if (SocketThreadManager.sharedInstance(this@DevelopmentActivity)?.isCanConnected()!!) {
                            tvReCanConnect.visibility = View.GONE
                        }
                        else {
                            tvReCanConnect.visibility = View.VISIBLE
                        }
                        ToastMsg("Connection successful！")
                    }
                }
            }
            else if (action == BaseVolume.BROADCAST_TCP_INFO_CAN) {
                val strType = intent.getStringExtra(BaseVolume.BROADCAST_TYPE)
                // 开始连接
                if (strType.equals(BaseVolume.BROADCAST_TCP_CONNECT_START)) {
                    ToastMsg("Can,Connecting！")
                }
                // 连接结果
                else if (strType.equals(BaseVolume.BROADCAST_TCP_CONNECT_CALLBACK)) {
                    val isConnected = intent.getBooleanExtra(BaseVolume.BROADCAST_TCP_STATUS, false)
                    val strMsg = intent.getStringExtra(BaseVolume.BROADCAST_MSG)

                    if (!isConnected) {
                        tvReCanConnect.visibility = View.VISIBLE
                        ToastMsg("Can Connect Fail！$strMsg")
                    }
                    else {
                        tvReCanConnect.visibility = View.GONE
                        if (SocketThreadManager.sharedInstance(this@DevelopmentActivity)?.isDeviceConnected()!!) {
                            tvReDeviceConnect.visibility = View.GONE
                        }
                        else {
                            tvReDeviceConnect.visibility = View.VISIBLE
                        }
                        ToastMsg("Can Connection successful！")
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
            R.id.tvReDeviceConnect ->
                SocketThreadManager.sharedInstance(this@DevelopmentActivity)?.createDeviceSocket()
            R.id.tvReCanConnect ->
                SocketThreadManager.sharedInstance(this@DevelopmentActivity)?.createCanSocket()
            R.id.rlLocation ->
                deviceSelectMenu(true,tvLocation.text.toString(),locationList)
            R.id.rlMassageMode ->
                deviceSelectMenu(false,tvMassageMode.text.toString(),massageModeList)

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
                    tvMassageMode.text = strItem
                }
            }
        })
        dialog1.show()

    }

    /** 位置调节 */
    fun setLocationData (number: Int) {
        when(number) {
            0 ->
                SocketThreadManager.sharedInstance(mContext).StartSendDataByCan(BaseVolume.COMMAND_CAN_LOCATION_1)
            1 ->
                SocketThreadManager.sharedInstance(mContext).StartSendDataByCan(BaseVolume.COMMAND_CAN_LOCATION_2)
            2 ->
                SocketThreadManager.sharedInstance(mContext).StartSendDataByCan(BaseVolume.COMMAND_CAN_LOCATION_3)
            3 ->
                SocketThreadManager.sharedInstance(mContext).StartSendDataByCan(BaseVolume.COMMAND_CAN_LOCATION_4)
            4 ->
                SocketThreadManager.sharedInstance(mContext).StartSendDataByCan(BaseVolume.COMMAND_CAN_LOCATION_5)
            5 ->
                SocketThreadManager.sharedInstance(mContext).StartSendDataByCan(BaseVolume.COMMAND_CAN_LOCATION_6)
        }
        // 每次发完位置后，要再发一条清零指令才会动作
        SocketThreadManager.sharedInstance(mContext).StartSendDataByCan(BaseVolume.COMMAND_CAN_LOCATION_0)

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
        // 发指令，设置16个气压
        val NowSendDataList = CreateCtrDataHelper.getAllPressValueBy16(strVA,strVSeat,strVB)
        NowSendDataList.forEach {
            SocketThreadManager.sharedInstance(this@DevelopmentActivity)?.StartSendData(it)
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

        nowDevelopDataInfo?.initData()
        //性别
        val sex =  findViewById<RadioButton>(rgSex.checkedRadioButtonId)
        //国家
        val nation = findViewById<RadioButton>(rgRace.checkedRadioButtonId)

        // 名称
        nowDevelopDataInfo?.strName = edPS.text.toString()
        //性别
        nowDevelopDataInfo?.m_gender   = sex.text.toString()
        //国家
        nowDevelopDataInfo?.m_national = nation.text.toString()
        // 人员-体重
        nowDevelopDataInfo?.m_weight =  edWeight.text.toString()
        // 人员-身高
        nowDevelopDataInfo?.m_height =  edHeight.text.toString()
        // 备注
        nowDevelopDataInfo?.strPSInfo =  edPS.text.toString()
        // 时间
        nowDevelopDataInfo?.saveTime =  DateUtil.getNowDateTime()

        // 初始化A面8组气压
        nowDevelopDataInfo?.p_init_back_A = strVA
        // 初始化靠背B面5组气压
        nowDevelopDataInfo?.p_init_back_B = strVB
        // 初始化坐垫3组气压
        nowDevelopDataInfo?.p_init_cushion = strVSeat

        // 识别后靠背A面8组
        nowDevelopDataInfo?.p_recog_back_A =  includeA.includeAAL.tvValueA.text.toString()
        nowDevelopDataInfo?.p_recog_back_B =  includeA.includeABL.tvValueA.text.toString()
        nowDevelopDataInfo?.p_recog_back_C =  includeA.includeACL.tvValueA.text.toString()
        nowDevelopDataInfo?.p_recog_back_D =  includeA.includeADL.tvValueA.text.toString()
        nowDevelopDataInfo?.p_recog_back_E =  includeA.includeAEL.tvValueA.text.toString()
        nowDevelopDataInfo?.p_recog_back_F =  includeA.includeAFL.tvValueA.text.toString()
        nowDevelopDataInfo?.p_recog_back_G =  includeA.includeAGL.tvValueA.text.toString()
        nowDevelopDataInfo?.p_recog_back_H =  includeA.includeAHL.tvValueA.text.toString()

        // 识别后坐垫3组
        nowDevelopDataInfo?.p_recog_cushion_1 =  includeA.includeASeat1.tvValueA.text.toString()
        nowDevelopDataInfo?.p_recog_cushion_2 =  includeA.includeASeat2.tvValueA.text.toString()
        nowDevelopDataInfo?.p_recog_cushion_3 =  includeA.includeASeat3.tvValueA.text.toString()

        // 识别后靠背B面5组
        nowDevelopDataInfo?.p_recog_back_4 =  includeB.includeB4.tvValueB.text.toString()
        nowDevelopDataInfo?.p_recog_back_5 =  includeB.includeB5.tvValueB.text.toString()
        nowDevelopDataInfo?.p_recog_back_6 =  includeB.includeB6.tvValueB.text.toString()
        nowDevelopDataInfo?.p_recog_back_7 =  includeB.includeB7.tvValueB.text.toString()
        nowDevelopDataInfo?.p_recog_back_8 =  includeB.includeB8.tvValueB.text.toString()

        ToastMsg("Adjust the pressure on B！")
        isSaveAData = true
        initUIFromB()
    }

    // 保存调整后的数据，并写入本地数据库
    fun saveAllData() {

        // 保存数据库！
        val developInfoDao = DevelopInfoDao(this)

        // 调节后坐垫3组
        nowDevelopDataInfo?.p_adjust_cushion_1 =  includeB.includeB1.tvValueB.text.toString()
        nowDevelopDataInfo?.p_adjust_cushion_2 =  includeB.includeB2.tvValueB.text.toString()
        nowDevelopDataInfo?.p_adjust_cushion_3 =  includeB.includeB3.tvValueB.text.toString()

        // 调节后靠背B面组
        nowDevelopDataInfo?.p_adjust_cushion_4 =  includeB.includeB4.tvValueB.text.toString()
        nowDevelopDataInfo?.p_adjust_cushion_5 =  includeB.includeB5.tvValueB.text.toString()
        nowDevelopDataInfo?.p_adjust_cushion_6 =  includeB.includeB6.tvValueB.text.toString()
        nowDevelopDataInfo?.p_adjust_cushion_7 =  includeB.includeB7.tvValueB.text.toString()
        nowDevelopDataInfo?.p_adjust_cushion_8 =  includeB.includeB8.tvValueB.text.toString()
        // 位置调节
        nowDevelopDataInfo?.l_location = tvLocation.text.toString()
        // 按摩模式
        nowDevelopDataInfo?.m_massage = tvMassageMode.text.toString()
        // 数据类型
        nowDevelopDataInfo?.dataType = DevelopDataInfo.DATA_TYPE_DEVELOP

        developInfoDao.insertSingleData(nowDevelopDataInfo)
        developInfoDao.closeDb()
        isSaveAData = false
        ToastMsg("Save successful！")

        initUIFromB()


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

    override fun onDestroy() {
        super.onDestroy()

        BaseVolume.strSensorInitValue = edAInitValue.text.toString()
        BaseVolume.strSeatInitValue = edSeatInitValue.text.toString()
        BaseVolume.strAdjustInitialValue = edBInitValue.text.toString()
        BaseVolume.strWeight = edWeight.text.toString()
        BaseVolume.strHight = edHeight.text.toString()

        unregisterReceiver(myNetReceiver)

    }



}