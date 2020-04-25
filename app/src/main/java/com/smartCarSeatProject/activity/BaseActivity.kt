package com.smartCarSeatProject.activity

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import com.smartCarSeatProject.R
import com.smartCarSeatProject.data.BaseVolume
import com.smartCarSeatProject.data.DataAnalysisHelper
import com.smartCarSeatProject.data.DeviceWorkInfo
import com.smartCarSeatProject.tcpInfo.ConnectAndDataListener
import com.smartCarSeatProject.tcpInfo.SocketThreadManager
import com.smartCarSeatProject.view.AreaAddWindowHint
import com.smartCarSeatProject.view.LoadingDialog
import com.smartCarSeatProject.view.ProgressBarWindowHint
import com.smartCarSeatProject.view.SureOperWindowHint
import com.umeng.analytics.MobclickAgent
import kotlinx.android.synthetic.main.layout_menu.*


open class BaseActivity : AppCompatActivity(){

    protected var mContext:Context? = null
    // 加载转圈
    protected var loadingDialog:LoadingDialog? = null
    // 自定义进度条
    var progressBarWindowHint: ProgressBarWindowHint? = null
    // 确认保压/重置
//    var sureKeepOrResetWindowHint : SureOperWindowHint? = null
    // 提示入座
    var areaSeatWindowHint : AreaAddWindowHint? = null
    // 开始检测
    var startCheckPeopleWindowHint : AreaAddWindowHint? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        pref = getSharedPreferences(PREF_NAME, 0)

        mContext = this
        loadingDialog = LoadingDialog(this, R.style.LoadingDialogStyle)
        progressBarWindowHint = ProgressBarWindowHint(this,R.style.Dialogstyle,"")
//        sureKeepOrResetWindowHint = SureOperWindowHint(this,R.style.Dialogstyle,"System",
//                object : SureOperWindowHint.PeriodListener {
//                    override fun btnRightListener() {
//                        // 重置
//                        SocketThreadManager.sharedInstance(this@BaseActivity)?.StartSendData(BaseVolume.COMMAND_SET_STATUS_RESET)
//                    }
//                    override fun btnLeftListener() {
//                        // 保压
//                        SocketThreadManager.sharedInstance(this@BaseActivity)?.StartSendData(BaseVolume.COMMAND_SET_STATUS_KEEP)
//                    }
//                },"The seats are currently unoccupied","Keep","Reset")

//        progressBarWindowHint!!.setOnDismissListener {
//            progressBarWindowHint!!.onStopProgress()
//        }

        areaSeatWindowHint = AreaAddWindowHint(this,R.style.Dialogstyle,"System",
                object : AreaAddWindowHint.PeriodListener {
                    override fun refreshListener(string: String) {

                    }
                },"The seats are free. Please take a seat!",true)

        startCheckPeopleWindowHint = AreaAddWindowHint(this,R.style.Dialogstyle,"System",
                object : AreaAddWindowHint.PeriodListener {
                    override fun refreshListener(string: String) {
                        // 通知座椅开始探测
                        SocketThreadManager.sharedInstance(this@BaseActivity)?.StartSendData(BaseVolume.COMMAND_SET_STATUS_PROBE)
                    }
                },"keep driving position, ready for recognization",true)



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
    val SEX_MAN = "SEX_MAN"
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

    /** 判断是否可以控制（前提是，所以气袋都没有在工作） */
    fun isCanControl():Boolean{
        val iCanCtr = DataAnalysisHelper.getInstance(this)!!.getAllChannelStatus()
        if (iCanCtr > 0)
            ToastMsg("气袋$iCanCtr ,正在工作，不能控制！")
        return iCanCtr > 0
    }

}
