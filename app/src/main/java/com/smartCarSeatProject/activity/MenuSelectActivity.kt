package com.smartCarSeatProject.activity

import ai.nuralogix.anura.sample.activities.ConfigActivity
import ai.nuralogix.anura.sample.activities.MainActivity
import ai.nuralogix.anura.sample.activities.MeasurementActivity
import ai.nuralogix.anura.sample.settings.CameraConfigurationFragment
import ai.nuralogix.anurasdk.camera.CameraAdapter
import ai.nuralogix.anurasdk.config.DfxPipeConfiguration
import ai.nuralogix.anurasdk.core.*
import ai.nuralogix.anurasdk.error.AnuraError
import ai.nuralogix.anurasdk.network.DeepAffexDataSpec
import ai.nuralogix.anurasdk.network.DeepFXClient
import ai.nuralogix.anurasdk.render.Render
import ai.nuralogix.anurasdk.render.opengl.GLSurfaceViewTracker
import ai.nuralogix.anurasdk.utils.*
import ai.nuralogix.anurasdk.views.TrackerView
import ai.nuralogix.dfx.ChunkPayload
import ai.nuralogix.dfx.Collector
import ai.nuralogix.dfx.ConstraintResult
import android.content.*
import android.content.res.AssetFileDescriptor
import android.media.MediaPlayer
import android.net.ConnectivityManager
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.os.IBinder
import android.os.SystemClock
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.View
import android.widget.Toast
import com.ai.nuralogix.anura.sample.face.MNNFaceDetectorAdapter
import com.ai.nuralogix.anura.sample.utils.BundleUtils
import com.alibaba.android.mnnkit.monitor.MNNMonitor
import com.smartCarSeatProject.BuildConfig
import com.smartCarSeatProject.R
import com.smartCarSeatProject.dao.DevelopDataInfo
import com.smartCarSeatProject.dao.RemoteSQLInfo
import com.smartCarSeatProject.data.*
import com.smartCarSeatProject.tcpInfo.SocketThreadManager
import com.smartCarSeatProject.view.AreaAddWindowHint
import com.smartCarSeatProject.wifiInfo.WIFIConnectionManager
import com.yotlive.matx.MatXDataMessage
import com.yotlive.matx.MatXService
import com.yotlive.matx.MatXService.MatXServiceBinder
import com.yotlive.matx.MatXStateMessage
import kotlinx.android.synthetic.main.layout_menu.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONArray
import org.opencv.core.Point
import java.io.*
import java.util.*
import kotlin.collections.ArrayList


class MenuSelectActivity : BaseActivity(),View.OnClickListener,DfxPipeListener, VideoSignalAnalysisListener, TrackerView.OnSizeChangedListener{

    var Tag = ""
    var isGotoAuto = false
    var isGotoManual = false
    var isGotoDevelop = false
    // 是否在控制界面
    var isControlShow = false
    // 是否在退出应用
    var isExitApplication = false
    // 是否开始采集数据，用于计算身高体重
    var isGatherPressDataBuffer = false
    // 检测人体的同时缓存A面气压值，用于后面的体重，身高计算
    var statPressABufferListByProbe = arrayListOf<ArrayList<String>>()
    lateinit var keepSeatWindowHint:AreaAddWindowHint
    // 座椅恢复初始化，先massage，massageoff，实现全部泄气
    var isInitSeatOnMassageOff = false
    // 流程：连接Can→ 调压模式，初始化 → 初始化完成后normal → 开始检测人体参数并缓存压力值 → 识别结束，计算身高体重 → 各个模式按钮亮起来！
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_menu)

        menuSelectActivity = this

        Tag = this.localClassName



        initUI()
        reciverBand()


        btn1.isEnabled = false
        btn2.isEnabled = false
        btn3.isEnabled = true
        btn4.isEnabled = false

//        val isMan = getBooleanBySharedPreferences(SEX_MAN)
        val isCN = getBooleanBySharedPreferences(COUNTRY_CN)
//        DataAnalysisHelper.deviceState.m_gender = isMan
        DataAnalysisHelper.deviceState.m_national = isCN

        // 人体采集相关
        AnuLogUtil.setShowLog(BuildConfig.DEBUG)
        MNNMonitor.setMonitorEnable(false)
        copyFileOrDir("regions.dat")
        // 自动开始采集
        IS_START_AUTOFACE = false
        initNuralogixInfo()
        rlCamera.visibility = View.INVISIBLE

        // 将座椅恢复到最初
        changeSeatState(SeatStatus.press_wait_reserve.iValue)



        initYotlive()

    }

    fun initUI() {

        imgClose.setOnClickListener(this)
        imgReset.setOnClickListener(this)
        imgWIFI.setOnClickListener(this)
        tvReCanConnect.setOnClickListener(this)
        tvReLocConnect.setOnClickListener(this)
        tvTitle.setOnClickListener(this)
        btn1.setOnClickListener(this)
        btn2.setOnClickListener(this)
        btn3.setOnClickListener(this)
        btn4.setOnClickListener(this)

        tvTitle.setOnLongClickListener {
            // 根据当前身高体重，得到16个通道的数据，并发送
            // 根据男女，身高体重，获取气压表
            val isMan = DataAnalysisHelper.deviceState.m_gender == 1
            val isCN = getBooleanBySharedPreferences(COUNTRY_CN)
            val willCtrPressValue = DataAnalysisHelper.getInstance(mContext).getAutoCtrPressByPersonStyle(isMan,isCN)
            // 设置气压，并提示用户，正在自动调整
            val sendData = CreateCtrDataHelper.getCtrPressAllValueByPerson(willCtrPressValue!!)
//            SocketThreadManager.sharedInstance(mContext).StartSendDataByCan(sendData[0])
//            SocketThreadManager.sharedInstance(mContext).StartSendDataByCan(sendData[1])
            true
        }

    }



    /** 监听广播  */
    private fun reciverBand() {
        val myIntentFilter = IntentFilter()
        myIntentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION)
        myIntentFilter.addAction(BaseVolume.BROADCAST_FINISH_APPLICATION)
        myIntentFilter.addAction(BaseVolume.BROADCAST_TCP_INFO_CAN)
        myIntentFilter.addAction(BaseVolume.BROADCAST_TCP_INFO_CAN2)
        myIntentFilter.addAction(BaseVolume.BROADCAST_SEND_INFO)
        myIntentFilter.addAction(BaseVolume.BROADCAST_RESULT_DATA_INFO)
        myIntentFilter.addAction(BaseVolume.BROADCAST_CTR_CALLBACK)
        myIntentFilter.addAction(BaseVolume.BROADCAST_GOBACK_MENU)
        myIntentFilter.addAction(BaseVolume.BROADCAST_RESET_ACTION)
        myIntentFilter.addAction(BaseVolume.COMMAND_TYPE_SEAT_MODE)
        myIntentFilter.addAction(BaseVolume.BROADCAST_NO_HAVE_PERSON)

        // 注册广播
        registerReceiver(myNetReceiver, myIntentFilter)
    }

    override fun onClick(p0: View?) {
        isGotoAuto = false
        isGotoManual = false
        isGotoDevelop = false

        when(p0?.id) {
            R.id.imgClose -> {
                val areaAddWindowHint = AreaAddWindowHint(this,R.style.Dialogstyle,"System",
                        object : AreaAddWindowHint.PeriodListener {
                            override fun refreshListener(string: String) {
                                startTimerHoldSeat(false)
                                // 如果连接已存在，则需要先泄气，再停止，最后断开连接，退出
                                if (SocketThreadManager.sharedInstance(mContext).isCanConnected()) {
                                    loadingDialog.showAndMsg("请稍后...")
                                    isExitApplication = true
                                    // 全部泄气1
                                    SocketThreadManager.sharedInstance(mContext).StartSendDataByCan(BaseVolume.COMMAND_CAN_ALL_DEFLATE_A_B)
                                }
                                else {
                                    finish()
//                                    System.exit(0)
                                }
                            }

                            override fun cancelListener() {
                            }
                        },"Are you sure to exit the application?",false)
                areaAddWindowHint?.show()
            }
            R.id.imgReset -> {
                val areaAddWindowHint = AreaAddWindowHint(this,R.style.Dialogstyle,"System",
                        object : AreaAddWindowHint.PeriodListener {
                            override fun refreshListener(string: String) {
                                // 将座椅恢复到最初
                                changeSeatState(SeatStatus.press_wait_reserve.iValue)
                                defaultSeatState()
                            }

                            override fun cancelListener() {
                            }
                        },"Are you sure to reset?",false)
                areaAddWindowHint?.show()
            }
            R.id.btn1 -> {
                gotoMainControlActivity(1)
            }
            R.id.btn2 -> {
                gotoMainControlActivity(2)
            }
            R.id.btn3 -> {
                gotoMainControlActivity(3)



            }
            R.id.btn4 -> {
                gotoMainControlActivity(4)
            }
            R.id.tvReCanConnect -> {
                SocketThreadManager.sharedInstance(this@MenuSelectActivity)?.createCanSocket()


            }
            R.id.tvReLocConnect -> {
                SocketThreadManager.sharedInstance(this@MenuSelectActivity)?.createLocSocket()
//                renderingVideoSink.stop()
            }
            R.id.tvTitle -> {
                // 计算身高体重
//                DataAnalysisHelper.getInstance(mContext).measureHeightWeight()
//                ToastMsg("计算结果，身高：${DataAnalysisHelper.deviceState.nowHeight}，体重：${DataAnalysisHelper.deviceState.nowWeight}")

//                if (tvTitle.tag.toString().toBoolean()) {
//                    tvTitle.tag = false
//                    tracker_ui_view.visibility = View.GONE
//                    tracker_opengl_view.visibility = View.GONE
//                }
//                else {
//                    tvTitle.tag = true
//                    tracker_ui_view.visibility = View.VISIBLE
//                    tracker_opengl_view.visibility = View.VISIBLE
//                }
//                val strSendData0 = CreateCtrDataHelper.getCtrModelAB(BaseVolume.COMMAND_CAN_MODEL_MASG_1,BaseVolume.COMMAND_CAN_MODEL_NORMAL)
//                SocketThreadManager.sharedInstance(mContext).StartChangeModelByCan(strSendData0)
//                val strSendData = CreateCtrDataHelper.getCtrModelAB(BaseVolume.COMMAND_CAN_MODEL_MASG_OFF,BaseVolume.COMMAND_CAN_MODEL_NORMAL)
//                SocketThreadManager.sharedInstance(mContext).StartChangeModelByCan(strSendData)

                Thread(Runnable {
                    val remoteSQLInfo = RemoteSQLInfo()
                    remoteSQLInfo.insertDataByDevelopData(DevelopDataInfo())
                }).start()




            }
        }
    }

    /** 切换页面 */
    fun gotoMainControlActivity(iNumber:Int) {
        isControlShow = true
        when(iNumber) {
            1 -> {
//                changeSeatState(SeatStatus.press_automatic.iValue)
            }
            2 -> {
                if (DataAnalysisHelper.deviceState.seatStatus == SeatStatus.press_automatic.iValue) {
                    playOrPauseMedia("",false,0)
                    // 释放A面气压，B面保持不动
                    releaseAPress()
                }


//                changeSeatState(SeatStatus.press_manual.iValue)
            }
            3 -> {
//                if (DataAnalysisHelper.deviceState.seatStatus == SeatStatus.press_automatic.iValue) {
//                    DataAnalysisHelper.deviceState.iNowAutoProgress = 0
//                    playOrPauseMedia("",false,0)
//                    // 释放A面气压，B面保持不动
//                    releaseAPress()
//                }


//                if (isCheckedPersonInfo)
//                    changeSeatState(SeatStatus.press_normal.iValue)
//                else {
//                    // 人体没检测，且连接断开，则恢复到待初始化
//                    if (!SocketThreadManager.sharedInstance(mContext).isCanConnected()) {
//                        changeSeatState(SeatStatus.press_wait_reserve.iValue)
//                    }
//                    else {
//                        changeSeatState(SeatStatus.press_normal.iValue)
//                    }
//
//                }

            }
            4 -> {
                if (DataAnalysisHelper.deviceState.seatStatus == SeatStatus.press_automatic.iValue) {
                    playOrPauseMedia("",false,0)
                    // 释放A面气压，B面保持不动
                    releaseAPress()
                }
                changeSeatState(SeatStatus.develop.iValue)
                startActivity(Intent(this@MenuSelectActivity,DevelopmentActivity::class.java))
                return
            }
        }

        val intent = Intent(this,MainControlActivity::class.java)
        intent.putExtra("iNumber",iNumber)
        startActivity(intent)
    }

    /**
     * 广播监听
     */
    private val myNetReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            // 网络监听的回调
            if (action == ConnectivityManager.CONNECTIVITY_ACTION) {
                val mConnectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                val netInfo = mConnectivityManager
                        .activeNetworkInfo
                if (netInfo != null && netInfo.isAvailable) {
                    // wifi网络
                    if (netInfo.type == ConnectivityManager.TYPE_WIFI) {
                        checkStartConnect()
                    } else if (netInfo.type == ConnectivityManager.TYPE_ETHERNET) {
                    } else if (netInfo.type == ConnectivityManager.TYPE_MOBILE) {
                    }// 3G网络
                    // 2G网络
                } else {
                    // 网络断开了
                    SocketThreadManager.sharedInstance(this@MenuSelectActivity).clearAllTCPClient()
                }
            }
            else if (action == BaseVolume.BROADCAST_FINISH_APPLICATION) {
                startTimerHoldSeat(false)
                // 如果连接已存在，则需要先泄气，再停止，最后断开连接，退出
                if (SocketThreadManager.sharedInstance(mContext).isCanConnected()) {
                    playOrPauseMedia("",false,0)
                    loadingDialog.showAndMsg("请稍后...")
                    isExitApplication = true
                    // 全部泄气1
                    SocketThreadManager.sharedInstance(mContext).StartSendDataByCan(BaseVolume.COMMAND_CAN_ALL_DEFLATE_A_B)
                }
                else {
                    finish()
//                    System.exit(0)
                }
            }
            else if (action == BaseVolume.BROADCAST_RESET_ACTION) {
                // 将座椅恢复到最初
                changeSeatState(SeatStatus.press_wait_reserve.iValue)
                playOrPauseMedia("",false,0)
                defaultSeatState()
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
                        ToastMsg("Can Connect Fail！$strMsg")
                        startTimerHoldSeat(false)
                    }
                    else {
                        imgWIFI.visibility = View.VISIBLE
                        tvReCanConnect.visibility = View.GONE
                        tvReLocConnect.visibility = View.GONE
                        if (!SocketThreadManager.sharedInstance(this@MenuSelectActivity).isCan2Connected()) {
                            imgWIFI.visibility = View.GONE
                            tvReLocConnect.visibility = View.VISIBLE
                        }
//                        ToastMsg("Can Connection successful！")
                        OnStartLoadData(isConnected)
                        changeSeatState(-1)
                        // 判断座椅状态 待初始化：先发调压模式，再调整气压
                        if (DataAnalysisHelper.deviceState.seatStatus == SeatStatus.press_wait_reserve.iValue) {
                            defaultSeatState()
                        }

                        // 测试阶段，A面气袋有问题，所以直接进入默认状态！fixme
//                        changeSeatState(SeatStatus.press_normal.iValue)
                    }


                }

            }
            else if (action == BaseVolume.BROADCAST_TCP_INFO_CAN2) {
                val strType = intent.getStringExtra(BaseVolume.BROADCAST_TYPE)
                // 开始连接
                if (strType.equals(BaseVolume.BROADCAST_TCP_CONNECT_START)) {
                    ToastMsg("Loc,Connecting！")
                }
                // 连接结果
                else if (strType.equals(BaseVolume.BROADCAST_TCP_CONNECT_CALLBACK)) {
                    val isConnected = intent.getBooleanExtra(BaseVolume.BROADCAST_TCP_STATUS, false)
                    val strMsg = intent.getStringExtra(BaseVolume.BROADCAST_MSG)
                    if (!isConnected) {
                        tvReLocConnect.visibility = View.VISIBLE
                        imgWIFI.visibility = View.GONE
                        ToastMsg("Loc Connect Fail！$strMsg")
                    }
                    else {
                        imgWIFI.visibility = View.VISIBLE
                        tvReLocConnect.visibility = View.GONE
                        tvReCanConnect.visibility = View.GONE
                        // 座椅状态属于初始化完成之前，则需要先on
                        if(DataAnalysisHelper.deviceState.seatStatus < SeatStatus.press_reserve.iValue) {
                            // 电源ON
                            SocketThreadManager.sharedInstance(mContext).StartSendDataByCan2(BaseVolume.COMMAND_CAN_LOCATION_ON)
                        }
                        if (!SocketThreadManager.sharedInstance(this@MenuSelectActivity).isCanConnected()) {
                            imgWIFI.visibility = View.GONE
                            tvReCanConnect.visibility = View.VISIBLE
                            SocketThreadManager.sharedInstance(this@MenuSelectActivity)?.createCanSocket()
                        }

//                        ToastMsg("Loc Connection successful！")
                    }
                }
            }
            else if (action == BaseVolume.BROADCAST_SEND_INFO) {
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
                    if (loadingDialog?.isShowing!!) {
                        loadingDialog?.dismiss()
                        ToastMsg("Send timeout！")
                    }
                }
            }
            // 数据回调
            else if (action == BaseVolume.BROADCAST_RESULT_DATA_INFO) {
                val strType = intent.getStringExtra(BaseVolume.BROADCAST_TYPE)
                val deviceWorkInfo = intent.getSerializableExtra(BaseVolume.BROADCAST_MSG) as DeviceWorkInfo
                // 通道状态
                if (strType == BaseVolume.COMMAND_TYPE_CHANNEL_STATUS) {
                    // 正在初始化
                    if (DataAnalysisHelper.deviceState.seatStatus == SeatStatus.press_resume_reserve.iValue && SocketThreadManager.isCheckChannelState) {
                        // massage off 泄气，虽然气袋全部恢复到了normall，但气袋其实还在泄气，所以将状态判断放到了气压值检测的事件里。
                        if (isInitSeatOnMassageOff) {
                            // 初始化控制A面所有，B面座垫12345678，通道充气，所以只需要判断12345678，abcdefgh这几个气袋
                            var isAllNormal = true
                            // A面所有气袋 abcdefgh
                            for (iState in DataAnalysisHelper.deviceState.sensePressStatusList) {
                                if (iState != DeviceWorkInfo.STATUS_NORMAL) {
                                    isAllNormal = false
                                    break
                                }
                            }
                            for (iState in DataAnalysisHelper.deviceState.controlPressStatusList) {
                                if (iState != DeviceWorkInfo.STATUS_NORMAL) {
                                    isAllNormal = false
                                    break
                                }
                            }
                            // 当前处于massage off，且都已经变为normall，说明所有气袋都已经泄气完毕，则开始充气
                            if (isAllNormal) {

                                // 虽然气袋全部恢复到了normall，但气袋其实还在泄气，所以延时10秒后执行充气
//                                val timer = Timer()
//                                timer?.schedule(object : TimerTask() {
//                                    override fun run() {
//                                        Log.e("default press","massage off 已全部完成，则开始充气")
//                                        SocketThreadManager.sharedInstance(mContext).startTimeOut(false)
//                                        SocketThreadManager.sharedInstance(mContext).StartChangeModelByCan(BaseVolume.COMMAND_CAN_MODEL_ADJUST_A_B)
//                                        // 座椅AB面气压恢复初始化！
//                                        val sendDataList = CreateCtrDataHelper.getAllPressValueBy16("1000","1000","0")
//                                        sendDataList.forEach {
//                                            SocketThreadManager.sharedInstance(mContext).StartSendDataByCan(it)
//                                        }
//                                        isInitSeatOnMassageOff = false
//                                    }
//                                }, (1 * 10000))

                            }
                            else {
                                Log.e("default press","massage off 还未全部完成！！！")
                            }
                        }
                        // 正在恢复气压值
                        else {
                            // 初始化控制A面所有，B面座垫12345678，通道充气，所以只需要判断12345678，abcdefgh这几个气袋
                            var isAllNormal = true
                            // A面所有气袋 abcdefgh
                            for (iState in DataAnalysisHelper.deviceState.sensePressStatusList) {
                                // 正在充气，说明还没完成
                                if (iState == DeviceWorkInfo.STATUS_SETTING)
                                    return
                                if (iState == DeviceWorkInfo.STATUS_SETTED)
                                    isAllNormal = false
                            }
                            for (iNumber in 0 .. 7) {
                                val iState = DataAnalysisHelper.deviceState.controlPressStatusList[iNumber]
                                // 正在充气，说明还没完成
                                if (iState == DeviceWorkInfo.STATUS_SETTING)
                                    return
                                if (iState == DeviceWorkInfo.STATUS_SETTED)
                                    isAllNormal = false
                            }

                            // 全部到了SETTED，则全部恢复到Normall
                            if (!isAllNormal) {
                                Log.e("default press","全部到了SETTED，则全部恢复到Normall！！！")
                                SocketThreadManager.sharedInstance(mContext).StartChangeModelByCan(BaseVolume.COMMAND_CAN_MODEL_NORMAL_A_B)
                            }
                            // 已经全部恢复到Normal，则将座椅切到恢复成功状态
                            else {
                                Log.e("default press","已全部恢复到Normall，座椅恢复成功！！！")
                                changeSeatState(SeatStatus.press_reserve.iValue)
                                SocketThreadManager.sharedInstance(mContext).startTimeOut(false)
                            }
                        }



                    }
                }
                // 气压值
                else if (strType == BaseVolume.COMMAND_TYPE_PRESS) {
                    var isLowPress = true
                    DataAnalysisHelper.deviceState.sensePressValueListl.forEach {
                        if (it.toInt() > 255)
                            isLowPress = false
                    }
                    DataAnalysisHelper.deviceState.controlPressValueList.forEach {
                        if (it.toInt() > 255)
                            isLowPress = false
                    }

                    // 正在退出
                    if (isExitApplication) {
                        if (!isLowPress) {
                            return
                        }
                        SocketThreadManager.sharedInstance(mContext).startTimeOut(false)
                        SocketThreadManager.sharedInstance(mContext).StartChangeModelByCan(BaseVolume.COMMAND_CAN_ALL_STOP_A_B)
                        loadingDialog.dismiss()
                        finish()
//                        System.exit(0)
                        return
                    }
                    // 正在massageoff且座椅处于正在恢复初始化阶段，则判断各个气袋是否已经泄气完成
                    if (isLowPress && isInitSeatOnMassageOff && SocketThreadManager.isCheckChannelState) {
                        if (isLowPress && DataAnalysisHelper.deviceState.seatStatus == SeatStatus.press_resume_reserve.iValue) {
                            Log.e("default press","massage off 已全部完成，则开始充气")
                            SocketThreadManager.sharedInstance(mContext).startTimeOut(false)
                            SocketThreadManager.sharedInstance(mContext).StartChangeModelByCan(BaseVolume.COMMAND_CAN_MODEL_ADJUST_A_B)
                            // 座椅AB面气压恢复初始化！
                            val sendDataList = CreateCtrDataHelper.getAllPressValueBy16("1000","1000","0")
                            sendDataList.forEach {
                                SocketThreadManager.sharedInstance(mContext).StartSendDataByCan(it)
                            }
                            isInitSeatOnMassageOff = false
                            massageoffTimeOut(false)
                        }
                    }


                    // 座椅正在检测人体，则收集A面气压值
                    if (DataAnalysisHelper.deviceState.seatStatus == SeatStatus.press_auto_probe.iValue && isGatherPressDataBuffer) {
                        statPressABufferListByProbe.add(DataAnalysisHelper.deviceState.sensePressValueListl)
                    }
                }
            }
            // 控制回调
            else if (action == BaseVolume.BROADCAST_CTR_CALLBACK) {
            }
            // 座椅状态切换
            else if (action == BaseVolume.COMMAND_TYPE_SEAT_MODE) {
                changeSeatState(-1)
            }
            // 从控制页面返回到主页
            else if (action == BaseVolume.BROADCAST_GOBACK_MENU) {
                isControlShow = false


            }
            // 检测到无人
            else if (action == BaseVolume.BROADCAST_NO_HAVE_PERSON) {
                if (isControlShow) {
                    return
                }

                if (!this@MenuSelectActivity::keepSeatWindowHint.isInitialized) {
                    keepSeatWindowHint = AreaAddWindowHint(mContext,R.style.Dialogstyle,"System",
                            object : AreaAddWindowHint.PeriodListener {
                                override fun refreshListener(string: String) {
                                    // 继续保持
                                    startTimerHoldSeat(true)
                                }
                                override fun cancelListener() {
                                    // 将座椅恢复到最初
                                    changeSeatState(SeatStatus.press_wait_reserve.iValue)
                                    defaultSeatState()
                                }
                            },"The seat is empty. Do you want to keep it?",false)
                }
                if (!keepSeatWindowHint.isShowing)
                    keepSeatWindowHint.show()

            }
        }
    }



    /**
     * 将座椅恢复到正在初始化状态，并重新调压，检测
     */
    private fun defaultSeatState() {
        startTimerHoldSeat(false)
        // 先发massage,massage off 实现全部泄气，之后再充气

        val strSendData0 = CreateCtrDataHelper.getCtrModelAB(BaseVolume.COMMAND_CAN_MODEL_MASG_1,BaseVolume.COMMAND_CAN_MODEL_MASG_1)
        SocketThreadManager.sharedInstance(mContext).StartChangeModelByCan(strSendData0)
        val strSendData = CreateCtrDataHelper.getCtrModelAB(BaseVolume.COMMAND_CAN_MODEL_MASG_OFF,BaseVolume.COMMAND_CAN_MODEL_MASG_OFF)
        // 然后延时1秒后执行off
        Timer().schedule(object : TimerTask() {
            override fun run() {
                SocketThreadManager.sharedInstance(mContext).StartChangeModelByCan(strSendData)
                SocketThreadManager.sharedInstance(mContext).StartChangeModelByCan(strSendData)
                SocketThreadManager.sharedInstance(mContext).StartChangeModelByCan(strSendData)
            }
        }, (1 * 1000))
        // 延时n秒后，判断状态
        SocketThreadManager.sharedInstance(mContext).startCheckState(true)
        isInitSeatOnMassageOff = true
        massageoffTimeOut(true)
//        SocketThreadManager.sharedInstance(mContext).StartChangeModelByCan(BaseVolume.COMMAND_CAN_MODEL_ADJUST_A_B)
//        // 座椅AB面气压恢复初始化！
//        val sendDataList = CreateCtrDataHelper.getAllPressValueBy16("1000","1000","0")
//        sendDataList.forEach {
//            SocketThreadManager.sharedInstance(mContext).StartSendDataByCan(it)
//        }
        // 切换到正在初始化模式
        changeSeatState(SeatStatus.press_resume_reserve.iValue)
        isCheckedPersonInfo = false

    }

    var massageTimer : Timer? = Timer()
    /**
     * massageoff 的超时
     */
    private fun massageoffTimeOut(isRun:Boolean) {
        massageTimer?.cancel()
        massageTimer = null
        if (isRun) {
            massageTimer = Timer()
            massageTimer?.schedule(object : TimerTask() {
                override fun run() {
                    // massageoff 超时，直接进入初始化充气状态
                    isInitSeatOnMassageOff = false
                    SocketThreadManager.sharedInstance(mContext).startTimeOut(false)
                    SocketThreadManager.sharedInstance(mContext).StartChangeModelByCan(BaseVolume.COMMAND_CAN_MODEL_ADJUST_A_B)
                    // 座椅AB面气压恢复初始化！
                    val sendDataList = CreateCtrDataHelper.getAllPressValueBy16("1000","1000","0")
                    sendDataList.forEach {
                        SocketThreadManager.sharedInstance(mContext).StartSendDataByCan(it)
                    }
                }
            }, (1 * 20000))
        }

    }

    /**
     * 切换座椅状态 -1:不改变座椅状态，仅更新UI
     */
    fun changeSeatState(iState : Int) {
        if (iState != -1) {
            DataAnalysisHelper.deviceState.seatStatus = iState
        }

        imgRun1.visibility = View.GONE
        imgRun2.visibility = View.GONE
        imgRun4.visibility = View.GONE
        btn1.isEnabled = false
        btn2.isEnabled = false
        btn3.isEnabled = true
        btn4.isEnabled = false
        // 座椅等待恢复
        if (DataAnalysisHelper.deviceState.seatStatus ==  SeatStatus.press_wait_reserve.iValue) {

        }
        // 座椅正在恢复 
        else if (DataAnalysisHelper.deviceState.seatStatus ==  SeatStatus.press_resume_reserve.iValue) {
            ToastMsg("Initializing...")

            stopMeasurement(true)
            rlCamera.visibility = View.INVISIBLE
            renderingVideoSink.stop()

            progressBarWindowHint?.updateContent("Initializing...")
            progressBarWindowHint?.onSelfShow()

            // 电机恢复默认位置
            if (SocketThreadManager.sharedInstance(mContext).isCan2Connected()) {

                // 默认位置
                SocketThreadManager.sharedInstance(mContext).StartSendDataByCan2(BaseVolume.COMMAND_CAN_LOCATION_DEFAULT)
                // 00 结束
                SocketThreadManager.sharedInstance(mContext).StartSendDataByCan2(BaseVolume.COMMAND_CAN_LOCATION_0)
            }

        }
        // 座椅恢复成功，可以检测了！！！！
        else if (DataAnalysisHelper.deviceState.seatStatus == SeatStatus.press_reserve.iValue){
            btn4.isEnabled = true
            loadingDialog?.dismiss()
            progressBarWindowHint?.onSelfDismiss()
            ToastMsg("Initialized！")
            // 座椅刚初始化完，同时保存座垫压力值
            DataAnalysisHelper.deviceState.init_cushion_valueList = DataAnalysisHelper.deviceState.cushion_now_valueList
            // 提醒用户调整坐姿
            showKeepPosition()
        }
        // 正在探测，则开始采集人体数据
        else if (DataAnalysisHelper.deviceState.seatStatus == SeatStatus.press_auto_probe.iValue) {
            btn4.isEnabled = true
            loadingDialog?.dismiss()
            progressBarWindowHint?.onSelfDismiss()
            ToastMsg("collecting data...")

            stopMeasurement(true)
            // 启动摄像头！
            rlCamera.visibility = View.VISIBLE
            IS_START_AUTOFACE = true
            if (this::core.isInitialized) {
                renderingVideoSink.start()
            }
            state = STATE.IDLE

            // 开启座椅检测是否有人
            startTimerHoldSeat(true)


        }
        // 默认模式，即人体数据采集完成，身高体重也都算出来啦！
        else if (DataAnalysisHelper.deviceState.seatStatus == SeatStatus.press_normal.iValue) {
            btn1.isEnabled = true
            btn2.isEnabled = true
            btn3.isEnabled = true
            btn4.isEnabled = true
            loadingDialog?.dismiss()
            progressBarWindowHint?.onSelfDismiss()

            // 开启座椅检测
//            startTimerHoldSeat(true)

        }
        // 座椅自动模式
        else if (DataAnalysisHelper.deviceState.seatStatus == SeatStatus.press_automatic.iValue){
            imgRun1.visibility = View.VISIBLE
            btn1.isEnabled = true
            btn2.isEnabled = true
            btn3.isEnabled = true
            btn4.isEnabled = true
            loadingDialog?.dismiss()
            progressBarWindowHint?.onSelfDismiss()
        }
        // 手动模式
        else if (DataAnalysisHelper.deviceState.seatStatus == SeatStatus.press_manual.iValue){
            imgRun2.visibility = View.VISIBLE
            btn1.isEnabled = true
            btn2.isEnabled = true
            btn3.isEnabled = true
            btn4.isEnabled = true
            loadingDialog?.dismiss()
            progressBarWindowHint?.onSelfDismiss()
        }
        // 开发者模式
        else if (DataAnalysisHelper.deviceState.seatStatus == SeatStatus.develop.iValue) {
            if (isCheckedPersonInfo) {
                btn1.isEnabled = true
                btn2.isEnabled = true
            }
            btn3.isEnabled = true
            btn4.isEnabled = true
            imgRun4.visibility = View.VISIBLE
            loadingDialog?.dismiss()
            progressBarWindowHint?.onSelfDismiss()

        }
    }

    /** 提醒用户保持坐姿 */
    var showKeepPositionDialog : AreaAddWindowHint? = null
    private fun showKeepPosition() {
        if (showKeepPositionDialog == null) {
            showKeepPositionDialog = AreaAddWindowHint(this,R.style.Dialogstyle,"System",
                    object : AreaAddWindowHint.PeriodListener {
                        override fun refreshListener(string: String) {
                            // 切换到正在检测
                            changeSeatState(SeatStatus.press_auto_probe.iValue)
                        }
                        override fun cancelListener() {
                        }
                    },"Take a seat, please keep your position!",true)
        }
        if (!showKeepPositionDialog!!.isShowing)
            showKeepPositionDialog?.show()
    }


    /** 提醒用户采集完成 */
    var showCompleteDialog : AreaAddWindowHint? = null
    private fun showComplete() {
        if (showCompleteDialog == null) {
            showCompleteDialog = AreaAddWindowHint(this,R.style.Dialogstyle,"System",
                    object : AreaAddWindowHint.PeriodListener {
                        override fun refreshListener(string: String) {

                        }
                        override fun cancelListener() {
                        }
                    },"Thanks for your patient, Please start now！",true)
        }
        if (!showCompleteDialog!!.isShowing)
            showCompleteDialog?.show()
    }

    /** 根据当前气压值，计算身高体重 */
    private fun checkHeightWeightByPress() {

        changeSeatState(SeatStatus.press_normal.iValue)

        // 1,将收集的A面气压集合取平均值
        val iSize = statPressABufferListByProbe.size
        var iSumList = arrayListOf<Int>(0,0,0,0,0,0,0,0,0,0,0)
        // 先把所有集合的每个字段相加
        statPressABufferListByProbe.forEach { it0 ->
            for (iNumber in 0 .. 10) {
                iSumList[iNumber] += (it0[iNumber].toInt())
            }
        }
        // 再将每个字段总和算平均值
        var iMeanList = arrayListOf<Int>()
        iSumList.forEach {
            val iValue = it/iSize
            iMeanList.add(iValue)
        }
        // 用拿到的平均值，计算身高体重,同时记录缓存
        DataAnalysisHelper.deviceState.measureHeightWeight(iMeanList)
        isCheckedPersonInfo = true
        // 释放A面气压，B面保持不动
        releaseAPress()

        // 2,计算座垫压力平均值
        val iCushionSize = statCushionValueListBySeat.size
        var iCushionSumList = arrayListOf<Double>(0.0,0.0,0.0,0.0,0.0,0.0)
        statCushionValueListBySeat.forEach { it0 ->
            for (iNumber in 0 .. 5) {
                iCushionSumList[iNumber] += (it0[iNumber].toDouble())
            }
        }
        // 再将每个字段总和算平均值
        var iCushionMeanList = arrayListOf<Double>()
        iCushionSumList.forEach {
            var iValue = it/iCushionSize
            iValue = String.format("%.1f", iValue).toDouble()
            iCushionMeanList.add(iValue)
        }
        // 缓存座垫压力平均值
        DataAnalysisHelper.deviceState.recog_cushion_valueList.clear()
        DataAnalysisHelper.deviceState.recog_cushion_valueList.addAll(iCushionMeanList)

    }


    /** 校验，是否可以开始连接 */
    fun checkStartConnect() {
        val nowWIFIName = WIFIConnectionManager.getInstance(this@MenuSelectActivity)?.nowConnectWifi ?: ""
        Loge(Tag,"当前连接的wifi：$nowWIFIName ！！！")
        if (nowWIFIName.indexOf(BaseVolume.WIFI_SIGN) < 0) {
            ToastMsg("Please connect'${BaseVolume.WIFI_SIGN}' WIFI！")
            imgWIFI.visibility = View.GONE
            imgReset.visibility = View.GONE
            tvReCanConnect.visibility = View.VISIBLE
            return
        }
        // 先断开连接
        SocketThreadManager.sharedInstance(this@MenuSelectActivity)?.clearAllTCPClient()
        SocketThreadManager.sharedInstance(this@MenuSelectActivity)?.createLocSocket()
//        SocketThreadManager.sharedInstance(this@MenuSelectActivity)?.createCanSocket()


    }

    fun OnStartLoadData(isConnected: Boolean) {
        if (!isConnected) {
            imgReset.visibility = View.GONE
            btn1.isEnabled = false
            btn2.isEnabled = false
            btn4.isEnabled = false

            imgRun1.visibility = View.GONE
            imgRun2.visibility = View.GONE
            imgRun4.visibility = View.GONE

        }
        else {
            imgReset.visibility = View.VISIBLE
            btn1.isEnabled = false
            btn2.isEnabled = false
            btn4.isEnabled = true
        }
    }

    override fun onBackPressed() {
//         super.onBackPressed();//注释掉这行,back键不退出activity

        val areaAddWindowHint = AreaAddWindowHint(this,R.style.Dialogstyle,"System",
                object : AreaAddWindowHint.PeriodListener {
                    override fun refreshListener(string: String) {
                        startTimerHoldSeat(false)
                        // 如果连接已存在，则需要先泄气，再停止，最后断开连接，退出
                        if (SocketThreadManager.sharedInstance(mContext).isCanConnected()) {
                            loadingDialog.showAndMsg("请稍后...")
                            isExitApplication = true
                            // 全部泄气1
                            SocketThreadManager.sharedInstance(mContext).StartSendDataByCan(BaseVolume.COMMAND_CAN_ALL_DEFLATE_A_B)
                        }
                        else {
                            finish()
//                            System.exit(0)
                        }
                    }

                    override fun cancelListener() {
                    }
                },"Are you sure to exit the application?",false)
        areaAddWindowHint?.show()
    }

    fun copyFileOrDir(path: String) {
        val assetManager = this.assets
        val assets: Array<String>?
        try {
            assets = assetManager.list(path)
            if (assets!!.isEmpty()) {
                copyFile(path)
            } else {
                val fullPath = filesDir.absolutePath + File.separator + path
                val dir = File(fullPath)
                if (!dir.exists())
                    dir.mkdir()
                for (asset in assets) {
                    copyFileOrDir("$path/$asset")
                }
            }
        } catch (ex: IOException) {
            AnuLogUtil.e(MainActivity.TAG, "copyFileOrDir exception: ${ex.message}")
        } catch (e: Exception) {
            AnuLogUtil.e(MainActivity.TAG, "copyFileOrDir exception: ${e.message}")
        }

    }

    private fun copyFile(filename: String) {
        val assetManager = this.assets

        val `in`: InputStream
        val out: OutputStream
        try {
            `in` = assetManager.open(filename)
            val newFileName = filesDir.absolutePath + File.separator + filename

            out = FileOutputStream(newFileName)
            val buffer = ByteArray(1024)
            var read = `in`.read(buffer)
            while (read != -1) {
                out.write(buffer, 0, read)
                read = `in`.read(buffer)
            }
            `in`.close()
            out.flush()
            out.close()

        } catch (e: Exception) {
            AnuLogUtil.e(MainActivity.TAG, "copyFile exception: ${e.message}")
        }

    }


    override fun onDestroy() {
        mBinder.stopSampling(CuShionSnum)
        unbindService(conn)
        super.onDestroy()

        RemoteSQLInfo().cloaseRemoteSQL()

        SocketThreadManager.sharedInstance(this)?.clearAllTCPClient()

        startTimerHoldSeat(false)

        unregisterReceiver(myNetReceiver)
//        if (IS_START_AUTOFACE)
//            return

        destoryCamera()


        EventBus.getDefault().unregister(this)

        System.exit(0)


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

        restoreConfig()

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

    private fun restoreConfig() {
        val pref = getSharedPreferences(ConfigActivity.PREF_NAME, MODE_PRIVATE)
        val restUrl = pref.getString(ConfigActivity.REST_SERVER_KEY, MeasurementActivity.SAMPLE_REST_URL)
        MeasurementActivity.SAMPLE_REST_URL = restUrl!!
        val wsUrl = pref.getString(ConfigActivity.WS_SERVER_KEY, MeasurementActivity.SAMPLE_WS_URL)
        MeasurementActivity.SAMPLE_WS_URL = wsUrl!!
        val email = pref.getString(ConfigActivity.EMAIL_KEY, MeasurementActivity.EMAIL)
        MeasurementActivity.EMAIL = email!!
        val password = pref.getString(ConfigActivity.PASSWORD_KEY, MeasurementActivity.PASSWORD)
        MeasurementActivity.PASSWORD = password!!
        val license = pref.getString(ConfigActivity.LICENSE_KEY, MeasurementActivity.LICENSE_KEY)
        MeasurementActivity.LICENSE_KEY = license!!
        val studyId = pref.getString(ConfigActivity.STUDY_ID_KEY, MeasurementActivity.STUDY_ID)
        MeasurementActivity.STUDY_ID = studyId!!
        MeasurementActivity.userToken = pref.getString(ConfigActivity.USER_TOKEN, null)
    }

    private var IS_START_AUTOFACE = false
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
    var strGender = "--"
    private fun initPipeline() {
        val faceIndex = intent.getIntExtra(MeasurementActivity.FACE_ENGINE_KEY, 0)
        core = Core.createAnuraCore(this)
        constraintAverager.setReasonSpan(60)
        val format = VideoFormat(VideoFormat.ColorFormat.BGRA, 30, MeasurementActivity.IMAGE_HEIGHT.toInt(), MeasurementActivity.IMAGE_WIDTH.toInt())

        //val videoFormat = VideoFormat(VideoFormat.VideoCodec.H264, VideoFormat.ColorFormat.BGRA, 30, IMAGE_HEIGHT.toInt(), IMAGE_WIDTH.toInt())

        render = Render.createGL20Render(format)
        viewTracker?.setRenderer(render as GLSurfaceView.Renderer)


        render.showFeatureRegion(showHistogramAndRegions)

        var visageFaceTracker = MNNFaceDetectorAdapter(baseContext)
        visageFaceTracker.setTrackingRegion(0, 0, MeasurementActivity.IMAGE_WIDTH.toInt(), MeasurementActivity.IMAGE_HEIGHT.toInt())
        visageFaceTracker.setCheckPersionHaveListener(checkPersionHaveListener)
        cloudAnalyzerListener = object : CloudAnalyzerListener {
            override fun onStartAnalyzing() {
            }

            override fun onResult(result: AnalyzerResult) {
                val jsonResult = result.jsonResult
                AnuLogUtil.d(MeasurementActivity.TAG, "JSON result: $jsonResult index: ${result.resultIndex}")

                runOnUiThread {
                    // 人体数据： id & 信噪比 & 心跳 & 情绪值 & 低压 & 高压
                    val strPersonDataInfo =  "${result.measurementID}&${result.snr}&${result.heartRate}&${result.msi}&${result.bpDiastolic}&${result.bpSystolic}"
                    Loge("MenuSelectActivity","人体数据：信噪比:${result.snr}&心跳:${result.heartRate}&情绪值:${result.msi}&低压:${result.bpDiastolic}&高压:${result.bpSystolic}&呼吸率：${result.brBp}&性别：${result.gender}")
                    DataAnalysisHelper.deviceState.snr = "${result.snr}"
                    if (result.heartRate != 0)
                        DataAnalysisHelper.deviceState.HeartRate = "${result.heartRate}"
                    if (result.msi != 0.0f)
                        DataAnalysisHelper.deviceState.E_Index = "${result.msi}"
                    if (result.bpDiastolic != 0)
                        DataAnalysisHelper.deviceState.Dia_BP = "${result.bpDiastolic}"
                    if (result.bpSystolic != 0)
                        DataAnalysisHelper.deviceState.Sys_BP = "${result.bpSystolic}"
                    if (result.gender != 0)
                        DataAnalysisHelper.deviceState.m_gender = result.gender
                    if (result.brBp != 0.0f)
                        DataAnalysisHelper.deviceState.BreathRate = "${result.brBp}"

                    if(DataAnalysisHelper.deviceState.m_gender == 1)
                        strGender = "男"
                    else if(DataAnalysisHelper.deviceState.m_gender == 2)
                        strGender = "女"

                    measureReuslt.text = "信噪比:${DataAnalysisHelper.deviceState.snr} & " +
                            "心跳:${DataAnalysisHelper.deviceState.HeartRate} & " +
                            "情绪值:${DataAnalysisHelper.deviceState.E_Index} & " +
                            "低压:${DataAnalysisHelper.deviceState.Dia_BP} & " +
                            "高压:${DataAnalysisHelper.deviceState.Sys_BP} & " +
                            "呼吸率:${DataAnalysisHelper.deviceState.BreathRate} & " +
                            "性别:${strGender}"

                    if (result.resultIndex + 1 >= MeasurementActivity.TOTAL_NUMBER_CHUNKS) {


                        Loge("MenuSelectActivity","人体数据：测量结束！开始计算身高体重")
                        if (DataAnalysisHelper.deviceState.seatStatus == SeatStatus.press_auto_probe.iValue) {
                            // 人体数据，只要有一个值为0，就提示用户是否重新检测！
                            if (((DataAnalysisHelper.deviceState.E_Index).toFloat() <= 0.0f) || ((DataAnalysisHelper.deviceState.Dia_BP).toFloat() <= 0) || ((DataAnalysisHelper.deviceState.Sys_BP).toFloat() <= 0) ||
                                    (result.gender == 0) || ((DataAnalysisHelper.deviceState.BreathRate).toFloat() <= 0.0f)) {
                                val errorDialog = AreaAddWindowHint(mContext,R.style.Dialogstyle,"System",
                                        object : AreaAddWindowHint.PeriodListener {
                                            override fun refreshListener(string: String) {
                                                isGatherPressDataBuffer = false
                                                stopMeasurement(true)
                                                cancelled_tv.visibility = View.INVISIBLE
                                                checkHeightWeightByPress()
                                            }
                                            override fun cancelListener() {
                                                cancelled_tv.visibility = View.VISIBLE
                                                stopMeasurement(true)
                                            }
                                        },"Data acquisition failed. Do you want to continue?",false)
                                errorDialog.udpateBtnName("Next","Detection")
                                errorDialog?.show()
                            }
                            else {
                                isGatherPressDataBuffer = false
                                cancelled_tv.visibility = View.INVISIBLE
                                stopMeasurement(true)
                                checkHeightWeightByPress()
                                // 提醒用户检测完成
                                showComplete()
                            }
                        }
                        else {
                            isGatherPressDataBuffer = false
                            stopMeasurement(true)
                            cancelled_tv.visibility = View.INVISIBLE
                        }
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
                core.createDFXFactory(getFilesDir().getAbsolutePath() + "/regions.dat", "discrete")!!, dfxConfig.toJSONObject().toString(), cloudAnalyzer, this, renderingVideoSink)

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
                        // 处于正在检测
                        if (DataAnalysisHelper.deviceState.seatStatus == SeatStatus.press_auto_probe.iValue) {
                            isGatherPressDataBuffer = true
                            cancelled_tv.visibility = View.VISIBLE
                            // 同时收集A面气袋的数据 fixme
                            statPressABufferListByProbe.clear()
                            // 座垫压力数据
                            statCushionValueListBySeat.clear()
                        }
                        else {
                            cancelled_tv.visibility = View.INVISIBLE
                        }
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
        trackerView.showMask(isGatherPressDataBuffer)
        trackerView.showMeasurementProgress(isGatherPressDataBuffer)
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
        trackerView.showMask(isGatherPressDataBuffer)
        trackerView.showMeasurementProgress(isGatherPressDataBuffer)
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
        if (!IS_START_AUTOFACE)
            return

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
                        val builder: AlertDialog.Builder? = this@MenuSelectActivity.let {
                            AlertDialog.Builder(it)
                        }

                        // 如果座椅处于正在探测人体数据状态，则显示弹窗
                        if (DataAnalysisHelper.deviceState.seatStatus == SeatStatus.press_auto_probe.iValue) {
//                            builder?.setMessage("Wait for final result...")?.setCancelable(false)
//                            dialog = builder?.create()!!
//                            dialog.show()
                        }

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



    /** ***********************座垫传感器相关************************* */

    companion object{
        // 座垫ID
        var CuShionSnum: Int = 405
        lateinit var menuSelectActivity: MenuSelectActivity
        fun getInstance():MenuSelectActivity{
            return menuSelectActivity
        }
    }

    // 是否已经开始检测
    var isStartSampling = false
    // 检测人体的同时缓存座垫压力值，用于后面的体重，身高计算
    var statCushionValueListBySeat = arrayListOf<ArrayList<Double>>()
    private fun initYotlive() {
        CuShionSnum = 405
        EventBus.getDefault().register(this)
        // Bind service and register event bus.
        val intent = Intent(this, MatXService::class.java)
        bindService(intent, conn, Context.BIND_AUTO_CREATE)


    }

    // MatX service properties.
    private lateinit var mBinder: MatXServiceBinder
    private val conn: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            mBinder = service as MatXServiceBinder
            Log.e("MenuSelectActivity", "MenuSelectActivity --- onServiceConnected.")
        }

        override fun onServiceDisconnected(name: ComponentName) {
            Log.e("MenuSelectActivity", "MenuSelectActivity --- onServiceDisconnected.")
        }
    }

    // Eventbus showing event notifications.
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun showMessageDialog(msg: MatXStateMessage) {
        Log.e("MenuSelectActivity", "msg.state.code:${msg.state.code}")
        val code = msg.state.code
        when (code) {
            10001 -> {
                dialog.setMessage("已接入AP")
                dialog.show()
            }
            10002 -> {
                dialog.setMessage("正在配网")
                dialog.show()
            }
            10003 -> {
                dialog.setMessage("配网完成")
                dialog.show()
            }
            10004 -> {
            }
            11000 ->                 // You can move this connection step to another place if you want to avoid auto connection.
                mBinder.connect(msg.deviceCode)
            11001 -> {
                CuShionSnum = msg.deviceCode
                // 连接成功，则开始通讯
                if (!isStartSampling) {
                    isStartSampling = true
                    mBinder.startSampling(CuShionSnum)
                    tvCushionState.text = "Cushion:$CuShionSnum connected"
                }

                Log.e("MenuSelectActivity", "device connected")
            }
            11006 -> {
            }
            else -> {
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun showData(msg: MatXDataMessage) {
        val code = msg.deviceCode
        if (CuShionSnum == code) {
            val areaAnalysis = AreaAnalysis(msg.data,0)
            DataAnalysisHelper.deviceState.cushion_now_valueList[0] = areaAnalysis.a1
            DataAnalysisHelper.deviceState.cushion_now_valueList[1] = areaAnalysis.a2
            DataAnalysisHelper.deviceState.cushion_now_valueList[2] = areaAnalysis.b1
            DataAnalysisHelper.deviceState.cushion_now_valueList[3] = areaAnalysis.b2
            DataAnalysisHelper.deviceState.cushion_now_valueList[4] = areaAnalysis.c1
            DataAnalysisHelper.deviceState.cushion_now_valueList[5] = areaAnalysis.c2
            // 当前正在检测压力值，则记录缓存
            if (isGatherPressDataBuffer) {
                statCushionValueListBySeat.add(DataAnalysisHelper.deviceState.cushion_now_valueList)
            }


        }
    }


    /**
     * 播放音乐
     * 播放/停止
     * 持续时间
     */
    fun playOrPauseMedia(playName:String,isPlay:Boolean,iTime:Long) {

        if (isPlay) {
            if (!mediaPlayer.isPlaying) {
                nowPlayName = playName
                mediaPlayer.reset()
                var fd = assets.openFd(nowPlayName)
                mediaPlayer.setDataSource(fd.fileDescriptor, fd.startOffset, fd.length)
                mediaPlayer.isLooping = true // 循环播放
                mediaPlayer.prepare()
                mediaPlayer.start()
            }
            else {
                // 当前播放的和要播放的歌曲不同，则停止重新播放
                if (!nowPlayName.equals(playName)) {
                    nowPlayName = playName
                    mediaPlayer.stop()
                    mediaPlayer.reset()
                    var fd = assets.openFd(nowPlayName)
                    mediaPlayer.setDataSource(fd.fileDescriptor, fd.startOffset, fd.length)
                    mediaPlayer.isLooping = true // 循环播放
                    mediaPlayer.prepare()
                    mediaPlayer.start()
                }
            }

            timerPlayer = Timer()
            timerPlayer?.schedule(object : TimerTask() {
                override fun run() {
                    Log.e("AutomaticActivity", "时间到,停止播放！")
                    mediaPlayer.stop()
                    DataAnalysisHelper.deviceState.iNowAutoProgress = 5
                    mContext.sendBroadcast(Intent(BaseVolume.BROADCAST_AUTO_MODEL))
                    releaseAPress()
                }
            }, iTime)
        }
        // 停止播放
        else {
            if (mediaPlayer.isPlaying) {
                mediaPlayer.stop()
            }
            timerPlayer?.cancel()
            timerPlayer = null
        }
    }



}