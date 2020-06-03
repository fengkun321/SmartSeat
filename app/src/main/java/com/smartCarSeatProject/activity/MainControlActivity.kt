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
import android.app.LocalActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.os.SystemClock
import android.support.v7.app.AlertDialog
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.Toast
import com.ai.nuralogix.anura.sample.face.MNNFaceDetectorAdapter
import com.ai.nuralogix.anura.sample.utils.BundleUtils
import com.alibaba.android.mnnkit.monitor.MNNMonitor
import com.smartCarSeatProject.BuildConfig
import com.smartCarSeatProject.R
import com.smartCarSeatProject.data.BaseVolume
import com.smartCarSeatProject.data.DataAnalysisHelper
import com.smartCarSeatProject.data.DeviceWorkInfo
import com.smartCarSeatProject.data.SeatStatus
import com.smartCarSeatProject.tcpInfo.SocketThreadManager
import com.smartCarSeatProject.view.AreaAddWindowHint
import kotlinx.android.synthetic.main.layout_control.*
import org.json.JSONArray
import org.opencv.core.Point
import java.util.*


class MainControlActivity : BaseActivity(),View.OnClickListener,DfxPipeListener, VideoSignalAnalysisListener, TrackerView.OnSizeChangedListener{

    var Tag = ""
    var mactivityManager: LocalActivityManager? = null
    // 当前选项
    var NowShowViewNumber = 1
    var hashMapViews = HashMap<String, View>()
    var oldKeyList: MutableList<String> = ArrayList()

    // 设置过的通道的值
    var setValueBufferByChannel = HashMap<Int,String>()
    var cameraIsStart = false

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

//        if (NowShowViewNumber == 1)
//            cameraIsStart = true
//        else
//            cameraIsStart = false


        switchActivityByNumber(NowShowViewNumber)

        updatePersonState()

        // 人体采集相关
        AnuLogUtil.setShowLog(BuildConfig.DEBUG)
        MNNMonitor.setMonitorEnable(false)
        initNuralogixInfo()

    }

    /** 更新人体数据 */
    fun updatePersonState() {
        // 显示人体数据： id & 信噪比 & 心跳 & 情绪值 & 低压 & 高压
        tvSN.text = "信噪比:"+DataAnalysisHelper.deviceState.snr
        tvHeart.text = "心跳:"+DataAnalysisHelper.deviceState.HeartRate
        tvMSI.text = "情绪值:"+DataAnalysisHelper.deviceState.E_Index
        tvBPD.text = "舒张压:"+DataAnalysisHelper.deviceState.Dia_BP
        tvBPS.text = "收缩压:"+DataAnalysisHelper.deviceState.Sys_BP

        tvHeight.text = "身高:"+DataAnalysisHelper.deviceState.nowHeight
        tvWeight.text = "体重:"+DataAnalysisHelper.deviceState.nowWeight

    }



    companion object {
        private var mainControlActivity : MainControlActivity? = null
        fun getInstance():MainControlActivity?{
            return mainControlActivity
        }
    }

    fun initUI() {
        tvReCanConnect.setOnClickListener(this)
        tvReLocConnect.setOnClickListener(this)
        imgClose.setOnClickListener(this)
        imgReset.setOnClickListener(this)
        imgLeft0.setOnClickListener(this)
        imgLeft1.setOnClickListener(this)
        imgLeft2.setOnClickListener(this)
        imgLeft3.setOnClickListener(this)
        imgLeft4.setOnClickListener(this)

        imgWIFI.visibility = View.VISIBLE
        tvReCanConnect.visibility = View.GONE
        tvReLocConnect.visibility = View.GONE
        if (!SocketThreadManager.sharedInstance(this@MainControlActivity).isCanConnected()) {
            imgWIFI.visibility = View.GONE
            tvReCanConnect.visibility = View.VISIBLE
        }
        if (!SocketThreadManager.sharedInstance(this@MainControlActivity).isCan2Connected()) {
            imgWIFI.visibility = View.GONE
            tvReLocConnect.visibility = View.VISIBLE
        }

    }

    /** 监听广播  */
    private fun reciverBand() {
        val myIntentFilter = IntentFilter()
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
                if (NowShowViewNumber != 1) {
                    SocketThreadManager.sharedInstance(mContext).StartChangeModelByCan(BaseVolume.COMMAND_CAN_MODEL_NORMAL_A_B)
                    switchActivityByNumber(1)
                }
                if (!cameraIsStart) {
                    onResumeCamera()
                }
            }
            R.id.imgLeft2 -> {
                if (NowShowViewNumber != 2) {
                    SocketThreadManager.sharedInstance(mContext).StartChangeModelByCan(BaseVolume.COMMAND_CAN_MODEL_NORMAL_A_B)
                    switchActivityByNumber(2)
                }

            }
            R.id.imgLeft3 -> {
//                rlCamera.x = rlCamera.x + 300
//                rlCamera.y = rlCamera.y + 300
                switchActivityByNumber(3)
            }

            R.id.imgLeft4 -> {
//                rlCamera.x = rlCamera.x - 300
//                rlCamera.y = rlCamera.y - 300
                changeSeatState(SeatStatus.develop.iValue)
            }

            R.id.tvReCanConnect -> {
                SocketThreadManager.sharedInstance(this@MainControlActivity)?.createCanSocket()
            }
            R.id.tvReLocConnect -> {
                SocketThreadManager.sharedInstance(this@MainControlActivity)?.createLocSocket()
            }
        }

    }

    /**
     * 自定义设置位置及其大小
     *
     * @param iv2
     */
    private fun setViewSize(view: RelativeLayout,iLocationValue : Float) {
        val margin = ViewGroup.MarginLayoutParams(view.layoutParams)
        val dpTop = view.top + dp2px(iLocationValue)
        val dpRight = view.right + dp2px(iLocationValue)
        val dpLeft = view.left
        val dpBootom = view.bottom
        margin.setMargins(dpLeft, dpTop, dpRight, dpBootom)
        val metric = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(metric)
        val layoutParams = RelativeLayout.LayoutParams(margin)
        view.layoutParams = layoutParams
    }

    fun dp2px(dpValue: Float): Int {
        val scale: Float = this.getResources().getDisplayMetrics().density
        return (dpValue * scale + 0.5f).toInt()
    }

    private fun switchActivityByNumber(number: Int) {
        NowShowViewNumber = number
        imgLeft1.setImageResource(R.drawable.img_left_1_false)
        imgLeft2.setImageResource(R.drawable.img_left_2_false)
        imgLeft3.setImageResource(R.drawable.img_left_3_false)
        var keyActivity = ""
        val intent1 = Intent()
        when (number) {
            1 -> {
                imgLeft1.setImageResource(R.drawable.img_left_1)
                keyActivity = "AutomaticActivity"
                intent1.setClass(this, AutomaticActivity::class.java!!)
                changeSeatState(SeatStatus.press_automatic.iValue)
            }
            2 -> {
                imgLeft2.setImageResource(R.drawable.img_left_2)
                keyActivity = "ManualActivity"
                intent1.setClass(this, ManualActivity::class.java!!)
                changeSeatState(SeatStatus.press_manual.iValue)
            }
            3 -> {
                imgLeft3.setImageResource(R.drawable.img_left_3)
                keyActivity = "SetWifiActivity"
                intent1.setClass(this, SetWifiActivity::class.java!!)
                changeSeatState(-1)
            }

        }

        if (number == 1) {
            llPersonInfo.visibility = View.VISIBLE
            rlCamera.visibility = View.VISIBLE
        }
        else {
            llPersonInfo.visibility = View.INVISIBLE
            rlCamera.visibility = View.INVISIBLE
        }

        oldKeyList.clear()
        intent1.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        var nowView: View? = hashMapViews.get(keyActivity)
        if (nowView == null) {
            nowView = mactivityManager?.startActivity(keyActivity, intent1)?.decorView
        }
        nowView?.let { hashMapViews.put(keyActivity, it) }
//        container.removeAllViews()
        /**
         * 取得view的父组件，然后移除view
         */
        if (nowView?.parent != null) {
            val viewGroup = (nowView?.parent as ViewGroup)
            viewGroup?.removeView(nowView)
        }
        container.addView(hashMapViews.get(keyActivity), ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT)

        if (number == 1) {
            sendBroadcast(Intent(BaseVolume.BROADCAST_AUTO_MODEL))
        }


    }

    /**
     * 切换座椅状态 -1:不改变座椅状态，仅更新UI
     */
    fun changeSeatState(iState : Int) {
        if (iState != -1) {
            DataAnalysisHelper.deviceState.seatStatus = iState
            // 通知上层
            sendBroadcast(Intent(BaseVolume.COMMAND_TYPE_SEX_MODE))
        }
        imgLeft1.setImageResource(R.drawable.img_left_1_hui)
        imgLeft2.setImageResource(R.drawable.img_left_2_hui)
        imgLeft4.setImageResource(R.drawable.img_left_4_hui)
        imgLeft1.isEnabled = false
        imgLeft2.isEnabled = false
        imgLeft4.isEnabled = false

        // 座椅没有初始化完，或前面流程没跑完，其他按钮都是灰的，不能点
        if (DataAnalysisHelper.deviceState.seatStatus < SeatStatus.press_normal.iValue) {

        }
        // 默认状态
        else if (DataAnalysisHelper.deviceState.seatStatus == SeatStatus.press_normal.iValue) {
            imgLeft1.isEnabled = true
            imgLeft2.isEnabled = true
            imgLeft4.isEnabled = true
            imgLeft1.setImageResource(R.drawable.img_left_1_false)
            imgLeft2.setImageResource(R.drawable.img_left_2_false)
            imgLeft4.setImageResource(R.drawable.img_left_4)
        }
        // 座椅自动模式
        else if (DataAnalysisHelper.deviceState.seatStatus == SeatStatus.press_automatic.iValue){
            imgLeft1.isEnabled = true
            imgLeft2.isEnabled = true
            imgLeft4.isEnabled = true
            imgLeft1.setImageResource(R.drawable.img_left_1_false)
            imgLeft2.setImageResource(R.drawable.img_left_2_false)
            imgLeft4.setImageResource(R.drawable.img_left_4)
            if (NowShowViewNumber == 1) {
                imgLeft1.setImageResource(R.drawable.img_left_1)
            }
        }
        // 手动模式
        else if (DataAnalysisHelper.deviceState.seatStatus == SeatStatus.press_manual.iValue){
            imgLeft1.isEnabled = true
            imgLeft2.isEnabled = true
            imgLeft4.isEnabled = true
            imgLeft1.setImageResource(R.drawable.img_left_1_false)
            imgLeft2.setImageResource(R.drawable.img_left_2_false)
            imgLeft4.setImageResource(R.drawable.img_left_4)
            if (NowShowViewNumber == 2) {
                imgLeft2.setImageResource(R.drawable.img_left_2)
            }
        }
        // 开发者模式
        else if (DataAnalysisHelper.deviceState.seatStatus == SeatStatus.develop.iValue){
            imgLeft1.isEnabled = true
            imgLeft2.isEnabled = true
            imgLeft4.isEnabled = true
            imgLeft1.setImageResource(R.drawable.img_left_1_false)
            imgLeft2.setImageResource(R.drawable.img_left_2_false)
            imgLeft4.setImageResource(R.drawable.img_left_4)
            startActivity(Intent(this,DevelopmentActivity::class.java))
            finish()
        }

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

    /****
     * 广播监听
     */
    private val myNetReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action

            if (action == BaseVolume.BROADCAST_TCP_INFO_CAN) {
                val strType = intent.getStringExtra(BaseVolume.BROADCAST_TYPE)
                // 开始连接
                if (strType.equals(BaseVolume.BROADCAST_TCP_CONNECT_START)) {
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
                        imgWIFI.visibility = View.VISIBLE
                        tvReCanConnect.visibility = View.GONE

                        tvReLocConnect.visibility = View.GONE
                        if (!SocketThreadManager.sharedInstance(this@MainControlActivity).isCan2Connected()) {
                            imgWIFI.visibility = View.GONE
                            tvReLocConnect.visibility = View.VISIBLE
                        }
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
                        tvReLocConnect.visibility = View.VISIBLE
                        imgWIFI.visibility = View.GONE
                    }
                    else {
                        imgWIFI.visibility = View.VISIBLE
                        tvReLocConnect.visibility = View.GONE
                        tvReCanConnect.visibility = View.GONE
                        if (!SocketThreadManager.sharedInstance(this@MainControlActivity).isCanConnected()) {
                            imgWIFI.visibility = View.GONE
                            tvReCanConnect.visibility = View.VISIBLE
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
            }
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
                    Loge("MenuSelectActivity","人体数据：id:${result.measurementID}&信噪比:${result.snr}&心跳:${result.heartRate}&情绪值:${result.msi}&低压:${result.bpDiastolic}&高压:${result.bpSystolic}")
                    DataAnalysisHelper.deviceState.snr = "${result.snr}"
                    DataAnalysisHelper.deviceState.HeartRate = "${result.heartRate}"
                    DataAnalysisHelper.deviceState.E_Index = "${result.msi}"
                    DataAnalysisHelper.deviceState.Dia_BP = "${result.bpDiastolic}"
                    DataAnalysisHelper.deviceState.Sys_BP = "${result.bpSystolic}"

                    tvSN.text = "信噪比:"+DataAnalysisHelper.deviceState.snr
                    tvHeart.text = "心跳:"+DataAnalysisHelper.deviceState.HeartRate
                    tvMSI.text = "情绪值:"+DataAnalysisHelper.deviceState.E_Index
                    if (result.bpDiastolic != 0) {
                        tvBPD.text = "舒张压:"+DataAnalysisHelper.deviceState.Dia_BP
                    }
                    if (result.bpSystolic != 0) {
                        tvBPS.text = "收缩压:"+DataAnalysisHelper.deviceState.Sys_BP
                    }

                    if (result.resultIndex + 1 >= MeasurementActivity.TOTAL_NUMBER_CHUNKS) {
                        Loge("MenuSelectActivity","人体数据：测量结束！开始计算身高体重")
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
        onResumeCamera()
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
        onPauseCamera()
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

    fun onResumeCamera() {
        if (NowShowViewNumber == 1) {
            cameraIsStart = true
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
    }

    fun onPauseCamera() {
        if (state != STATE.UNKNOWN) {
            stopMeasurement(true)
            if (this::core.isInitialized) {
                renderingVideoSink.stop()
            }
        }
    }

    override fun onDestroy() {

        destoryCamera()

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