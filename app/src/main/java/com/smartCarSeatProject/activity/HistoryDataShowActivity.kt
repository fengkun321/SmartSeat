package com.smartCarSeatProject.activity


import android.os.Bundle
import com.smartCarSeatProject.R
import com.smartCarSeatProject.adapter.MyHisDateAdapter
import com.smartCarSeatProject.dao.DevelopDataInfo
import com.smartCarSeatProject.dao.DevelopInfoDao
import com.smartCarSeatProject.view.AreaAddWindowHint
import kotlinx.android.synthetic.main.activity_history.*
open class HistoryDataShowActivity : BaseActivity(){

    var myHisDateAdapter:MyHisDateAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        imgBack.setOnClickListener {
            finish()
        }

        // 保存数据库！
        val developInfoDao = DevelopInfoDao(this)
        var mDataSource =  developInfoDao.queryHistDataInf()
        developInfoDao.closeDb()
        myHisDateAdapter = MyHisDateAdapter(mDataSource, this)

        his_list.adapter = myHisDateAdapter

        his_list.setOnItemLongClickListener { adapterView, view, i, l ->

            val areaAddWindowHint = AreaAddWindowHint(this@HistoryDataShowActivity,R.style.Dialogstyle,"System",
                    object : AreaAddWindowHint.PeriodListener {
                        override fun refreshListener(string: String) {
                            val developDataInfo = mDataSource[i]
                            val developInfoDao1 = DevelopInfoDao(this@HistoryDataShowActivity)
                            val isResult = developInfoDao1.deleteDataByInfo(developDataInfo)
                            developInfoDao1.closeDb()
                            if (isResult) {
                                mDataSource.remove(developDataInfo)
                                myHisDateAdapter?.notifyDataSetChanged()
                            }
                        }
                    }
            ,"Are you sure you want to delete this data?"
            )

            areaAddWindowHint.show()
            return@setOnItemLongClickListener true
        }

    }





}

