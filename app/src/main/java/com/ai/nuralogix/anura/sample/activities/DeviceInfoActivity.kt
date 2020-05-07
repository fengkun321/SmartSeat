package com.ai.nuralogix.anura.sample.activities

import ai.nuralogix.anurasdk.utils.AnuLogUtil
import ai.nuralogix.anurasdk.utils.CameraCapacityCheckUtil
import android.content.Context
import android.graphics.ImageFormat
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.widget.TextView
import com.smartCarSeatProject.R
import java.io.RandomAccessFile
import java.util.*

class DeviceInfoActivity : AppCompatActivity() {
    companion object {
        const val TAG = "DeviceInfoActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device_info)
        val tvInfo: TextView = findViewById(R.id.tv_info)
        tvInfo.movementMethod = ScrollingMovementMethod()

        val stringBuffer = StringBuffer()
        stringBuffer.append("\n----------------Mobile phone basic information------------------\n")
        getBaseInfo(stringBuffer)

        val isSupportVersionCodes = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
        if (isSupportVersionCodes) {
            stringBuffer.append("\n----------------CameraCapacity------------------\n")
            checkCameraCapacity(stringBuffer)
            stringBuffer.append("\n----------------CameraInfo------------------\n")
            getCameraInfo(stringBuffer);
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

    private fun checkCameraCapacity(stringBuffer: StringBuffer) {
        try {
            val lensFacing = CameraCharacteristics.LENS_FACING_FRONT
            val manager = baseContext.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val cameraIdList = manager.cameraIdList
            if (cameraIdList.size <= 0) {
                stringBuffer.append(getString(R.string.no_front_camera))
                stringBuffer.append("\n")
                return
            }
            var cameraCharacteristics: CameraCharacteristics? = null
            for (cameraId in cameraIdList) {
                cameraCharacteristics = manager.getCameraCharacteristics(cameraId)
                val integer = cameraCharacteristics.get(CameraCharacteristics.LENS_FACING)
                if (null != integer && lensFacing == integer) {
                    break
                }
            }
            if (null == cameraCharacteristics) {
                stringBuffer.append("null == cameraCharacteristic:" + getString(R.string.unknown_error))
                stringBuffer.append("\n")
                return
            }
            val isSupportHardwareLevel = CameraCapacityCheckUtil.getCameraDeviceLevel(cameraCharacteristics) !== CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY
            stringBuffer.append("Is Camera Level greater than LEVEL_LIMITED:$isSupportHardwareLevel")
            stringBuffer.append("\n")

            val fpsRanges = cameraCharacteristics.get(CameraCharacteristics.CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES)
            Log.d(MainActivity.TAG, "fpsRanges: " + Arrays.toString(fpsRanges))
            val isSupportFPS = CameraCapacityCheckUtil.isSupportFpsRange(fpsRanges)
            stringBuffer.append("Does the camera frame rate have 30FPS:$isSupportFPS")
            stringBuffer.append("\n")

            val streamConfigurationMap = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
            var maxPixel: Long = 0
            if (null != streamConfigurationMap) {
                val sizes = streamConfigurationMap.getOutputSizes(ImageFormat.YUV_420_888)
                if (null != sizes && sizes.size > 0) {
                    var tempPixel: Long
                    for (size in sizes) {
                        tempPixel = size.height * size.width.toLong()
                        Log.d(MainActivity.TAG, "previewSize width=" + size.width + " height=" + size.height)
                        if (tempPixel > maxPixel) {
                            maxPixel = tempPixel
                        }
                    }
                }
            }
            Log.d(MainActivity.TAG, "maxPixel=$maxPixel")
            val isSupportMaxPix = maxPixel >= 2000000
            stringBuffer.append("Whether the maximum pixel meets 2 million:$isSupportMaxPix")
            stringBuffer.append("\n")

            val isoRange = cameraCharacteristics.get(CameraCharacteristics.SENSOR_INFO_SENSITIVITY_RANGE)
            if (null != isoRange) {
                val isoUpper = isoRange.upper
                val isoLower = isoRange.lower
                Log.d(MainActivity.TAG, "isoRange: RangeUpper=$isoUpper getLower=$isoLower")
            } else {
                Log.w(MainActivity.TAG, "Can't get isoRange")
            }
            val isSupportAdjustIso = null != isoRange
            stringBuffer.append(" Is it possible to adjust the ISO:$isSupportAdjustIso")
            stringBuffer.append("\n")
        } catch (e: Throwable) {
            e.printStackTrace()
            Log.e(MainActivity.TAG, e.message)
            stringBuffer.append("checkCameraCapacity error:" + e.message)
            stringBuffer.append("\n")
        }
    }

    private fun getCameraInfo(stringBuffer: StringBuffer) {
        try {
            val lensFacing = CameraCharacteristics.LENS_FACING_FRONT
            val manager = baseContext.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val cameraIdList = manager.cameraIdList
            if (cameraIdList.size <= 0) {
                stringBuffer.append(getString(R.string.no_front_camera))
                stringBuffer.append("\n")
                return
            }
            var cameraCharacteristics: CameraCharacteristics? = null
            for (cameraId in cameraIdList) {
                cameraCharacteristics = manager.getCameraCharacteristics(cameraId)
                val integer = cameraCharacteristics.get(CameraCharacteristics.LENS_FACING)
                if (null != integer && lensFacing == integer) {
                    break
                }
            }
            if (null == cameraCharacteristics) {
                stringBuffer.append("getCameraInfo null == cameraCharacteristic:" + getString(R.string.unknown_error))
                stringBuffer.append("\n")
                return
            }
            val deviceLevel: Int? = cameraCharacteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL)
            when (deviceLevel) {
                CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_FULL -> {
                    stringBuffer.append("hardware supported level:LEVEL_FULL")
                    stringBuffer.append("\n")
                }
                CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY -> {
                    stringBuffer.append("hardware supported level:LEVEL_LEGACY")
                    stringBuffer.append("\n")
                }
                CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_3 -> {
                    stringBuffer.append("hardware supported level:LEVEL_3")
                    stringBuffer.append("\n")
                }
                CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED -> {
                    stringBuffer.append("hardware supported level:LEVEL_LIMITED")
                    stringBuffer.append("\n")
                }
            }
            val fpsRanges = cameraCharacteristics.get(CameraCharacteristics.CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES)
            stringBuffer.append("fpsRanges: " + Arrays.toString(fpsRanges))
            stringBuffer.append("\n")

            val streamConfigurationMap = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
            if (null != streamConfigurationMap) {
                val sizes = streamConfigurationMap.getOutputSizes(ImageFormat.YUV_420_888)
                if (null != sizes && sizes.size > 0) {
                    for (size in sizes) {
                        stringBuffer.append("previewSize width=" + size.width + " height=" + size.height)
                        stringBuffer.append("\n")
                    }
                }
            }
            val isoRange = cameraCharacteristics.get(CameraCharacteristics.SENSOR_INFO_SENSITIVITY_RANGE)
            if (null != isoRange) {
                val isoUpper = isoRange.upper
                val isoLower = isoRange.lower
                stringBuffer.append("isoRange: RangeUpper=$isoUpper getLower=$isoLower")
                stringBuffer.append("\n")
            } else {
                stringBuffer.append("Can't get isoRange")
                stringBuffer.append("\n")
            }
        } catch (e: Throwable) {
            e.printStackTrace()
            Log.e(MainActivity.TAG, e.message)
            stringBuffer.append("getCameraInfo error:" + e.message)
            stringBuffer.append("\n")
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
