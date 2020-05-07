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

package com.ai.nuralogix.anura.sample.activities

import ai.nuralogix.anurasdk.network.DeepAffexDataSpec
import ai.nuralogix.anurasdk.network.DeepFXClient
import ai.nuralogix.anurasdk.network.RestClient
import ai.nuralogix.anurasdk.utils.AnuLogUtil
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.smartCarSeatProject.BuildConfig
import com.smartCarSeatProject.R
import org.json.JSONException
import org.json.JSONObject

class ConfigActivity : AppCompatActivity() {

    companion object {
        const val TAG = "ANURA_ConfigActivity"

        const val REST_SERVER_KEY = "rest_server_key"
        const val WS_SERVER_KEY = "ws_server_key"
        const val EMAIL_KEY = "email_key"
        const val PASSWORD_KEY = "password_key"
        const val LICENSE_KEY = "license_key"
        const val STUDY_ID_KEY = "study_id_key"
        const val PREF_NAME = "nura_sample_config"
        const val USER_TOKEN = "user_token"
    }

    private lateinit var restServerEt: EditText
    private lateinit var wsServerEt: EditText
    private lateinit var emailEt: EditText
    private lateinit var passwordEt: EditText
    private lateinit var licenseEt: EditText
    private lateinit var studyIdEt: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_config)
        restServerEt = findViewById(R.id.rest_server_et)
        wsServerEt = findViewById(R.id.ws_server_et)
        emailEt = findViewById(R.id.email_et)
        passwordEt = findViewById(R.id.password_et)
        licenseEt = findViewById(R.id.license_et)
        studyIdEt = findViewById(R.id.study_et)


        restoreConfig()

        RestClient.getInstance().setListener(object : RestClient.Listener {
            override fun onResult(token: Int, result: String?) {
                when (token) {
                    705 -> onResultRegisterLicense(result)
                    201 -> onResultLogin(result)
                }
            }

            override fun onError(message: String?, token: Int) {
                runOnUiThread {
                    Toast.makeText(this@ConfigActivity, "Rest API error: $message", Toast.LENGTH_LONG).show()
                }
            }

        })

        AnuLogUtil.d(TAG, "Activity onCreate, savedInstanceState is: " + savedInstanceState)
    }

    override fun onPause() {
        updateConfig()

        AnuLogUtil.d(TAG, "Configure the following for measurement: \n" +
                " REST server: ${MeasurementActivity.SAMPLE_REST_URL} \n" +
                " WS server: ${MeasurementActivity.SAMPLE_WS_URL} \n" +
                " email: ${MeasurementActivity.EMAIL} \n")
        super.onPause()
    }

    override fun onDestroy() {
        AnuLogUtil.d(TAG, "Activity onDestroy.")
        saveConfig()
        super.onDestroy()
    }

    fun onConnectClick(v: View) {
        updateConfig()

        DeepAffexDataSpec.REST_SERVER = MeasurementActivity.SAMPLE_REST_URL
        DeepAffexDataSpec.WS_SERVER = MeasurementActivity.SAMPLE_WS_URL

        val results = StringBuilder()
                .append(Build.MANUFACTURER)
                .append(" / ")
                .append(Build.MODEL)
                .append(" / ")
                .append(Build.VERSION.RELEASE)
        val device = results.toString()

        RestClient.getInstance().registerLicense(device, BuildConfig.VERSION_NAME, MeasurementActivity.LICENSE_KEY)
    }

    private fun updateConfig() {
        MeasurementActivity.SAMPLE_REST_URL = restServerEt.text.toString()
        MeasurementActivity.SAMPLE_WS_URL = wsServerEt.text.toString()
        MeasurementActivity.EMAIL = emailEt.text.toString()
        MeasurementActivity.PASSWORD = passwordEt.text.toString()
        MeasurementActivity.LICENSE_KEY = licenseEt.text.toString()
        MeasurementActivity.STUDY_ID = studyIdEt.text.toString()
    }


    private fun saveConfig() {
        val pref = getSharedPreferences(PREF_NAME, 0)
        val editor = pref.edit()
        editor.putString(REST_SERVER_KEY, MeasurementActivity.SAMPLE_REST_URL)
        editor.putString(WS_SERVER_KEY, MeasurementActivity.SAMPLE_WS_URL)
        editor.putString(EMAIL_KEY, MeasurementActivity.EMAIL)
        editor.putString(PASSWORD_KEY, MeasurementActivity.PASSWORD)
        editor.putString(LICENSE_KEY, MeasurementActivity.LICENSE_KEY)
        editor.putString(STUDY_ID_KEY, MeasurementActivity.STUDY_ID)
        editor.commit()
    }

    private fun restoreConfig() {
        val pref = getSharedPreferences(PREF_NAME, 0)
        val restUrl = pref.getString(REST_SERVER_KEY, MeasurementActivity.SAMPLE_REST_URL)
        restServerEt.setText(restUrl)
        MeasurementActivity.SAMPLE_REST_URL = restUrl!!
        val wsUrl = pref.getString(WS_SERVER_KEY, MeasurementActivity.SAMPLE_WS_URL)
        wsServerEt.setText(wsUrl)
        MeasurementActivity.SAMPLE_WS_URL = wsUrl!!
        val email = pref.getString(EMAIL_KEY, MeasurementActivity.EMAIL)
        emailEt.setText(email)
        MeasurementActivity.EMAIL = email!!
        val password = pref.getString(PASSWORD_KEY, MeasurementActivity.PASSWORD)
        passwordEt.setText(password)
        MeasurementActivity.PASSWORD = password!!
        val license = pref.getString(LICENSE_KEY, MeasurementActivity.LICENSE_KEY)
        licenseEt.setText(license)
        MeasurementActivity.LICENSE_KEY = license!!
        val studyId = pref.getString(STUDY_ID_KEY, MeasurementActivity.STUDY_ID)
        studyIdEt.setText(studyId)
        MeasurementActivity.STUDY_ID = studyId!!
    }

    private fun onResultRegisterLicense(result: String?) {

        try {
            val json = JSONObject(result)
            val deviceID = json.getString("DeviceID")
            val deviceToken = json.getString("Token")
            DeepFXClient.getInstance().setTokenAuthorisation(deviceToken)
            RestClient.getInstance().login(MeasurementActivity.EMAIL, "", MeasurementActivity.PASSWORD, deviceToken)
        } catch (e: JSONException) {
            runOnUiThread {
                Toast.makeText(this, "Register license failed... ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun onResultLogin(result: String?) {
        try {
            val json = JSONObject(result)
            MeasurementActivity.userToken = json.getString("Token")
            DeepFXClient.getInstance().setTokenAuthorisation(MeasurementActivity.userToken)
            Toast.makeText(this, "Login with ${MeasurementActivity.EMAIL} success", Toast.LENGTH_LONG).show()
            val pref = getSharedPreferences(PREF_NAME, 0)
            val editor = pref.edit()
            editor.putString(USER_TOKEN, MeasurementActivity.userToken)
            editor.commit()
        } catch (e: JSONException) {
            runOnUiThread {
                Toast.makeText(this, "Login failed... ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

}
