package com.smartCarSeatProject.view

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.os.IBinder
import android.text.InputFilter
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.TextView

import com.smartCarSeatProject.R
import com.smartCarSeatProject.data.DeviceWorkInfo
import kotlinx.android.synthetic.main.window_area_save_value.*

class SureSaveValueWindow : Dialog, View.OnClickListener {
    private lateinit var listener: PeriodListener
    private var strTitle = ""

    private var inputFilter: InputFilter? = null

    private var mContext : Context? = null
    private lateinit var deviceWorkInfo : DeviceWorkInfo

    constructor(context: Context) : super(context) {
    }

    constructor(context: Context, theme: Int, strTitle: String, listener: PeriodListener,deviceWorkInfo : DeviceWorkInfo) : super(context, theme) {
        this.mContext = context
        this.strTitle = strTitle
        this.listener = listener
        this.deviceWorkInfo = deviceWorkInfo
    }

    /****
     *
     * @author mqw
     */
    interface PeriodListener {
        fun confirmListener(deviceWorkInfo : DeviceWorkInfo,strName: String)
        fun cancelListener()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.setContentView(R.layout.window_area_save_value)
        edName!!.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            //			       if (hasFocus) {
            //			            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            //			       }
        }

        confirm_btn.setOnClickListener(this)
        cancel_btn.setOnClickListener(this)
        scrollView.setOnClickListener(this)
        setCancelable(false)
        setCanceledOnTouchOutside(false)

        updateData()


    }

    private fun updateData() {
        rbNan.isChecked = deviceWorkInfo.m_gender
        rbNv.isChecked = !deviceWorkInfo.m_gender
        rbDongFang.isChecked = deviceWorkInfo.m_national
        rbXiFang.isChecked = !deviceWorkInfo.m_national
        edHeight.setText("${deviceWorkInfo.nowHeight}")
        edWeight.setText("${deviceWorkInfo.nowWeight}")
        edName.setText("")
        tvXinLv.text = "心率："+deviceWorkInfo.HeartRate
        tvHuXiLv.text = "呼吸率："+deviceWorkInfo.BreathRate
        tvQingXu.text = "情绪值："+deviceWorkInfo.E_Index
        tvShuZhang.text = "舒张压："+deviceWorkInfo.Dia_BP
        tvShouSuo.text = "收缩压："+deviceWorkInfo.Sys_BP
        tvWeiZhi.text = "位置："+deviceWorkInfo.l_location

    }

    /** 设置输入框内格式--长度  */
    fun setInputFilter(inputFilter: InputFilter) {
        this.inputFilter = inputFilter
    }

    override fun onClick(v: View) {
        // TODO Auto-generated method stub
        val id = v.id
        when (id) {
            R.id.cancel_btn -> {
                val imm = context!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(v.windowToken, 0)
                listener?.cancelListener()
                dismiss()
            }
            R.id.confirm_btn -> {
                val strName = edName.text.toString()
                val strHeight = edHeight.text.toString()
                val strWeight = edWeight.text.toString()
                if (strName == "" || strHeight == "" || strWeight == "") {
                    return
                }

                deviceWorkInfo.m_gender = rbNan.isChecked
                deviceWorkInfo.m_national = rbDongFang.isChecked

                deviceWorkInfo.nowHeight = strHeight.toDouble()
                deviceWorkInfo.nowWeight = strWeight.toDouble()

                val imm1 = context!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm1.hideSoftInputFromWindow(v.windowToken, 0)

                listener?.confirmListener(deviceWorkInfo,strName)
                dismiss()
            }
            R.id.scrollView -> {
                hideSoftInput(scrollView.windowToken)
            }

        }
    }

    /**
     * 隐藏软键盘
     */
    private fun hideSoftInput(token: IBinder?) {
        if (token != null) {
            val manager = mContext?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            manager.hideSoftInputFromWindow(token, InputMethodManager.HIDE_NOT_ALWAYS)
        }
    }


}