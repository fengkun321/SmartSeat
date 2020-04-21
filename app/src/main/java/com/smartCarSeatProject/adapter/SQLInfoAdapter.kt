package com.smartCarSeatProject.adapter

import android.content.Context
import android.net.wifi.ScanResult
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.smartCarSeatProject.R
import com.smartCarSeatProject.dao.MemoryDataInfo
import java.util.ArrayList

class SQLInfoAdapter(internal var list: ArrayList<MemoryDataInfo>, internal var con: Context) : BaseAdapter() {
    internal lateinit var deviceInfo: MemoryDataInfo
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

    fun updateList(li: ArrayList<MemoryDataInfo>) {
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
        holder.m_tvDeviceName!!.text = deviceInfo.strDataName

        var strPressInfo = ""
        strPressInfo+= "${deviceInfo.strPress1},"
        strPressInfo+= "${deviceInfo.strPress2},"
        strPressInfo+= "${deviceInfo.strPress3},"
        strPressInfo+= "${deviceInfo.strPress4},"
        strPressInfo+= "${deviceInfo.strPress5},"
        strPressInfo+= "${deviceInfo.strPress6},"
        strPressInfo+= "${deviceInfo.strPress7},"
        strPressInfo+= "${deviceInfo.strPress8},"
        strPressInfo+= "${deviceInfo.strPressA},"
        strPressInfo+= "${deviceInfo.strPressB},"
        strPressInfo+= "${deviceInfo.strPressC},"
        strPressInfo+= "${deviceInfo.strPressD},"
        strPressInfo+= "${deviceInfo.strPressE},"
        strPressInfo+= "${deviceInfo.strPressF},"
        strPressInfo+= "${deviceInfo.strPressG},"
        strPressInfo+= "${deviceInfo.strPressH}"

        holder.m_tvDeviceType!!.text = strPressInfo

        return convertView
    }

    internal inner class ViewHolder {

        var m_tvDeviceName: TextView? = null

        var m_tvDeviceType: TextView? = null

        var m_tvState: TextView? = null


    }

}
