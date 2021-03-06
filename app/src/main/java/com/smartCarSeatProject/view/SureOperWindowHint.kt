package com.smartCarSeatProject.view

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView

import com.smartCarSeatProject.R


class SureOperWindowHint : Dialog, View.OnClickListener {
    private var confirmBtn: Button? = null
    private var cancelBtn: Button? = null
    private var tvContent: TextView? = null
    private var isShowTost: Boolean = false

    private var titleTv: TextView? = null
    private val period = ""
    private var listener: PeriodListener? = null
    private var defaultName = ""
    private var title: String? = null
    private var strLeft = "Cancel"
    private var strRight = "Confirm"

    private var mContext:Context? = null

    constructor(context: Context) : super(context) {
        this.mContext = context
    }

    constructor(context: Context, theme: Int, titleName: String, listener: PeriodListener, defaultName: String, strLeft: String, strRight: String) : super(context, theme) {
        this.mContext = context
        this.listener = listener
        this.defaultName = defaultName
        this.title = titleName
        this.strLeft = strLeft
        this.strRight = strRight
    }

    /****
     *
     * @author mqw
     */
    interface PeriodListener {
        fun btnLeftListener()
        fun btnRightListener()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.setContentView(R.layout.window_area_hint)
        confirmBtn = findViewById<View>(R.id.confirm_btn) as Button
        cancelBtn = findViewById<View>(R.id.cancel_btn) as Button
        tvContent = findViewById<View>(R.id.areaName) as TextView
        titleTv = findViewById<View>(R.id.dialog_title) as TextView
        titleTv!!.text = title

        if (isShowTost!!) {
            findViewById<View>(R.id.view1).visibility = View.GONE
            cancelBtn!!.visibility = View.GONE
        }

        confirmBtn?.text = strRight
        cancelBtn?.text = strLeft

        confirmBtn!!.setOnClickListener(this)
        cancelBtn!!.setOnClickListener(this)
        tvContent!!.text = defaultName

        setCancelable(false)


    }

    /** 更新显示内容  */
    fun updateContent(strNewContent: String) {
        this.defaultName = strNewContent
        tvContent!!.text = defaultName
    }

    override fun onClick(v: View) {
        // TODO Auto-generated method stub
        val id = v.id
        when (id) {
            R.id.cancel_btn -> {
                dismiss()
                listener!!.btnLeftListener()
            }
            R.id.confirm_btn -> {
                dismiss()
                listener!!.btnRightListener()
            }
        }
    }
}