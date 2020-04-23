package com.smartCarSeatProject.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import com.smartCarSeatProject.R
import com.smartCarSeatProject.data.BaseVolume
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


class WelcomeActivity  : BaseActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_welcome)

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
                val intent = Intent(this,MenuSelectActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
        false
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