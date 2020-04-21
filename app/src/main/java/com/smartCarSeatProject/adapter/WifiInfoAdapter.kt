package com.smartCarSeatProject.adapter

import android.content.Context
import android.net.wifi.ScanResult
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.smartCarSeatProject.R
import java.util.ArrayList

class WifiInfoAdapter(internal var list: ArrayList<ScanResult>, internal var con: Context,var nowSSID:String) : BaseAdapter() {
    internal lateinit var deviceInfo: ScanResult
    internal lateinit var holder: ViewHolder
    internal var number = 0
    private var nowSelectSSID = nowSSID
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

    fun updateList(li: ArrayList<ScanResult>,strSelect : String) {
        this.list = li
        this.nowSelectSSID = strSelect
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
            holder.m_imgRight = convertView.findViewById(R.id.imgRight) as ImageView
            convertView!!.tag = holder
        } else {
            holder = convertView.tag as ViewHolder
        }
        deviceInfo = list[position]
        holder.m_tvDeviceName!!.text = deviceInfo.SSID
        holder.m_tvDeviceType!!.text = "MAC:" + deviceInfo.BSSID

        if (nowSelectSSID.equals(deviceInfo.SSID)) {
            holder.m_tvState!!.visibility = View.VISIBLE
            holder.m_imgRight!!.visibility = View.GONE
            holder.m_tvDeviceName!!.setTextColor(con.getColor(R.color.device_red))
            holder.m_tvDeviceType!!.setTextColor(con.getColor(R.color.device_red))
            holder.m_tvState!!.setTextColor(con.getColor(R.color.device_red))
        }
        else {
            holder.m_tvState!!.visibility = View.GONE
            holder.m_imgRight!!.visibility = View.VISIBLE
            holder.m_tvDeviceName!!.setTextColor(con.getColor(R.color.colorWhite))
            holder.m_tvDeviceType!!.setTextColor(con.getColor(R.color.colorWhite))
            holder.m_tvState!!.setTextColor(con.getColor(R.color.colorWhite))
        }
        return convertView
    }

    internal inner class ViewHolder {

        var m_tvDeviceName: TextView? = null

        var m_tvDeviceType: TextView? = null

        var m_tvState: TextView? = null
        var m_imgRight: ImageView? = null


    }

}
