package com.smartCarSeatProject.activity

import ai.nuralogix.anurasdk.utils.AnuLogUtil
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.View
import com.ai.nuralogix.anura.sample.activities.MainActivity
import com.ai.nuralogix.anura.sample.activities.MeasurementActivity
import com.alibaba.android.mnnkit.monitor.MNNMonitor
import com.smartCarSeatProject.BuildConfig
import com.smartCarSeatProject.R
import com.smartCarSeatProject.dao.MemoryDataInfo
import com.smartCarSeatProject.dao.MemoryInfoDao
import com.smartCarSeatProject.data.BaseVolume
import com.smartCarSeatProject.data.DataAnalysisHelper
import com.smartCarSeatProject.data.DeviceWorkInfo
import com.smartCarSeatProject.data.SeatStatus
import com.smartCarSeatProject.data.SeatStatus.*
import com.smartCarSeatProject.tcpInfo.SocketThreadManager
import com.smartCarSeatProject.view.AreaAddWindowHint
import com.smartCarSeatProject.view.SureOperWindowHint
import com.smartCarSeatProject.wifiInfo.WIFIConnectionManager
import kotlinx.android.synthetic.main.layout_menu.*
import com.umeng.commonsdk.statistics.AnalyticsConstants.LOG_TAG
import java.io.*


class MenuSelectActivity : BaseActivity(),View.OnClickListener{

    var Tag = ""
    var isGotoAuto = false
    var isGotoManual = false
    var isGotoDevelop = false
    // 是否在控制界面
    var isControlShow = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_menu)

        Tag = this.localClassName

        initUI()
        reciverBand()

        btn1.isEnabled = true
        btn2.isEnabled = true
        btn3.isEnabled = true
        btn4.isEnabled = true

//        val memoryInfoDao = MemoryInfoDao(this)
//        memoryInfoDao.insertSingleData(MemoryDataInfo())
//        memoryInfoDao.closeDb()

        // 人体采集相关
        AnuLogUtil.setShowLog(BuildConfig.DEBUG)
        MNNMonitor.setMonitorEnable(false)
        copyFileOrDir("r21r23h-8.dat")

    }

    fun initUI() {

        imgClose.setOnClickListener(this)
        imgReset.setOnClickListener(this)
        imgWIFI.setOnClickListener(this)
        tvReCanConnect.setOnClickListener(this)
        tvReDeviceConnect.setOnClickListener(this)
        btn1.setOnClickListener(this)
        btn2.setOnClickListener(this)
        btn3.setOnClickListener(this)
        btn4.setOnClickListener(this)
        tvPersonInfo.setOnClickListener(this)
        btn1.isEnabled = false

    }

    /** 监听广播  */
    private fun reciverBand() {
        val myIntentFilter = IntentFilter()
        myIntentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION)
        myIntentFilter.addAction(BaseVolume.BROADCAST_FINISH_APPLICATION)
        myIntentFilter.addAction(BaseVolume.BROADCAST_TCP_INFO)
        myIntentFilter.addAction(BaseVolume.BROADCAST_TCP_INFO_CAN)
        myIntentFilter.addAction(BaseVolume.BROADCAST_SEND_INFO)
        myIntentFilter.addAction(BaseVolume.BROADCAST_RESULT_DATA_INFO)
        myIntentFilter.addAction(BaseVolume.BROADCAST_CTR_CALLBACK)
        myIntentFilter.addAction(BaseVolume.BROADCAST_GOBACK_MENU)
        myIntentFilter.addAction(BaseVolume.BROADCAST_RESET_ACTION)

        // 注册广播
        registerReceiver(myNetReceiver, myIntentFilter)
    }

    override fun onClick(p0: View?) {
        isGotoAuto = false
        isGotoManual = false
        isGotoDevelop = false

        when(p0?.id) {
            R.id.tvReDeviceConnect -> {
                SocketThreadManager.sharedInstance(this@MenuSelectActivity)?.createDeviceSocket()
//                btn1.isEnabled = !btn1.isEnabled
            }

            R.id.imgClose -> {
                val areaAddWindowHint = AreaAddWindowHint(this,R.style.Dialogstyle,"System",
                        object : AreaAddWindowHint.PeriodListener {
                            override fun refreshListener(string: String) {
                                finish()
                            }
                        },"Are you sure to exit the application?",false)
                areaAddWindowHint?.show()
            }
            R.id.imgReset -> {
                val areaAddWindowHint = AreaAddWindowHint(this,R.style.Dialogstyle,"System",
                        object : AreaAddWindowHint.PeriodListener {
                            override fun refreshListener(string: String) {
                                SocketThreadManager.sharedInstance(this@MenuSelectActivity)?.StartSendData(BaseVolume.COMMAND_SET_STATUS_RESET)
                            }
                        },"Are you sure to reset?",false)
                areaAddWindowHint?.show()
            }
            R.id.btn1 -> {

                gotoMainControlActivity(1)

                // 已经在自动模式下，则直接进入
//                if (DataAnalysisHelper.deviceState.seatStatus == SeatStatus.press_automatic.iValue ||
//                        DataAnalysisHelper.deviceState.seatStatus == SeatStatus.press_reserve.iValue) {
//                    gotoMainControlActivity(1)
//                }
//                else {
//                    // 发送指令，切换到自动模式
//                    isGotoAuto = true
//                    SocketThreadManager.sharedInstance(this@MenuSelectActivity)?.StartSendData(BaseVolume.COMMAND_SET_MODE_AUTO)
//                }

            }
            R.id.btn2 -> {

                gotoMainControlActivity(2)

                // 已经在手动模式下，则直接进入
//                if (DataAnalysisHelper.deviceState.seatStatus == SeatStatus.press_automatic_manual.iValue) {
//                    gotoMainControlActivity(2)
//                }
//                else {
//                    // 发送指令，切换到手动模式
//                    isGotoManual = true
//                    SocketThreadManager.sharedInstance(this@MenuSelectActivity)?.StartSendData(BaseVolume.COMMAND_SET_MODE_MANUAL)
//                }

            }
            R.id.btn3 ->
                gotoMainControlActivity(3)
            R.id.btn4 -> {
                startActivity(Intent(this@MenuSelectActivity,DevelopmentActivity::class.java))

                // 已经在开发者模式下，则直接进入
//                if (DataAnalysisHelper.deviceState.seatStatus == SeatStatus.develop.iValue) {
//                    val intent = Intent()
//                    intent.setClass(this@MenuSelectActivity,DevelopmentActivity::class.java)
//                    startActivity(intent)
//                }
//                else {
//                    // 发送指令，切换到开发者模式
//                    isGotoDevelop = true
//                    SocketThreadManager.sharedInstance(this@MenuSelectActivity)?.StartSendData(BaseVolume.COMMAND_SET_MODE_DEVELOP)
//                }
            }
            R.id.tvReCanConnect -> {
                SocketThreadManager.sharedInstance(this@MenuSelectActivity)?.createCanSocket()
            }
            R.id.tvPersonInfo -> {
                startActivity(Intent(mContext,MeasurementActivity().javaClass))
            }
        }
    }



    fun gotoMainControlActivity(iNumber:Int) {

        isControlShow = true

        val intent = Intent()
        intent.setClass(this,MainControlActivity::class.java)
        intent.putExtra("iNumber",iNumber)
        startActivity(intent)
    }

    /****
     * 广播监听
     */
    private val myNetReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            // 网络监听的回调
            if (action == ConnectivityManager.CONNECTIVITY_ACTION) {
                val mConnectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                val netInfo = mConnectivityManager
                        .activeNetworkInfo
                if (netInfo != null && netInfo.isAvailable) {
                    // wifi网络
                    if (netInfo.type == ConnectivityManager.TYPE_WIFI) {
                        checkStartConnect()
                    } else if (netInfo.type == ConnectivityManager.TYPE_ETHERNET) {
                    } else if (netInfo.type == ConnectivityManager.TYPE_MOBILE) {
                    }// 3G网络
                    // 2G网络
                } else {
                    // 网络断开了
                }
            }
            else if (action == BaseVolume.BROADCAST_FINISH_APPLICATION) {
                finish()
                System.out
            }
            else if (action == BaseVolume.BROADCAST_RESET_ACTION) {
                isControlShow = false
                SocketThreadManager.sharedInstance(this@MenuSelectActivity)?.StartSendData(BaseVolume.COMMAND_SET_STATUS_RESET)
            }

            else if (action == BaseVolume.BROADCAST_TCP_INFO) {
                val strType = intent.getStringExtra(BaseVolume.BROADCAST_TYPE)
                // 开始连接
                if (strType.equals(BaseVolume.BROADCAST_TCP_CONNECT_START)) {
                    loadingDialog?.show()
                    ToastMsg("Connecting....")
                }
                // 连接结果
                else if (strType.equals(BaseVolume.BROADCAST_TCP_CONNECT_CALLBACK)) {
                    val isConnected = intent.getBooleanExtra(BaseVolume.BROADCAST_TCP_STATUS,false)
                    val strMsg = intent.getStringExtra(BaseVolume.BROADCAST_MSG)
                    loadingDialog?.dismiss()
                    if (!isConnected) {
                        tvReDeviceConnect.visibility = View.VISIBLE
                        imgWIFI.visibility = View.GONE
                        ToastMsg("Connect Fail！$strMsg")
                    }
                    else {
                        tvReDeviceConnect.visibility = View.GONE
                        if (SocketThreadManager.sharedInstance(this@MenuSelectActivity)?.isCanConnected()!!) {
                            imgWIFI.visibility = View.VISIBLE
                            tvReCanConnect.visibility = View.GONE
                        }
                        else {
                            imgWIFI.visibility = View.GONE
                            tvReCanConnect.visibility = View.VISIBLE
                            // 开始连接Can
                            SocketThreadManager.sharedInstance(this@MenuSelectActivity)?.createCanSocket()
                        }



                        ToastMsg("Connection successful！")
                    }
                    OnStartLoadData(isConnected)
                }
            }
            else if (action == BaseVolume.BROADCAST_TCP_INFO_CAN) {
                val strType = intent.getStringExtra(BaseVolume.BROADCAST_TYPE)
                // 开始连接
                if (strType.equals(BaseVolume.BROADCAST_TCP_CONNECT_START)) {
                    ToastMsg("Can,Connecting！")
                }
                // 连接结果
                else if (strType.equals(BaseVolume.BROADCAST_TCP_CONNECT_CALLBACK)) {
                    val isConnected = intent.getBooleanExtra(BaseVolume.BROADCAST_TCP_STATUS, false)
                    val strMsg = intent.getStringExtra(BaseVolume.BROADCAST_MSG)

                    if (!isConnected) {
                        tvReCanConnect.visibility = View.VISIBLE
                        imgWIFI.visibility = View.GONE
                        ToastMsg("Can Connect Fail！$strMsg")
                    }
                    else {
                        tvReCanConnect.visibility = View.GONE
                        if (SocketThreadManager.sharedInstance(this@MenuSelectActivity)?.isDeviceConnected()!!) {
                            imgWIFI.visibility = View.VISIBLE
                            tvReDeviceConnect.visibility = View.GONE
                        }
                        else {
                            imgWIFI.visibility = View.GONE
                            tvReDeviceConnect.visibility = View.VISIBLE
                        }
                        ToastMsg("Can Connection successful！")
                    }
                }
            }
            else if (action == BaseVolume.BROADCAST_SEND_INFO) {
                val strType = intent.getStringExtra(BaseVolume.BROADCAST_TYPE)
                // 开始发送数据
                if (strType.equals(BaseVolume.BROADCAST_SEND_DATA_START)) {
                    if (!loadingDialog!!.isShowing) {
                        loadingDialog?.show()
                    }
                }
                // 停止发送
                else if (strType.equals(BaseVolume.BROADCAST_SEND_DATA_END)) {
                    loadingDialog?.dismiss()
                }
                // 发送超时
                else if (strType.equals(BaseVolume.BROADCAST_SEND_DATA_TIME_OUT)) {
                    if (loadingDialog?.isShowing!!) {
                        loadingDialog?.dismiss()
                        ToastMsg("Send timeout！")
                    }
                }
            }
            // 数据回调
            else if (action == BaseVolume.BROADCAST_RESULT_DATA_INFO) {
                val strType = intent.getStringExtra(BaseVolume.BROADCAST_TYPE)
                val deviceWorkInfo = intent.getSerializableExtra(BaseVolume.BROADCAST_MSG) as DeviceWorkInfo
                // 男女国别
                if (strType.equals(BaseVolume.COMMAND_TYPE_SEX,true)) {
                    SocketThreadManager.sharedInstance(this@MenuSelectActivity)?.StopSendData()
                    // 查询状态
                    SocketThreadManager.sharedInstance(this@MenuSelectActivity)?.StartSendData(BaseVolume.COMMAND_READ_SEAT_STATUS)
                }
                // 状态
                else if (strType.equals(BaseVolume.COMMAND_TYPE_SEAT_STATUS,true)) {
                    SocketThreadManager.sharedInstance(this@MenuSelectActivity)?.StopSendData()
                    loadingDialog?.dismiss()
                    // 状态未达到预设值，则让A灰掉，且不能点击
                    if (deviceWorkInfo?.seatStatus < SeatStatus.press_reserve.iValue) {
                        DataAnalysisHelper.deviceState.isProbe = false

                        imgRun1.visibility = View.GONE
                        imgRun2.visibility = View.GONE
                        imgRun4.visibility = View.GONE
                        ToastMsg("Initializing...")
                        btn1.isEnabled = false
                        btn2.isEnabled = false
                        btn4.isEnabled = true

                        areaSeatWindowHint?.dismiss()
                        startCheckPeopleWindowHint?.dismiss()

                        if (!isControlShow) {
                            progressBarWindowHint?.updateContent("Initializing...")
                            progressBarWindowHint?.onSelfShow()
                        }

                    }
                    // 座椅已经达到预设值，则按钮A是亮的，但还没初始化完，所以2是灰的
                    else if (deviceWorkInfo?.seatStatus == SeatStatus.press_reserve.iValue){

                        DataAnalysisHelper.deviceState.isProbe = false

                        areaSeatWindowHint?.dismiss()
                        startCheckPeopleWindowHint?.dismiss()
                        progressBarWindowHint?.onSelfDismiss()

                        imgRun1.visibility = View.VISIBLE
                        imgRun2.visibility = View.GONE
                        imgRun4.visibility = View.GONE
                        btn1.isEnabled = true
                        btn2.isEnabled = false
                        btn4.isEnabled = true
                        loadingDialog?.dismiss()

                        ToastMsg("Seat initialization is complete！")
                        // 如果菜单界面，触发了进入自动模式
                        if (isGotoAuto) {
                            isGotoAuto = false
                            gotoMainControlActivity(1)
                        }

                    }
                    // 正在探测，则按钮A是亮的，但还没完成，所以2是灰的
                    else if (deviceWorkInfo?.seatStatus == SeatStatus.press_auto_probe.iValue) {
                        DataAnalysisHelper.deviceState.isProbe = false

                        imgRun1.visibility = View.VISIBLE
                        imgRun2.visibility = View.GONE
                        imgRun4.visibility = View.GONE
                        btn1.isEnabled = true
                        btn2.isEnabled = false
                        btn4.isEnabled = true
                        loadingDialog?.dismiss()
                        areaSeatWindowHint?.dismiss()
                        startCheckPeopleWindowHint?.dismiss()

                        ToastMsg("Is to detect！")
                        if (!isControlShow) {
                            progressBarWindowHint?.updateContent("Is to detect...")
                            progressBarWindowHint?.onSelfShow()
                        }

                    }
                    // 座椅自动模式，则自动和手动按钮都亮
                    else if (deviceWorkInfo?.seatStatus == SeatStatus.press_automatic.iValue){

                        DataAnalysisHelper.deviceState.isProbe = true

                        imgRun1.visibility = View.VISIBLE
                        imgRun2.visibility = View.GONE
                        imgRun4.visibility = View.GONE
                        btn1.isEnabled = true
                        btn2.isEnabled = true
                        btn4.isEnabled = true
                        loadingDialog?.dismiss()
                        areaSeatWindowHint?.dismiss()
                        startCheckPeopleWindowHint?.dismiss()

                        if (progressBarWindowHint?.isShowing!!) {
                            progressBarWindowHint?.onSelfDismiss()
                            ToastMsg("Automatic mode！")
                        }

                        // 如果菜单界面，触发了进入自动模式
                        if (isGotoAuto) {
                            isGotoAuto = false
                            gotoMainControlActivity(1)
                        }
                    }
                    // 座椅自动下的手动模式
                    else if (deviceWorkInfo?.seatStatus == SeatStatus.press_automatic_manual.iValue){
                        imgRun1.visibility = View.GONE
                        imgRun2.visibility = View.VISIBLE
                        imgRun4.visibility = View.GONE
                        btn1.isEnabled = true
                        btn2.isEnabled = true
                        btn4.isEnabled = true
                        loadingDialog?.dismiss()
                        // 是菜单界面触发的手动模式
                        if (isGotoManual) {
                            isGotoManual = false
                            gotoMainControlActivity(2)
                        }

                    }
                    // 开发者模式
                    else if (deviceWorkInfo?.seatStatus == SeatStatus.develop.iValue) {
                        btn1.isEnabled = true
                        btn2.isEnabled = false
                        // 已经探测过
                        if (deviceWorkInfo?.isProbe) {
                            btn2.isEnabled = true
                        }

                        imgRun1.visibility = View.GONE
                        imgRun2.visibility = View.GONE
                        imgRun4.visibility = View.VISIBLE
                        btn4.isEnabled = true
                        loadingDialog?.dismiss()
                        // 是菜单界面触发的开发者模式
                        if (isGotoDevelop) {
                            isGotoDevelop = false
                            val intent = Intent()
                            intent.setClass(this@MenuSelectActivity,DevelopmentActivity::class.java)
                            startActivity(intent)
                        }
                    }
                    // 临时自检
                    else if (DataAnalysisHelper.deviceState.seatStatus == SeatStatus.short_check.iValue) {
                        loadingDialog?.dismiss()
                        // 控制界面已经展示，则这个界面不需要展示
                        if (!isControlShow) {
                            areaSeatWindowHint?.show()
                        }

                    }

                }
                // 数据表
                else if (strType.equals(BaseVolume.COMMAND_TYPE_SQL_CTR,true)) {
                    SocketThreadManager.sharedInstance(this@MenuSelectActivity)?.StopSendData()
                }
            }
            // 控制回调
            else if (action == BaseVolume.BROADCAST_CTR_CALLBACK) {
                // 停止定时发送
                SocketThreadManager.sharedInstance(this@MenuSelectActivity)?.StopSendData()
            }
            // 从控制页面返回到主页
            else if (action == BaseVolume.BROADCAST_GOBACK_MENU) {
                isControlShow = false
            }
        }
    }

    /** 校验，是否可以开始连接 */
    fun checkStartConnect() {
        val nowWIFIName = WIFIConnectionManager.getInstance(this@MenuSelectActivity)?.nowConnectWifi ?: ""
        Loge(Tag,"当前连接的wifi：$nowWIFIName ！！！")
        if (nowWIFIName.indexOf(BaseVolume.WIFI_SIGN) < 0) {
            ToastMsg("Please connect'${BaseVolume.WIFI_SIGN}' WIFI！")
            imgWIFI.visibility = View.GONE
            imgReset.visibility = View.GONE
            tvReDeviceConnect.visibility = View.VISIBLE
            tvReCanConnect.visibility = View.VISIBLE
            return
        }
        // 先断开连接
        SocketThreadManager.sharedInstance(this@MenuSelectActivity)?.clearAllTCPClient()
        // 开始建立连接！
        SocketThreadManager.sharedInstance(this@MenuSelectActivity)?.createDeviceSocket()
//        SocketThreadManager.sharedInstance(this@MenuSelectActivity)?.createCanSocket()


    }

    fun OnStartLoadData(isConnected: Boolean) {
        if (!isConnected) {
            imgReset.visibility = View.GONE
            btn1.isEnabled = false
            btn2.isEnabled = false
            btn4.isEnabled = false

            imgRun1.visibility = View.GONE
            imgRun2.visibility = View.GONE
            imgRun4.visibility = View.GONE

        }
        else {
            imgReset.visibility = View.VISIBLE
            btn1.isEnabled = false
            btn2.isEnabled = false
            btn4.isEnabled = true

            loadingDialog?.show()

            // 连接成功后，查询顺序：男女国别→座椅状态
            // 查询男女国别
//            SocketThreadManager.sharedInstance(this)?.StartSendData(BaseVolume.COMMAND_READ_SEAT_SEX)
            // 查询座椅状态
            SocketThreadManager.sharedInstance(this)?.StartSendData(BaseVolume.COMMAND_READ_SEAT_STATUS)
        }
    }

    override fun onBackPressed() {
//         super.onBackPressed();//注释掉这行,back键不退出activity

        val areaAddWindowHint = AreaAddWindowHint(this,R.style.Dialogstyle,"System",
                object : AreaAddWindowHint.PeriodListener {
                    override fun refreshListener(string: String) {
                        finish()
                    }
                },"Are you sure to exit the application?",false)
        areaAddWindowHint?.show()
    }

    fun copyFileOrDir(path: String) {
        val assetManager = this.assets
        val assets: Array<String>?
        try {
            assets = assetManager.list(path)
            if (assets!!.isEmpty()) {
                copyFile(path)
            } else {
                val fullPath = filesDir.absolutePath + File.separator + path
                val dir = File(fullPath)
                if (!dir.exists())
                    dir.mkdir()
                for (asset in assets) {
                    copyFileOrDir("$path/$asset")
                }
            }
        } catch (ex: IOException) {
            AnuLogUtil.e(MainActivity.TAG, "copyFileOrDir exception: ${ex.message}")
        } catch (e: Exception) {
            AnuLogUtil.e(MainActivity.TAG, "copyFileOrDir exception: ${e.message}")
        }

    }

    private fun copyFile(filename: String) {
        val assetManager = this.assets

        val `in`: InputStream
        val out: OutputStream
        try {
            `in` = assetManager.open(filename)
            val newFileName = filesDir.absolutePath + File.separator + filename

            out = FileOutputStream(newFileName)
            val buffer = ByteArray(1024)
            var read = `in`.read(buffer)
            while (read != -1) {
                out.write(buffer, 0, read)
                read = `in`.read(buffer)
            }
            `in`.close()
            out.flush()
            out.close()

        } catch (e: Exception) {
            AnuLogUtil.e(MainActivity.TAG, "copyFile exception: ${e.message}")
        }

    }


    override fun onDestroy() {
        super.onDestroy()
        SocketThreadManager.sharedInstance(this)?.clearAllTCPClient()
        unregisterReceiver(myNetReceiver)


    }


}