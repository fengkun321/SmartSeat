package com.smartCarSeatProject.activity

import ai.nuralogix.anura.sample.activities.ConfigActivity
import ai.nuralogix.anura.sample.activities.MainActivity
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
import android.net.ConnectivityManager
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.os.SystemClock
import android.support.v7.app.AlertDialog
import android.view.View
import android.widget.Toast
import com.ai.nuralogix.anura.sample.face.MNNFaceDetectorAdapter
import com.ai.nuralogix.anura.sample.utils.BundleUtils
import com.alibaba.android.mnnkit.monitor.MNNMonitor
import com.smartCarSeatProject.BuildConfig
import com.smartCarSeatProject.R
import com.smartCarSeatProject.data.*
import com.smartCarSeatProject.data.SeatStatus.*
import com.smartCarSeatProject.tcpInfo.SocketThreadManager
import com.smartCarSeatProject.view.AreaAddWindowHint
import com.smartCarSeatProject.view.SureOperWindowHint
import com.smartCarSeatProject.wifiInfo.WIFIConnectionManager
import kotlinx.android.synthetic.main.layout_menu.*
import com.umeng.commonsdk.statistics.AnalyticsConstants.LOG_TAG
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.opencv.core.Point
import java.io.*


class MenuSelectActivity : BaseActivity(),View.OnClickListener,DfxPipeListener, VideoSignalAnalysisListener, TrackerView.OnSizeChangedListener{

    var Tag = ""
    var isGotoAuto = false
    var isGotoManual = false
    var isGotoDevelop = false
    // 是否在控制界面
    var isControlShow = false
    // 流程：连接设备TCP（7688A和Can）→ 查询座椅状态，是否恢复到初始值 → 恢复完毕提示用户坐好，座椅切换到正在检测 → 正在检测，检测人体参数 → 检测完毕开始测量身高体重并设置气压值 → 充放气完毕后,切换自动模式，点亮
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_menu)

        Tag = this.localClassName

        initUI()
        reciverBand()

        btn1.isEnabled = true
        btn2.isEnabled = true
        btn3.isEnabled = true
        btn4.isEnabled = true

//        val memoryInfoDao = MemoryInfoDao(this)
//        memoryInfoDao.insertSingleData(MemoryDataInfo())
//        memoryInfoDao.closeDb()

        // 人体采集相关
        AnuLogUtil.setShowLog(BuildConfig.DEBUG)
        MNNMonitor.setMonitorEnable(false)
        copyFileOrDir("r21r23h-8.dat")
        // 自动开始采集
        IS_START_AUTOFACE = true
        initNuralogixInfo()



    }

    fun initUI() {

        imgClose.setOnClickListener(this)
        imgReset.setOnClickListener(this)
        imgWIFI.setOnClickListener(this)
        tvReCanConnect.setOnClickListener(this)
        tvReDeviceConnect.setOnClickListener(this)
        btn1.setOnClickListener(this)
        btn2.setOnClickListener(this)
        btn3.setOnClickListener(this)
        btn4.setOnClickListener(this)
        btn1.isEnabled = false

    }

    /** 监听广播  */
    private fun reciverBand() {
        val myIntentFilter = IntentFilter()
        myIntentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION)
        myIntentFilter.addAction(BaseVolume.BROADCAST_FINISH_APPLICATION)
        myIntentFilter.addAction(BaseVolume.BROADCAST_TCP_INFO)
        myIntentFilter.addAction(BaseVolume.BROADCAST_TCP_INFO_CAN)
        myIntentFilter.addAction(BaseVolume.BROADCAST_SEND_INFO)
        myIntentFilter.addAction(BaseVolume.BROADCAST_RESULT_DATA_INFO)
        myIntentFilter.addAction(BaseVolume.BROADCAST_CTR_CALLBACK)
        myIntentFilter.addAction(BaseVolume.BROADCAST_GOBACK_MENU)
        myIntentFilter.addAction(BaseVolume.BROADCAST_RESET_ACTION)
        myIntentFilter.addAction(BaseVolume.COMMAND_TYPE_CHANNEL_STATUS)

        // 注册广播
        registerReceiver(myNetReceiver, myIntentFilter)
    }

    override fun onClick(p0: View?) {
        isGotoAuto = false
        isGotoManual = false
        isGotoDevelop = false

        when(p0?.id) {
            R.id.tvReDeviceConnect -> {
                SocketThreadManager.sharedInstance(this@MenuSelectActivity)?.createDeviceSocket()
//                btn1.isEnabled = !btn1.isEnabled
            }

            R.id.imgClose -> {
                val areaAddWindowHint = AreaAddWindowHint(this,R.style.Dialogstyle,"System",
                        object : AreaAddWindowHint.PeriodListener {
                            override fun refreshListener(string: String) {
                                finish()
//                                SocketThreadManager.sharedInstance(this@MenuSelectActivity)?.clearAllTCPClient()
                            }
                        },"Are you sure to exit the application?",false)
                areaAddWindowHint?.show()
            }
            R.id.imgReset -> {
                val areaAddWindowHint = AreaAddWindowHint(this,R.style.Dialogstyle,"System",
                        object : AreaAddWindowHint.PeriodListener {
                            override fun refreshListener(string: String) {
                                SocketThreadManager.sharedInstance(this@MenuSelectActivity)?.StartSendData(BaseVolume.COMMAND_SET_STATUS_RESET)
                            }
                        },"Are you sure to reset?",false)
                areaAddWindowHint?.show()
            }
            R.id.btn1 -> {

//                gotoMainControlActivity(1)

                // 已经在自动模式下，则直接进入
                if (DataAnalysisHelper.deviceState.seatStatus == SeatStatus.press_automatic.iValue ||
                        DataAnalysisHelper.deviceState.seatStatus == SeatStatus.press_reserve.iValue) {
                    gotoMainControlActivity(1)
                }
                else {
                    // 发送指令，切换到自动模式
                    isGotoAuto = true
                    SocketThreadManager.sharedInstance(this@MenuSelectActivity)?.StartSendData(BaseVolume.COMMAND_SET_MODE_AUTO)
                }

            }
            R.id.btn2 -> {

//                gotoMainControlActivity(2)

                // 已经在手动模式下，则直接进入
                if (DataAnalysisHelper.deviceState.seatStatus == SeatStatus.press_automatic_manual.iValue) {
                    gotoMainControlActivity(2)
                }
                else {
                    // 发送指令，切换到手动模式
                    isGotoManual = true
                    SocketThreadManager.sharedInstance(this@MenuSelectActivity)?.StartSendData(BaseVolume.COMMAND_SET_MODE_MANUAL)
                }

            }
            R.id.btn3 ->
                gotoMainControlActivity(3)
            R.id.btn4 -> {
//                startActivity(Intent(this@MenuSelectActivity,DevelopmentActivity::class.java))

                // 已经在开发者模式下，则直接进入
                if (DataAnalysisHelper.deviceState.seatStatus == SeatStatus.develop.iValue) {
                    val intent = Intent()
                    intent.setClass(this@MenuSelectActivity,DevelopmentActivity::class.java)
                    startActivity(intent)
                }
                else {
                    // 发送指令，切换到开发者模式
                    isGotoDevelop = true
                    SocketThreadManager.sharedInstance(this@MenuSelectActivity)?.StartSendData(BaseVolume.COMMAND_SET_MODE_DEVELOP)
                }
            }
            R.id.tvReCanConnect -> {
                SocketThreadManager.sharedInstance(this@MenuSelectActivity)?.createCanSocket()
            }
        }
    }



    fun gotoMainControlActivity(iNumber:Int) {

        isControlShow = true

        val intent = Intent()
        intent.setClass(this,MainControlActivity::class.java)
        intent.putExtra("iNumber",iNumber)
        startActivity(intent)
    }

    /****
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
                    SocketThreadManager.sharedInstance(this@MenuSelectActivity)?.clearAllTCPClient()
                }
            }
            else if (action == BaseVolume.BROADCAST_FINISH_APPLICATION) {
                finish()
                System.out
            }
            else if (action == BaseVolume.BROADCAST_RESET_ACTION) {
                isControlShow = false
                SocketThreadManager.sharedInstance(this@MenuSelectActivity)?.StartSendData(BaseVolume.COMMAND_SET_STATUS_RESET)
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
                        imgWIFI.visibility = View.GONE
                        ToastMsg("Connect Fail！$strMsg")
                        startTimerHoldSeat(false)
                    }
                    else {
                        tvReDeviceConnect.visibility = View.GONE
                        if (SocketThreadManager.sharedInstance(this@MenuSelectActivity)?.isCanConnected()!!) {
                            imgWIFI.visibility = View.VISIBLE
                            tvReCanConnect.visibility = View.GONE
                        }
                        else {
                            imgWIFI.visibility = View.GONE
                            tvReCanConnect.visibility = View.VISIBLE
                            // 开始连接Can
//                            SocketThreadManager.sharedInstance(this@MenuSelectActivity)?.createCanSocket()
                        }



                        ToastMsg("Connection successful！")
                    }
                    OnStartLoadData(isConnected)
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
                        ToastMsg("Can Connect Fail！$strMsg")
                        startTimerHoldSeat(false)
                    }
                    else {
                        tvReCanConnect.visibility = View.GONE
                        if (SocketThreadManager.sharedInstance(this@MenuSelectActivity)?.isDeviceConnected()!!) {
                            imgWIFI.visibility = View.VISIBLE
                            tvReDeviceConnect.visibility = View.GONE
                        }
                        else {
                            imgWIFI.visibility = View.GONE
                            tvReDeviceConnect.visibility = View.VISIBLE
                        }
                        ToastMsg("Can Connection successful！")
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
                // 状态
                if (strType.equals(BaseVolume.COMMAND_TYPE_SEAT_STATUS,true)) {
                    SocketThreadManager.sharedInstance(this@MenuSelectActivity)?.StopSendData()
                    loadingDialog?.dismiss()
                    // 状态未达到预设值，则让A灰掉，且不能点击
                    if (deviceWorkInfo?.seatStatus < SeatStatus.press_reserve.iValue) {
                        startTimerHoldSeat(false)
                        imgRun1.visibility = View.GONE
                        imgRun2.visibility = View.GONE
                        imgRun4.visibility = View.GONE
                        ToastMsg("Initializing...")
                        btn1.isEnabled = false
                        btn2.isEnabled = false
                        btn4.isEnabled = true

                        areaSeatWindowHint?.dismiss()
                        startCheckPeopleWindowHint?.dismiss()

                        if (!isControlShow) {
                            progressBarWindowHint?.updateContent("Initializing...")
                            progressBarWindowHint?.onSelfShow()
                        }

                    }
                    // 座椅已经达到预设值，提示用户入座，准备检测了
                    else if (deviceWorkInfo?.seatStatus == SeatStatus.press_reserve.iValue){
                        areaSeatWindowHint?.dismiss()
                        startCheckPeopleWindowHint?.dismiss()
                        progressBarWindowHint?.onSelfDismiss()

                        imgRun1.visibility = View.GONE
                        imgRun2.visibility = View.GONE
                        imgRun4.visibility = View.GONE
                        btn1.isEnabled = false
                        btn2.isEnabled = false
                        btn4.isEnabled = true
                        loadingDialog?.dismiss()


                        ToastMsg("Seat initialization is complete！")
                        // 如果菜单界面，触发了进入自动模式
                        if (isGotoAuto) {
                            isGotoAuto = false
                            gotoMainControlActivity(1)
                        }

                        // 提示用户落座,并将座椅切换到正在探测状态
                        startCheckPeopleWindowHint?.showByListener(object : AreaAddWindowHint.PeriodListener {
                            override fun refreshListener(string: String) {
                                SocketThreadManager.sharedInstance(this@MenuSelectActivity)?.StartSendData(BaseVolume.COMMAND_SET_STATUS_PROBE)

                            }
                        })

                    }
                    // 正在探测，则开始采集人体数据
                    else if (deviceWorkInfo?.seatStatus == SeatStatus.press_auto_probe.iValue) {
//                        startAiNuralogixFace()

                        imgRun1.visibility = View.GONE
                        imgRun2.visibility = View.GONE
                        imgRun4.visibility = View.GONE
                        btn1.isEnabled = false
                        btn2.isEnabled = false
                        btn4.isEnabled = true
                        loadingDialog?.dismiss()
                        areaSeatWindowHint?.dismiss()
                        startCheckPeopleWindowHint?.dismiss()
                        ToastMsg("Is to detect！")

                    }
                    // 座椅自动模式，则自动和手动按钮都亮
                    else if (deviceWorkInfo?.seatStatus == SeatStatus.press_automatic.iValue){

                        imgRun1.visibility = View.VISIBLE
                        imgRun2.visibility = View.GONE
                        imgRun4.visibility = View.GONE
                        btn1.isEnabled = true
                        btn2.isEnabled = true
                        btn4.isEnabled = true
                        loadingDialog?.dismiss()
                        areaSeatWindowHint?.dismiss()
                        startCheckPeopleWindowHint?.dismiss()

                        if (progressBarWindowHint?.isShowing!!) {
                            progressBarWindowHint?.onSelfDismiss()
                            ToastMsg("Automatic mode！")
                            startTimerHoldSeat(true)
                        }

                        // 如果菜单界面，触发了进入自动模式
                        if (isGotoAuto) {
                            isGotoAuto = false
                            gotoMainControlActivity(1)
                        }
                    }
                    // 座椅自动下的手动模式
                    else if (deviceWorkInfo?.seatStatus == SeatStatus.press_automatic_manual.iValue){
                        imgRun1.visibility = View.GONE
                        imgRun2.visibility = View.VISIBLE
                        imgRun4.visibility = View.GONE
                        btn1.isEnabled = true
                        btn2.isEnabled = true
                        btn4.isEnabled = true
                        loadingDialog?.dismiss()
                        // 是菜单界面触发的手动模式
                        if (isGotoManual) {
                            isGotoManual = false
                            gotoMainControlActivity(2)
                        }

                    }
                    // 开发者模式
                    else if (deviceWorkInfo?.seatStatus == SeatStatus.develop.iValue) {
                        btn1.isEnabled = true
                        btn2.isEnabled = true

                        imgRun1.visibility = View.GONE
                        imgRun2.visibility = View.GONE
                        imgRun4.visibility = View.VISIBLE
                        btn4.isEnabled = true
                        loadingDialog?.dismiss()
                        // 是菜单界面触发的开发者模式
                        if (isGotoDevelop) {
                            isGotoDevelop = false
                            val intent = Intent()
                            intent.setClass(this@MenuSelectActivity,DevelopmentActivity::class.java)
                            startActivity(intent)
                        }
                    }
                    // 临时自检
                    else if (DataAnalysisHelper.deviceState.seatStatus == SeatStatus.short_check.iValue) {
                        loadingDialog?.dismiss()
                        // 控制界面已经展示，则这个界面不需要展示
                        if (!isControlShow) {
                            areaSeatWindowHint?.show()
                        }

                    }


                }
                // 数据表
                else if (strType.equals(BaseVolume.COMMAND_TYPE_SQL_CTR,true)) {
                    SocketThreadManager.sharedInstance(this@MenuSelectActivity)?.StopSendData()
                }
                // 通道状态
                else if (action == BaseVolume.COMMAND_TYPE_CHANNEL_STATUS) {
                    // 座椅处于检测状态
                    if (DataAnalysisHelper.deviceState.seatStatus == SeatStatus.press_auto_probe.iValue) {
                        // 各个通道都已经平稳，则说明已经自动调整完毕
                        if (DataAnalysisHelper.getInstance(mContext)?.getAllChannelStatus()!! < 0) {
                            SocketThreadManager.sharedInstance(this@MenuSelectActivity)?.StartSendData(BaseVolume.COMMAND_SET_MODE_AUTO)
                        }


                    }


                }
            }
            // 控制回调
            else if (action == BaseVolume.BROADCAST_CTR_CALLBACK) {
                // 停止定时发送
                SocketThreadManager.sharedInstance(this@MenuSelectActivity)?.StopSendData()
            }
            // 从控制页面返回到主页
            else if (action == BaseVolume.BROADCAST_GOBACK_MENU) {
                isControlShow = false

            }
        }
    }

    /** 开始检测 */
    private fun startAiNuralogixFace() {
        IS_START_AUTOFACE = true
        initNuralogixInfo()
        if (this::core.isInitialized) {
            renderingVideoSink.start()
        }
        state = STATE.IDLE
    }



    /** 校验，是否可以开始连接 */
    fun checkStartConnect() {
        val nowWIFIName = WIFIConnectionManager.getInstance(this@MenuSelectActivity)?.nowConnectWifi ?: ""
        Loge(Tag,"当前连接的wifi：$nowWIFIName ！！！")
        if (nowWIFIName.indexOf(BaseVolume.WIFI_SIGN) < 0) {
            ToastMsg("Please connect'${BaseVolume.WIFI_SIGN}' WIFI！")
            imgWIFI.visibility = View.GONE
            imgReset.visibility = View.GONE
            tvReDeviceConnect.visibility = View.VISIBLE
            tvReCanConnect.visibility = View.VISIBLE
            return
        }
        // 先断开连接
        SocketThreadManager.sharedInstance(this@MenuSelectActivity)?.clearAllTCPClient()
        // 开始建立连接！
        SocketThreadManager.sharedInstance(this@MenuSelectActivity)?.createDeviceSocket()
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

            loadingDialog?.show()
            // 查询座椅状态
            SocketThreadManager.sharedInstance(this)?.StartSendData(BaseVolume.COMMAND_READ_SEAT_STATUS)
        }
    }

    override fun onBackPressed() {
//         super.onBackPressed();//注释掉这行,back键不退出activity

        val areaAddWindowHint = AreaAddWindowHint(this,R.style.Dialogstyle,"System",
                object : AreaAddWindowHint.PeriodListener {
                    override fun refreshListener(string: String) {
                        finish()
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
        super.onDestroy()
        SocketThreadManager.sharedInstance(this)?.clearAllTCPClient()

        startTimerHoldSeat(false)

        unregisterReceiver(myNetReceiver)
//        if (IS_START_AUTOFACE)
//            return

        destoryCamera()
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
                    BaseVolume.strPersonDataInfo = strPersonDataInfo
                    Loge("MenuSelectActivity","人体数据：id:${result.measurementID}&信噪比:${result.snr}&心跳:${result.heartRate}&情绪值:${result.msi}&低压:${result.bpDiastolic}&高压:${result.bpSystolic}")
                    measureReuslt.text = "id:${result.measurementID}&信噪比:${result.snr}&心跳:${result.heartRate}&情绪值:${result.msi}&低压:${result.bpDiastolic}&高压:${result.bpSystolic}"

                    if (result.resultIndex + 1 >= MeasurementActivity.TOTAL_NUMBER_CHUNKS) {
                        Loge("MenuSelectActivity","人体数据：测量结束！开始计算身高体重")

                        stopMeasurement(true)

//                        cancelled_tv.visibility = View.GONE
//                        trackerView.visibility = View.GONE
//                        state = STATE.DONE
//                        cloudAnalyzer.stopAnalyzing()
//                        dfxPipe.stopCollect()
//                        if (dialog.isShowing) {
//                            dialog.dismiss()
//                        }

                        if (DataAnalysisHelper.deviceState.seatStatus != SeatStatus.press_auto_probe.iValue) {
                            return@runOnUiThread
                        }

                        progressBarWindowHint?.updateContent("Adjusting now, please wait a moment...")
                        progressBarWindowHint?.onSelfShow()

                        // 测试，跳过调节气压部分，直接进入自动模式
                        SocketThreadManager.sharedInstance(this@MenuSelectActivity).StartSendData(BaseVolume.COMMAND_SET_MODE_AUTO)

//                        // 计算身高体重
//                        DataAnalysisHelper.getInstance(mContext).measureHeightWeight()
//
//                        // 根据男女，身高体重，获取气压表
//                        val isMan = getBooleanBySharedPreferences(SEX_MAN)
//                        val isCN = getBooleanBySharedPreferences(COUNTRY_CN)
//                        val willCtrPressValue = DataAnalysisHelper.getInstance(mContext).getAutoCtrPressByPersonStyle(isMan,isCN)
//                        // 设置气压，并提示用户，正在自动调整
//                        val sendData = CreateCtrDataHelper.getCtrPressAllValueByPerson(willCtrPressValue!!)
//                        SocketThreadManager.sharedInstance(mContext).StartSendDataByCan(sendData[0])
//                        SocketThreadManager.sharedInstance(mContext).StartSendDataByCan(sendData[1])
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


}