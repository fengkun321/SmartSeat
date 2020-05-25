package ai.nuralogix.anura.sample.activities

import ai.nuralogix.anurasdk.camera.CameraCapability
import ai.nuralogix.anurasdk.camera.CameraInfo
import ai.nuralogix.anurasdk.utils.AnuLogUtil
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.method.ScrollingMovementMethod
import android.widget.TextView
import com.smartCarSeatProject.R
import java.io.RandomAccessFile
import java.util.*

class DeviceInfoActivity : AppCompatActivity() {
    companion object {
        const val TAG = "DeviceInfoActivity"
    }

    var cameraCapability: CameraCapability? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device_info)
        val tvInfo: TextView = findViewById(R.id.tv_info)
        tvInfo.movementMethod = ScrollingMovementMethod()

        val stringBuffer = StringBuffer()
        stringBuffer.append("\n----------------Mobile phone basic information------------------\n")
        getBaseInfo(stringBuffer)

        cameraCapability = CameraCapability.createCameraCapabilityInstance(this)
        val isSupportVersionCodes = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
        if (isSupportVersionCodes) {
            stringBuffer.append("\n----------------Camera Information------------------\n")
            populateCameraInfos(stringBuffer)
            stringBuffer.append("\n----------------Aura Core Supported Cameras------------------\n")
            populateAnuraSupportedCameraInfos(stringBuffer)
        } else {
            stringBuffer.append(getString(R.string.version_low))
            stringBuffer.append("\n")
        }
        stringBuffer.append("\n----------------CPUInfo------------------\n")
        getCPUInfo(stringBuffer)
        tvInfo.text = stringBuffer.toString()
    }

    private fun getBaseInfo(stringBuffer: StringBuffer) {
        stringBuffer.append("System version:Android " + Build.VERSION.RELEASE + "/API:" + Build.VERSION.SDK_INT)
        stringBuffer.append("\n")
        stringBuffer.append("Phone model:" + Build.MODEL + "/" + Build.MANUFACTURER)
        stringBuffer.append("\n")
        stringBuffer.append("Fingerprint:" + Build.FINGERPRINT)
        stringBuffer.append("\n")
    }

    private fun getCPUInfo(stringBuffer: StringBuffer) {
        val cpuCount = Runtime.getRuntime().availableProcessors()
        stringBuffer.append("CPU core number:$cpuCount")
        stringBuffer.append("\n")

        val maxFreq = getMaxFreq() / 1000f
        stringBuffer.append("CPU maximum frequency:$maxFreq GHz")
        stringBuffer.append("\n")
    }

    private fun populateCameraInfos(stringBuffer: StringBuffer) {

        val cameraInfos = cameraCapability!!.cameraInfos

        if (cameraInfos.size <= 0) {
            stringBuffer.append(getString(R.string.no_front_camera))
            stringBuffer.append("\n")
            return
        }

        for (cameraInfo in cameraInfos) {
            stringBuffer.append("Camera Id: ${cameraInfo.cameraId} \n")
            stringBuffer.append("           facing direction:  ${cameraInfo.cameraFacing.name} \n")
            stringBuffer.append("           device level: ${cameraInfo.deviceLevel.name} \n")
            stringBuffer.append("           ISO max: ${cameraInfo.isoRange?.upper} - min: ${cameraInfo.isoRange?.lower} \n")
            stringBuffer.append("           FPS ranges: " + Arrays.toString(cameraInfo.fpsRanges) + " \n")
            if (null != cameraInfo.outputSizes) {
                for (size in cameraInfo.outputSizes!!) {
                    stringBuffer.append("           previewSize width=" + size.width + " height=" + size.height)
                    stringBuffer.append("\n")
                }
            }
            stringBuffer.append("\n")
        }
    }

    private fun populateAnuraSupportedCameraInfos(stringBuffer: StringBuffer) {

        val cameraInfos = cameraCapability!!.getAnuraSupportedCameras(CameraInfo.CAMERA_CHECK_MASK and CameraInfo.CAMERA_CHECK_ISO_ADJUSTABLE_FLAG.inv())

        if (cameraInfos.size <= 0) {
            stringBuffer.append(getString(R.string.no_front_camera))
            stringBuffer.append("\n")
            return
        }

        for (cameraInfo in cameraInfos) {
            stringBuffer.append("Camera Id: ${cameraInfo.cameraId} \n")
        }
    }

    private fun getMaxFreq(): Long {
        val cpuCount = Runtime.getRuntime().availableProcessors()
        var cpuMaxFreq: Long = -1
        for (i in 0 until cpuCount) {
            val cpuMaxFreqTemp = getCpuMaxFreq(i)
            if (cpuMaxFreq < 0 || cpuMaxFreqTemp > cpuMaxFreq) cpuMaxFreq = cpuMaxFreqTemp
        }
        return cpuMaxFreq
    }


    private fun getCpuMaxFreq(coreNumber: Int): Long {
        val maxPath = "/sys/devices/system/cpu/cpu$coreNumber/cpufreq/cpuinfo_max_freq"
        try {
            val reader = RandomAccessFile(maxPath, "r")
            val maxMhz = reader.readLine().toLong() / 1000
            reader.close()
            return maxMhz
        } catch (e: Exception) {
            AnuLogUtil.e(TAG, e)
        }
        return 0
    }
}
