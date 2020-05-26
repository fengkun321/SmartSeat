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
import android.graphics.drawable.GradientDrawable
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.os.SystemClock
import android.support.v7.app.AlertDialog
import android.view.View
import android.widget.CompoundButton
import android.widget.TextView
import android.widget.Toast
import com.ai.nuralogix.anura.sample.face.MNNFaceDetectorAdapter
import com.ai.nuralogix.anura.sample.utils.BundleUtils
import com.alibaba.android.mnnkit.monitor.MNNMonitor
import com.smartCarSeatProject.BuildConfig
import com.smartCarSeatProject.R
import com.smartCarSeatProject.data.*
import com.smartCarSeatProject.tcpInfo.SocketThreadManager
import kotlinx.android.synthetic.main.layout_auto_seat.view.*
import kotlinx.android.synthetic.main.layout_automatic.*
import kotlinx.android.synthetic.main.layout_automatic.cancelled_tv
import kotlinx.android.synthetic.main.layout_menu.*
import org.opencv.core.Point


class AutomaticActivity: BaseActivity(), View.OnClickListener,DfxPipeListener, VideoSignalAnalysisListener, TrackerView.OnSizeChangedListener{


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

        updatePersonState()

        // 人体采集相关
        AnuLogUtil.setShowLog(BuildConfig.DEBUG)
        MNNMonitor.setMonitorEnable(false)
//        copyFileOrDir("r21r23h-8.dat")
        // 自动开始采集
        IS_START_AUTOFACE = true
        initNuralogixInfo()

    }

    /** 更新人体数据 */
    fun updatePersonState() {
        // 显示人体数据： id & 信噪比 & 心跳 & 情绪值 & 低压 & 高压
        tvSN.text = "信噪比："+DataAnalysisHelper.deviceState.snr
        tvHeart.text = "心跳："+DataAnalysisHelper.deviceState.HeartRate
        tvMSI.text = "情绪："+DataAnalysisHelper.deviceState.E_Index
        tvBPD.text = "舒张压："+DataAnalysisHelper.deviceState.Dia_BP
        tvBPS.text = "收缩压："+DataAnalysisHelper.deviceState.Sys_BP
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
        DataAnalysisHelper.deviceState.m_gender = isMan
        DataAnalysisHelper.deviceState.m_national = isCN
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
        // 体压自适应启动
        if (cbAutoTiYa.isChecked) {
            // 根据男女，身高体重，获取气压表
            val isMan = getBooleanBySharedPreferences(SEX_MAN)
            val isCN = getBooleanBySharedPreferences(COUNTRY_CN)
            val willCtrPressValue = DataAnalysisHelper.getInstance(mContext)?.getAutoCtrPressByPersonStyle(isMan,isCN)
            // 设置气压，并提示用户，正在自动调整
            val sendData = CreateCtrDataHelper.getCtrPressAllValueByPerson(willCtrPressValue!!)
            SocketThreadManager.sharedInstance(mContext)?.StartSendDataByCan(sendData[0])
            SocketThreadManager.sharedInstance(mContext)?.StartSendDataByCan(sendData[1])
        }



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
                DataAnalysisHelper.deviceState.m_gender = true
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
                DataAnalysisHelper.deviceState.m_gender = false
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
                DataAnalysisHelper.deviceState.m_national = true
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
                DataAnalysisHelper.deviceState.m_national = false
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

    private var IS_START_AUTOFACE = false
    private var IMAGE_WIDTH = 640.0f
    private var IMAGE_HEIGHT = 480.0f
    var MEASUREMENT_DURATION = 30.0
    var TOTAL_NUMBER_CHUNKS = 6
    private var faceIndex = 0
    private lateinit var core: Core
    private val constraintAverager = RollingConstraintAverager()
    private lateinit var render: Render
    private lateinit var viewTracker: GLSurfaceViewTracker
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
        viewTracker.setRenderer(render as GLSurfaceView.Renderer)
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
                    Loge("MenuSelectActivity","人体数据：id:${result.measurementID}&信噪比:${result.snr}&心跳:${result.heartRate}&情绪值:${result.msi}&低压:${result.bpDiastolic}&高压:${result.bpSystolic}")
//                    measureReuslt.text = "id:${result.measurementID}&信噪比:${result.snr}&心跳:${result.heartRate}&情绪值:${result.msi}&低压:${result.bpDiastolic}&高压:${result.bpSystolic}"
                    DataAnalysisHelper.deviceState.snr = "${result.snr}"
                    DataAnalysisHelper.deviceState.HeartRate = "${result.heartRate}"
                    DataAnalysisHelper.deviceState.E_Index = "${result.msi}"
                    updatePersonState()

                    if (result.resultIndex + 1 >= MeasurementActivity.TOTAL_NUMBER_CHUNKS) {
                        Loge("AutomaticActivity","人体数据：测量结束！开始计算身高体重")

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
        if (state == MenuSelectActivity.STATE.EXPOSURE) {
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
                if (state == MenuSelectActivity.STATE.IDLE) {
                    render.showFeatureRegion(showHistogramAndRegions)
                    render.showHistograms(showHistogramAndRegions)
                    state = STATE.EXPOSURE
                }
            }
        } else if (eStatus == ConstraintResult.ConstraintStatus.Error) {
            setCancelledReason(ConstraintResult.ConstraintReason.values()[eReason])
            if (state == MenuSelectActivity.STATE.MEASURING) {
                runOnUiThread {
                    stopMeasurement(true)
                }
            } else if (state == MenuSelectActivity.STATE.COUNTDOWN) {
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
            if (state == MenuSelectActivity.STATE.MEASURING) {
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
        if (state == MenuSelectActivity.STATE.MEASURING) {
            runOnUiThread {
                if (state == MenuSelectActivity.STATE.MEASURING) {
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
                        val builder: AlertDialog.Builder? = this.let {
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


    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(myNetReceiver)

        destoryCamera()

    }

    /** 释放和终止摄像头控件 */
    private fun destoryCamera() {
        var closeStartTime = SystemClock.elapsedRealtime()
        trackerView.destoryView()
        if (state != STATE.UNKNOWN && this::core.isInitialized) {
            core.close()
            cameraSource.close()
            preprocessPipe.close()
            faceTrackerPipe.close()
            dfxPipe.close()
            renderingVideoSink.close()
        }
        if (this::dialog.isInitialized && dialog.isShowing) {
            dialog.dismiss()
        }
    }


}