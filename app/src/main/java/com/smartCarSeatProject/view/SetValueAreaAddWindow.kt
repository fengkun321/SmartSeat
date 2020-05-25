package com.smartCarSeatProject.view

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.os.IBinder
import android.text.InputFilter
import android.text.InputType
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView

import com.smartCarSeatProject.R
import kotlinx.android.synthetic.main.window_area_set_value.*

class SetValueAreaAddWindow : Dialog, View.OnClickListener {
    private var confirmBtn: Button? = null
    private var cancelBtn: Button? = null
    private var tvContent: EditText? = null
    private var isShowTost = false

    private var titleTv: TextView? = null
    private var titleTvTwo: TextView? = null
    private var listener: PeriodListener? = null
    private var strTitle = ""
    private var defaultName = ""

    private var inputFilter: InputFilter? = null

    private var mContext : Context? = null

    constructor(context: Context) : super(context) {
    }

    constructor(context: Context, theme: Int, strTitle: String, listener: PeriodListener?, defaultName: String, isTost: Boolean) : super(context, theme) {
        this.mContext = context
        this.strTitle = strTitle
        this.listener = listener
        this.defaultName = defaultName
        this.isShowTost = isTost
    }

    /****
     *
     * @author mqw
     */
    interface PeriodListener {
        fun confirmListener(string: String)
        fun cancelListener()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.setContentView(R.layout.window_area_set_value)
        confirmBtn = findViewById<View>(R.id.confirm_btn) as Button
        cancelBtn = findViewById<View>(R.id.cancel_btn) as Button
        tvContent = findViewById<View>(R.id.areaName) as EditText
        titleTv = findViewById<View>(R.id.dialog_title) as TextView
        titleTvTwo = findViewById<View>(R.id.dialog_title_two) as TextView
        titleTv!!.text = strTitle

        if (isShowTost) {
            findViewById<View>(R.id.view1).visibility = View.GONE
            cancelBtn!!.visibility = View.GONE
        }

        tvContent!!.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            //			       if (hasFocus) {
            //			            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            //			       }
        }

        confirmBtn!!.setOnClickListener(this)
        cancelBtn!!.setOnClickListener(this)
        scrollView.setOnClickListener(this)
        if (defaultName == null || defaultName == "") {
            //			tvContent.setText("");
        } else {
            tvContent!!.setText(defaultName + "")
            tvContent!!.setSelection((defaultName + "").length)//将光标移至文字末尾
        }
        setCancelable(true)



    }

    fun setNowValue(strValue:String) {
        this.defaultName = strValue
        if (tvContent != null) {
            tvContent!!.setText(defaultName + "")
        }

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
                val strPeriod = tvContent!!.text.toString()

                if (strPeriod == "") {
                    return
                }

                val imm1 = context!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm1.hideSoftInputFromWindow(v.windowToken, 0)

                listener?.confirmListener(strPeriod)
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