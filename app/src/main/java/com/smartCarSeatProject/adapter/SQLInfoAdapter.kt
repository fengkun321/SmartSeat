package com.smartCarSeatProject.adapter

import android.content.Context
import android.net.wifi.ScanResult
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.smartCarSeatProject.R
import com.smartCarSeatProject.dao.DevelopDataInfo
import java.util.ArrayList

class SQLInfoAdapter(internal var list: ArrayList<DevelopDataInfo>, internal var con: Context) : BaseAdapter() {
    internal lateinit var deviceInfo: DevelopDataInfo
    internal lateinit var holder: ViewHolder
    internal var number = 0
    override fun getCount(): Int {
        // TODO Auto-generated method stub
        return list.size
    }

    override fun getItem(position: Int): Any {
        // TODO Auto-generated method stub
        return list[position]
    }

    override fun getItemId(position: Int): Long {
        // TODO Auto-generated method stub
        return position.toLong()
    }

    fun updateList() {
        notifyDataSetChanged()
    }

    fun updateList(li: ArrayList<DevelopDataInfo>) {
        this.list = li
        notifyDataSetChanged()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        if (convertView == null) {
            holder = ViewHolder()
            convertView = LayoutInflater.from(con).inflate(R.layout.item_device, null)
            holder.m_tvDeviceName = convertView.findViewById(R.id.tvDeviceName) as TextView
            holder.m_tvDeviceType = convertView.findViewById(R.id.tvDeviceType) as TextView
            holder.m_tvState = convertView.findViewById(R.id.tvState) as TextView
            convertView!!.tag = holder
        } else {
            holder = convertView.tag as ViewHolder
        }
        deviceInfo = list[position]
        holder.m_tvDeviceName!!.text = deviceInfo.strName

        var strPressInfo = ""
        strPressInfo+= "${deviceInfo.p_adjust_cushion_1},"
        strPressInfo+= "${deviceInfo.p_adjust_cushion_2},"
        strPressInfo+= "${deviceInfo.p_adjust_cushion_3},"
        strPressInfo+= "${deviceInfo.p_adjust_cushion_4},"
        strPressInfo+= "${deviceInfo.p_adjust_cushion_5},"
        strPressInfo+= "${deviceInfo.p_adjust_cushion_6},"
        strPressInfo+= "${deviceInfo.p_adjust_cushion_7},"
        strPressInfo+= "${deviceInfo.p_adjust_cushion_8},"
        strPressInfo+= "${deviceInfo.p_recog_back_A},"
        strPressInfo+= "${deviceInfo.p_recog_back_B},"
        strPressInfo+= "${deviceInfo.p_recog_back_C},"
        strPressInfo+= "${deviceInfo.p_recog_back_D},"
        strPressInfo+= "${deviceInfo.p_recog_back_E},"
        strPressInfo+= "${deviceInfo.p_recog_back_F},"
        strPressInfo+= "${deviceInfo.p_recog_back_G},"
        strPressInfo+= "${deviceInfo.p_recog_back_H}"

        holder.m_tvDeviceType!!.text = strPressInfo

        return convertView
    }

    internal inner class ViewHolder {

        var m_tvDeviceName: TextView? = null

        var m_tvDeviceType: TextView? = null

        var m_tvState: TextView? = null


    }

}
