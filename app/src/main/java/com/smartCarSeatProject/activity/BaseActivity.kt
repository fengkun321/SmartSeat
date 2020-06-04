package com.smartCarSeatProject.activity

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import com.smartCarSeatProject.R
import com.smartCarSeatProject.data.BaseVolume
import com.smartCarSeatProject.data.DataAnalysisHelper
import com.smartCarSeatProject.tcpInfo.SocketThreadManager
import com.smartCarSeatProject.view.AreaAddWindowHint
import com.smartCarSeatProject.view.LoadingDialog
import com.smartCarSeatProject.view.ProgressBarWindowHint
import com.umeng.analytics.MobclickAgent
import java.util.*


open class BaseActivity : AppCompatActivity(){

    protected lateinit var mContext:Context
    // 加载转圈
    lateinit var loadingDialog:LoadingDialog
    // 自定义进度条
    lateinit var progressBarWindowHint: ProgressBarWindowHint
    // 是否已经检测过人体数据
    protected var isCheckedPersonInfo = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        pref = getSharedPreferences(PREF_NAME, 0)

        mContext = this
        loadingDialog = LoadingDialog(this, R.style.LoadingDialogStyle)
        progressBarWindowHint = ProgressBarWindowHint(this,R.style.Dialogstyle,"")

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN) //隐藏状态栏
//        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN) //显示状态栏

    }

    fun ToastMsg(msg : String) {
        Toast.makeText(this,msg,Toast.LENGTH_SHORT).show()
    }

    fun Loge(tag:String,log:String) {
        Log.e(tag,log)
    }

    public override fun onResume() {
        super.onResume()
        MobclickAgent.onResume(this)
    }

    public override fun onPause() {
        super.onPause()
        MobclickAgent.onPause(this)
    }

    // 性别-男
//    val SEX_MAN = "SEX_MAN"
    // 国别-亚洲
    val COUNTRY_CN = "COUNTRY_CN"
    private val PREF_NAME = "SharedPreferencesInfo"
    private var pref : SharedPreferences? = null
    fun saveBooleanBySharedPreferences(strKey:String,isOK:Boolean) {
        val editor = pref?.edit()
        editor?.putBoolean(strKey, isOK)
        editor?.commit()
    }

    fun saveStringBySharedPreferences(strKey:String,strValue:String) {
        val editor = pref?.edit()
        editor?.putString(strKey, strValue)
        editor?.commit()
    }

    fun saveIntBySharedPreferences(strKey:String,iValue:Int) {
        val editor = pref?.edit()
        editor?.putInt(strKey, iValue)
        editor?.commit()
    }

    fun getBooleanBySharedPreferences(strKey:String) : Boolean {
        return pref!!.getBoolean(strKey,true)
    }

    fun getStringBySharedPreferences(strKey:String):String? {
        return pref!!.getString(strKey,"")
    }

    fun getIntBySharedPreferences(strKey:String):Int {
        return pref!!.getInt(strKey,-1)
    }


    var timer = Timer()

    /**
     * 启动/暂停 60秒检测是否有人
     */
    fun startTimerHoldSeat(isRun: Boolean) {
        timer.cancel()
        if (!isRun) {
            return
        }
        timer = Timer()
        timer.schedule(object : TimerTask() {
            override fun run() {
//                MainControlActivity.getInstance()?.finish()
            }
        }, 1000 * 60) // 设定指定的时间time,此处为60秒
    }


}
