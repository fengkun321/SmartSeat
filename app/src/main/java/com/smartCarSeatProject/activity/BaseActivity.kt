package com.smartCarSeatProject.activity

import ai.nuralogix.dfx.Face
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import com.smartCarSeatProject.R
import com.smartCarSeatProject.data.BaseVolume
import com.smartCarSeatProject.data.CreateCtrDataHelper
import com.smartCarSeatProject.data.DataAnalysisHelper
import com.smartCarSeatProject.data.SeatStatus
import com.smartCarSeatProject.tcpInfo.SocketThreadManager
import com.smartCarSeatProject.view.LoadingDialog
import com.smartCarSeatProject.view.ProgressBarWindowHint
import com.umeng.analytics.MobclickAgent
import org.greenrobot.eventbus.EventBus
import java.util.*


open class BaseActivity : AppCompatActivity(){

    protected lateinit var mContext:Context
    // 加载转圈
    lateinit var loadingDialog:LoadingDialog
    // 自定义进度条
    lateinit var progressBarWindowHint: ProgressBarWindowHint
    // 是否已经检测过人体数据
    protected var isCheckedPersonInfo = false
    protected lateinit var mHandler:Handler


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mHandler = Handler()

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

    override fun onDestroy() {
        super.onDestroy()

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
                // 座椅处于正在检测，此时需要判断A面气压
                if (DataAnalysisHelper.deviceState.seatStatus == SeatStatus.press_auto_probe.iValue) {
                    // 同时检测到人，则重新倒计时
                    if (DataAnalysisHelper.deviceState.isCheckHavePerson() && isCheckHaveFace ) {
                        startTimerHoldSeat(true)
                    }
                    else {
                        // 座椅和人脸只要有一个没有检测到人，则提示用户，并重置座椅
                        sendBroadcast(Intent(BaseVolume.BROADCAST_NO_HAVE_PERSON))
                    }
                }
                // 座椅处于某种工作模式，只需要判断人脸
                else {
                    // 检测到人，则重新倒计时
                    if (isCheckHaveFace ) {
                        startTimerHoldSeat(true)
                    }
                    else {
                        // 人脸没有检测到人，则提示用户，并重置座椅
                        sendBroadcast(Intent(BaseVolume.BROADCAST_NO_HAVE_PERSON))
                    }
                }

            }
        }, 1000 * 30) // 设定指定的时间time,此处为60秒
    }

    protected var isCheckHaveFace = false
    protected var checkPersionHaveListener = object :CheckPersionHaveListener {
        override fun checkResult(isHaveFace: Boolean) {
            Log.e("人脸检测", "checkPersionHaveListener：$isHaveFace")
            isCheckHaveFace = isHaveFace
        }
    }
    public interface CheckPersionHaveListener {
        fun checkResult(isHaveFace:Boolean)
    }

    /** 释放A面气压，B面保持不动 */
    protected fun releaseAPress() {

        // 需要将A面的气袋全部泄气，要先发按摩，然后发off
        val strSendData0 = CreateCtrDataHelper.getCtrModelAB(BaseVolume.COMMAND_CAN_MODEL_MASG_1,BaseVolume.COMMAND_CAN_MODEL_NORMAL)
        SocketThreadManager.sharedInstance(mContext).StartChangeModelByCan(strSendData0)
        val strSendData = CreateCtrDataHelper.getCtrModelAB(BaseVolume.COMMAND_CAN_MODEL_MASG_OFF,BaseVolume.COMMAND_CAN_MODEL_NORMAL)
        // 然后延时1秒后执行off
        val timer = Timer()
        timer?.schedule(object : TimerTask() {
            override fun run() {
                SocketThreadManager.sharedInstance(mContext).StartChangeModelByCan(strSendData)
                SocketThreadManager.sharedInstance(mContext).StartChangeModelByCan(strSendData)
                SocketThreadManager.sharedInstance(mContext).StartChangeModelByCan(strSendData)
            }
        }, (1 * 1000))
    }


}
