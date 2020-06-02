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

class MyHisDateAdapter(internal var list: ArrayList<DevelopDataInfo>, internal var con: Context) : BaseAdapter() {
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
            convertView = LayoutInflater.from(con).inflate(R.layout.layout_his_item, null)
            holder.m_tvName = convertView.findViewById(R.id.his_name) as TextView
            holder.m_tvSex = convertView.findViewById(R.id.his_sex) as TextView
            holder.m_tvNation = convertView.findViewById(R.id.his_nation) as TextView
            holder.m_tvHeight = convertView.findViewById(R.id.his_height) as TextView
            holder.m_tvWeight = convertView.findViewById(R.id.his_weight) as TextView

            holder.m_tvInitA = convertView.findViewById(R.id.tvInitA) as TextView
            holder.m_tvInitB = convertView.findViewById(R.id.tvInitB) as TextView
            holder.m_tvInitSeat = convertView.findViewById(R.id.tvInitSeat) as TextView

            holder.m_tvA_A = convertView.findViewById(R.id.A_A) as TextView
            holder.m_tvA_B = convertView.findViewById(R.id.A_B) as TextView
            holder.m_tvA_C = convertView.findViewById(R.id.A_C) as TextView
            holder.m_tvA_D = convertView.findViewById(R.id.A_D) as TextView
            holder.m_tvA_E = convertView.findViewById(R.id.A_E) as TextView
            holder.m_tvA_F = convertView.findViewById(R.id.A_F) as TextView
            holder.m_tvA_G = convertView.findViewById(R.id.A_G) as TextView
            holder.m_tvA_H = convertView.findViewById(R.id.A_H) as TextView


            holder.m_tvSet_1 = convertView.findViewById(R.id.ZD_1) as TextView
            holder.m_tvSet_2 = convertView.findViewById(R.id.ZD_2) as TextView
            holder.m_tvSet_3 = convertView.findViewById(R.id.ZD_3) as TextView

            holder.m_tvB_4 = convertView.findViewById(R.id.B_4) as TextView
            holder.m_tvB_5 = convertView.findViewById(R.id.B_5) as TextView
            holder.m_tvB_6 = convertView.findViewById(R.id.B_6) as TextView
            holder.m_tvB_7 = convertView.findViewById(R.id.B_7) as TextView
            holder.m_tvB_8 = convertView.findViewById(R.id.B_8) as TextView

            holder.m_tvAD_1 = convertView.findViewById(R.id.AD_1) as TextView
            holder.m_tvAD_2 = convertView.findViewById(R.id.AD_2) as TextView
            holder.m_tvAD_3 = convertView.findViewById(R.id.AD_3) as TextView

            holder.m_tvAD_4 = convertView.findViewById(R.id.AD_4) as TextView
            holder.m_tvAD_5 = convertView.findViewById(R.id.AD_5) as TextView
            holder.m_tvAD_6 = convertView.findViewById(R.id.AD_6) as TextView
            holder.m_tvAD_7 = convertView.findViewById(R.id.AD_7) as TextView
            holder.m_tvAD_8 = convertView.findViewById(R.id.AD_8) as TextView
            holder.m_tvLocation = convertView.findViewById(R.id.lLocation) as TextView
            holder.m_tvHeartRate = convertView.findViewById(R.id.HeartRate) as TextView
            holder.m_tvBreathRate = convertView.findViewById(R.id.BreathRate) as TextView
            holder.m_tvE_Index = convertView.findViewById(R.id.E_Index) as TextView
            holder.m_tvDia_BP = convertView.findViewById(R.id.Dia_BP) as TextView
            holder.m_tvSys_BP = convertView.findViewById(R.id.Sys_BP) as TextView
            holder.m_tvMassage = convertView.findViewById(R.id.mMassage) as TextView

            convertView!!.tag = holder
        } else {
            holder = convertView.tag as ViewHolder
        }
        deviceInfo = list[position]
        holder.m_tvName!!.text = deviceInfo.strName
        holder.m_tvSex!!.text = deviceInfo.m_gender
        holder.m_tvNation!!.text =  deviceInfo.m_national

        holder.m_tvHeight!!.text = deviceInfo.m_height
        holder.m_tvWeight!!.text = deviceInfo.m_weight

        holder.m_tvInitA!!.text = deviceInfo.p_init_back_A
        holder.m_tvInitB!!.text = deviceInfo.p_init_back_B
        holder.m_tvInitSeat!!.text =  deviceInfo.p_init_cushion

        holder.m_tvA_A!!.text = deviceInfo.p_recog_back_A
        holder.m_tvA_B!!.text = deviceInfo.p_recog_back_B
        holder.m_tvA_C!!.text = deviceInfo.p_recog_back_C
        holder.m_tvA_D!!.text = deviceInfo.p_recog_back_D
        holder.m_tvA_E!!.text = deviceInfo.p_recog_back_E
        holder.m_tvA_F!!.text = deviceInfo.p_recog_back_F
        holder.m_tvA_G!!.text = deviceInfo.p_recog_back_G
        holder.m_tvA_H!!.text = deviceInfo.p_recog_back_H

        holder.m_tvSet_1!!.text = deviceInfo.p_recog_back_1
        holder.m_tvSet_2!!.text = deviceInfo.p_recog_back_2
        holder.m_tvSet_3!!.text = deviceInfo.p_recog_back_3


        holder.m_tvB_4!!.text = deviceInfo.p_recog_back_4
        holder.m_tvB_5!!.text = deviceInfo.p_recog_back_5
        holder.m_tvB_6!!.text = deviceInfo.p_recog_cushion_6
        holder.m_tvB_7!!.text = deviceInfo.p_recog_cushion_7
        holder.m_tvB_8!!.text = deviceInfo.p_recog_cushion_8

        holder.m_tvAD_1!!.text = deviceInfo.p_adjust_cushion_1
        holder.m_tvAD_2!!.text = deviceInfo.p_adjust_cushion_2
        holder.m_tvAD_3!!.text = deviceInfo.p_adjust_cushion_3
        holder.m_tvAD_4!!.text = deviceInfo.p_adjust_cushion_4
        holder.m_tvAD_5!!.text = deviceInfo.p_adjust_cushion_5
        holder.m_tvAD_6!!.text = deviceInfo.p_adjust_cushion_6
        holder.m_tvAD_7!!.text = deviceInfo.p_adjust_cushion_7
        holder.m_tvAD_8!!.text = deviceInfo.p_adjust_cushion_8

        holder.m_tvLocation!!.text = deviceInfo.l_location
        holder.m_tvHeartRate!!.text = deviceInfo.HeartRate
        holder.m_tvBreathRate!!.text = deviceInfo.BreathRate
        holder.m_tvE_Index!!.text = deviceInfo.E_Index
        holder.m_tvDia_BP!!.text = deviceInfo.Dia_BP
        holder.m_tvSys_BP!!.text = deviceInfo.Sys_BP


        return convertView
    }
    internal inner class ViewHolder {
        var m_tvName: TextView? = null
        var m_tvSex: TextView? = null
        var m_tvNation: TextView? = null
        var m_tvWeight: TextView? = null
        var m_tvHeight: TextView? = null

        var m_tvInitA: TextView? = null
        var m_tvInitB: TextView? = null
        var m_tvInitSeat: TextView? = null

        var m_tvA_A: TextView? = null
        var m_tvA_B: TextView? = null
        var m_tvA_C: TextView? = null
        var m_tvA_D: TextView? = null
        var m_tvA_E: TextView? = null
        var m_tvA_F: TextView? = null
        var m_tvA_G: TextView? = null
        var m_tvA_H: TextView? = null

        var m_tvSet_1: TextView? = null
        var m_tvSet_2: TextView? = null
        var m_tvSet_3: TextView? = null

        var m_tvB_4: TextView? = null
        var m_tvB_5: TextView? = null
        var m_tvB_6: TextView? = null
        var m_tvB_7: TextView? = null
        var m_tvB_8: TextView? = null

        var m_tvAD_1: TextView? = null
        var m_tvAD_2: TextView? = null
        var m_tvAD_3: TextView? = null

        var m_tvAD_4: TextView? = null
        var m_tvAD_5: TextView? = null
        var m_tvAD_6: TextView? = null
        var m_tvAD_7: TextView? = null
        var m_tvAD_8: TextView? = null
        var m_tvLocation: TextView? = null
        var m_tvHeartRate: TextView? = null
        var m_tvBreathRate: TextView? = null
        var m_tvE_Index: TextView? = null
        var m_tvDia_BP: TextView? = null
        var m_tvSys_BP: TextView? = null
        var m_tvMassage: TextView? = null


    }

}
