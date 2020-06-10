/*
 *              Copyright (c) 2016-2019, Nuralogix Corp.
 *                      All Rights reserved
 *
 *      THIS SOFTWARE IS LICENSED BY AND IS THE CONFIDENTIAL AND
 *      PROPRIETARY PROPERTY OF NURALOGIX CORP. IT IS
 *      PROTECTED UNDER THE COPYRIGHT LAWS OF THE USA, CANADA
 *      AND OTHER FOREIGN COUNTRIES. THIS SOFTWARE OR ANY
 *      PART THEREOF, SHALL NOT, WITHOUT THE PRIOR WRITTEN CONSENT
 *      OF NURALOGIX CORP, BE USED, COPIED, DISCLOSED,
 *      DECOMPILED, DISASSEMBLED, MODIFIED OR OTHERWISE TRANSFERRED
 *      EXCEPT IN ACCORDANCE WITH THE TERMS AND CONDITIONS OF A
 *      NURALOGIX CORP SOFTWARE LICENSE AGREEMENT.
 */

package ai.nuralogix.anura.sample.activities

import ai.nuralogix.anura.sample.activities.MeasurementActivity.Companion.EMAIL
import ai.nuralogix.anura.sample.activities.MeasurementActivity.Companion.SAMPLE_REST_URL
import ai.nuralogix.anura.sample.settings.CameraConfigurationFragment
import ai.nuralogix.anura.sample.settings.DfxPipeConfigurationFragment
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
import android.content.Intent
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.*
import android.widget.TextView
import android.widget.Toast
import com.ai.nuralogix.anura.sample.face.MNNFaceDetectorAdapter
import com.ai.nuralogix.anura.sample.utils.BundleUtils
import com.smartCarSeatProject.BuildConfig
import org.json.JSONArray
import org.opencv.core.Point
import java.io.FileWriter
import java.io.IOException
import com.smartCarSeatProject.R
import com.smartCarSeatProject.data.BaseVolume

class MeasurementActivity : AppCompatActivity(), DfxPipeListener, VideoSignalAnalysisListener, TrackerView.OnSizeChangedListener {

    companion object {
        const val TAG = "MeasurementActivity"

        var SAMPLE_REST_URL = BuildConfig.SAMPLE_REST_URL
        var SAMPLE_WS_URL = BuildConfig.SAMPLE_WS_URL
        var EMAIL = BuildConfig.EMAIL
        var PASSWORD = BuildConfig.PASSWORD
        var LICENSE_KEY = BuildConfig.LICENSE_KEY
        var STUDY_ID = BuildConfig.STUDY_ID

        var userToken: String? = null

        //Generally, the front camera has a rotation angle, and the width and height are reversed, please note
        var IMAGE_WIDTH = 640.0f
        var IMAGE_HEIGHT = 480.0f
        var MEASUREMENT_DURATION = 30.0
        var TOTAL_NUMBER_CHUNKS = 6

        val handler = Handler(Looper.getMainLooper())

        val FACE_ENGINE_KEY = "face_engine"
    }

    private lateinit var lastStatus: ConstraintResult.ConstraintStatus
    private lateinit var lastConstraintReason: ConstraintResult.ConstraintReason
    private lateinit var core: Core
    private lateinit var cameraAdapter: CameraAdapter
    private lateinit var cloudAnalyzer: CloudAnalyzer
    private lateinit var cloudAnalyzerListener: CloudAnalyzerListener
    private lateinit var cameraSource: VideoSource
    private lateinit var preprocessPipe: VideoPipe
    private lateinit var faceTrackerPipe: VideoPipe
    private lateinit var dfxPipe: DfxPipe
    private lateinit var signalAnalysisPipe: VideoPipe
    private lateinit var renderingVideoSink: RenderingVideoSink
    private lateinit var render: Render
    private val constraintAverager = RollingConstraintAverager()

    private lateinit var viewTracker: GLSurfaceViewTracker
    private lateinit var countdownText: TextView
    private lateinit var trackerView: TrackerView
    private lateinit var countdown: Countdown
    private lateinit var snrTextView: TextView
    private lateinit var heartBeatTextView: TextView
    private lateinit var cancelledReasonTv: TextView
    private lateinit var meansurementIdTv: TextView
    private lateinit var msiTv: TextView
    private lateinit var bpDTv: TextView
    private lateinit var bpSTv: TextView
    private var state = STATE.UNKNOWN
    private var firstFrameTimestamp = 0L
    private val showHistogramAndRegions = true
    private lateinit var dialog: AlertDialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) //FLAG_KEEP_SCREEN_ON
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity_measurement)

        val previewSize = ImagePreviewResolutionUtils.getFitImagePreviewResolution()
        IMAGE_WIDTH = previewSize.width.toFloat();
        IMAGE_HEIGHT = previewSize.height.toFloat();

        restoreConfig()

        viewTracker = findViewById(R.id.tracker_opengl_view)
        countdownText = findViewById(R.id.countdown)
        trackerView = findViewById(R.id.tracker_ui_view)
        trackerView.setImageDimension(IMAGE_HEIGHT, IMAGE_WIDTH)
        trackerView.showMask(true)
        trackerView.setOnSizeChangedListener(this)

        snrTextView = findViewById(R.id.snr_tv)
        heartBeatTextView = findViewById(R.id.heartbeat_tv)
        cancelledReasonTv = findViewById(R.id.cancelled_tv)
        meansurementIdTv = findViewById(R.id.measurementId_tv)
        msiTv = findViewById(R.id.msi_tv)
        bpDTv = findViewById(R.id.bpd_tv)
        bpSTv = findViewById(R.id.bps_tv)

        // start pipeline
        if (userToken != null) {
            DeepAffexDataSpec.REST_SERVER = SAMPLE_REST_URL
            DeepAffexDataSpec.WS_SERVER = SAMPLE_WS_URL
            DeepFXClient.getInstance().setTokenAuthorisation(userToken)
            DeepFXClient.getInstance().connect()
            initPipeline()
        } else {
            Toast.makeText(this, resources.getString(R.string.Error_Server_Connection), Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        menuInflater.inflate(R.menu.menus, menu)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        when (item?.itemId) {
            R.id.action_dfx_setting -> {
                val intent = SettingsActivity.createIntent(this, DfxPipeConfigurationFragment::class.java.name)
                intent.putExtra(BundleUtils.DFX_BUNDLE_KEY, BundleUtils.createRuntimeBundle(dfxPipe.configuration))
                startActivity(intent)
                return true
            }
        }


        return super.onOptionsItemSelected(item)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        AnuLogUtil.d(TAG, "onSizeChanged w=" + w + " h=" + h)
        if (!this::dfxPipe.isInitialized) {
            return
        }
        val targetBox = ImageUtils.getFaceTargetBox(w, h, IMAGE_HEIGHT.toInt(), IMAGE_WIDTH.toInt())
        dfxPipe.configuration.setStartupParameter(DfxPipeConfiguration.StartupKey.BOX_CENTER_X_PCT, targetBox.boxCenterX_pct.toString())
        dfxPipe.configuration.setStartupParameter(DfxPipeConfiguration.StartupKey.BOX_CENTER_Y_PCT, targetBox.boxCenterY_pct.toString())
        dfxPipe.configuration.setStartupParameter(DfxPipeConfiguration.StartupKey.BOX_WIDTH_PCT, targetBox.boxWidth_pct.toString())
        dfxPipe.configuration.setStartupParameter(DfxPipeConfiguration.StartupKey.BOX_HEIGHT_PCT, targetBox.boxHeight_pct.toString())
        dfxPipe.updateStartUpConfig()
        trackerView.setFaceTargetBox(targetBox)
    }

    /**************************************Below are private methods****************************************/

    private fun restoreConfig() {
        val pref = getSharedPreferences(ConfigActivity.PREF_NAME, MODE_PRIVATE)
        val restUrl = pref.getString(ConfigActivity.REST_SERVER_KEY, SAMPLE_REST_URL)
        SAMPLE_REST_URL = restUrl!!
        val wsUrl = pref.getString(ConfigActivity.WS_SERVER_KEY, SAMPLE_WS_URL)
        SAMPLE_WS_URL = wsUrl!!
        val email = pref.getString(ConfigActivity.EMAIL_KEY, EMAIL)
        EMAIL = email!!
        val password = pref.getString(ConfigActivity.PASSWORD_KEY, PASSWORD)
        PASSWORD = password!!
        val license = pref.getString(ConfigActivity.LICENSE_KEY, LICENSE_KEY)
        LICENSE_KEY = license!!
        val studyId = pref.getString(ConfigActivity.STUDY_ID_KEY, STUDY_ID)
        STUDY_ID = studyId!!
        userToken = pref.getString(ConfigActivity.USER_TOKEN, null)
    }

    private fun initPipeline() {
        val faceIndex = intent.getIntExtra(FACE_ENGINE_KEY, 0)
        core = Core.createAnuraCore(this)
        constraintAverager.setReasonSpan(60)
        val format = VideoFormat(VideoFormat.ColorFormat.BGRA, 30, IMAGE_HEIGHT.toInt(), IMAGE_WIDTH.toInt())

        //val videoFormat = VideoFormat(VideoFormat.VideoCodec.H264, VideoFormat.ColorFormat.BGRA, 30, IMAGE_HEIGHT.toInt(), IMAGE_WIDTH.toInt())

        render = Render.createGL20Render(format)
        viewTracker.setRenderer(render as GLSurfaceView.Renderer)
        render.showFeatureRegion(showHistogramAndRegions)

        var visageFaceTracker: FaceTrackerAdapter = MNNFaceDetectorAdapter(baseContext)
        visageFaceTracker.setTrackingRegion(0, 0, IMAGE_WIDTH.toInt(), IMAGE_HEIGHT.toInt())

        cloudAnalyzerListener = object : CloudAnalyzerListener {
            override fun onStartAnalyzing() {
            }

            override fun onResult(result: AnalyzerResult) {
                val jsonResult = result.jsonResult
                AnuLogUtil.d(TAG, "JSON result: $jsonResult index: ${result.resultIndex}")

                runOnUiThread {
                    meansurementIdTv.text = "Measurement ID: ${result.measurementID}"
                    snrTextView.text = "SNR: ${result.snr}"
                    heartBeatTextView.text = "Heart Beat: ${result.heartRate}"
                    msiTv.text = "MSI: ${result.msi}"
                    bpDTv.text = "Blood Pressure Diastolic: ${result.bpDiastolic}"
                    bpSTv.text = "Blood Pressure Systolic: ${result.bpSystolic}"

                    // 人体数据： id & 信噪比 & 心跳 & 情绪值 & 低压 & 高压
                    val strPersonDataInfo =  "${result.measurementID}&${result.snr}&${result.heartRate}&${result.msi}&${result.bpDiastolic}&${result.bpSystolic}"


                    if (result.resultIndex + 1 >= TOTAL_NUMBER_CHUNKS) {
                        stopMeasurement(true)
                    }
                }
            }

            override fun onError(error: AnuraError) {
                AnuLogUtil.e(TAG, "CloudAnalyzerListener onError:" + error.name)
                runOnUiThread {
                    if (error == AnuraError.LOW_SNR) {
                        AnuLogUtil.e(TAG, "SNR is less than 1, signal quality is not good")
                        Toast.makeText(baseContext, "SNR is less than 1, signal quality is not good", Toast.LENGTH_LONG).show()
                    }
                    stopMeasurement(true)
                }
            }
        }

//        fileVideoSource = FileVideoSourceImpl("FileVideoSource", core, videoFormat)
//        val videoDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
//        AnuLogUtil.d(TAG, "Video file path: ${videoDir.absolutePath}")
//        fileVideoSource.setFileSource(videoDir.absolutePath + "/Camera/1583406136199.mp4")

        val dfxConfig = DfxPipeConfiguration(this, null)
        TOTAL_NUMBER_CHUNKS = dfxConfig.getRuntimeParameterInt(DfxPipeConfiguration.RuntimeKey.TOTAL_NUMBER_CHUNKS, 6)
        val duration = TOTAL_NUMBER_CHUNKS * dfxConfig.getRuntimeParameterFloat(DfxPipeConfiguration.RuntimeKey.DURATION_PER_CHUNK, 5f)
        MEASUREMENT_DURATION = duration.toDouble()
        trackerView.setMeasurementDuration(duration.toDouble())

        cameraAdapter = CameraAdapter.createAndroidCamera2Adapter(intent.getStringExtra(CameraConfigurationFragment.CAMERA_ID_KEY), core, null)
        cloudAnalyzer = CloudAnalyzer.createCloudAnalyzer(core, DeepFXClient.getInstance(), cloudAnalyzerListener)
        cameraSource = VideoSource.createCameraSource("CameraSource", core, format, cameraAdapter)
        preprocessPipe = VideoPipe.createPreprocessPipe("PreprocessPipe", core, format)
        faceTrackerPipe = VideoPipe.createFaceTrackerPipe("FaceTrackerPipe", core, format, visageFaceTracker)
        renderingVideoSink = RenderingVideoSink.createRenderingVideoSink("RenderingSink", core, format, render)
        //dumpVideoSink = VideoSink.createDumpVideoSink("DumpVideoSink", core, format)


        val display = resources.displayMetrics
        val targetBox = ImageUtils.getFaceTargetBox(display.widthPixels, display.heightPixels, IMAGE_HEIGHT.toInt(), IMAGE_WIDTH.toInt())
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
                    countdownText.text = value.toString()
                }
            }

            override fun onCountdownEnd() {
                if (userToken != null) {
                    runOnUiThread {
                        startMeasurement()
                    }
                } else {
                    runOnUiThread {
                        stopMeasurement(true)
                        countdown.start()
                        AnuLogUtil.d(TAG, "No user token restart...")
                    }
                }
            }

            override fun onCountdownCancel() {

                runOnUiThread {
                    stopMeasurement(true)
                    countdownText.text = "0"
                }
            }

        })

        trackerView.setMeasurementDuration(MEASUREMENT_DURATION)
    }

    override fun onResume() {

        if (this::core.isInitialized) {
            renderingVideoSink.start()
        }
        state = STATE.IDLE
        super.onResume()
    }

    override fun onNewIntent(intent: Intent?) {
        val configBundle = intent?.getBundleExtra(BundleUtils.DFX_BUNDLE_KEY)
        configBundle?.let {
            AnuLogUtil.d(TAG, "on resume with bundle $configBundle")
            BundleUtils.updateRuntimeConfiguration(dfxPipe.configuration, configBundle)
            dfxPipe.updateRuntimeConfig()
            TOTAL_NUMBER_CHUNKS = dfxPipe.configuration.getRuntimeParameterInt(DfxPipeConfiguration.RuntimeKey.TOTAL_NUMBER_CHUNKS, 6)
            val duration = TOTAL_NUMBER_CHUNKS * dfxPipe.configuration.getRuntimeParameterFloat(DfxPipeConfiguration.RuntimeKey.DURATION_PER_CHUNK, 5f)
            MEASUREMENT_DURATION = duration.toDouble()
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

    override fun onDestroy() {
        AnuLogUtil.d(TAG, "onDestroy")

        var closeStartTime = SystemClock.elapsedRealtime()
        trackerView.destoryView()
        if (state != STATE.UNKNOWN && this::core.isInitialized) {

            core.close()
            var closeStep0Time = SystemClock.elapsedRealtime()
            AnuLogUtil.d(TAG, "Close step 0 consuming: ${closeStep0Time - closeStartTime}")
            cameraSource.close()
            var closeStep1Time = SystemClock.elapsedRealtime()
            AnuLogUtil.d(TAG, "Close step 1 consuming: ${closeStep1Time - closeStep0Time}")
            preprocessPipe.close()
            faceTrackerPipe.close()
            var closeStep2Time = SystemClock.elapsedRealtime()
            AnuLogUtil.d(TAG, "Close step 1 consuming: ${closeStep2Time - closeStep1Time}")
            dfxPipe.close()
            var closeStep3Time = SystemClock.elapsedRealtime()
            AnuLogUtil.d(TAG, "Close step 2 consuming: ${closeStep3Time - closeStep2Time}")
            renderingVideoSink.close()
            var closeStep4Time = SystemClock.elapsedRealtime()
            AnuLogUtil.d(TAG, "Close step 4 consuming: ${closeStep4Time - closeStep3Time}")
        }
        if (this::dialog.isInitialized && dialog.isShowing) {
            dialog.dismiss()
        }
        super.onDestroy()
        var closeStep5Time = SystemClock.elapsedRealtime()
        AnuLogUtil.d(TAG, "Close step 5 consuming: ${closeStep5Time - closeStartTime}")
    }

    override fun onDCValuesReceived(dcResult: DCResult?) {
        if (state == STATE.EXPOSURE) {
            if (dcResult != null && !cameraAdapter.adjustExposure(dcResult.greenDC, 115.0f, 124.0f, 100)) {
                AnuLogUtil.d(TAG, "Adjust exposure success.")
                countdown.start()
                state = STATE.COUNTDOWN
                runOnUiThread {
                    cancelledReasonTv.text = resources.getString(R.string.PERFECT_HOLD_STILL)
                }
            } else if (dcResult == null) {
                AnuLogUtil.d(TAG, "No dc value, no need to adjust exposure.")
                countdown.start()
                state = STATE.COUNTDOWN
                runOnUiThread {
                    cancelledReasonTv.text = resources.getString(R.string.PERFECT_HOLD_STILL)
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
            AnuLogUtil.v(TAG, "Constraints Status: $eStatus Reason: $eReason State: $state")
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
                runOnUiThread {
                    countdownText.text = "0"
                }
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
        AnuLogUtil.d(TAG, "Receives chunk payload: ${payload.chunkNumber}")
        runOnUiThread {
            cancelledReasonTv.text = resources.getString(R.string.Analyzing_Data) + " " + (payload.chunkNumber + 1)
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
                    val progressPercent = totalFrameDuration * 100 / MEASUREMENT_DURATION
                    if (progressPercent >= 100.0) {
                        state = STATE.DONE
                        stopMeasurement(false)
                        if (this@MeasurementActivity::dialog.isInitialized && dialog.isShowing) {
                            dialog.dismiss()
                        }
                        val builder: AlertDialog.Builder? = this@MeasurementActivity.let {
                            AlertDialog.Builder(it)
                        }
                        builder?.setMessage("Wait for final result...")?.setCancelable(false)
                        dialog = builder?.create()!!
                        dialog.show()
                    }
                    trackerView.setMeasurementProgress(progressPercent.toFloat())
                    if (frameNumber % 10 == 0L) {
                        AnuLogUtil.d(TAG, "Frame number: $frameNumber total frame length: $totalFrameDuration seconds fps: $frameRate")
                    }
                }
            }
        } else {
            if (frameNumber % 10 == 0L) {
                AnuLogUtil.d(TAG, "Frame number: $frameNumber seconds fps: $frameRate")
            }
        }
    }

    private fun startMeasurement() {
        if (lastStatus != ConstraintResult.ConstraintStatus.Good) {
            AnuLogUtil.e(TAG, "lastStatus=" + lastStatus.name + " !=Good")
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
        cancelledReasonTv.text = resources.getString(R.string.MEASUREMENT_STARTED)
        countdownText.visibility = View.GONE
        meansurementIdTv.text = "Measurement ID: "
        snrTextView.text = "SNR: "
        heartBeatTextView.text = "Heart Beat: "
        msiTv.text = "MSI: "
        bpDTv.text = "Blood Pressure Diastolic: "
        bpSTv.text = "Blood Pressure Systolic: "

        state = STATE.MEASURING
        dfxPipe.startCollect("")
        cloudAnalyzer.startAnalyzing(STUDY_ID, "")
    }

    private fun stopMeasurement(stopResult: Boolean) {
        AnuLogUtil.d(TAG, "Stop measurement: $stopResult")
        if (!this::core.isInitialized) {
            return
        }
        handler.removeCallbacksAndMessages(null)
        countdown.stop()
        cameraAdapter.lockWhiteBalance(false)
        cameraAdapter.lockExposure(false)
        cameraAdapter.lockFocus(false)

        render.showHistograms(false)
        render.showFeatureRegion(false)
        trackerView.showMask(true)
        trackerView.showMeasurementProgress(false)
        trackerView.setMeasurementProgress(0.0f)
        countdownText.text = "0"
        countdownText.visibility = View.VISIBLE
        cancelledReasonTv.text = ""

        if (stopResult) {
            state = STATE.IDLE
            cloudAnalyzer.stopAnalyzing()
            dfxPipe.stopCollect()
            if (this@MeasurementActivity::dialog.isInitialized && dialog.isShowing) {
                dialog.dismiss()
            }
        }
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
            AnuLogUtil.e(TAG, reason)
            cameraAdapter.lockExposure(false)
            cameraAdapter.lockWhiteBalance(false)
            cameraAdapter.lockFocus(false)
        }
        lastConstraintReason = eReason

        runOnUiThread {
            cancelledReasonTv.text = reason
        }
    }

    private enum class STATE {
        UNKNOWN,
        IDLE,
        EXPOSURE,
        COUNTDOWN,
        MEASURING,
        DONE,
    }
}