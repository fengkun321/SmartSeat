package com.ai.nuralogix.anura.sample.settings

import ai.nuralogix.anurasdk.config.DfxPipeConfiguration
import android.os.Bundle
import android.support.v7.preference.CheckBoxPreference
import android.support.v7.preference.EditTextPreference
import android.support.v7.preference.Preference
import android.support.v7.preference.PreferenceFragmentCompat
import com.smartCarSeatProject.R

class DfxPipeConfigurationFragment : PreferenceFragmentCompat() {


    override fun onCreatePreferences(bundle: Bundle?, s: String?) {
        setPreferencesFromResource(R.xml.pref_dfx_config, s)

        setupViews()
    }


    private fun setupViews() {

        val totalChunkNumberView = findPreference(getString(R.string.pref_dfx_total_chunk_number)) as EditTextPreference
        val chunkDurationView = findPreference(getString(R.string.pref_dfx_duration_per_chunk)) as EditTextPreference
        val minFpsView = findPreference(getString(R.string.pref_dfx_min_fps)) as EditTextPreference
        val darkThresholdView = findPreference(getString(R.string.pref_dfx_dark_threshold)) as EditTextPreference
        val brightThresholdView = findPreference(getString(R.string.pref_dfx_bright_threshold)) as EditTextPreference
        val backLightThreholdView = findPreference(getString(R.string.pref_dfx_back_light_threshold)) as EditTextPreference
        val backLightSearchFactorView = findPreference(getString(R.string.pref_dfx_back_light_search_factor)) as EditTextPreference
        val backLightMaxPixelsPctView = findPreference(getString(R.string.pref_dfx_back_light_max_pixels_pct)) as EditTextPreference
        val maxFaceRotateLRDegView = findPreference(getString(R.string.pref_dfx_max_face_rotate_lr_deg)) as EditTextPreference
        val maxFaceRotateUDDegView = findPreference(getString(R.string.pref_dfx_max_face_rotate_ud_deg)) as EditTextPreference
        val minInterPupilDistPxView = findPreference(getString(R.string.pref_dfx_min_inter_pupil_dist_px)) as EditTextPreference
        val maxFaceMovementMmView = findPreference(getString(R.string.pref_dfx_max_face_movement_mm)) as EditTextPreference
        val faceMovementWindowMsView = findPreference(getString(R.string.pref_dfx_face_movement_window_ms)) as EditTextPreference
        val maxEyebrowMovementMmView = findPreference(getString(R.string.pref_dfx_max_eyebrow_movement_mm)) as EditTextPreference
        val cameraRotateChunkThresholdView = findPreference(getString(R.string.pref_dfx_camera_rotate_chunk_threshold)) as EditTextPreference
        val cameraRotateWindowThresholdView = findPreference(getString(R.string.pref_dfx_camera_rotate_window_threshold)) as EditTextPreference

        val minFpsCheckBox = findPreference(getString(R.string.pref_dfx_check_min_fps_enabled)) as CheckBoxPreference
        val faceCenteredCheckBox = findPreference(getString(R.string.pref_dfx_check_face_centered)) as CheckBoxPreference
        val faceDistanceBox = findPreference(getString(R.string.pref_dfx_check_face_distance)) as CheckBoxPreference
        val faceDirectionCheckBox = findPreference(getString(R.string.pref_dfx_check_face_direction)) as CheckBoxPreference
        val lightingCheckBox = findPreference(getString(R.string.pref_dfx_check_lighting)) as CheckBoxPreference
        val faceMovementCheckBox = findPreference(getString(R.string.pref_dfx_check_face_movement)) as CheckBoxPreference
        val backLightCheckBox = findPreference(getString(R.string.pref_dfx_check_back_light)) as CheckBoxPreference
        val cameraMovementCheckBox = findPreference(getString(R.string.pref_dfx_check_camera_movement)) as CheckBoxPreference
        val eyebrowMovementCheckBox = findPreference(getString(R.string.pref_dfx_check_eyebrow_movement)) as CheckBoxPreference

        updateViewValue(totalChunkNumberView, getTextForEditTextPreference(totalChunkNumberView, DfxPipeConfiguration.RuntimeKey.TOTAL_NUMBER_CHUNKS))
        setPrefViewListener(totalChunkNumberView, DfxPipeConfiguration.RuntimeKey.TOTAL_NUMBER_CHUNKS)

        updateViewValue(chunkDurationView, getTextForEditTextPreference(chunkDurationView, DfxPipeConfiguration.RuntimeKey.DURATION_PER_CHUNK))
        setPrefViewListener(chunkDurationView, DfxPipeConfiguration.RuntimeKey.DURATION_PER_CHUNK)

        updateViewValue(minFpsView, getTextForEditTextPreference(minFpsView, DfxPipeConfiguration.RuntimeKey.MINIMUM_FRAME_RATE))
        setPrefViewListener(minFpsView, DfxPipeConfiguration.RuntimeKey.MINIMUM_FRAME_RATE)

        updateViewValue(darkThresholdView, getTextForEditTextPreference(darkThresholdView, DfxPipeConfiguration.RuntimeKey.DARK_THRESHOLD))
        setPrefViewListener(darkThresholdView, DfxPipeConfiguration.RuntimeKey.DARK_THRESHOLD)

        updateViewValue(brightThresholdView, getTextForEditTextPreference(brightThresholdView, DfxPipeConfiguration.RuntimeKey.BRIGHT_THRESHOLD))
        setPrefViewListener(brightThresholdView, DfxPipeConfiguration.RuntimeKey.BRIGHT_THRESHOLD)

        updateViewValue(backLightThreholdView, getTextForEditTextPreference(backLightThreholdView, DfxPipeConfiguration.RuntimeKey.BACK_LIGHT_THRESHOLD))
        setPrefViewListener(backLightThreholdView, DfxPipeConfiguration.RuntimeKey.BACK_LIGHT_THRESHOLD)

        updateViewValue(backLightSearchFactorView, getTextForEditTextPreference(backLightSearchFactorView, DfxPipeConfiguration.RuntimeKey.BACK_LIGHT_SEARCH_FACTOR))
        setPrefViewListener(backLightSearchFactorView, DfxPipeConfiguration.RuntimeKey.BACK_LIGHT_SEARCH_FACTOR)

        updateViewValue(backLightMaxPixelsPctView, getTextForEditTextPreference(backLightMaxPixelsPctView, DfxPipeConfiguration.RuntimeKey.BACK_LIGHT_MAX_PIXELS_PCT))
        setPrefViewListener(backLightMaxPixelsPctView, DfxPipeConfiguration.RuntimeKey.BACK_LIGHT_MAX_PIXELS_PCT)

        updateViewValue(maxFaceRotateLRDegView, getTextForEditTextPreference(maxFaceRotateLRDegView, DfxPipeConfiguration.RuntimeKey.MAX_FACE_ROTATE_LR_DEG))
        setPrefViewListener(maxFaceRotateLRDegView, DfxPipeConfiguration.RuntimeKey.MAX_FACE_ROTATE_LR_DEG)

        updateViewValue(maxFaceRotateUDDegView, getTextForEditTextPreference(maxFaceRotateUDDegView, DfxPipeConfiguration.RuntimeKey.MAX_FACE_ROTATE_UD_DEG))
        setPrefViewListener(maxFaceRotateUDDegView, DfxPipeConfiguration.RuntimeKey.MAX_FACE_ROTATE_UD_DEG)

        updateViewValue(minInterPupilDistPxView, getTextForEditTextPreference(minInterPupilDistPxView, DfxPipeConfiguration.RuntimeKey.MIN_INTER_PUPIL_DIST_PX))
        setPrefViewListener(minInterPupilDistPxView, DfxPipeConfiguration.RuntimeKey.MIN_INTER_PUPIL_DIST_PX)

        updateViewValue(maxFaceMovementMmView, getTextForEditTextPreference(maxFaceMovementMmView, DfxPipeConfiguration.RuntimeKey.MAX_EYEBROW_MOVEMENT_MM))
        setPrefViewListener(maxFaceMovementMmView, DfxPipeConfiguration.RuntimeKey.MAX_EYEBROW_MOVEMENT_MM)

        updateViewValue(faceMovementWindowMsView, getTextForEditTextPreference(faceMovementWindowMsView, DfxPipeConfiguration.RuntimeKey.FACE_MOVEMENT_WINDOW_MS))
        setPrefViewListener(faceMovementWindowMsView, DfxPipeConfiguration.RuntimeKey.FACE_MOVEMENT_WINDOW_MS)

        updateViewValue(maxEyebrowMovementMmView, getTextForEditTextPreference(maxEyebrowMovementMmView, DfxPipeConfiguration.RuntimeKey.MAX_EYEBROW_MOVEMENT_MM))
        setPrefViewListener(maxEyebrowMovementMmView, DfxPipeConfiguration.RuntimeKey.MAX_EYEBROW_MOVEMENT_MM)

        updateViewValue(cameraRotateChunkThresholdView, getTextForEditTextPreference(cameraRotateChunkThresholdView, DfxPipeConfiguration.RuntimeKey.CAMERA_ROTATE_CHUNK_THRESHOLD))
        setPrefViewListener(cameraRotateChunkThresholdView, DfxPipeConfiguration.RuntimeKey.CAMERA_ROTATE_CHUNK_THRESHOLD)

        updateViewValue(cameraRotateWindowThresholdView, getTextForEditTextPreference(cameraRotateWindowThresholdView, DfxPipeConfiguration.RuntimeKey.CAMERA_ROTATE_WINDOW_THRESHOLD))
        setPrefViewListener(cameraRotateWindowThresholdView, DfxPipeConfiguration.RuntimeKey.CAMERA_ROTATE_WINDOW_THRESHOLD)

        setPrefViewListener(minFpsCheckBox, DfxPipeConfiguration.RuntimeKey.CHECK_MINIMUM_FRAME_RATE)
        setPrefViewListener(backLightCheckBox, DfxPipeConfiguration.RuntimeKey.CHECK_BACK_LIGHT)
        setPrefViewListener(faceCenteredCheckBox, DfxPipeConfiguration.RuntimeKey.CHECK_FACE_CENTERED)
        setPrefViewListener(faceDirectionCheckBox, DfxPipeConfiguration.RuntimeKey.CHECK_FACE_DIRECTION)
        setPrefViewListener(faceDistanceBox, DfxPipeConfiguration.RuntimeKey.CHECK_FACE_DISTANCE)
        setPrefViewListener(faceMovementCheckBox, DfxPipeConfiguration.RuntimeKey.CHECK_FACE_MOVEMENT)
        setPrefViewListener(lightingCheckBox, DfxPipeConfiguration.RuntimeKey.CHECK_LIGHTING)
        setPrefViewListener(cameraMovementCheckBox, DfxPipeConfiguration.RuntimeKey.CHECK_CAMERA_MOVEMENT)
        setPrefViewListener(eyebrowMovementCheckBox, DfxPipeConfiguration.RuntimeKey.CHECK_EYEBROW_MOVEMENT)
    }

    private fun setPrefViewListener(view: CheckBoxPreference, runtimeKey: DfxPipeConfiguration.RuntimeKey) {
        arguments?.putString(runtimeKey.name, if (view.isChecked ) "true" else "false")
        view.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { preference, `object` ->
            view.isChecked = `object` as Boolean
            arguments?.putString(runtimeKey.name, if (view.isChecked ) "true" else "false")

            false
        }
    }

    private fun setPrefViewListener(view: EditTextPreference, runtimeKey: DfxPipeConfiguration.RuntimeKey) {
        view.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { preference, `object` ->
            val valueStr = `object`.toString()
            updateViewValue(view, valueStr)
            arguments?.putString(runtimeKey.name, valueStr)

            false
        }
    }

    /**
     * Update provided Edit Text preference view with the value.
     *
     * @param view  Edit Text preference view.
     * @param value Value to updated to.
     */
    private fun updateViewValue(view: EditTextPreference, value: String?) {
        view.summary = value
        view.text = value
    }

    private fun getTextForEditTextPreference(view: EditTextPreference, runtimeKey: DfxPipeConfiguration.RuntimeKey): String? {
        return if (view.text == null) {
            arguments?.getString(runtimeKey.name)
        } else {
            arguments?.putString(runtimeKey.name, view.text)
            view.text
        }
    }
}