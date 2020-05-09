package ai.nuralogix.anura.sample.settings

import com.smartCarSeatProject.R
import ai.nuralogix.anurasdk.camera.CameraCapability
import ai.nuralogix.anurasdk.camera.CameraInfo
import ai.nuralogix.anurasdk.utils.AnuLogUtil
import android.os.Bundle
import android.support.v7.preference.ListPreference
import android.support.v7.preference.Preference
import android.support.v7.preference.PreferenceFragmentCompat

class CameraConfigurationFragment: PreferenceFragmentCompat() {
    companion object {
        const val TAG = "CameraConfigFragment"
        const val CAMERA_ID_KEY = "cameraId"
    }

    override fun onCreatePreferences(bundle: Bundle?, s: String?) {
        setPreferencesFromResource(R.xml.pref_camera_config, s)
        setupViews()
    }

    private fun setupViews() {
        val cameraCapability = CameraCapability.createCameraCapabilityInstance(context!!)
        val cameraInfoPref = findPreference(getString(R.string.pref_camera_info)) as ListPreference
        val cameraInfos = cameraCapability.cameraInfos

        val cameraInfoEntries = arrayListOf<String>()
        val cameraInfoEntryValues = arrayListOf<String>()
        var defaultIndex = 0
        for ((index, info) in cameraInfos.withIndex()) {
            if (info.cameraFacing == CameraInfo.CameraFacing.FACING_FRONT) {
                defaultIndex = index
            }
            cameraInfoEntries.add(info.cameraId + " " + info.cameraFacing.name)
            cameraInfoEntryValues.add(info.cameraId)
        }

        cameraInfoPref.entries = cameraInfoEntries.toTypedArray()
        cameraInfoPref.entryValues = cameraInfoEntryValues.toTypedArray()
        cameraInfoPref.setValueIndex(defaultIndex)
        cameraInfoPref.summary = cameraInfos[defaultIndex].toString()

        cameraInfoPref.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { preference, `object` ->
            AnuLogUtil.d(TAG, "preference before ${cameraInfoPref.value}, $arguments")
            val selectedIndex = cameraInfoPref.findIndexOfValue(`object` as String)
            cameraInfoPref.setValueIndex(selectedIndex)
            arguments?.putString(CAMERA_ID_KEY, `object`)
            cameraInfoPref.summary = cameraInfos[selectedIndex].toString()
            AnuLogUtil.d(TAG, "preference after ${cameraInfoPref.value}, $arguments")

            false
        }
    }
}