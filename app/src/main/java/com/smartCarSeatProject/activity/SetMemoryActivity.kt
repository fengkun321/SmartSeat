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
            val strSendDataList = CreateCtrDataHelper.getCtrPressBy16Manual(nowSelectMemory)
            strSendDataList.forEach {
                SocketThreadManager.sharedInstance(this@SetMemoryActivity).StartSendData(it)
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
            // 控制回调
            if (action == BaseVolume.BROADCAST_CTR_CALLBACK) {
                val strType = intent.getStringExtra(BaseVolume.BROADCAST_TYPE)
                val strMsg = intent.getStringExtra(BaseVolume.BROADCAST_MSG)
                if (strType.equals(BaseVolume.COMMAND_TYPE_SQL_CTR,true)) {

                }
                else if (strType.equals(BaseVolume.COMMAND_TYPE_PRESS,true)) {
                    if (strMsg.equals(BaseVolume.COMMAND_ACK,true)) {
                        MainControlActivity.getInstance()?.ReturnBack("SetMemoryActivity")
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(myNetReceiver)

    }

}