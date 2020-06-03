package com.smartCarSeatProject.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.ScanResult
import android.os.Bundle
import com.smartCarSeatProject.R
import com.smartCarSeatProject.adapter.SQLInfoAdapter
import com.smartCarSeatProject.adapter.WifiInfoAdapter
import com.smartCarSeatProject.dao.DevelopDataInfo
import com.smartCarSeatProject.dao.DevelopInfoDao
import com.smartCarSeatProject.data.*
import com.smartCarSeatProject.tcpInfo.SocketThreadManager
import com.smartCarSeatProject.view.AreaAddWindowHint
import com.smartCarSeatProject.view.LoadingDialog
import com.smartCarSeatProject.view.SetValueAreaAddWindow
import kotlinx.android.synthetic.main.layout_setmemory.*
import java.util.ArrayList

class SetMemoryActivity: BaseActivity() {

    private var list :ArrayList<DevelopDataInfo> = arrayListOf()
    private var sqlInfoAdapter: SQLInfoAdapter? = null
    lateinit var nowSelectMemory:DevelopDataInfo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_setmemory)

        val strFrom = intent.getStringExtra("from")
        val memoryInfoDao = DevelopInfoDao(this)
        list = memoryInfoDao.queryHistDataInfByDataType(DevelopDataInfo.DATA_TYPE_USE)
        memoryInfoDao.closeDb()
        initUI()

        reciverBand()

    }

    fun initUI() {

        sqlInfoAdapter = SQLInfoAdapter(list, this)
        list_view.adapter = sqlInfoAdapter

        list_view.setOnItemClickListener { adapterView, view, i, l ->
            nowSelectMemory = list[i]

            loadingDialog.showAndMsg("请稍后...")
            // 只调整B面的，所以将A面设为normal，B面设为adjust
            SocketThreadManager.sharedInstance(mContext)?.StartChangeModelByCan(CreateCtrDataHelper.getCtrModelAB(BaseVolume.COMMAND_CAN_MODEL_NORMAL,BaseVolume.COMMAND_CAN_MODEL_ADJUST))
            val strSendDataList = CreateCtrDataHelper.getCtrPressBy8Manual(nowSelectMemory)
            strSendDataList.forEach {
                SocketThreadManager.sharedInstance(mContext).StartSendDataByCan(it)
            }

        }

        list_view.setOnItemLongClickListener { adapterView, view, i, l ->
            val areaAddWindowHint = AreaAddWindowHint(this@SetMemoryActivity,R.style.Dialogstyle,"System",
                    object : AreaAddWindowHint.PeriodListener {
                        override fun refreshListener(string: String) {
                            nowSelectMemory = list[i]
                            val memoryInfoDao = DevelopInfoDao(this@SetMemoryActivity)
                            val isResult = memoryInfoDao.deleteDataByInfo(nowSelectMemory)
                            memoryInfoDao.closeDb()

                            if (isResult)
                                list.remove(nowSelectMemory!!)

                            sqlInfoAdapter!!.updateList()

                        }
                    }
                    ,"Are you sure you want to delete this data?"
            )
            areaAddWindowHint.show()
            return@setOnItemLongClickListener true
        }

        imgBack.setOnClickListener {
            MainControlActivity.getInstance()?.ReturnBack("SetMemoryActivity")
        }
    }

    /** 监听广播  */
    private fun reciverBand() {
        val myIntentFilter = IntentFilter()
        myIntentFilter.addAction(BaseVolume.BROADCAST_RESULT_DATA_INFO)
        myIntentFilter.addAction(BaseVolume.BROADCAST_CTR_CALLBACK)
        // 注册广播
        registerReceiver(myNetReceiver, myIntentFilter)
    }

    /****
     * 广播监听
     */
    private val myNetReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            // 数据回调
            if (action == BaseVolume.BROADCAST_RESULT_DATA_INFO) {
                val strType = intent.getStringExtra(BaseVolume.BROADCAST_TYPE)
                val deviceWorkInfo = intent.getSerializableExtra(BaseVolume.BROADCAST_MSG) as DeviceWorkInfo
                // 通道状态
                if (strType == BaseVolume.COMMAND_TYPE_CHANNEL_STATUS) {
                    // 手动模式
                    if (DataAnalysisHelper.deviceState.seatStatus == SeatStatus.press_manual.iValue && SocketThreadManager.isCheckChannelState) {
                        // 初始化控制A面所有，B面座垫678，通道充气，所以只需要判断678，abcdefgh这几个气袋
                        var isAllNormal = true
                        // B面所有气袋 12345678
                        for (iNumber in 0 .. 7) {
                            val iState = DataAnalysisHelper.deviceState.controlPressStatusList[iNumber]
                            // 正在充气，说明还没完成
                            if (iState == DeviceWorkInfo.STATUS_SETTING)
                                return
                            if (iState == DeviceWorkInfo.STATUS_SETTED)
                                isAllNormal = false
                        }
                        // 全部恢复到Normal
                        if (!isAllNormal) {
                            SocketThreadManager.sharedInstance(mContext).StartChangeModelByCan(BaseVolume.COMMAND_CAN_MODEL_NORMAL_A_B)
                        }
                        // 已经全部恢复到Normal，则将座椅切到恢复成功状态
                        else {
                            loadingDialog.dismiss()
                            SocketThreadManager.sharedInstance(mContext).startTimeOut(false)
                        }
                    }
                }
            }
            // 控制回调
            else if (action == BaseVolume.BROADCAST_CTR_CALLBACK) {
                val strType = intent.getStringExtra(BaseVolume.BROADCAST_TYPE)
                val strMsg = intent.getStringExtra(BaseVolume.BROADCAST_MSG)

            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(myNetReceiver)

    }

}