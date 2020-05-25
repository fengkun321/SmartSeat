package com.smartCarSeatProject.activity

import ai.nuralogix.anura.sample.activities.ConfigActivity
import ai.nuralogix.anurasdk.network.DeepAffexDataSpec
import ai.nuralogix.anurasdk.network.DeepFXClient
import ai.nuralogix.anurasdk.network.RestClient
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.widget.Toast
import com.smartCarSeatProject.BuildConfig
import com.smartCarSeatProject.R
import com.smartCarSeatProject.data.BaseVolume
import org.json.JSONException
import org.json.JSONObject
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


class WelcomeActivity  : BaseActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_welcome)


        RestClient.getInstance().setListener(object : RestClient.Listener {
            override fun onResult(token: Int, result: String?) {
                when (token) {
                    705 -> onResultRegisterLicense(result)
                    201 -> onResultLogin(result)
                }
            }
            override fun onError(message: String?, token: Int) {
                runOnUiThread {
                    Toast.makeText(mContext, "Rest API error: $message", Toast.LENGTH_LONG).show()
                }
            }
        })

        mHander?.sendEmptyMessageDelayed(0,1500)

    }


    val mHander : Handler = Handler{ it: Message ->
        when (it.what) {
            0 -> {
//                val isValid = checkVaild()
//                if (!isValid){
//                    Log.e("APP测试版，使用倒计时", "30天，已到期，不可使用！")
//                    ToastMsg("APP测试版，使用时间已到期！")
//                }
//                else {
//                    val intent = Intent(this,MenuSelectActivity::class.java)
//                    startActivity(intent)
//                }
//                val intent = Intent(this,MenuSelectActivity::class.java)

                checkUserTokenValid();

//                val intent = Intent(this,MenuSelectActivity::class.java)
//                startActivity(intent)
//                finish()
            }
        }
        false
    }

    var SAMPLE_REST_URL = BuildConfig.SAMPLE_REST_URL
    var SAMPLE_WS_URL = BuildConfig.SAMPLE_WS_URL
    var EMAIL = BuildConfig.EMAIL
    var PASSWORD = BuildConfig.PASSWORD
    var LICENSE_KEY = BuildConfig.LICENSE_KEY
    var STUDY_ID = BuildConfig.STUDY_ID
    var userToken = ""

    /** 检测是否有UserToken,如果没有，就注册 */
    private fun checkUserTokenValid() {
        val pref = getSharedPreferences(ConfigActivity.PREF_NAME, MODE_PRIVATE)
        userToken = pref.getString(ConfigActivity.USER_TOKEN, "").toString()
        if (userToken == "") {
            loadingDialog?.showAndMsg("register...")
            // 自动注册
            DeepAffexDataSpec.REST_SERVER = SAMPLE_REST_URL
            DeepAffexDataSpec.WS_SERVER = SAMPLE_WS_URL

            val results = StringBuilder()
                    .append(Build.MANUFACTURER)
                    .append(" / ")
                    .append(Build.MODEL)
                    .append(" / ")
                    .append(Build.VERSION.RELEASE)
            val device = results.toString()

            RestClient.getInstance().registerLicense(device, BuildConfig.VERSION_NAME, LICENSE_KEY)
        }
        else {
            val intent = Intent(this,MenuSelectActivity::class.java)
            startActivity(intent)
            finish()
        }

    }

    private fun onResultRegisterLicense(result: String?) {

        try {
            val json = JSONObject(result)
            val deviceID = json.getString("DeviceID")
            val deviceToken = json.getString("Token")
            DeepFXClient.getInstance().setTokenAuthorisation(deviceToken)
            RestClient.getInstance().login(EMAIL, "", PASSWORD, deviceToken)
        } catch (e: JSONException) {
            runOnUiThread {
                loadingDialog?.dismiss()
                Toast.makeText(this, "Register license failed... ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun onResultLogin(result: String?) {
        try {
            loadingDialog?.dismiss()
            val json = JSONObject(result)
            userToken = json.getString("Token")
            DeepFXClient.getInstance().setTokenAuthorisation(userToken)
            Toast.makeText(this, "Login with ${EMAIL} success", Toast.LENGTH_LONG).show()
            val pref = getSharedPreferences(ConfigActivity.PREF_NAME, 0)
            val editor = pref.edit()
            editor.putString(ConfigActivity.USER_TOKEN, userToken)
            editor.commit()

            val intent = Intent(this,MenuSelectActivity::class.java)
            startActivity(intent)
            finish()
        } catch (e: JSONException) {
            runOnUiThread {
                Toast.makeText(this, "Login failed... ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun getDatePoor(endDate: Date, nowDate: Date): Long {

        val nd = (1000 * 24 * 60 * 60).toLong()
        val nh = (1000 * 60 * 60).toLong()
        val nm = (1000 * 60).toLong()
        // long ns = 1000;
        // 获得两个时间的毫秒时间差异
        val diff = nowDate.time - endDate.time
        // 计算差多少天
        val day = diff / nd
        // 计算差多少小时
        val hour = diff % nd / nh
        // 计算差多少分钟
        val min = diff % nd % nh / nm
        // 计算差多少秒//输出结果
        // long sec = diff % nd % nh % nm / ns;
        return day
    }


    private fun checkVaild(): Boolean {
        // 这里来做判断，时间是否已经到期（30天）
        val df = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        var firstDate: Date? = null
        try {
            firstDate = df.parse(BaseVolume.StartTime)
            val curDate = Date(System.currentTimeMillis())//获取当前时
            // 小于30天,可以继续使用
            val gap = getDatePoor(firstDate!!, curDate)
            return if (gap > 30) {
                false
            } else {
                true
            }
        } catch (e: ParseException) {
            e.printStackTrace()
        }

        return false
    }

}