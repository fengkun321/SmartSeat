package com.smartCarSeatProject

import android.app.Application
import android.graphics.Typeface
import com.smartCarSeatProject.data.BaseVolume
import com.smartCarSeatProject.data.CreateCtrDataHelper
import com.umeng.commonsdk.UMConfigure

class BaseApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        BaseVolume.getColorByPressValue(3000,5)

//        val NowSendData = CreateCtrDataHelper.getCtrPressBy16Develop("1500","1800","2000")

        // 统一更改字体样式
        initTypeface()

        /**
         * 注意: 即使您已经在AndroidManifest.xml中配置过appkey和channel值，也需要在App代码中调
         * 用初始化接口（如需要使用AndroidManifest.xml中配置好的appkey和channel值，
         * UMConfigure.init调用中appkey和channel参数请置为null）。
         */
        UMConfigure.init(this, "5d722f4f4ca357f80d00071f", "Umeng", UMConfigure.DEVICE_TYPE_PHONE,null)



    }

    fun initTypeface() {

        var typefaceAudiType = Typeface.createFromAsset(assets,"Normal.ttf")
        try {
            var fil = Typeface::class.java.getDeclaredField("MONOSPACE")
            fil.isAccessible = true
            // 替换默认字体
            fil.set(null, typefaceAudiType)
        } catch (e: NoSuchFieldException) {
            e.printStackTrace()
        }


    }


}