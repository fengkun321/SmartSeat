package com.smartCarSeatProject.view

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import com.smartCarSeatProject.R


class ProgressBarWindowHint : Dialog,DialogInterface.OnDismissListener{


    private var tvContent: TextView? = null
    private var title: String? = null
    private var mContext:Context? = null
    private var progressBar:ProgressBar? = null

    constructor(context: Context) : super(context) {
        this.mContext = context
    }

    constructor(context: Context, theme: Int, titleName: String) : super(context, theme) {
        this.mContext = context
        this.title = titleName
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.setContentView(R.layout.window_progress_hint)
        tvContent = findViewById<View>(R.id.areaName) as TextView
        progressBar = findViewById<View>(R.id.progressBar1) as ProgressBar
        tvContent!!.text = title

//        dialog.setCancelable(false),对话框弹出后点击或按返回键不消失;
//        dialog.setCanceledOnTouchOutside(false),对话框弹出后点击不消失,按返回键消失.
        setCanceledOnTouchOutside(false)

        this.setOnDismissListener(this)

    }



    /** 更新显示内容  */
    fun updateContent(strNewContent: String) {
        this.title = strNewContent
        if (tvContent != null) {
            tvContent!!.text = title
        }
    }

    fun onSelfShow() {
        super.show()
        mHandler.removeMessages(0)
        progressBar?.progress = 0
        mHandler.sendEmptyMessage(0)
    }

    fun onSelfDismiss() {
        super.dismiss()
        mHandler.removeMessages(0)
    }

    fun onStopProgress() {
        mHandler.removeMessages(0)
    }

    override fun onDismiss(p0: DialogInterface?) {
        mHandler.removeMessages(0)
    }

    val mHandler = object : Handler() {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            var iProcess = progressBar!!.progress
            iProcess += 10
            if (iProcess > 100) {
                iProcess = 10
            }
            progressBar!!.progress = iProcess

            this.sendEmptyMessageDelayed(0,1000)
        }
    }


}