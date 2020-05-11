package com.smartCarSeatProject.activity

import android.app.LocalActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import com.smartCarSeatProject.R
import com.smartCarSeatProject.data.BaseVolume
import com.smartCarSeatProject.data.DataAnalysisHelper
import com.smartCarSeatProject.data.DeviceWorkInfo
import com.smartCarSeatProject.data.SeatStatus
import com.smartCarSeatProject.tcpInfo.SocketThreadManager
import com.smartCarSeatProject.view.AreaAddWindowHint
import com.smartCarSeatProject.view.ProgressBarWindowHint
import com.smartCarSeatProject.view.SureOperWindowHint
import kotlinx.android.synthetic.main.layout_control.*
import java.util.ArrayList
import java.util.HashMap


class MainControlActivity : BaseActivity(),View.OnClickListener{

    var Tag = ""
    var mactivityManager: LocalActivityManager? = null
    // 当前选项
    var NowShowViewNumber = 1
    var hashMapViews = HashMap<String, View>()
    var oldKeyList: MutableList<String> = ArrayList()

    // 设置过的通道的值
    var setValueBufferByChannel = HashMap<Int,String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_control)

        NowShowViewNumber = intent.getIntExtra("iNumber",1)
        Tag = this.localClassName
        mainControlActivity = this

        mactivityManager = LocalActivityManager(this, true)
        mactivityManager!!.dispatchCreate(savedInstanceState)

        initUI()
        reciverBand()

        switchActivityByNumber(NowShowViewNumber)

        // Number = 3，wifi设置界面
//        if (NowShowViewNumber == 3)
//            switchActivityByNumber(NowShowViewNumber)
//        // 如果处于某种模式，则根据座椅状态，显示不同提醒或界面
//        else
//            checkSeatStatus()

    }

    /**
     * 同级函数，类似于静态属性
     */
    companion object {
        private var mainControlActivity : MainControlActivity? = null
        fun getInstance():MainControlActivity?{
            return mainControlActivity
        }
    }

    fun initUI() {
        tvReCanConnect.setOnClickListener(this)
        tvReDeviceConnect.setOnClickListener(this)
        imgClose.setOnClickListener(this)
        imgReset.setOnClickListener(this)
        imgLeft0.setOnClickListener(this)
        imgLeft1.setOnClickListener(this)
        imgLeft2.setOnClickListener(this)
        imgLeft3.setOnClickListener(this)
        imgLeft4.setOnClickListener(this)

        if (SocketThreadManager.sharedInstance(this@MainControlActivity)?.isTCPAllConnected()!!) {
            imgWIFI.visibility = View.VISIBLE
            imgReset.visibility = View.VISIBLE
            tvReCanConnect.visibility = View.GONE
            tvReDeviceConnect.visibility = View.GONE
            imgLeft4.setImageResource(R.drawable.img_left_4)
            imgLeft4.isEnabled = true
        }
        else {

            tvReCanConnect.visibility = View.GONE
            tvReDeviceConnect.visibility = View.GONE
            imgWIFI.visibility = View.GONE
            imgReset.visibility = View.GONE

            if (!SocketThreadManager.sharedInstance(this@MainControlActivity)?.isCanConnected()!!)
                tvReCanConnect.visibility = View.VISIBLE
            if (!SocketThreadManager.sharedInstance(this@MainControlActivity)?.isDeviceConnected()!!)
                tvReDeviceConnect.visibility = View.VISIBLE

            imgLeft4.setImageResource(R.drawable.img_left_4_hui)
            imgLeft4.isEnabled = false
        }


    }

    /** 监听广播  */
    private fun reciverBand() {
        val myIntentFilter = IntentFilter()
        myIntentFilter.addAction(BaseVolume.BROADCAST_TCP_INFO)
        myIntentFilter.addAction(BaseVolume.BROADCAST_TCP_INFO_CAN)
        myIntentFilter.addAction(BaseVolume.BROADCAST_SEND_INFO)
        myIntentFilter.addAction(BaseVolume.BROADCAST_RESULT_DATA_INFO)

        // 注册广播
        registerReceiver(myNetReceiver, myIntentFilter)
    }

    override fun onClick(p0: View?) {
        when(p0?.id) {
            R.id.imgClose -> {
                val areaAddWindowHint = AreaAddWindowHint(this,R.style.Dialogstyle,"System",
                        object : AreaAddWindowHint.PeriodListener {
                            override fun refreshListener(string: String) {
                                finish()
                                sendBroadcast(Intent(BaseVolume.BROADCAST_FINISH_APPLICATION))
                            }
                        },"Are you sure to exit the application?",false)
                areaAddWindowHint?.show()
            }
            R.id.imgReset -> {
                val areaAddWindowHint = AreaAddWindowHint(this,R.style.Dialogstyle,"System",
                        object : AreaAddWindowHint.PeriodListener {
                            override fun refreshListener(string: String) {
                                finish()
                                sendBroadcast(Intent(BaseVolume.BROADCAST_RESET_ACTION))
//                                SocketThreadManager.sharedInstance(this@MainControlActivity)?.StartSendData(BaseVolume.COMMAND_SET_STATUS_RESET)
                            }
                        },"Are you sure to reset?",false)
                areaAddWindowHint?.show()
            }
            R.id.imgLeft0 ->
                finish()
            R.id.imgLeft1 -> {
                // 已经在自动模式下，则直接进入
                if (DataAnalysisHelper.deviceState.seatStatus == SeatStatus.press_automatic.iValue ||
                        DataAnalysisHelper.deviceState.seatStatus == SeatStatus.press_reserve.iValue) {
                    switchActivityByNumber(1)
                }
                else
                    SocketThreadManager.sharedInstance(this@MainControlActivity)?.StartSendData(BaseVolume.COMMAND_SET_MODE_AUTO)
//                switchActivity(1)
            }
            R.id.imgLeft2 -> {
                // 已经在手动模式下，则直接进入
                if (DataAnalysisHelper.deviceState.seatStatus == SeatStatus.press_automatic_manual.iValue) {
                    switchActivityByNumber(2)
                }
                else
                    SocketThreadManager.sharedInstance(this@MainControlActivity)?.StartSendData(BaseVolume.COMMAND_SET_MODE_MANUAL)
//                switchActivity(2)
            }
            R.id.imgLeft3 -> {
                switchActivityByNumber(3)
            }

            R.id.imgLeft4 -> {
                // 已经在开发者模式下，则直接进入
                if (DataAnalysisHelper.deviceState.seatStatus == SeatStatus.develop.iValue) {
                    val intent = Intent()
                    intent.setClass(this@MainControlActivity,DevelopmentActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                else {
                    SocketThreadManager.sharedInstance(this@MainControlActivity)?.StartSendData(BaseVolume.COMMAND_SET_MODE_DEVELOP)
                }
            }

            R.id.tvReCanConnect -> {
                SocketThreadManager.sharedInstance(this@MainControlActivity)?.createCanSocket()
            }
            R.id.tvReDeviceConnect -> {
                SocketThreadManager.sharedInstance(this@MainControlActivity)?.createDeviceSocket()
            }

        }

    }

    private fun switchActivityByNumber(number: Int) {

        NowShowViewNumber = number

        imgLeft3.setImageResource(R.drawable.img_left_3_false)

        var keyActivity = ""
        val intent1 = Intent()
        when (number) {
            1 -> {

                llAllLogo.visibility = View.GONE

                imgLeft1.setImageResource(R.drawable.img_left_1)
                keyActivity = "AutomaticActivity"
                intent1.setClass(this, AutomaticActivity::class.java!!)

                // 座椅已经达到预设值，提示用户调整坐姿！
                if (DataAnalysisHelper.deviceState.seatStatus == SeatStatus.press_reserve.iValue){

                    areaSeatWindowHint?.dismiss()
                    progressBarWindowHint?.onSelfDismiss()

                    ToastMsg("Seat initialization is complete！")
                    loadingDialog?.dismiss()

                    // 按钮1亮，其他的都灰且不能点
                    imgLeft1.setImageResource(R.drawable.img_left_1)
                    imgLeft2.setImageResource(R.drawable.img_left_2_hui)
                    imgLeft1.isEnabled = true
                    imgLeft2.isEnabled = false
                    startCheckPeopleWindowHint?.show()

                }

            }
            2 -> {

                llAllLogo.visibility = View.GONE

                setValueBufferByChannel.clear()
                imgLeft1.setImageResource(R.drawable.img_left_1_false)
                imgLeft2.setImageResource(R.drawable.img_left_2)
                keyActivity = "ManualActivity"
                intent1.setClass(this, ManualActivity::class.java!!)
            }
            3 -> {
                llAllLogo.visibility = View.GONE
                // 状态未达到预设值，提示用户等待
                if (DataAnalysisHelper.deviceState.seatStatus < SeatStatus.press_reserve.iValue) {
                    imgLeft1.setImageResource(R.drawable.img_left_1_hui)
                    imgLeft2.setImageResource(R.drawable.img_left_2_hui)
                    imgLeft1.isEnabled = false
                    imgLeft2.isEnabled = false
                }
                // 座椅已达到预设值 或 正在检测
                else if (DataAnalysisHelper.deviceState.seatStatus == SeatStatus.press_reserve.iValue
                        || DataAnalysisHelper.deviceState.seatStatus == SeatStatus.press_auto_probe.iValue){
                    imgLeft1.setImageResource(R.drawable.img_left_1_false)
                    imgLeft2.setImageResource(R.drawable.img_left_2_hui)
                    imgLeft1.isEnabled = true
                    imgLeft2.isEnabled = false
                }
                // 座椅自动或手动模式
                else if (DataAnalysisHelper.deviceState.seatStatus == SeatStatus.press_automatic.iValue ||
                        DataAnalysisHelper.deviceState.seatStatus == SeatStatus.press_automatic_manual.iValue){
                    imgLeft1.setImageResource(R.drawable.img_left_1_false)
                    imgLeft2.setImageResource(R.drawable.img_left_2_false)
                    imgLeft1.isEnabled = true
                    imgLeft2.isEnabled = true
                }
                // 开发者模式
                else if (DataAnalysisHelper.deviceState.seatStatus == SeatStatus.develop.iValue) {
                    imgLeft1.setImageResource(R.drawable.img_left_1_false)
                    imgLeft2.setImageResource(R.drawable.img_left_2_hui)
                    imgLeft1.isEnabled = true
                    imgLeft2.setImageResource(R.drawable.img_left_2_false)
                    imgLeft2.isEnabled = true
                }
                imgLeft3.setImageResource(R.drawable.img_left_3)
                keyActivity = "SetWifiActivity"
                intent1.setClass(this, SetWifiActivity::class.java!!)
            }
            4 -> {
                imgLeft4.setImageResource(R.drawable.img_left_4)
                keyActivity = "DevelopmentActivity"
                intent1.setClass(this, DevelopmentActivity::class.java!!)
            }

        }

        oldKeyList.clear()
        //		intent1.putExtra(BaseVolume.DEVICE, nowDevice);
        intent1.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        var nowView: View? = hashMapViews.get(keyActivity)
        if (nowView == null) {
            nowView = mactivityManager?.startActivity(keyActivity, intent1)?.decorView
        }
        nowView?.let { hashMapViews.put(keyActivity, it) }
        container.removeAllViews()
        container.addView(hashMapViews.get(keyActivity), ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT)

    }

    /**
     * 返回上一页
     * @param key 当前页面
     */
    fun ReturnBack(key: String) {
        hashMapViews.remove(key)
        mactivityManager?.destroyActivity(key, true)
        container.removeAllViews()
        val oldKey = oldKeyList.get(oldKeyList.size - 1)
        container.addView(hashMapViews.get(oldKey), ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT)
        oldKeyList.removeAt(oldKeyList.size - 1)
    }

    /**
     * 跳转页面
     * @param fromAct 从当前界面
     * @param toAct   到指定界面
     * @param intent
     */
    fun GotoNewActivity(fromAct: String, toAct: String, intent: Intent) {
        val view01 = mactivityManager?.startActivity(toAct, intent)?.decorView
        oldKeyList.add(fromAct)
        view01?.let { hashMapViews.put(toAct, it) }
        container.removeAllViews()
        container.addView(hashMapViews.get(toAct), ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT)
    }

    override fun onResume() {
        super.onResume()
        mactivityManager?.dispatchResume()

    }

    /****
     * 广播监听
     */
    private val myNetReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (action == BaseVolume.BROADCAST_TCP_INFO) {
                val strType = intent.getStringExtra(BaseVolume.BROADCAST_TYPE)
                // 开始连接
                if (strType.equals(BaseVolume.BROADCAST_TCP_CONNECT_START)) {
                    loadingDialog?.show()
                    ToastMsg("Connecting！")
                }
                // 连接结果
                else if (strType.equals(BaseVolume.BROADCAST_TCP_CONNECT_CALLBACK)) {
                    val isConnected = intent.getBooleanExtra(BaseVolume.BROADCAST_TCP_STATUS, false)
                    val strMsg = intent.getStringExtra(BaseVolume.BROADCAST_MSG)
                    if (!isConnected) {
                        imgWIFI.visibility = View.GONE
                        imgReset.visibility = View.GONE
                        tvReDeviceConnect.visibility = View.VISIBLE
                        imgLeft1.setImageResource(R.drawable.img_left_1_hui)
                        imgLeft1.isEnabled = false
                        imgLeft2.setImageResource(R.drawable.img_left_2_hui)
                        imgLeft2.isEnabled = false
                        imgLeft4.setImageResource(R.drawable.img_left_4_hui)
                        imgLeft4.isEnabled = false
                    } else {
                        imgReset.visibility = View.VISIBLE
                        tvReDeviceConnect.visibility = View.GONE
                        imgLeft4.setImageResource(R.drawable.img_left_4)
                        imgLeft4.isEnabled = true

                        if (SocketThreadManager.sharedInstance(this@MainControlActivity)?.isCanConnected()!!) {
                            imgWIFI.visibility = View.VISIBLE
                            tvReCanConnect.visibility = View.GONE
                        }
                        else {
                            imgWIFI.visibility = View.GONE
                            tvReCanConnect.visibility = View.VISIBLE
                        }

                    }
                    loadingDialog?.dismiss()

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
                        imgWIFI.visibility = View.GONE
                    }
                    else {
                        tvReCanConnect.visibility = View.GONE
                        if (SocketThreadManager.sharedInstance(this@MainControlActivity)?.isDeviceConnected()!!) {
                            imgWIFI.visibility = View.VISIBLE
                            tvReDeviceConnect.visibility = View.GONE
                        }
                        else {
                            imgWIFI.visibility = View.GONE
                            tvReDeviceConnect.visibility = View.VISIBLE
                        }
                    }
                }
            }
            else if (action == BaseVolume.BROADCAST_SEND_INFO) {
                val strType = intent.getStringExtra(BaseVolume.BROADCAST_TYPE)
                // 开始发送数据
                if (strType.equals(BaseVolume.BROADCAST_SEND_DATA_START)) {
                    if (!loadingDialog!!.isShowing) {
//                        loadingDialog?.show()
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
                if (strType.equals(BaseVolume.COMMAND_TYPE_SEAT_STATUS,true)) {
                    checkSeatStatus()
                }
            }
        }
    }

    fun checkSeatStatus() {
        // 状态未知，回退到主页面
        if (DataAnalysisHelper.deviceState.seatStatus == SeatStatus.press_unknown.iValue) {
            finish()
        }
        // 状态未达到预设值，提示用户等待
        else if (DataAnalysisHelper.deviceState.seatStatus < SeatStatus.press_reserve.iValue) {

            finish()

//            ToastMsg("Initializing...")
//            loadingDialog?.show()
//
//            // 按钮1亮，其他的都灰且不能点
//            imgLeft1.setImageResource(R.drawable.img_left_1)
//            imgLeft2.setImageResource(R.drawable.img_left_2_hui)
//            imgLeft1.isEnabled = true
//            imgLeft2.isEnabled = false
//            switchActivityByNumber(1)
//
//            startCheckPeopleWindowHint?.dismiss()
//            areaSeatWindowHint?.dismiss()
//            progressBarWindowHint?.updateContent("Initializing...")
//            progressBarWindowHint?.onSelfShow()

        }
        // 座椅已经达到预设值，提示用户调整坐姿！
        else if (DataAnalysisHelper.deviceState.seatStatus == SeatStatus.press_reserve.iValue){

            areaSeatWindowHint?.dismiss()
            progressBarWindowHint?.onSelfDismiss()

            ToastMsg("Seat initialization is complete！")
            loadingDialog?.dismiss()

            // 按钮1亮，其他的都灰且不能点
            imgLeft1.setImageResource(R.drawable.img_left_1)
            imgLeft2.setImageResource(R.drawable.img_left_2_hui)
            imgLeft1.isEnabled = true
            imgLeft2.isEnabled = false
            switchActivityByNumber(1)
            startCheckPeopleWindowHint?.show()

        }
        // 正在探测
        else if (DataAnalysisHelper.deviceState.seatStatus == SeatStatus.press_auto_probe.iValue){
            ToastMsg("Is to detect！")

            // 按钮1亮，其他的都灰且不能点
            imgLeft1.setImageResource(R.drawable.img_left_1)
            imgLeft2.setImageResource(R.drawable.img_left_2_hui)
            imgLeft1.isEnabled = true
            imgLeft2.isEnabled = false
            switchActivityByNumber(1)

            startCheckPeopleWindowHint?.dismiss()
            areaSeatWindowHint?.dismiss()
            progressBarWindowHint?.updateContent("Is to detect...")
            progressBarWindowHint?.onSelfShow()

        }
        // 座椅自动模式正常运行，则其他功能都可以用
        else if (DataAnalysisHelper.deviceState.seatStatus == SeatStatus.press_automatic.iValue){
            progressBarWindowHint?.onSelfDismiss()
            startCheckPeopleWindowHint?.dismiss()
            areaSeatWindowHint?.dismiss()

            ToastMsg("Automatic mode！")
            loadingDialog?.dismiss()

            imgLeft1.setImageResource(R.drawable.img_left_1)
            imgLeft2.setImageResource(R.drawable.img_left_2_false)
            imgLeft1.isEnabled = true
            imgLeft2.isEnabled = true

            switchActivityByNumber(1)
        }
        // 座椅手动模式运行
        else if (DataAnalysisHelper.deviceState.seatStatus == SeatStatus.press_automatic_manual.iValue){
            ToastMsg("Manual mode！")
            loadingDialog?.dismiss()
            progressBarWindowHint?.onSelfDismiss()
            startCheckPeopleWindowHint?.dismiss()
            areaSeatWindowHint?.dismiss()
            imgLeft1.setImageResource(R.drawable.img_left_1_false)
            imgLeft2.setImageResource(R.drawable.img_left_2)
            imgLeft1.isEnabled = true
            imgLeft2.isEnabled = true
            switchActivityByNumber(2)
        }
        // 开发者模式
        else if (DataAnalysisHelper.deviceState.seatStatus == SeatStatus.develop.iValue) {
            ToastMsg("Developer model！")
            progressBarWindowHint?.onSelfDismiss()
            startCheckPeopleWindowHint?.dismiss()
            areaSeatWindowHint?.dismiss()
            loadingDialog?.dismiss()
            val intent = Intent()
            intent.setClass(this@MainControlActivity,DevelopmentActivity::class.java)
            startActivity(intent)
            finish()
        }
        // 临时自检
        else if (DataAnalysisHelper.deviceState.seatStatus == SeatStatus.short_check.iValue) {
            loadingDialog?.dismiss()
            progressBarWindowHint?.onSelfDismiss()
            startCheckPeopleWindowHint?.dismiss()
            areaSeatWindowHint?.show()
        }

    }

    override fun onDestroy() {

        // 通知返回主页
        sendBroadcast(Intent(BaseVolume.BROADCAST_GOBACK_MENU))

        super.onDestroy()
        unregisterReceiver(myNetReceiver)

        // 遍历hashMap
        hashMapViews.forEach{
            val strActivity = it.key
//            val strActivity = it.value
            mactivityManager?.destroyActivity(strActivity, true)
        }

    }

}